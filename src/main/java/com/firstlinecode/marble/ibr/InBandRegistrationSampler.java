package com.firstlinecode.marble.ibr;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;

import com.firstlinecode.basalt.protocol.core.stanza.error.Conflict;
import com.firstlinecode.basalt.protocol.core.stanza.error.FeatureNotImplemented;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAcceptable;
import com.firstlinecode.basalt.protocol.core.stanza.error.RemoteServerTimeout;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.basalt.protocol.core.stanza.error.UndefinedCondition;
import com.firstlinecode.basalt.xeps.ibr.IqRegister;
import com.firstlinecode.basalt.xeps.ibr.RegistrationField;
import com.firstlinecode.basalt.xeps.ibr.RegistrationForm;
import com.firstlinecode.chalk.IChatClient;
import com.firstlinecode.chalk.StandardChatClient;
import com.firstlinecode.chalk.core.stream.INegotiationListener;
import com.firstlinecode.chalk.core.stream.IStream;
import com.firstlinecode.chalk.core.stream.IStreamNegotiant;
import com.firstlinecode.chalk.core.stream.NegotiationException;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;
import com.firstlinecode.chalk.xeps.ibr.IRegistration;
import com.firstlinecode.chalk.xeps.ibr.IRegistrationCallback;
import com.firstlinecode.chalk.xeps.ibr.IbrError;
import com.firstlinecode.chalk.xeps.ibr.IbrPlugin;
import com.firstlinecode.chalk.xeps.ibr.RegistrationException;
import com.firstlinecode.marble.AbstractXmppSampler;
import com.firstlinecode.marble.XmppMessage;
import com.firstlinecode.marble.XmppSampleResult;

public class InBandRegistrationSampler extends AbstractXmppSampler {
	private static final long serialVersionUID = 1L;
	
	public static final String HOST = "InBandRegistrationSampler.host";
	public static final String PORT = "InBandRegistrationSampler.port";
	public static final String TLS_REQUIRED = "InBandRegistrationSampler.tlsRequired";
	public static final String USERNAME = "InBandRegistrationSampler.username";
	public static final String PASSWORD = "InBandRegistrationSampler.password";
	
	@Override
	public SampleResult sample(Entry entry) {
		checkArguments();
		
		IChatClient chatClient = createChatClient(getStreamConfig());
		
		chatClient.register(IbrPlugin.class);
		
		IRegistration registration = chatClient.createApi(IRegistration.class);
		
		StringBuilder samplerData = new StringBuilder();
		StreamConfig streamConfig = getStreamConfig();
		samplerData.append("In Band Registration\n");
		samplerData.append("Host: ").append(streamConfig.getHost()).append('\n');
		samplerData.append("Port: ").append(streamConfig.getPort()).append('\n');
		samplerData.append("Registration API Implementation Class: " + registration.getClass().getName());
		samplerData.append('\n');
		samplerData.append("Registration Callback: " + DefaultRegistrationCallback.class.getName());
		
		XmppSampleResult result = createXmppResult();
		RegistrationListener registrationListener = new RegistrationListener(result);
		registration.addConnectionListener(registrationListener);
		registration.addNegotiationListener(registrationListener);
		
		result.sampleStart();
		try {
			registration.register(getRegistrationCallback(samplerData));
			result.setResponseCodeOK();
			result.setResponseMessageOK();
			result.setSuccessful(true);
		} catch (RegistrationException e) {
			result.setSuccessful(false);
			result.setResponseCode(getResponseCode(e.getError()));
			Throwable cause = e.getCause();
			if (cause != null) {
				result.setResponseMessage(e.getMessage() == null ?
						"Exception: " + cause.getClass().getName() :
							cause.getMessage());
			} else {
				result.setResponseMessage("Negotiation Failure: " + e.getError());
			}
		} finally {
			String[] negotiants = registrationListener.getNegotiants();
			if (negotiants.length != 0) {
				samplerData.append('\n');
				samplerData.append("Negotiants: ");
				
				for (String negotiant : negotiants) {
					samplerData.append(negotiant).append(',');
				}
				
				// remove last ',' char.
				samplerData.deleteCharAt(samplerData.length() - 1);
			}
			
			result.setSamplerData(samplerData.toString());
			
			appendXmppMessagesToResult(result);
			
			result.sampleEnd();
		}
		
		return result;
	}

	private void checkArguments() {
		if (isBlank(getHost())) {
			throw new IllegalArgumentException("null host. it's required.");
		}
		
		if (isBlank(getUsername()) || isBlank(getPassword())) {
			throw new IllegalArgumentException("null username or password. they're required.");
		}
	}

