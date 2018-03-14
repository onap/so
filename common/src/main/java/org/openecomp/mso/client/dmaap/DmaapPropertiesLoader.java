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

package org.openecomp.mso.client.dmaap;

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
		return this.getImpl(false);
	}
	public DmaapProperties getNewImpl() {
		return this.getImpl(true);
	}
	private DmaapProperties getImpl(boolean forceNewInstance) {
		for (DmaapProperties service : services) {
			if (forceNewInstance) {
				try {
					return service.getClass().newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					/* all spi implementations must provide a public
					 * no argument constructor
					 */
				}
			} else {
				return service;
			}
		}
		
		return null;
	}
}
