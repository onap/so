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

package org.openecomp.mso.db.catalog.test;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.ServiceToAllottedResources;

/**
 */

public class ServiceToAllottedResourcesTest {

	@Test
	public final void serviceToAllottedResourcesDataTest() {
		ServiceToAllottedResources serviceToAllottedResources = new ServiceToAllottedResources();
		serviceToAllottedResources.setArModelCustomizationUuid("arModelCustomizationUuid");
		assertTrue(
				serviceToAllottedResources.getArModelCustomizationUuid().equalsIgnoreCase("arModelCustomizationUuid"));
		serviceToAllottedResources.setCreated(new Timestamp(System.currentTimeMillis()));
		assertTrue(serviceToAllottedResources.getCreated() != null);
		serviceToAllottedResources.setServiceModelUuid("serviceModelUuid");
		assertTrue(serviceToAllottedResources.getServiceModelUuid().equalsIgnoreCase("serviceModelUuid"));
//		assertTrue(serviceToAllottedResources.toString() != null);

	}

}
