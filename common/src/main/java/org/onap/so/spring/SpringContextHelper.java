package org.onap.so.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHelper implements ApplicationContextAware {

	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		context = applicationContext;
	}

	public static ApplicationContext getAppContext() {
		return context;
	}
}