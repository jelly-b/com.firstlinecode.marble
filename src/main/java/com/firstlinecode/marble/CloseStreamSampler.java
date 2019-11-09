package com.firstlinecode.marble;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

import com.firstlinecode.chalk.IChatClient;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;
import com.firstlinecode.marble.XmppMessage.Direction;

public class CloseStreamSampler extends AbstractXmppSampler {
	private static final long serialVersionUID = 4273399851769186550L;
	
	@Override
	public SampleResult sample(Entry arg0) {
		IChatClient chatClient = getChatClient();
		
		if (chatClient == null) {
			throw new IllegalStateException("can't get chat client. you should create a open stream sampler first.");
		}
		
		XmppSampleResult result = createXmppResult();
		result.setSamplerData("Close Stream");
		
		chatClient.addConnectionListener(new CloseStreamListener(result));
		
		result.sampleStart();
		
		chatClient.close();
		
		if (result.getResponseMessage() != null) {
			result.setResponseCodeOK();
			result.setResponseMessageOK();
		}
		result.setSuccessful(true);
		
		result.sampleEnd();		
		
		return result;
	}
	
	private class CloseStreamListener implements IConnectionListener {
		private XmppSampleResult result;
		
		public CloseStreamListener(XmppSampleResult result) {
			this.result = result;
		}
		
		@Override
		public void occurred(ConnectionException exception) {}

		@Override
		public void received(String message) {
			if (message.toLowerCase().indexOf("</stream:stream>") != -1) {
				result.addMessage(new XmppMessage(Direction.RECEIVED, message));
				result.setBodySize(result.getBodySize() + message.getBytes().length);
				result.setResponseData(message, "UTF-8");
			}
		}

		@Override
		public void sent(String message) {
			if (message.toLowerCase().indexOf("</stream:stream>") != -1) {
				result.addMessage(new XmppMessage(Direction.SENT, message));
				result.setBodySize(result.getBodySize() + message.getBytes().length);
			}
		}
	}
	
}
