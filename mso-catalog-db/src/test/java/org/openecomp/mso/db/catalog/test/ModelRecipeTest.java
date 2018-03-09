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
import org.openecomp.mso.db.catalog.beans.ModelRecipe;

/**
 */

public class ModelRecipeTest {

	@Test
	public final void modelRecipeDataTest() {
		ModelRecipe modelRecipe = new ModelRecipe();
		modelRecipe.setAction("action");
		assertTrue(modelRecipe.getAction().equalsIgnoreCase("action"));
		modelRecipe.setCreated(new Timestamp(System.currentTimeMillis()));
		assertTrue(modelRecipe.getCreated() != null);
		modelRecipe.setDescription("description");
		assertTrue(modelRecipe.getDescription().equalsIgnoreCase("description"));
		modelRecipe.setId(1);
		assertTrue(modelRecipe.getId() == 1);
		modelRecipe.setModelId(1);
		assertTrue(modelRecipe.getModelId() == 1);
		modelRecipe.setModelParamXSD("modelParamXSD");
		assertTrue(modelRecipe.getModelParamXSD().equalsIgnoreCase("modelParamXSD"));
		modelRecipe.setOrchestrationUri("orchestrationUri");
		assertTrue(modelRecipe.getOrchestrationUri().equalsIgnoreCase("orchestrationUri"));
		modelRecipe.setRecipeTimeout(1);
		assertTrue(modelRecipe.getRecipeTimeout() == 1);
		modelRecipe.setSchemaVersion("schemaVersion");
		assertTrue(modelRecipe.getSchemaVersion().equalsIgnoreCase("schemaVersion"));
//		assertTrue(modelRecipe.toString() != null);
	}

}
