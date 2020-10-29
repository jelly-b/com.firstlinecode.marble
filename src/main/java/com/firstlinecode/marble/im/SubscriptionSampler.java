package com.firstlinecode.marble.im;

import java.util.Random;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.MalformedJidException;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.core.stanza.error.RemoteServerTimeout;
import com.firstlinecode.basalt.protocol.core.stanza.error.UndefinedCondition;
import com.firstlinecode.basalt.protocol.im.roster.Item;
import com.firstlinecode.basalt.protocol.im.roster.Item.Subscription;
import com.firstlinecode.basalt.protocol.im.roster.Roster;
import com.firstlinecode.basalt.protocol.im.stanza.Presence;
import com.firstlinecode.chalk.im.IInstantingMessager;
import com.firstlinecode.chalk.im.InstantingMessagerPlugin;
import com.firstlinecode.chalk.im.subscription.ISubscriptionListener;
import com.firstlinecode.chalk.im.subscription.SubscriptionError;
import com.firstlinecode.marble.AbstractXmppSampler;
import com.firstlinecode.marble.IPluginContributor;
import com.firstlinecode.marble.XmppMessage;
import com.firstlinecode.marble.XmppMessage.Direction;
import com.firstlinecode.marble.XmppSampleResult;
import com.firstlinecode.marble.XmppSampleResultWatcher;

public class SubscriptionSampler extends AbstractXmppSampler implements IPluginContributor {

	private static final char CHAR_AT = '@';
	private static final String STRING_SEQ_PLACE_HOLDER = "%";

	private static final long serialVersionUID = 8449416448745580087L;
	
	public static final String CONTACT_JID = "SubscriptionSampler.contactJid";
	public static final String SUBSCRIPTION_TIMEOUT = "SubscriptionSampler.subscriptionTimeout";
	public static final String SELECT_CONTACT_RANDOMLY = "SubscriptionSampler.selectContactRandomly";
	public static final String SEQ_START_NUMBER = "SubscriptionSampler.seqStartNumber";
	public static final String SEQ_END_NUMBER = "SubscriptionSampler.seqEndNumber";
	
