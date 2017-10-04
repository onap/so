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
import org.openecomp.mso.db.catalog.beans.Model;

/**
 */

public class ModelTest {

	@Test
	public final void modelDataTest() {
		Model model = new Model();
		model.setId(1);
		assertTrue(model.getId() == 1);

		model.setCreated(new Timestamp(System.currentTimeMillis()));
		assertTrue(model.getCreated() != null);
		model.setModelCustomizationId("modelCustomizationId");

		assertTrue(model.getModelCustomizationId().equalsIgnoreCase("modelCustomizationId"));
		model.setModelCustomizationName("modelCustomizationName");
		assertTrue(model.getModelCustomizationName().equalsIgnoreCase("modelCustomizationName"));

		model.setModelInvariantId("modelInvariantId");
		assertTrue(model.getModelInvariantId().equalsIgnoreCase("modelInvariantId"));
		model.setModelName("modelName");
		assertTrue(model.getModelName().equalsIgnoreCase("modelName"));

		model.setModelType("modelType");
		assertTrue(model.getModelType().equalsIgnoreCase("modelType"));
		model.setModelVersion("modelVersion");
		assertTrue(model.getModelVersion().equalsIgnoreCase("modelVersion"));
		model.setModelVersionId("modelVersionId");
		assertTrue(model.getModelVersionId().equalsIgnoreCase("modelVersionId"));
		model.setVersion("1");
		assertTrue(model.getVersion().equalsIgnoreCase("1"));
		model.setRecipes(null);

		assertTrue(model.getRecipes() == null);
//		assertTrue(model.toString() != null);

	}

}
