/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import org.openecomp.mso.db.catalog.beans.Recipe;

/**
 * serivce csar query support 
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP Beijing Release  2018-02-28
 */
public class QueryResourceRecipe extends CatalogQuery{
    
    private Recipe resourceRecipe;
    
    public QueryResourceRecipe(Recipe resourceRecipe){
        this.resourceRecipe =resourceRecipe;
    }

    private final String template =
            "\t{\n"+
            "\t\t\"id\"                     : <ID>,\n"+
            "\t\t\"action\"                 : <ACTION>,\n"+
            "\t\t\"orchestrationUri\"       : <ORCHESTRATION_URI>,\n"+
            "\t\t\"recipeTimeout\"          : <RECIPE_TIMEOUT>,\n"+
            "\t\t\"paramXSD\"               : <PARAM_XSD>,\n"+
            "\t\t\"description\"            : <DESCRIPTION>\n"+
            "\t}";
    
    @Override
    public String toString() {

        return resourceRecipe.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        Map<String, String> valueMap = new HashMap<>();
        put(valueMap, "ID", null == resourceRecipe ? null : resourceRecipe.getId());
        put(valueMap, "ACTION", null == resourceRecipe ? null : resourceRecipe.getAction());
        put(valueMap, "ORCHESTRATION_URI", null == resourceRecipe ? null : resourceRecipe.getOrchestrationUri());
        put(valueMap, "RECIPE_TIMEOUT", null == resourceRecipe ? null : resourceRecipe.getRecipeTimeout());
        put(valueMap, "PARAM_XSD", null == resourceRecipe ? null : resourceRecipe.getParamXSD());
        put(valueMap, "DESCRIPTION", null == resourceRecipe ? null : resourceRecipe.getDescription());
        return this.setTemplate(template, valueMap);
    }

}
