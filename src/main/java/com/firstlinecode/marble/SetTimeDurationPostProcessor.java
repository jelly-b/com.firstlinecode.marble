package com.firstlinecode.marble;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;

public class SetTimeDurationPostProcessor extends AbstractScopedTestElement implements PostProcessor, ThreadListener {

	private static final long serialVersionUID = 4185637766190802599L;
	
	private static final String TIME_DURATION_TYPE = "SetTimeDurationPostProcessor.timeDurationType";
	private static final String VAR_NAME = "SetTimeDurationPostProcessor.varName";
	
	public enum TimeDurationType {
		TEST {
			@Override
			public String toString() {
				return "Test Time Duration";
			}
		},
		THREAD {
			@Override
			public String toString() {
				return "Thread Time Duration";
			}
		}
	}

	@Override
	public void process() {
		checkArguments();
		
		setTimeDurationVariable();
	}

	private void setTimeDurationVariable() {
		long start;
		if (getTimeDurationType() == TimeDurationType.TEST) {
			start = JMeterContextService.getTestStartTime();
		} else {
			start = getThreadContext().getThread().getStartTime();
		}
		
		long current = System.currentTimeMillis();
		
		getThreadContext().getVariables().putObject(getVarName(), Long.toString((current - start) / 1000));
	}
	
	private void checkArguments() {
		if (getTimeDurationType() == null) {
			throw new IllegalArgumentException("null time duration type. it's required.");
		}
		
		String varName = getVarName();
		if (varName == null || "".equals(varName)) {
			throw new IllegalArgumentException("null var name. it's required.");
		}
	}

	public void setTimeDurationType(TimeDurationType timeDurationType) {
		setProperty(new ObjectProperty(TIME_DURATION_TYPE, timeDurationType));
	}
	
	public TimeDurationType getTimeDurationType() {
		TimeDurationType timeDurationType = (TimeDurationType)getProperty(TIME_DURATION_TYPE).getObjectValue();
		
		return timeDurationType;
	}
	
	public void setVarName(String varName) {
		setProperty(new StringProperty(VAR_NAME, varName));
	}
	
	public String getVarName() {
		return getPropertyAsString(VAR_NAME);
	}

	@Override
	public void threadFinished() {}

	@Override
	public void threadStarted() {
		setTimeDurationVariable();
	}

}
