package com.firstlinecode.marble;

import java.awt.BorderLayout;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

public class CloseStreamSamplerGui extends AbstractSamplerGui {

	private static final long serialVersionUID = -6894062649548665623L;
	
	public CloseStreamSamplerGui() {
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());
		
		add(makeTitlePanel(), BorderLayout.NORTH);
	}
	
	@Override
	public TestElement createTestElement() {
		CloseStreamSampler sampler = new CloseStreamSampler();
		modifyTestElement(sampler);
		 
		return sampler;
	}
	
	@Override
	public String getLabelResource() {
		return "xmpp_close_stream_sampler_title";
	}

	@Override
	public void modifyTestElement(TestElement sampler) {
		super.configureTestElement(sampler);
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
	}

}