	protected IChatClient createChatClient(StreamConfig streamConfig) {
		return new StandardChatClient(getStreamConfig());
	}

	private String getResponseCode(IbrError error) {
		String definedCondition;
		if (error == IbrError.CONFLICT) {
			definedCondition = Conflict.DEFINED_CONDITION;
		} else if (error == IbrError.CONNECTION_ERROR) {
			definedCondition = ServiceUnavailable.DEFINED_CONDITION;
		} else if (error == IbrError.NOT_ACCEPTABLE) {
			definedCondition = NotAcceptable.DEFINED_CONDITION;
		} else if (error == IbrError.NOT_SUPPORTED) {
			definedCondition = FeatureNotImplemented.DEFINED_CONDITION;
		} else if (error == IbrError.TIMEOUT) {
			definedCondition = RemoteServerTimeout.DEFINED_CONDITION;
		} else {
			definedCondition = UndefinedCondition.DEFINED_CONDITION;
		}
		
		return getErrorCode(definedCondition);
	}

	protected DefaultRegistrationCallback getRegistrationCallback(StringBuilder samplerData) {
		return new DefaultRegistrationCallback(samplerData);
	}
	
	private class RegistrationListener implements IConnectionListener, INegotiationListener {
		private XmppSampleResult result;
		private List<String> negotiants;
		
		public RegistrationListener(XmppSampleResult result) {
			this.result = result;
			negotiants = new ArrayList<String>();
		}
		
		@Override
		public void sent(String message) {
			result.addMessage(new XmppMessage(XmppMessage.Direction.SENT, message));
		}

		@Override
		public void received(String message) {
			if (result.getLatency() == 0) {
				result.latencyEnd();
			}
			
			result.addMessage(new XmppMessage(XmppMessage.Direction.RECEIVED, message));
			result.setBodySize(result.getBodySize() + message.getBytes().length);
			result.setBytes(result.getBytes() + message.getBytes().length);
		}

		@Override
		public void occurred(ConnectionException exception) {}

		@Override
		public void before(IStreamNegotiant source) {
			negotiants.add(source.getClass().getName());
		}

		@Override
		public void after(IStreamNegotiant source) {}

		@Override
		public void occurred(NegotiationException exception) {}

		@Override
		public void done(IStream stream) {
			result.connectEnd();
		}
		
		public String[] getNegotiants() {
			return negotiants.toArray(new String[negotiants.size()]);
		}
	}

	private class DefaultRegistrationCallback implements IRegistrationCallback {
		private StringBuilder samplerData;
		
		public DefaultRegistrationCallback(StringBuilder samplerData) {
			this.samplerData = samplerData;
		}
		
		@Override
		public Object fillOut(IqRegister iqRegister) {
			if (iqRegister.getRegister() instanceof RegistrationForm) {
				samplerData.append('\n');
				samplerData.append("Registration Info: ").append("username = ").append(getUsername()).
					append(',').append("password = ").append(getPassword());
				
				RegistrationForm form = new RegistrationForm();
				form.getFields().add(new RegistrationField("username", getUsername()));
				form.getFields().add(new RegistrationField("password", getPassword()));
				
				return form;
			} else {
				throw new RuntimeException("can't get registration form");
			}
		}
	}
	
	private StandardStreamConfig getStreamConfig() {
		StandardStreamConfig streamConfig = new StandardStreamConfig(getHost(), getPortAsInt());
		streamConfig.setTlsPreferred(isTlsRequired());
		
		return streamConfig;
	}

	public void setHost(String host) {
		setProperty(new StringProperty(HOST, host));
	}
	
	public String getHost() {
		return getPropertyAsString(HOST);
	}
	
	public void setPort(String port) {
		setProperty(PORT, port, "");
	}
	
	public String getPort() {
		return getPropertyAsString(PORT, "");
	}
	
	public int getPortAsInt() {
		return getPropertyAsInt(PORT, 5222);
	}
	
	public void setTlsRequired(boolean tlsRequired) {
		setProperty(new BooleanProperty(TLS_REQUIRED, tlsRequired));
	}
	
	public boolean isTlsRequired() {
		return getPropertyAsBoolean(TLS_REQUIRED, true);
	}
	
	public void setUsername(String username) {
		setProperty(new StringProperty(USERNAME, username));
	}
	
	public String getUsername() {
		return getPropertyAsString(USERNAME);
	}
	
	public void setPassword(String password) {
		setProperty(new StringProperty(PASSWORD, password));
	}
	
	public String getPassword() {
		return getPropertyAsString(PASSWORD);
	}

}
