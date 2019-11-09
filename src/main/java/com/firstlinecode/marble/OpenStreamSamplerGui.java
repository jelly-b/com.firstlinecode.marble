package com.firstlinecode.marble;

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

public class OpenStreamSamplerGui extends AbstractSamplerGui {

	private static final long serialVersionUID = 8253934933567089296L;
	
	private JTextField host;
	private JTextField port;
	private JCheckBox tlsRequired;
	private JTextField username;
	private JTextField password;
	private JTextField resource;
	
	public OpenStreamSamplerGui() {
		init();
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());
		
		VerticalPanel mainPanel = new VerticalPanel();
		
		JPanel hostPanel = new HorizontalPanel();
		hostPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("xmpp_host_config")));
		hostPanel.add(createHostPanel(), BorderLayout.CENTER);
		hostPanel.add(createPortPanel(), BorderLayout.EAST);
		
		JPanel securityPanel = new HorizontalPanel();
		securityPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("xmpp_security_config")));
		securityPanel.add(createTlsRequiredPanel());
		
		JPanel loginPanel = new VerticalPanel();
		loginPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("xmpp_login_config")));
		loginPanel.add(createUsernamePanel());
		loginPanel.add(createPasswordPanel());
		loginPanel.add(createResourcePanel());
		
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
	
	private JPanel createResourcePanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("xmpp_resource"));
		
		resource = new JTextField(10);
		label.setLabelFor(resource);
		
		JPanel resourcePanel = new JPanel(new BorderLayout(5, 0));
		resourcePanel.add(label, BorderLayout.WEST);
		resourcePanel.add(resource, BorderLayout.CENTER);
		
		return resourcePanel;
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

    private JPanel createPortPanel() {
    	port = new JTextField(4);
    	
    	JLabel label = new JLabel(JMeterUtils.getResString("xmpp_port"));
    	label.setLabelFor(port);
    	
    	JPanel panel = new JPanel(new BorderLayout(5, 0));
    	panel.add(label, BorderLayout.WEST);
    	panel.add(port, BorderLayout.CENTER);
    	
    	return panel;
    }

	@Override
	public TestElement createTestElement() {
		OpenStreamSampler sampler = new OpenStreamSampler();
		modifyTestElement(sampler);
		 
		return sampler;
	}

	@Override
	public String getLabelResource() {
		return "xmpp_open_stream_sampler_title";
	}
		
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		
		OpenStreamSampler openStreamSampler = (OpenStreamSampler)element;
		
		host.setText(openStreamSampler.getHost());
		port.setText(openStreamSampler.getPort());
		
		tlsRequired.setSelected(openStreamSampler.isTlsRequired());
		
		username.setText(openStreamSampler.getUsername());
		password.setText(openStreamSampler.getPassword());
		resource.setText(openStreamSampler.getResource());
	}
	
	@Override
	public void modifyTestElement(TestElement sampler) {
		OpenStreamSampler openStreamSampler = (OpenStreamSampler)sampler;
		openStreamSampler.setHost(host.getText());
		openStreamSampler.setPort(port.getText());
		
		openStreamSampler.setTlsRequired(tlsRequired.isSelected());
		
		openStreamSampler.setUsername(username.getText());
		openStreamSampler.setPassword(password.getText());
		openStreamSampler.setResource(resource.getText());
		
		super.configureTestElement(sampler);
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		
		host.setText("");
		port.setText("");
		
		tlsRequired.setSelected(true);
		
		username.setText("");
		password.setText("");
		resource.setText("");
	}

}
