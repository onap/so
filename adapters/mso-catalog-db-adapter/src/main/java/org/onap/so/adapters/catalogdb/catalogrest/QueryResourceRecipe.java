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
package org.onap.so.adapters.catalogdb.catalogrest;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.so.db.catalog.beans.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * serivce csar query support <br>
 * <p>
 * </p>
 *
 * @author
 * @version ONAP Beijing Release 2018-02-28
 */
public class QueryResourceRecipe extends CatalogQuery {
    protected static Logger logger = LoggerFactory.getLogger(QueryResourceRecipe.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    }

    private Recipe resourceRecipe;

    public QueryResourceRecipe(Recipe resourceRecipe) {
        this.resourceRecipe = resourceRecipe;
    }

    @Override
    public String toString() {

        return resourceRecipe.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("id", null == resourceRecipe || null == resourceRecipe.getId() ? StringUtils.EMPTY
                : String.valueOf(resourceRecipe.getId()));
        valueMap.put("action", null == resourceRecipe || null == resourceRecipe.getAction() ? StringUtils.EMPTY
                : resourceRecipe.getAction());
        valueMap.put("orchestrationUri",
                null == resourceRecipe || null == resourceRecipe.getOrchestrationUri() ? StringUtils.EMPTY
                        : resourceRecipe.getOrchestrationUri());
        valueMap.put("recipeTimeout",
                null == resourceRecipe || null == resourceRecipe.getRecipeTimeout() ? StringUtils.EMPTY
                        : String.valueOf(resourceRecipe.getRecipeTimeout()));
        valueMap.put("paramXSD", null == resourceRecipe || null == resourceRecipe.getParamXsd() ? StringUtils.EMPTY
                : resourceRecipe.getParamXsd());
        valueMap.put("description",
                null == resourceRecipe || null == resourceRecipe.getDescription() ? StringUtils.EMPTY
                        : resourceRecipe.getDescription());
        String jsonStr = "";
        try {
            jsonStr = mapper.writeValueAsString(valueMap);
        } catch (JsonProcessingException e) {
            logger.error("Error creating JSON", e);
        }
        return jsonStr;
    }

}
