/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
	
	public <T> T getNewImpl(Class<? extends RestProperties> clazz) {
		return this.getImpl(clazz, true);
	}
	public <T> T getImpl(Class<? extends RestProperties> clazz) {
		return this.getImpl(clazz, false);
	}
	
	private <T> T getImpl(Class<? extends RestProperties> clazz, boolean forceNewInstance) {
		T result = null;
		Iterator<RestProperties> propertyImpls = services.iterator();
		RestProperties item;
		while (propertyImpls.hasNext()) {
			item = propertyImpls.next();
			if (clazz.isAssignableFrom(item.getClass())) {
				try {
					if (forceNewInstance) {	
						result = (T)item.getClass().newInstance();
					} else {
						result = (T)item;
					}
				} catch (InstantiationException | IllegalAccessException e) {
					/* all spi implementations must provide a public
					 * no argument constructor
					 */
					
				}
				//break;
			}
		}
		
		return result;
	}
}
