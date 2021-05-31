package com.firstlinecode.marble;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.error.Forbidden;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.chalk.core.AuthFailureException;
import com.firstlinecode.chalk.core.IChatClient;
import com.firstlinecode.chalk.core.IPlugin;
import com.firstlinecode.chalk.core.StandardChatClient;
import com.firstlinecode.chalk.core.stream.INegotiationListener;
import com.firstlinecode.chalk.core.stream.IStream;
import com.firstlinecode.chalk.core.stream.IStreamNegotiant;
import com.firstlinecode.chalk.core.stream.NegotiationException;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.core.stream.UsernamePasswordToken;
import com.firstlinecode.chalk.im.IInstantingMessager;
import com.firstlinecode.chalk.im.subscription.ISubscriptionListener;
import com.firstlinecode.chalk.im.subscription.SubscriptionError;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.IConnectionListener;
import com.firstlinecode.marble.im.SubscriptionManager;
import com.firstlinecode.marble.im.SubscriptionManager.Action;

public class OpenStreamSampler extends AbstractXmppSampler {
	
	private static final long serialVersionUID = -6178727173054977886L;
	
	public static final String HOST = "OpenStreamSampler.host";
	public static final String PORT = "OpenStreamSampler.port";
	public static final String TLS_REQUIRED = "OpenStreamSampler.tlsRequired";
	public static final String USERNAME = "OpenStreamSampler.username";
	public static final String PASSWORD = "OpenStreamSampler.password";
	public static final String RESOURCE = "OpenStreamSampler.resource";
	
	private static final Logger logger = LoggingManager.getLoggerForClass();
	
	private SubscriptionManager subscriptionManager;
	
	@SuppressWarnings("unchecked")
	@Override
	public SampleResult sample(Entry arg0) {
		checkArguments();
		
		IChatClient chatClient = new StandardChatClient(getStreamConfig());
		
		XmppSampleResult result = createXmppResult();
		
		StringBuilder samplerData = new StringBuilder();
		StreamConfig streamConfig = getStreamConfig();
		samplerData.append("Open Stream\n");
		samplerData.append("Host: ").append(streamConfig.getHost()).append('\n');
		samplerData.append("Port: ").append(streamConfig.getPort()).append('\n');
		
		OpenStreamListener openStreamListener = new OpenStreamListener(result);
		chatClient.getConnection().addListener(openStreamListener);
		chatClient.addNegotiationListener(openStreamListener);
		
		result.sampleStart();
		try {
			chatClient.connect(new UsernamePasswordToken(getUsername(), getPassword()));
			if (logger.isDebugEnabled())
				logger.debug("chat client has connected to server");
			
			JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
			List<JMeterTreeNode> nodes = treeModel.getNodesOfType(IPluginContributor.class);
			
			for (JMeterTreeNode node : nodes) {
				IPluginContributor pluginContributor = (IPluginContributor)node.getUserObject();
				Class<?>[] plugins = pluginContributor.getPlugins();
				if (plugins != null && plugins.length != 0) {
					for (Class<?> contributedPlugin : plugins) {
						Class<? extends IPlugin> plugin = null;
						try {
							plugin = (Class<? extends IPlugin>)contributedPlugin;
						} catch (Exception e) {
							throw new IllegalArgumentException(String.format("%s isn't a plugin type", contributedPlugin));
						}
						
						chatClient.register(plugin);
						
						if (logger.isDebugEnabled())
							logger.debug(String.format("register plugin %s to chat client", plugin));
					}
				}
			}
			
			setChatClient(chatClient);
			
			if (subscriptionManager != null && subscriptionManager.getAction() != Action.NONE) {
				IInstantingMessager im = chatClient.createApi(IInstantingMessager.class);
				
				im.getSubscriptionService().addSubscriptionListener(new SubscriptionAutoReplyListener(
						im, subscriptionManager.getAction()));
			}
			
			IInstantingMessager im;
			try {
				// IM plugin has been registered. it's a test for IM protocols.
				// so we need to retrieve roster information.
				im = chatClient.createApi(IInstantingMessager.class);
				im.getRosterService().retrieve();
			} catch (Exception e) {
				// maybe it's not a test for IM protocols. ignore the exception.
			}
			
			result.setResponseCodeOK();
			result.setResponseMessageOK();
			result.setSuccessful(true);
		} catch (ConnectionException e) {
			result.setSuccessful(false);
			result.setResponseCode(getErrorCode(ServiceUnavailable.DEFINED_CONDITION));
			if (e.getMessage() != null) {
				result.setResponseMessage(e.getMessage());
			} else {
				result.setResponseMessage("Exception: " + e.getClass().getName());
			}
			
			getThreadContext().getThread().stop();
		} catch (AuthFailureException e) {
			result.setSuccessful(false);
			result.setResponseCode(getErrorCode(Forbidden.DEFINED_CONDITION));
			result.setResponseMessage("Auth Failure");
			
			getThreadContext().getThread().stop();
		} catch (RuntimeException e) {
			if (e.getCause() instanceof NegotiationException) {
				result.setSuccessful(false);
				result.setResponseCode(getErrorCode(Forbidden.DEFINED_CONDITION));
				NegotiationException ne = (NegotiationException)e.getCause();
				result.setResponseMessage(String.format("Negotiation Failure. Source: %s",
						ne.getSource().getClass().getSimpleName()));
			}
		} finally {
			String[] negotiants = openStreamListener.getNegotiants();
			if (negotiants.length != 0) {
				samplerData.append('\n');
				samplerData.append("Negotiants: ");
				
				for (String negotiant : negotiants) {
					samplerData.append(negotiant).append(',');
				}
				
				// remove last ',' char.
				samplerData.deleteCharAt(samplerData.length() - 1);
			}
			
			StringBuilder responseData = new StringBuilder();
			if (!result.getMessages().isEmpty()) {
				responseData.append("Messages:");
				samplerData.append("\nMessages:");
				
				for (XmppMessage message : result.getMessages()) {
					if (message.getDirection() == XmppMessage.Direction.SENT) {
						samplerData.append('\n');
						samplerData.append(message.getMessage());
					} else {
						responseData.append('\n');
						responseData.append(message.getMessage());
					}
				}
			}
			
			result.setSamplerData(samplerData.toString());
			result.setResponseData(responseData.toString(), "UTF-8");
			
			result.sampleEnd();
		}
		
		return result;
	}
	
