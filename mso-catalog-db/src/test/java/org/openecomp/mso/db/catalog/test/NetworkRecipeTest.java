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
import org.openecomp.mso.db.catalog.beans.NetworkRecipe;

/**
 */

public class NetworkRecipeTest {

	@Test
	public final void networkRecipeDataTest() {

		NetworkRecipe networkRecipe = new NetworkRecipe();
		networkRecipe.setAction("action");
		assertTrue(networkRecipe.getAction().equalsIgnoreCase("action"));
		networkRecipe.setCreated(new Timestamp(System.currentTimeMillis()));
		assertTrue(networkRecipe.getCreated() != null);
		networkRecipe.setDescription("description");
		assertTrue(networkRecipe.getDescription().equalsIgnoreCase("description"));
		networkRecipe.setId(1);
		assertTrue(networkRecipe.getId() == 1);
		networkRecipe.setModelName("modelName");
		assertTrue(networkRecipe.getModelName().equalsIgnoreCase("modelName"));
		networkRecipe.setParamXSD("networkParamXSD");
		assertTrue(networkRecipe.getParamXSD().equalsIgnoreCase("networkParamXSD"));
		networkRecipe.setOrchestrationUri("orchestrationUri");
		assertTrue(networkRecipe.getOrchestrationUri().equalsIgnoreCase("orchestrationUri"));
		networkRecipe.setRecipeTimeout(1);
		assertTrue(networkRecipe.getRecipeTimeout() == 1);
		networkRecipe.setServiceType("serviceType");
		assertTrue(networkRecipe.getServiceType().equalsIgnoreCase("serviceType"));
		networkRecipe.setVersion("version");
		assertTrue(networkRecipe.getVersion().equalsIgnoreCase("version"));
//		assertTrue(networkRecipe.toString() != null);

	}

}
