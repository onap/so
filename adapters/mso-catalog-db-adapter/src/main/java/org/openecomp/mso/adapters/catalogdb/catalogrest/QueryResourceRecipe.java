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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

    @Override
    public String toString() {

        return resourceRecipe.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {    	
    	
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("id",  null == resourceRecipe ? null :String.valueOf(resourceRecipe.getId()));
        valueMap.put("action",  null == resourceRecipe ? null :resourceRecipe.getAction());
        valueMap.put("orchestrationUri", null == resourceRecipe ? null : resourceRecipe.getOrchestrationUri());
        valueMap.put("recipeTimeout", null == resourceRecipe ? null : String.valueOf(resourceRecipe.getRecipeTimeout()));
        valueMap.put("paramXSD", null == resourceRecipe ? null : resourceRecipe.getParamXSD());
        valueMap.put("description", null == resourceRecipe ? null : resourceRecipe.getDescription());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        String jsonStr = "";
        try {
            jsonStr = mapper.writeValueAsString(valueMap);
        } catch(JsonProcessingException e) {

            e.printStackTrace();
        }
        return jsonStr;
    }

}
