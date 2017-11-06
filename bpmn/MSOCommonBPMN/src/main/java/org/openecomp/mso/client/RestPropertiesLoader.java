package org.openecomp.mso.client;

import java.util.Iterator;
import java.util.ServiceLoader;

public class RestPropertiesLoader {

	private final ServiceLoader<RestProperties> services;
	private RestPropertiesLoader() {
		services = ServiceLoader.load(RestProperties.class);
	}
	
	private static class Helper {
		private static final RestPropertiesLoader INSTANCE = new RestPropertiesLoader();
	}
	
	public static RestPropertiesLoader getInstance() {
		return Helper.INSTANCE;
	}
	
	public <T> T getImpl(Class<? extends RestProperties> clazz) {
		T result = null;
		Iterator<RestProperties> propertyImpls = services.iterator();
		RestProperties item;
		while (propertyImpls.hasNext()) {
			item = propertyImpls.next();
			if (clazz.isAssignableFrom(item.getClass())) {
				result = (T)item;
				break;
			}
		}
		
		return result;
	}
}
