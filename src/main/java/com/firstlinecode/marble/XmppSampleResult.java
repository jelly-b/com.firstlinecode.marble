package com.firstlinecode.marble;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.samplers.SampleResult;

public class XmppSampleResult extends SampleResult {
	private static final long serialVersionUID = 6373679103236735106L;
	
	private List<XmppMessage> messages = new ArrayList<XmppMessage>();
	
	public synchronized void addMessage(XmppMessage message) {
		messages.add(message);
	}
	
	public List<XmppMessage> getMessages() {
		return messages;
	}
}
