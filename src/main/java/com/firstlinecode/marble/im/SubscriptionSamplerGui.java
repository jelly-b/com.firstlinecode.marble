package com.firstlinecode.marble.im;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class SubscriptionSamplerGui extends AbstractSamplerGui {

	private static final long serialVersionUID = 5858378149969981057L;
	
	private JTextField contactJid;
	private JTextField subscriptionTimeout;
	private JCheckBox selectRandomly;
	private JTextField seqStartNumber;
	private JTextField seqEndNumber;
	
	public SubscriptionSamplerGui() {
		init();
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());
		
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(createContactPanel());
		
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel createRandomPanel() {
		VerticalPanel randomPanel = new VerticalPanel();
		
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_subscription_select_randomly"));
		
		selectRandomly = new JCheckBox();
		selectRandomly.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectRandomly.isSelected()) {
					enableSeqNumbers();
				} else {
					disableSeqNumbers();
				}
			}

			private void disableSeqNumbers() {
				if (seqStartNumber.isEnabled()) {
					seqStartNumber.setEnabled(false);
				}
				
				if (seqEndNumber.isEnabled()) {
					seqEndNumber.setEnabled(false);
				}
			}

			private void enableSeqNumbers() {
				if (!seqStartNumber.isEnabled()) {
					seqStartNumber.setEnabled(true);
				}
				
				if (!seqEndNumber.isEnabled()) {
					seqEndNumber.setEnabled(true);
				}
			}
			
		});
		label.setLabelFor(selectRandomly);
		
		JPanel selectRandomlyPanel = new JPanel(new BorderLayout(5, 0));
		selectRandomlyPanel.add(selectRandomly, BorderLayout.WEST);
		selectRandomlyPanel.add(label, BorderLayout.CENTER);
		
		JPanel seqRangePanel = new HorizontalPanel();
		seqRangePanel.add(createSeqStartNumberPanel());
		seqRangePanel.add(createSeqEndNumberPanel());
		
		randomPanel.add(selectRandomlyPanel);
		randomPanel.add(seqRangePanel);
		
		return randomPanel;
	}

	private Component createSeqEndNumberPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_subscription_seq_end_number"));
		
		seqEndNumber = new JTextField(10);
		label.setLabelFor(seqEndNumber);
		
		JPanel seqEndNumberPanel = new JPanel(new BorderLayout(5, 0));
		seqEndNumberPanel.add(label, BorderLayout.WEST);
		seqEndNumberPanel.add(seqEndNumber, BorderLayout.CENTER);
		
		return seqEndNumberPanel;	
	}

	private Component createSeqStartNumberPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_subscription_seq_start_number"));
		
		seqStartNumber = new JTextField(10);
		label.setLabelFor(seqStartNumber);
		
		JPanel seqStartNumberPanel = new JPanel(new BorderLayout(5, 0));
		seqStartNumberPanel.add(label, BorderLayout.WEST);
		seqStartNumberPanel.add(seqStartNumber, BorderLayout.CENTER);
		
		return seqStartNumberPanel;	
	}

	private JPanel createContactPanel() {
		VerticalPanel contactPanel = new VerticalPanel();
		contactPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("xmpp_subscription_contact_to_subscribe")));
		
		contactPanel.add(createContactJidPanel());
		contactPanel.add(createTimeoutPanel());
		contactPanel.add(createRandomPanel());
		
		return contactPanel;
	}
	
	private JPanel createTimeoutPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_subscription_subscription_timeout"));
		
		subscriptionTimeout = new JTextField(10);
		label.setLabelFor(subscriptionTimeout);
		
		JPanel timeoutPanel = new JPanel(new BorderLayout(5, 0));
		timeoutPanel.add(label, BorderLayout.WEST);
		timeoutPanel.add(subscriptionTimeout, BorderLayout.CENTER);
		
		return timeoutPanel;
	}

	private JPanel createContactJidPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_subscription_contact_jid"));
		
		contactJid = new JTextField(10);
		label.setLabelFor(contactJid);
		
		JPanel contactJidPanel = new JPanel(new BorderLayout(5, 0));
		contactJidPanel.add(label, BorderLayout.WEST);
		contactJidPanel.add(contactJid, BorderLayout.CENTER);
		
		return contactJidPanel;
	}

	@Override
	public String getLabelResource() {
		return "xmpp_subscription_request_sampler_title";
	}

	@Override
	public TestElement createTestElement() {
		SubscriptionSampler sampler = new SubscriptionSampler();
		modifyTestElement(sampler);
		
		return sampler;
	}

	@Override
	public void modifyTestElement(TestElement element) {
		SubscriptionSampler sampler = (SubscriptionSampler)element;
		sampler.setContactJid(contactJid.getText());
		sampler.setSubscriptionTimeout(subscriptionTimeout.getText());
		sampler.setSelectContactRandomly(selectRandomly.isSelected());
		if (selectRandomly.isSelected()) {
			sampler.setSeqStartNumber(seqStartNumber.getText());
			sampler.setSeqEndNumber(seqEndNumber.getText());
		}
		
		super.configureTestElement(element);
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		
		SubscriptionSampler sampler = (SubscriptionSampler)element;
		contactJid.setText(sampler.getContactJid());
		subscriptionTimeout.setText(sampler.getSubscriptionTimeout());
		
		if (sampler.isSelectContactRandomly()) {
			selectRandomly.setSelected(true);
			
			if (!seqStartNumber.isEnabled()) {
				seqStartNumber.setEnabled(true);
			}
			seqStartNumber.setText(sampler.getSeqStartNumber());
			
			if (!seqEndNumber.isEnabled()) {
				seqEndNumber.setEnabled(true);
			}
			seqEndNumber.setText(sampler.getSeqEndNumber());
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		
		contactJid.setText("");
		subscriptionTimeout.setText("");
		
		selectRandomly.setSelected(false);
		
		seqStartNumber.setEnabled(false);
		seqStartNumber.setText("");
		
		seqEndNumber.setEnabled(false);
		seqEndNumber.setText("");
	}

}
