package com.firstlinecode.marble.im;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class MessageSamplerGui extends AbstractSamplerGui {
	private static final long serialVersionUID = 5175573247190818151L;
	
	private JTextField contactJid;
	private JCheckBox selectRandomly;
	
	public MessageSamplerGui() {
		init();
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());
		
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(createMessagePanel());
		
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}
	
	private JPanel createMessagePanel() {
		VerticalPanel contactPanel = new VerticalPanel();
		contactPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("xmpp_message_contact_jid_to_send_message")));
		
		contactPanel.add(createContactJidPanel());
		contactPanel.add(createRandomPanel());
		
		return contactPanel;
	}
	
	private JPanel createRandomPanel() {
		VerticalPanel randomPanel = new VerticalPanel();
		
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_message_select_randomly"));
		
		selectRandomly = new JCheckBox();
		selectRandomly.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectRandomly.isSelected()) {
					disableContactJid();
				} else {
					enableContactJid();
				}
			}

			private void disableContactJid() {
				if (contactJid.isEnabled()) {
					contactJid.setEnabled(false);
				}
			}

			private void enableContactJid() {
				if (!contactJid.isEnabled()) {
					contactJid.setEnabled(true);
				}
			}
			
		});
		label.setLabelFor(selectRandomly);
		
		randomPanel.add(label, BorderLayout.WEST);
		randomPanel.add(selectRandomly, BorderLayout.CENTER);
		
		return randomPanel;
	}
	
	private JPanel createContactJidPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_message_contact_jid"));
		
		contactJid = new JTextField(10);
		label.setLabelFor(contactJid);
		
		JPanel contactJidPanel = new JPanel(new BorderLayout(5, 0));
		contactJidPanel.add(label, BorderLayout.WEST);
		contactJidPanel.add(contactJid, BorderLayout.CENTER);
		
		return contactJidPanel;
	}

	@Override
	public TestElement createTestElement() {
		MessageSampler sampler = new MessageSampler();
		modifyTestElement(sampler);
		
		return sampler;
	}

	@Override
	public String getLabelResource() {
		return "xmpp_message_sampler_title";
	}

	@Override
	public void modifyTestElement(TestElement element) {
		MessageSampler sampler = (MessageSampler)element;
		sampler.setSelectContactRandomly(selectRandomly.isSelected());
		if (!selectRandomly.isSelected()) {
			sampler.setContactJid(contactJid.getText());			
		} else {
			sampler.setContactJid(null);
		}
		
		super.configureTestElement(element);
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		
		MessageSampler sampler = (MessageSampler)element;		
		if (sampler.isSelectContactRandomly()) {
			selectRandomly.setSelected(true);
			if (contactJid.isEnabled())
				contactJid.setEnabled(false);
		} else {
			contactJid.setText(sampler.getContactJid());
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		
		contactJid.setText("");
		selectRandomly.setSelected(false);
	}

}
