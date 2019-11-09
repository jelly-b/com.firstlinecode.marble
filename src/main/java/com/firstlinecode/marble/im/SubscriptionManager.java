package com.firstlinecode.marble.im;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.ObjectProperty;

import com.firstlinecode.chalk.im.InstantingMessagerPlugin;
import com.firstlinecode.marble.IPluginContributor;

public class SubscriptionManager extends ConfigTestElement implements IPluginContributor {
	
	private static final long serialVersionUID = -1481731790425863107L;

	public enum Action {
		NONE,
		ACCEPT,
		REJECT
	}
	
	public static final String ACTION = "SubscriptionManager.action";
	
	public void setAction(Action action) {
		setProperty(new ObjectProperty(ACTION, action));
	}
	
	public Action getAction() {
		Action action = (Action)getProperty(ACTION).getObjectValue();
		
		return action == null ? Action.NONE : action;
	}

	@Override
	public Class<?>[] getPlugins() {
		return new Class<?>[] {InstantingMessagerPlugin.class};
	}
	
}
