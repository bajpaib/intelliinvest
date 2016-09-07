package com.intelliinvest.util;

import org.apache.camel.CamelContext;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.bean.RegistryBean;

public abstract class IntelliInvestProcessor extends BeanProcessor{
	
	public IntelliInvestProcessor(CamelContext camelContext, String name, String method) {
		super(new RegistryBean(camelContext, name));
		setMethod(method);
	}
	
}
