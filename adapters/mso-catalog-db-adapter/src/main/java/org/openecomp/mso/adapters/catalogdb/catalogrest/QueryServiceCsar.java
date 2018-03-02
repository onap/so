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

import org.openecomp.mso.db.catalog.beans.ToscaCsar;

/**
 * serivce csar query support 
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP Beijing Release  2018-02-28
 */
public class QueryServiceCsar extends CatalogQuery{
    
    private ToscaCsar toscaCsar;
    
    public QueryServiceCsar(ToscaCsar toscaCsar){
        this.toscaCsar = toscaCsar;
    }
    
    private final String template =
            "\t{\n"+
            "\t\t\"artifactUUID\"         : <ARTIFACT_UUID>,\n"+
            "\t\t\"name\"                 : <NAME>,\n"+
            "\t\t\"version\"              : <VERSION>,\n"+
            "\t\t\"artifactChecksum\"     : <ARTIFACT_CHECK_SUM>,\n"+
            "\t\t\"url\"                  : <URL>,\n"+
            "\t\t\"description\"          : <DESCRIPTION>\n"+
            "\t}";
    
    @Override
    public String toString() {

        return toscaCsar.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        Map<String, String> valueMap = new HashMap<>();
        put(valueMap, "ARTIFACT_UUID", null == toscaCsar ? null : toscaCsar.getArtifactUUID());
        put(valueMap, "NAME", null == toscaCsar ? null : toscaCsar.getName());
        put(valueMap, "VERSION", null == toscaCsar ? null : toscaCsar.getVersion());
        put(valueMap, "ARTIFACT_CHECK_SUM", null == toscaCsar ? null : toscaCsar.getArtifactChecksum());
        put(valueMap, "URL", null == toscaCsar ? null : toscaCsar.getUrl());
        put(valueMap, "DESCRIPTION", null == toscaCsar ? null : toscaCsar.getDescription());
        return this.setTemplate(template, valueMap);
    }

}
