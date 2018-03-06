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
import org.openecomp.mso.db.catalog.beans.VnfRecipe;

/**
 */

public class VnfRecipeTest {

	@Test
	public final void vnfRecipeDataTest() {
		VnfRecipe vnfRecipe = new VnfRecipe();
		vnfRecipe.setCreated(new Timestamp(System.currentTimeMillis()));
		assertTrue(vnfRecipe.getCreated() != null);
		vnfRecipe.setDescription("description");
		assertTrue(vnfRecipe.getDescription().equalsIgnoreCase("description"));

		vnfRecipe.setOrchestrationUri("orchestrationUri");
		assertTrue(vnfRecipe.getOrchestrationUri().equalsIgnoreCase("orchestrationUri"));

		vnfRecipe.setRecipeTimeout(1);
		assertTrue(vnfRecipe.getRecipeTimeout() == 1);
		vnfRecipe.setVnfType("vnfType");
		assertTrue(vnfRecipe.getVnfType().equalsIgnoreCase("vnfType"));

		vnfRecipe.setServiceType("serviceType");
		assertTrue(vnfRecipe.getServiceType().equalsIgnoreCase("serviceType"));
		vnfRecipe.setVersion("version");
		assertTrue(vnfRecipe.getVersion().equalsIgnoreCase("version"));
		vnfRecipe.setParamXSD("vnfParamXSD");
		assertTrue(vnfRecipe.getParamXSD().equalsIgnoreCase("vnfParamXSD"));
		vnfRecipe.setVfModuleId("vfModuleId");
		assertTrue(vnfRecipe.getVfModuleId().equalsIgnoreCase("vfModuleId"));
//		assertTrue(vnfRecipe.toString() == null);

	}

}
