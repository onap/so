package org.openecomp.mso.client.dmaap;

import java.util.Iterator;
import java.util.ServiceLoader;

public class DmaapPropertiesLoader {

	private final ServiceLoader<DmaapProperties> services;
	private DmaapPropertiesLoader() {
		services = ServiceLoader.load(DmaapProperties.class);
	}
	
	private static class Helper {
		private static final DmaapPropertiesLoader INSTANCE = new DmaapPropertiesLoader();
	}
	
	public static DmaapPropertiesLoader getInstance() {
		return Helper.INSTANCE;
	}
	
	public DmaapProperties getImpl() {
		Iterator<DmaapProperties> propertyImpls = services.iterator();
		while (propertyImpls.hasNext()) {
			return propertyImpls.next();
		}
		
		return null;
	}
}
