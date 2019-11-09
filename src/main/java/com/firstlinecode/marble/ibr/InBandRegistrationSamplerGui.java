package com.firstlinecode.marble.ibr;

import java.awt.BorderLayout;
import java.awt.Component;

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

public class InBandRegistrationSamplerGui extends AbstractSamplerGui {
	private static final long serialVersionUID = 3407218947396368827L;
	
	private JTextField host;
	private JTextField port;
	private JCheckBox tlsRequired;
	private JTextField username;
	private JTextField password;
	
	public InBandRegistrationSamplerGui() {
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());
		
		VerticalPanel mainPanel = new VerticalPanel();
		
		JPanel hostPanel = new HorizontalPanel();
		hostPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("xmpp_host_config")));
		hostPanel.add(createHostPanel(), BorderLayout.CENTER);
		hostPanel.add(getPortPanel(), BorderLayout.EAST);
		
		
		JPanel securityPanel = new HorizontalPanel();
		securityPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("xmpp_security_config")));
		securityPanel.add(createTlsRequiredPanel());
		
		JPanel loginPanel = new VerticalPanel();
		loginPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("xmpp_registration_info")));
		loginPanel.add(createUsernamePanel());
		loginPanel.add(createPasswordPanel());
		
		mainPanel.add(hostPanel);
		mainPanel.add(securityPanel);
		mainPanel.add(loginPanel);
		
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}
	
	private JPanel createUsernamePanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_username"));
		
		username = new JTextField(10);
		label.setLabelFor(username);
		
		JPanel usernamePanel = new JPanel(new BorderLayout(5, 0));
		usernamePanel.add(label, BorderLayout.WEST);
		usernamePanel.add(username, BorderLayout.CENTER);
		
		return usernamePanel;
    }
	
	private JPanel createPasswordPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_password"));
		
		password = new JTextField(10);
		label.setLabelFor(password);
		
		JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
		passwordPanel.add(label, BorderLayout.WEST);
		passwordPanel.add(password, BorderLayout.CENTER);
		
		return passwordPanel;
    }
	
	private Component createTlsRequiredPanel() {
		tlsRequired = new JCheckBox(JMeterUtils.getResString("xmpp_tls_required"));
		
		JPanel tlsRequiredPanel = new JPanel(new BorderLayout(5, 0));
		tlsRequiredPanel.add(tlsRequired, BorderLayout.CENTER);
		
		return tlsRequiredPanel;
	}

	private JPanel createHostPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_host"));
		
		host = new JTextField(10);
		label.setLabelFor(host);
		
		JPanel hostPanel = new JPanel(new BorderLayout(5, 0));
		hostPanel.add(label, BorderLayout.WEST);
		hostPanel.add(host, BorderLayout.CENTER);
		
		return hostPanel;
    }

    private JPanel getPortPanel() {
    	port = new JTextField(4);
    	
    	JLabel label = new JLabel(JMeterUtils.getResString("xmpp_port"));
    	label.setLabelFor(port);
    	
    	JPanel panel = new JPanel(new BorderLayout(5, 0));
    	panel.add(label, BorderLayout.WEST);
    	panel.add(port, BorderLayout.CENTER);
    	
    	return panel;
    }

	@Override
	public String getLabelResource() {
		return "xmpp_in_band_registration_sampler_title";
	}

	@Override
	public TestElement createTestElement() {
		InBandRegistrationSampler sampler =  new InBandRegistrationSampler();
		modifyTestElement(sampler);
		
		return sampler;
	}

	@Override
	public void modifyTestElement(TestElement element) {
		InBandRegistrationSampler sampler = (InBandRegistrationSampler)element;
		sampler.setHost(host.getText());
		sampler.setPort(port.getText());
		sampler.setTlsRequired(tlsRequired.isSelected());
		sampler.setUsername(username.getText());
		sampler.setPassword(password.getText());
		
		super.configureTestElement(element);
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		
		InBandRegistrationSampler sampler = (InBandRegistrationSampler)element;
		
		host.setText(sampler.getHost());
		port.setText(sampler.getPort());
		
		tlsRequired.setSelected(sampler.isTlsRequired());
		
		username.setText(sampler.getUsername());
		password.setText(sampler.getPassword());
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		
		host.setText("");
		port.setText("");
		
		tlsRequired.setSelected(true);
		
		username.setText("");
		password.setText("");
		tlsRequired.setSelected(true);
	}

}
