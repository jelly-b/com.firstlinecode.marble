package com.firstlinecode.marble;

import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.chalk.core.stream.IStanzaWatcher;
import com.firstlinecode.marble.XmppMessage.Direction;

public abstract class XmppSampleResultWatcher implements IStanzaWatcher {
	
	private XmppSampleResult result;
	
	public XmppSampleResultWatcher(XmppSampleResult result) {
		this.result = result;
	}

	@Override
	public void sent(Stanza stanza, String message) {
		watch(stanza, new XmppMessage(XmppMessage.Direction.SENT, message));
	}
	
	@Override
	public void received(Stanza stanza, String message) {
		watch(stanza, new XmppMessage(XmppMessage.Direction.RECEIVED, message));
	}
	
	private void watch(Stanza stanza, XmppMessage message) {
		if (accepts(stanza, message)) {
			processXmppMessage(message);
		}
	}

	private synchronized void processXmppMessage(XmppMessage message) {
		result.addMessage(message);
		
		if (message.getDirection() == Direction.SENT)
			return;
		
		if (result.getLatency() == 0) {
			result.latencyEnd();
		}
		
		result.setBodySize(result.getBodySize() + message.getMessage().getBytes().length);
		result.setBytes(result.getBytes() + message.getMessage().getBytes().length);
	}
	
	protected abstract boolean accepts(Stanza stanza, XmppMessage message);
}