	@Override
	public SampleResult sample(Entry entry) {
		checkArguments();
		
		int timeout = getSubscriptionTimeoutAsInt();
		
		WaitSubscriptionResultListener listener = new WaitSubscriptionResultListener();
		
		IInstantingMessager im = getChatClient().createApi(IInstantingMessager.class);
		im.getSubscriptionService().addSubscriptionListener(listener);
		
		XmppSampleResult result = createXmppResult();
		
		final JabberId contactJid = pickOutContactJid(im, getChatClient().getStream().getJid());
		
		result.setSamplerData(String.format("Subscription\nUser: %s\nContact: %s",
				getChatClient().getStream().getJid(), contactJid));
		
		SubscriptionSampleResultWatcher watcher = new SubscriptionSampleResultWatcher(result, contactJid);
		getChatClient().getStream().addStanzaWatcher(watcher);
		
		result.sampleStart();
 		im.getSubscriptionService().subscribe(contactJid);
		try {
			synchronized (this) {
				this.wait(timeout);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		getChatClient().getStream().removeStanzaWatcher(watcher);
		
		SubscriptionResult subscriptionResult = listener.getResult();
		if (subscriptionResult == null) {
			// no response from server.
			result.setSuccessful(false);
			result.setResponseCode(getErrorCode(RemoteServerTimeout.DEFINED_CONDITION));
			result.setResponseMessage("Subscription Timeout");
		} else if (subscriptionResult == SubscriptionResult.ERROR) {
			result.setSuccessful(false);
			result.setResponseCode(getResponseCode(listener.getError()));
			result.setResponseMessage(String.format("Error: %s", listener.getError().getReason().toString()));
		} else {
			result.setResponseCodeOK();
			result.setResponseMessageOK();
			result.setSuccessful(true);
		}
		
		result.sampleEnd();
		
		appendXmppMessagesToResult(result);
		
		return result;
	}
	
	private class SubscriptionSampleResultWatcher extends XmppSampleResultWatcher {
		private JabberId contactJid;
		
		public SubscriptionSampleResultWatcher(XmppSampleResult result, JabberId contactJid) {
			super(result);
			
			this.contactJid = contactJid;
		}

		@Override
		public boolean accepts(Stanza stanza, XmppMessage message) {
			if (message.getDirection() == Direction.SENT) {
				return isRosterSetMessage(stanza) ||
						isSubscribeMessage(stanza) ||
						isAckMessage(stanza);
			} else {
				return isResultMessage(stanza) ||
						isApprovedMessage(stanza) ||
						isRosterPushMessageOfTheSubscription(stanza);
			}
		}
		
		private boolean isRosterSetMessage(Stanza stanza) {
			return (stanza instanceof Iq) && (stanza.getObject() instanceof Roster);
		}
		
		private boolean isRosterPushMessageOfTheSubscription(Stanza stanza) {
			if ((stanza instanceof Iq) && (stanza.getObject() instanceof Roster)) {
				Roster roster = (Roster)stanza.getObject();
				Item item = roster.getItem(contactJid);
				if (item == null)
					return false;
				
				IInstantingMessager im = getChatClient().createApi(IInstantingMessager.class);
				Item localItem = im.getRosterService().getLocal().getItem(contactJid);
				
				if (localItem == null) {
					return item.getSubscription() == Item.Subscription.NONE;
				} else if (localItem.getSubscription() == Item.Subscription.NONE) {
					return item.getSubscription() == Item.Subscription.TO ||
							(item.getSubscription() == Item.Subscription.NONE &&
								item.getAsk() != null);
				} else if (localItem.getSubscription() == Item.Subscription.FROM) {
					return item.getSubscription() == Item.Subscription.BOTH ||
							(item.getSubscription() == Item.Subscription.FROM &&
							item.getAsk() != null);
				} else {
					return false;
				}
			}
			
			return false;
		}
		
		private boolean isResultMessage(Stanza stanza) {
			return (stanza instanceof Iq) && (((Iq)stanza).getType() == Iq.Type.RESULT);
		}
		
		private boolean isApprovedMessage(Stanza stanza) {
			if (!(stanza instanceof Presence)) {
				return false;
			}
			
			Presence presence = (Presence)stanza;
			if (!presence.getFrom().equals(contactJid))
				return false;
			
			return presence.getType() == Presence.Type.SUBSCRIBED;
		}
		
		private boolean isAckMessage(Stanza stanza) {
			if (!(stanza instanceof Presence)) {
				return false;
			}
			
			Presence presence = (Presence)stanza;
			if (!presence.getTo().equals(contactJid))
				return false;
			
			return presence.getType() == Presence.Type.SUBSCRIBED;
		}

		private boolean isSubscribeMessage(Stanza stanza) {
			if (!(stanza instanceof Presence)) {
				return false;
			}
			
			Presence presence = (Presence)stanza;
			if (!presence.getTo().equals(contactJid))
				return false;
			
			return presence.getType() == Presence.Type.SUBSCRIBE;
		}
		
	}
	
	private JabberId pickOutContactJid(IInstantingMessager im, JabberId userJid) {
		String sContactJid = getContactJid();
		if (sContactJid.indexOf(CHAR_AT) == -1) {
			sContactJid = sContactJid + CHAR_AT + getChatClient().getStream().getStreamConfig().getHost();
		}
		
		JabberId contactJid;
		try {
			contactJid = JabberId.parse(sContactJid);
		} catch (MalformedJidException e) {
			throw new IllegalArgumentException(String.format("illegal contact jid: %s", getContactJid()));
		}
		
		if (!isSelectContactRandomly())
			return contactJid;
		
		String node = contactJid.getNode();
		do {
			int randomNumber = getRandomNumber();
			int seqPlaceHolderIndex = node.indexOf(STRING_SEQ_PLACE_HOLDER);
			
			String newName;
			if (seqPlaceHolderIndex != -1) {
				newName = node.replaceAll(STRING_SEQ_PLACE_HOLDER, Integer.toString(randomNumber));
			} else {
				newName = node + Integer.toString(randomNumber);
			}
			contactJid.setNode(newName);
		} while (isUserOrSubscribedContact(contactJid, im, userJid));
		
		return contactJid;
	}
	
	private boolean isUserOrSubscribedContact(JabberId contactJid, IInstantingMessager im, JabberId userJid) {
		if (contactJid.equals(userJid.getBareId()))
			return true;
		
		Item item = im.getRosterService().getLocal().getItem(contactJid);
		
		if (item != null) {
			if (item.getAsk() != null)
				return true;
			
			if (item.getSubscription() == Subscription.TO ||
					item.getSubscription() == Subscription.BOTH)
				return true;
		}
		
		return false;
	}
	
	private String getResponseCode(SubscriptionError error) {
		String definedCondition;
		if (error.getReason() == SubscriptionError.Reason.ROSTER_SET_TIMEOUT) {
			definedCondition = RemoteServerTimeout.DEFINED_CONDITION;
		}/* else if (error.getReason() == SubscriptionError.Reason.ROSTER_SET_ERROR) {
			definedCondition = UndefinedCondition.DEFINED_CONDITION;
		} */else {
			definedCondition = UndefinedCondition.DEFINED_CONDITION;;
		}
		
		return getErrorCode(definedCondition);
	}
	
	public enum SubscriptionResult {
		APPROVED,
		REFUSED,
		ERROR
	};
	
	private class WaitSubscriptionResultListener implements ISubscriptionListener {
		private SubscriptionResult result;
		private SubscriptionError error;

		@Override
		public void asked(JabberId user) {}

		@Override
		public void approved(JabberId contact) {
			result = SubscriptionResult.APPROVED;
			synchronized (SubscriptionSampler.this) {
				SubscriptionSampler.this.notify();
			}
		}

		@Override
		public void refused(JabberId contact) {
			result = SubscriptionResult.REFUSED;
			synchronized (SubscriptionSampler.this) {
				SubscriptionSampler.this.notify();
			}
		}

		@Override
		public void revoked(JabberId user) {}

		@Override
		public void occurred(SubscriptionError error) {
			result = SubscriptionResult.ERROR;
			this.error = error;
			synchronized (SubscriptionSampler.this) {
				SubscriptionSampler.this.notify();
			}
		}
		
		public SubscriptionResult getResult() {
			return result;
		}
		
		public SubscriptionError getError() {
			return error;
		}
		
	}
	
	private int getRandomNumber() {
		int startNumber;
		int endNumber;
		
		try {
			startNumber = Integer.parseInt(getSeqStartNumber());
			endNumber = Integer.parseInt(getSeqEndNumber());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("sequence start number or end number isn't an integer");
		}
		
		if (startNumber < 0 || endNumber < 0) {
			throw new IllegalArgumentException("sequence start number or end number isn't an positive integer");
		}
		
		if (startNumber >= endNumber) {
			throw new IllegalArgumentException("sequence end number must be bigger than start number");
		}
		
		return new Random().nextInt(endNumber - startNumber + 1) + startNumber;
	}

	private void checkArguments() {
		if (isBlank(getContactJid())) {
			throw new IllegalArgumentException("null contact jid. it's required.");
		}
		
		if (isSelectContactRandomly() && (isBlank(getSeqStartNumber()) || isBlank(getSeqEndNumber()))) {
			throw new IllegalArgumentException("null sequence start number or end number. they're required.");
		}
	}
	
	public void setContactJid(String contactJid) {
		setProperty(new StringProperty(CONTACT_JID, contactJid));
	}
	
	public String getContactJid() {
		return getPropertyAsString(CONTACT_JID);
	}
	
	public void setSubscriptionTimeout(String subscriptionTimeout) {
		setProperty(SUBSCRIPTION_TIMEOUT, subscriptionTimeout, "");
	}
	
	public String getSubscriptionTimeout() {
		return getPropertyAsString(SUBSCRIPTION_TIMEOUT, "");
	}
	
	public int getSubscriptionTimeoutAsInt() {
		return getPropertyAsInt(SUBSCRIPTION_TIMEOUT, 2000);
	}
	
	public void setSelectContactRandomly(boolean selectContactRandomly) {
		setProperty(new BooleanProperty(SELECT_CONTACT_RANDOMLY, selectContactRandomly));
	}
	
	public boolean isSelectContactRandomly() {
		return getPropertyAsBoolean(SELECT_CONTACT_RANDOMLY);
	}
	
	public void setSeqStartNumber(String seqStartNumber) {
		setProperty(new StringProperty(SEQ_START_NUMBER, seqStartNumber));
	}
	
	public String getSeqStartNumber() {
		return getPropertyAsString(SEQ_START_NUMBER);
	}
	
	public void setSeqEndNumber(String seqEndNumber) {
		setProperty(new StringProperty(SEQ_END_NUMBER, seqEndNumber));
	}
	
	public String getSeqEndNumber() {
		return getPropertyAsString(SEQ_END_NUMBER);
	}

	@Override
	public Class<?>[] getPlugins() {
		return new Class<?>[] {InstantingMessagerPlugin.class};
	}

}
