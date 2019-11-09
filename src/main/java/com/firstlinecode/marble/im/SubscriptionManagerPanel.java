package com.firstlinecode.marble.im;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import com.firstlinecode.marble.im.SubscriptionManager.Action;

public class SubscriptionManagerPanel extends AbstractConfigGui {

	private static final long serialVersionUID = 31785093672001161L;
	
	private JRadioButton accept;
	private JRadioButton refuse;
	private JRadioButton none;

	public SubscriptionManagerPanel() {
		init();
	}
	
	public void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
        
		VerticalPanel mainPanel = new VerticalPanel();
		
		JPanel subscriptionActionPanel = new HorizontalPanel();
		subscriptionActionPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString(
				"xmpp_subscription_action_taken")));
		subscriptionActionPanel.add(createSubscriptionActionPanel(), BorderLayout.CENTER);
		
		mainPanel.add(subscriptionActionPanel);
		
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel createSubscriptionActionPanel() {
		VerticalPanel panel = new VerticalPanel();
		
		accept = new JRadioButton(JMeterUtils.getResString("xmpp_subscription_action_accept"));
		refuse = new JRadioButton(JMeterUtils.getResString("xmpp_subscription_action_refuse"));
		none = new JRadioButton(JMeterUtils.getResString("xmpp_subscription_action_none"));
		
		ButtonGroup group = new ButtonGroup();
		group.add(accept);
		group.add(refuse);
		group.add(none);
		
		panel.add(accept);
		panel.add(refuse);
		panel.add(none);

		return panel;
	}

	@Override
	public TestElement createTestElement() {
		SubscriptionManager element = new SubscriptionManager();
		modifyTestElement(element);
		
		return element;
	}

	@Override
	public String getLabelResource() {
		return "xmpp_subscription_manager_title";
	}

	@Override
	public void modifyTestElement(TestElement element) {
		super.configureTestElement(element);
		
		SubscriptionManager subscriptionManager = (SubscriptionManager)element;
		if (accept.isSelected()) {
			subscriptionManager.setAction(Action.ACCEPT);
		} else if (refuse.isSelected()) {
			subscriptionManager.setAction(Action.REJECT);
		} else {
			subscriptionManager.setAction(Action.NONE);
		}
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		
		SubscriptionManager subscriptionManager = (SubscriptionManager)element;
		if (subscriptionManager.getAction() == Action.ACCEPT) {
			accept.setSelected(true);
		} else if (subscriptionManager.getAction() == Action.REJECT) {
			refuse.setSelected(true);
		} else {
			none.setSelected(true);
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		
		none.setSelected(true);
	}

}
