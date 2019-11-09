package com.firstlinecode.marble;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;

import com.firstlinecode.basalt.protocol.core.stanza.error.BadRequest;
import com.firstlinecode.basalt.protocol.core.stanza.error.Conflict;
import com.firstlinecode.basalt.protocol.core.stanza.error.FeatureNotImplemented;
import com.firstlinecode.basalt.protocol.core.stanza.error.Forbidden;
import com.firstlinecode.basalt.protocol.core.stanza.error.Gone;
import com.firstlinecode.basalt.protocol.core.stanza.error.InternalServerError;
import com.firstlinecode.basalt.protocol.core.stanza.error.ItemNotFound;
import com.firstlinecode.basalt.protocol.core.stanza.error.JidMalformed;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAcceptable;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAllowed;
import com.firstlinecode.basalt.protocol.core.stanza.error.PaymentRequired;
import com.firstlinecode.basalt.protocol.core.stanza.error.RecipientUnavailable;
import com.firstlinecode.basalt.protocol.core.stanza.error.Redirect;
import com.firstlinecode.basalt.protocol.core.stanza.error.RegistrationRequired;
import com.firstlinecode.basalt.protocol.core.stanza.error.RemoteServerNotFound;
import com.firstlinecode.basalt.protocol.core.stanza.error.RemoteServerTimeout;
import com.firstlinecode.basalt.protocol.core.stanza.error.ServiceUnavailable;
import com.firstlinecode.basalt.protocol.core.stanza.error.StanzaError;
import com.firstlinecode.basalt.protocol.core.stanza.error.SubscriptionRequired;
import com.firstlinecode.basalt.protocol.core.stanza.error.UndefinedCondition;
import com.firstlinecode.basalt.protocol.core.stanza.error.UnexpectedRequest;
import com.firstlinecode.basalt.protocol.core.stream.error.ResourceConstraint;
import com.firstlinecode.chalk.IChatClient;

public abstract class AbstractXmppSampler extends AbstractSampler {
	private static final String STATUS_CODE_302 = "302";
	private static final String STATUS_CODE_400 = "400";
	private static final String STATUS_CODE_402 = "402";
	private static final String STATUS_CODE_403 = "403";
	private static final String STATUS_CODE_404 = "404";
	private static final String STATUS_CODE_405 = "405";
	private static final String STATUS_CODE_406 = "406";
	private static final String STATUS_CODE_407 = "407";
	private static final String STATUS_CODE_409 = "409";
	private static final String STATUS_CODE_500 = "500";
	private static final String STATUS_CODE_501 = "501";
	private static final String STATUS_CODE_503 = "503";
	private static final String STATUS_CODE_504 = "504";

	private static final long serialVersionUID = -5581312885569552924L;
	
	public static final String CHAT_CLIENT_INSTANCE = "AbstractXmppSampler.chatClient";
	
	protected IChatClient getChatClient() {
		JMeterContext context = getThreadContext();
		
		IChatClient chatClient = (IChatClient)context.getVariables().getObject(CHAT_CLIENT_INSTANCE);
		if (chatClient == null) {
			throw new IllegalStateException("null chat client");
		}
		
		return chatClient;
	}
	
	protected boolean isBlank(String argument) {
		return argument == null || "".equals(argument);
	}
	
	protected XmppSampleResult createXmppResult() {
		XmppSampleResult result = new XmppSampleResult();
		result.setSampleLabel(getName());
		
		result.setContentType("text/xml");
		result.setDataType(SampleResult.TEXT);
		
		return result;
	}
	
	protected String getErrorCode(StanzaError error) {
		return getErrorCode(error.getDefinedCondition());
	}
	
	// XEP-0086
	protected String getErrorCode(String definedCondition) {
		if (BadRequest.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_400;
		} else if (Conflict.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_409;
		} else if (FeatureNotImplemented.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_501;
		} else if (Forbidden.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_403;
		} else if (Gone.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_302;
		} else if (InternalServerError.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_500;
		} else if (ItemNotFound.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_404;
		} else if (JidMalformed.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_400;
		} else if (NotAcceptable.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_406;
		} else if (NotAllowed.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_405;
		} else if (PaymentRequired.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_402;
		} else if (RecipientUnavailable.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_404;
		} else if (Redirect.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_302;
		} else if (RegistrationRequired.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_407;
		} else if (RemoteServerNotFound.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_404;
		} else if (RemoteServerTimeout.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_504;
		} else if (ResourceConstraint.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_500;
		} else if (ServiceUnavailable.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_503;
		} else if (SubscriptionRequired.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_407;
		} else if (UndefinedCondition.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_500;
		} else if (UnexpectedRequest.DEFINED_CONDITION.equals(definedCondition)) {
			return STATUS_CODE_400;
		} else {
			return STATUS_CODE_400;
		}
	}
	
	protected void appendXmppMessagesToResult(XmppSampleResult result) {
		StringBuilder samplerData = new StringBuilder();
		if (result.getSamplerData() != null && result.getSamplerData().length() > 0) {
			samplerData.append("\nMessages:");
		} else {
			samplerData.append("Messages:");
		}
		
		StringBuilder responseData = new StringBuilder();
		
		if (!result.getMessages().isEmpty()) {
			for (XmppMessage message : result.getMessages()) {
				if (message.getDirection() == XmppMessage.Direction.SENT) {
					if (samplerData.length() > 0) {
							samplerData.append('\n');
					}
					
					samplerData.append(message.getMessage());
				} else {
					if (responseData.length() > 0) {
						responseData.append('\n');
					}
					
					responseData.append(message.getMessage());
				}
			}
		}
		
		if (result.getSamplerData() != null) {
			result.setSamplerData(result.getSamplerData() + samplerData.toString());
		} else {
			result.setSamplerData(samplerData.toString());
		}
		
		if (result.getResponseData() != null) {
			result.setResponseData(result.getResponseDataAsString() + responseData.toString(), "UTF-8");
		} else {
			result.setResponseData(responseData.toString(), "UTF-8");
		}
		
	}
}
