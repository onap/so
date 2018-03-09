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

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.Recipe;

/**
 */

public class RecipeTest {

    @Test
    public final void recipeDataTest() {
        Recipe recipe = new Recipe();
        recipe.setAction("action");
        assertTrue(recipe.getAction().equalsIgnoreCase("action"));
        recipe.setCreated(new Timestamp(System.currentTimeMillis()));
        assertTrue(recipe.getCreated() != null);
        recipe.setDescription("description");
        assertTrue(recipe.getDescription().equalsIgnoreCase("description"));
        recipe.setId(1);
        assertTrue(recipe.getId() == 1);
        recipe.setOrchestrationUri("orchestrationUri");
        assertTrue(recipe.getOrchestrationUri().equalsIgnoreCase("orchestrationUri"));
        recipe.setRecipeTimeout(1);
        assertTrue(recipe.getRecipeTimeout() == 1);
        recipe.setServiceType("serviceType");
        assertTrue(recipe.getServiceType().equalsIgnoreCase("serviceType"));
//		assertTrue(recipe.toString() != null);
    }

}