	private class SubscriptionAutoReplyListener implements ISubscriptionListener {
		private IInstantingMessager im;
		private Action action;
		
		public SubscriptionAutoReplyListener(IInstantingMessager im, Action action) {
			this.im = im;
			this.action = action;
		}

		@Override
		public void asked(JabberId user) {
			if (action == Action.ACCEPT) {
				im.getSubscriptionService().approve(user);
			} else {
				im.getSubscriptionService().refuse(user);
			}
		}

		@Override
		public void approved(JabberId contact) {}

		@Override
		public void refused(JabberId contact) {}

		@Override
		public void revoked(JabberId user) {}

		@Override
		public void occurred(SubscriptionError error) {}
	}
	
	private void checkArguments() {
		if (isBlank(getHost())) {
			throw new IllegalArgumentException("null host. it's required.");
		}
		
		String sPort = getPort();
		if (!isBlank(sPort)) {
			try {
				Integer.parseInt(sPort);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("port must be a number.");
			}
		}
		
		if (isBlank(getUsername()) || isBlank(getPassword())) {
			throw new IllegalArgumentException("null username or password. they're required.");
		}
	}
	
	private StandardStreamConfig getStreamConfig() {
		StandardStreamConfig streamConfig = new StandardStreamConfig(getHost(), getPortAsInt());
		streamConfig.setResource(getResource());
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
	
	public void setResource(String resource) {
		setProperty(new StringProperty(RESOURCE, resource));
	}
	
	public String getResource() {
		return getPropertyAsString(RESOURCE);
	}
	
	private void setChatClient(IChatClient chatClient) {
		JMeterContext context = getThreadContext();
		
		context.getVariables().putObject(AbstractXmppSampler.CHAT_CLIENT_INSTANCE, chatClient);
	}
	
	@Override
	public void addTestElement(TestElement element) {
		if (element instanceof SubscriptionManager) {
			subscriptionManager = (SubscriptionManager)element;
		} else {
			super.addTestElement(element);
		}
	}
	
	private class OpenStreamListener implements IConnectionListener, INegotiationListener {
		private XmppSampleResult result;
		private List<String> negotiants;
		
		public OpenStreamListener(XmppSampleResult result) {
			this.result = result;
			negotiants = new ArrayList<String>();
		}
		
		@Override
		public void messageSent(String message) {
			result.addMessage(new XmppMessage(XmppMessage.Direction.SENT, message));
			result.setBodySize(result.getBodySize() + message.getBytes().length);
		}

		@Override
		public void messageReceived(String message) {
			if (result.getLatency() == 0) {
				result.latencyEnd();
			}
			
			result.addMessage(new XmppMessage(XmppMessage.Direction.RECEIVED, message));
			result.setBytes(result.getBytes() + message.getBytes().length);
		}

		@Override
		public void exceptionOccurred(ConnectionException exception) {}
		
		@Override
		public void heartBeatsReceived(int length) {}

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
	
}
