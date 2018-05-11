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

package org.openecomp.mso.adapters.catalogdb.catalogrest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.Recipe;
import org.openecomp.mso.jsonpath.JsonPathUtil;

public class QueryResourceRecipeTest {

    private static final int RECIPE_ID = 123;
    private static final String RECIPE_ACTION = "actionTest";
    private static final String RECIPE_URI = "uriTest";
    private static final int RECIPE_TIMEOUT = 100;
    private static final String RECIPE_PARAMS_XSD = "paramsXsdTest";
    private static final String RECIPE_DESCRIPTION = "descrTest";

    private QueryResourceRecipe testedObject;

    @Before
    public void init() {
        testedObject = new QueryResourceRecipe(createRecipe());
    }

    @Test
    public void convertingToJsonSuccessful() {
        String jsonResult = testedObject.JSON2(true, true);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.id")).contains(String.valueOf(RECIPE_ID));
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.action")).contains(RECIPE_ACTION);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.orchestrationUri")).contains(RECIPE_URI);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.recipeTimeout"))
                .contains(String.valueOf(RECIPE_TIMEOUT));
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.paramXSD")).contains(RECIPE_PARAMS_XSD);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.description")).contains(RECIPE_DESCRIPTION);
    }

    @Test
    public void toString_properContent() {
        assertThat(testedObject.toString()).contains("RECIPE: actionTest,uri=uriTest");
    }

    private Recipe createRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(RECIPE_ID);
        recipe.setAction(RECIPE_ACTION);
        recipe.setOrchestrationUri(RECIPE_URI);
        recipe.setRecipeTimeout(RECIPE_TIMEOUT);
        recipe.setParamXSD(RECIPE_PARAMS_XSD);
        recipe.setDescription(RECIPE_DESCRIPTION);
        return recipe;
    }

}
