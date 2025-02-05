/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019, CMCC Technologies Co., Ltd.
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

import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "serviceProxyCustomizations")
public class QueryServiceProxyCustomization extends CatalogQuery {

    protected static Logger logger = LoggerFactory.getLogger(QueryServiceProxyCustomization.class);

    private List<ServiceProxyResourceCustomization> serviceProxyResourceCustomizationList;

    private static final String TEMPLATE =
            "\t{\n" + "\t\t\"modelInfo\"                : {\n" + "\t\t\t\"modelName\"              : <MODEL_NAME>,\n"
                    + "\t\t\t\"modelUuid\"              : <MODEL_UUID>,\n"
                    + "\t\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_UUID>,\n"
                    + "\t\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"
                    + "\t\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>,\n"
                    + "\t\t\t\"modelInstanceName\"      : <MODEL_INSTANCE_NAME>\n" + "\t},\n"
                    + "\t\t\"toscaNodeType\"            : <TOSCA_NODE_TYPE>,\n"
                    + "\t\t\"description\"            : <DESCRIPTION>,\n"
                    + "\t\t\"sourceModelUuid\"            : <SOURCE_SERVICE_MODEL_UUID>\n" + "\t}";

    public QueryServiceProxyCustomization() {
        super();
        this.serviceProxyResourceCustomizationList = new ArrayList<>();
    }

    public QueryServiceProxyCustomization(
            List<ServiceProxyResourceCustomization> serviceProxyResourceCustomizationList) {
        this.serviceProxyResourceCustomizationList = serviceProxyResourceCustomizationList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        int i = 1;
        for (ServiceProxyResourceCustomization o : serviceProxyResourceCustomizationList) {
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
            sb.append("\"serviceProxy\": [");
        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        if (this.serviceProxyResourceCustomizationList != null) {
            for (ServiceProxyResourceCustomization o : serviceProxyResourceCustomizationList) {
                if (first)
                    sb.append("\n");

                first = false;

                boolean arNull = o == null;

                put(valueMap, "MODEL_CUSTOMIZATION_UUID", arNull ? null : o.getModelCustomizationUUID());
                put(valueMap, "MODEL_INSTANCE_NAME", arNull ? null : o.getModelInstanceName());
                put(valueMap, "MODEL_UUID", arNull ? null : o.getModelUUID());
                put(valueMap, "MODEL_INVARIANT_UUID", arNull ? null : o.getModelInvariantUUID());
                put(valueMap, "MODEL_VERSION", arNull ? null : o.getModelVersion());
                put(valueMap, "MODEL_NAME", arNull ? null : o.getModelName());
                put(valueMap, "TOSCA_NODE_TYPE", arNull ? null : o.getToscaNodeType());
                put(valueMap, "DESCRIPTION", arNull ? null : o.getDescription());
                put(valueMap, "SOURCE_SERVICE_MODEL_UUID", (String) (arNull ? null
                        : o.getSourceService() == null ? null : o.getSourceService().getModelUUID()));

                sb.append(sep).append(this.setTemplate(TEMPLATE, valueMap));
                sep = ",\n";
            }
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
