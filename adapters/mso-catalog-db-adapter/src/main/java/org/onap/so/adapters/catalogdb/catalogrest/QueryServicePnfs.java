/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 *  Modifications Copyright (C) 2019 IBM.
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
/* should be called QueryVnfResource.java */

import org.apache.commons.lang3.StringEscapeUtils;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "servicePnfs")
public class QueryServicePnfs extends CatalogQuery {
    protected static Logger logger = LoggerFactory.getLogger(QueryServiceVnfs.class);

    private List<PnfResourceCustomization> pnfResourceCustomizations;
    private static final String TEMPLATE = "\n" + "\t{ \"modelInfo\"                    : {\n"
            + "\t\t\"modelName\"              : <MODEL_NAME>,\n" + "\t\t\"modelUuid\"              : <MODEL_UUID>,\n"
            + "\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"
            + "\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"
            + "\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>,\n"
            + "\t\t\"modelInstanceName\"      : <MODEL_INSTANCE_NAME>\n" + "\t\t},\n"
            + "\t\"toscaNodeType\"            : <TOSCA_NODE_TYPE>,\n"
            + "\t\"nfFunction\"           	: <NF_FUNCTION>,\n" + "\t\"nfType\"              		: <NF_TYPE>,\n"
            + "\t\"nfRole\"              		: <NF_ROLE>,\n" + "\t\"nfNamingCode\"         	: <NF_NAMING_CODE>,\n"
            + "\t\"multiStageDesign\"         : <MULTI_STEP_DESIGN>,\n"
            + "\t\"resourceInput\"            : <RESOURCE_INPUT>,\n" + "\t}";

    public QueryServicePnfs() {
        super();
        pnfResourceCustomizations = new ArrayList<>();
    }

    public QueryServicePnfs(List<PnfResourceCustomization> vlist) {
        pnfResourceCustomizations = new ArrayList<>();
        for (PnfResourceCustomization o : vlist) {
            if (logger.isDebugEnabled())
                logger.debug(o.toString());
            pnfResourceCustomizations.add(o);
        }
    }

    public List<PnfResourceCustomization> getPnfResourceCustomizations() {
        return this.pnfResourceCustomizations;
    }

    public void setPnfResourceCustomizations(List<PnfResourceCustomization> v) {
        this.pnfResourceCustomizations = v;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        int i = 1;
        for (PnfResourceCustomization o : pnfResourceCustomizations) {
            sb.append(i).append("\t");
            if (!first)
                sb.append("\n");
            first = false;
            sb.append(o);
        }
        return sb.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        StringBuilder sb = new StringBuilder();
        if (!isEmbed && isArray)
            sb.append("{ ");
        if (isArray)
            sb.append("\"servicePnfs\": [");
        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        for (PnfResourceCustomization o : pnfResourceCustomizations) {
            if (first)
                sb.append("\n");
            first = false;

            boolean vrNull = o.getPnfResources() == null;

            put(valueMap, "MODEL_NAME", vrNull ? null : o.getPnfResources().getModelName());
            put(valueMap, "MODEL_UUID", vrNull ? null : o.getPnfResources().getModelUUID());
            put(valueMap, "MODEL_INVARIANT_ID", vrNull ? null : o.getPnfResources().getModelInvariantId());
            put(valueMap, "MODEL_VERSION", vrNull ? null : o.getPnfResources().getModelVersion());
            put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUUID());
            put(valueMap, "MODEL_INSTANCE_NAME", o.getModelInstanceName());
            put(valueMap, "TOSCA_NODE_TYPE", vrNull ? null : o.getPnfResources().getToscaNodeType());
            put(valueMap, "NF_FUNCTION", o.getNfFunction());
            put(valueMap, "NF_TYPE", o.getNfType());
            put(valueMap, "NF_ROLE", o.getNfRole());
            put(valueMap, "NF_NAMING_CODE", o.getNfNamingCode());
            put(valueMap, "MULTI_STEP_DESIGN", o.getMultiStageDesign());
            if (isJSONValid(StringEscapeUtils.unescapeJava(o.getResourceInput()))) {
                put(valueMap, "RESOURCE_INPUT", o.getResourceInput());
            }

            sb.append(sep).append(this.setTemplate(TEMPLATE, valueMap));
            sep = ",\n";
        }
        if (!first)
            sb.append("\n");
        if (isArray)
            sb.append("]");
        if (!isEmbed && isArray)
            sb.append("}");
        return sb.toString();
    }

}
