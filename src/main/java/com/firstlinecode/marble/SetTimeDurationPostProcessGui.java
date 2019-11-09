package com.firstlinecode.marble;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import com.firstlinecode.marble.SetTimeDurationPostProcessor.TimeDurationType;

public class SetTimeDurationPostProcessGui extends AbstractPostProcessorGui {

	private static final long serialVersionUID = -8804214939622006704L;
	private JComboBox<TimeDurationType> timeDurationType;
	private JTextField varName;
	
	public SetTimeDurationPostProcessGui() {
		init();
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());
		
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(createSetTimeDurationPanel());
		
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel createSetTimeDurationPanel() {
		VerticalPanel setTimeDurationPanel = new VerticalPanel();
		setTimeDurationPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("set_time_duration_set_time_duration")));
		
		setTimeDurationPanel.add(createTimeDurationTypePanel());
		setTimeDurationPanel.add(createVariablePanel());
		
		return setTimeDurationPanel;
	}

	private JPanel createTimeDurationTypePanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("set_time_duration_type"));
		timeDurationType = new JComboBox<TimeDurationType>(getTimeDurationTypes());
		
		label.setLabelFor(timeDurationType);
		

		JPanel timeDurationTypePanel = new JPanel(new BorderLayout(5, 0));
		timeDurationTypePanel.add(label, BorderLayout.WEST);
		timeDurationTypePanel.add(timeDurationType, BorderLayout.CENTER);

		return timeDurationTypePanel;
	}

	private TimeDurationType[] getTimeDurationTypes() {
		return new TimeDurationType[] {TimeDurationType.TEST, TimeDurationType.THREAD};
	}

	private JPanel createVariablePanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("set_time_duration_var_name"));
		varName = new JTextField();
		
		label.setLabelFor(varName);
		

		JPanel varNamePanel = new JPanel(new BorderLayout(5, 0));
		varNamePanel.add(label, BorderLayout.WEST);
		varNamePanel.add(varName, BorderLayout.CENTER);

		return varNamePanel;
	}

	@Override
	public TestElement createTestElement() {
		SetTimeDurationPostProcessor postProcessor = new SetTimeDurationPostProcessor();
		modifyTestElement(postProcessor);
		 
		return postProcessor;
	}

	@Override
	public String getLabelResource() {
		return "set_time_duration_title";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		SetTimeDurationPostProcessor postProcessor = (SetTimeDurationPostProcessor)arg0;
		postProcessor.setTimeDurationType((TimeDurationType)timeDurationType.getSelectedItem());
		postProcessor.setVarName(varName.getText());
		
		super.configureTestElement(postProcessor);
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		
		SetTimeDurationPostProcessor setTimeDurationPostProcessor = (SetTimeDurationPostProcessor)element;
		
		timeDurationType.setSelectedItem(setTimeDurationPostProcessor.getTimeDurationType());
		varName.setText(setTimeDurationPostProcessor.getVarName());
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		
		timeDurationType.setSelectedIndex(0);
		varName.setText("");
	}

}
