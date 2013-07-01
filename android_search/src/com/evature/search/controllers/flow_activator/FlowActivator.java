package com.evature.search.controllers.flow_activator;

import com.evaapis.flow.FlowElement;

public abstract class FlowActivator {

	protected FlowElement  element;
	
	public FlowActivator(FlowElement element) {
		this.element = element;
	}
	
	public abstract void activate(); 
}
