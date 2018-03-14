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
