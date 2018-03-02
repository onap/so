package org.openecomp.mso.client.grm;

import java.util.Iterator;
import java.util.ServiceLoader;

public class GRMPropertiesLoader {

	private final ServiceLoader<GRMProperties> services;
	private GRMPropertiesLoader() {
		services = ServiceLoader.load(GRMProperties.class);
	}
	
	private static class Helper {
		private static final GRMPropertiesLoader INSTANCE = new GRMPropertiesLoader();
	}
	
	public static GRMPropertiesLoader getInstance() {
		return Helper.INSTANCE;
	}
	
	public GRMProperties getImpl() {
		Iterator<GRMProperties> propertyImpls = services.iterator();
		while (propertyImpls.hasNext()) {
			return propertyImpls.next();
		}
		return null;
	}
}
