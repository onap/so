/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
import java.util.Date;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;

/**
 */

public class ServiceRecipeTest {

	@Test
	public final void serviceRecipeDataTest() {

		ServiceRecipe serviceRecipe = new ServiceRecipe();
		serviceRecipe.setAction("action");
		assertTrue(serviceRecipe.getAction().equalsIgnoreCase("action"));
		serviceRecipe.setCreated(new Timestamp(System.currentTimeMillis()));
		assertTrue(serviceRecipe.getCreated() != null);
		serviceRecipe.setDescription("description");
		assertTrue(serviceRecipe.getDescription().equalsIgnoreCase("description"));
		serviceRecipe.setId(1);
		assertTrue(serviceRecipe.getId() == 1);
		serviceRecipe.setOrchestrationUri("orchestrationUri");
		assertTrue(serviceRecipe.getOrchestrationUri().equalsIgnoreCase("orchestrationUri"));
		serviceRecipe.setRecipeTimeout(1);
		assertTrue(serviceRecipe.getRecipeTimeout() == 1);
		serviceRecipe.setVersion("version");
		assertTrue(serviceRecipe.getVersion().equalsIgnoreCase("version"));
		serviceRecipe.setServiceTimeoutInterim(1);
		assertTrue(serviceRecipe.getServiceTimeoutInterim() == 1);
		serviceRecipe.setServiceParamXSD("serviceParamXSD");
		assertTrue(serviceRecipe.getServiceParamXSD().equalsIgnoreCase("serviceParamXSD"));
		assertTrue(serviceRecipe.toString() != null);
		ServiceRecipe serviceRecipeWithValue = new ServiceRecipe(1, "string", "string", "string", "string", "string", 1,
				1, new Date());
		assertTrue(serviceRecipeWithValue.toString() != null);

	}

}
