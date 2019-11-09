package com.firstlinecode.marble.im;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.protocol.im.roster.Item;
import com.firstlinecode.basalt.protocol.im.roster.Item.Subscription;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.chalk.im.IInstantingMessager;
import com.firstlinecode.marble.AbstractXmppSampler;
import com.firstlinecode.marble.XmppMessage;
import com.firstlinecode.marble.XmppSampleResult;
import com.firstlinecode.marble.XmppSampleResultWatcher;
import com.firstlinecode.marble.XmppMessage.Direction;

public class MessageSampler extends AbstractXmppSampler {
	private static final long serialVersionUID = 4109484542472338833L;
	
	private static final String RESOURCE_MISSION_STATEMENTS = "com/firstlinecode/marble/resources/mission_statements.txt";
	private static String[] missionStatements;
	
	public static final String CONTACT_JID = "MessageSampler.contactJid";
	public static final String SELECT_CONTACT_RANDOMLY = "SubscriptionSampler.selectContactRandomly";
	
	static {
		missionStatements = loadMissionStatements();
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		checkArguments();
		
		IInstantingMessager im = getChatClient().createApi(IInstantingMessager.class);
		JabberId contactJid = pickOutContactJid(im);
		
		XmppSampleResult result = new XmppSampleResult();
		
		result.setSampleLabel(getName());
		result.setContentType("text/xml");
		result.setDataType(SampleResult.TEXT);
		
		result.setSamplerData(String.format("Message\nUser: %s\nContact: %s",
				getChatClient().getStream().getJid(), contactJid));
		
		MessageSampleResultWatcher watcher = new MessageSampleResultWatcher(result);
		getChatClient().getStream().addStanzaWatcher(watcher);
		
		result.sampleStart();
        
		im.send(contactJid, new Message(pickOutMissionStatement()));
		
		getChatClient().getStream().removeStanzaWatcher(watcher);
		
		result.setResponseOK();
		result.setResponseData("Oneway message has no response data", null);
		result.sampleEnd();
		
		appendXmppMessagesToResult(result);
		
		return result;
	}

	private class MessageSampleResultWatcher extends XmppSampleResultWatcher {
		public MessageSampleResultWatcher(XmppSampleResult result) {
			super(result);
		}

		@Override
		protected boolean accepts(Stanza stanza, XmppMessage message) {
			return message.getDirection() == Direction.SENT && (stanza instanceof Message);
		}
		
	}

	private String pickOutMissionStatement() {
		int random = new Random().nextInt(missionStatements.length);
		return missionStatements[random];
	}

	private JabberId pickOutContactJid(IInstantingMessager im) {
		if (!isSelectContactRandomly()) {
			return JabberId.parse(getContactJid());
		}
		
		JabberId[] subscribedJids = getSubscribedContacts(im.getRosterService().getLocal().getItems());
		int random = new Random().nextInt(subscribedJids.length);
		return subscribedJids[random];
	}

	private JabberId[] getSubscribedContacts(Item[] items) {
		List<JabberId> contacts = new ArrayList<JabberId>();
		
		for (Item item : items) {
			if (item.getSubscription() == Subscription.TO || item.getSubscription() == Subscription.BOTH) {
				contacts.add(item.getJid());
			}
		}
		
		return contacts.toArray(new JabberId[contacts.size()]);
	}

	private void checkArguments() {
		if (!isSelectContactRandomly() && isBlank(getContactJid())) {
			throw new IllegalArgumentException("null contact jid.");
		}
	}

	private static String[] loadMissionStatements() {
		BufferedReader reader = null;
		try {
			URL url = Thread.currentThread().getContextClassLoader().getResource(RESOURCE_MISSION_STATEMENTS);
			
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			
			String line = null;
			List<String> lines = new ArrayList<String>();
			while (true) {
				line = reader.readLine();
				if (line == null)
					break;
				
				lines.add(line);
			}
			
			return lines.toArray(new String[lines.size()]);
		} catch (Exception e) {
			throw new RuntimeException(String.format("can't load mission statements from resource: '%'."), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
	
	public void setContactJid(String contactJid) {
		setProperty(new StringProperty(CONTACT_JID, contactJid));
	}
	
	public String getContactJid() {
		return getPropertyAsString(CONTACT_JID);
	}
	
	public void setSelectContactRandomly(boolean selectContactRandomly) {
		setProperty(new BooleanProperty(SELECT_CONTACT_RANDOMLY, selectContactRandomly));
	}
	
	public boolean isSelectContactRandomly() {
		return getPropertyAsBoolean(SELECT_CONTACT_RANDOMLY);
	}

}
