/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "serviceNetworks")
public class QueryServiceNetworks extends CatalogQuery {
    protected static Logger logger = LoggerFactory.getLogger(QueryServiceNetworks.class);
    private List<NetworkResourceCustomization> serviceNetworks;
    private static final String TEMPLATE =
            "\t{\n" + "\t\t\"modelInfo\"                : {\n" + "\t\t\t\"modelName\"              : <MODEL_NAME>,\n"
                    + "\t\t\t\"modelUuid\"              : <MODEL_UUID>,\n"
                    + "\t\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"
                    + "\t\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"
                    + "\t\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>,\n"
                    + "\t\t\t\"modelInstanceName\"      : <MODEL_INSTANCE_NAME>\n" + "\t},\n"
                    + "\t\t\"toscaNodeType\"            : <TOSCA_NODE_TYPE>,\n"
                    + "\t\t\"networkType\"              : <NETWORK_TYPE>,\n"
                    + "\t\t\"networkTechnology\"        : <NETWORK_TECHNOLOGY>,\n"
                    + "\t\t\"resourceInput\"            : <RESOURCE_INPUT>,\n"
                    + "\t\t\"networkRole\"              : <NETWORK_ROLE>,\n"
                    + "\t\t\"networkScope\"             : <NETWORK_SCOPE>\n" + "\t}";

    public QueryServiceNetworks() {
        super();
        serviceNetworks = new ArrayList<>();
    }

    public QueryServiceNetworks(List<NetworkResourceCustomization> vlist) {
        logger.debug("QueryServiceNetworks:");
        serviceNetworks = new ArrayList<>();
        for (NetworkResourceCustomization o : vlist) {
            if (logger.isDebugEnabled())
                logger.debug(o.toString());
            serviceNetworks.add(o);
        }
    }

    public List<NetworkResourceCustomization> getServiceNetworks() {
        return this.serviceNetworks;
    }

    public void setServiceNetworks(List<NetworkResourceCustomization> v) {
        this.serviceNetworks = v;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        int i = 1;
        for (NetworkResourceCustomization o : serviceNetworks) {
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
            sb.append("\"serviceNetworks\": [");

        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        for (NetworkResourceCustomization o : serviceNetworks) {
            if (first)
                sb.append("\n");
            first = false;
            boolean nrNull = o.getNetworkResource() == null;
            put(valueMap, "MODEL_NAME", nrNull ? null : o.getNetworkResource().getModelName());
            put(valueMap, "MODEL_UUID", nrNull ? null : o.getNetworkResource().getModelUUID());
            put(valueMap, "MODEL_INVARIANT_ID", nrNull ? null : o.getNetworkResource().getModelInvariantUUID());
            put(valueMap, "MODEL_VERSION", nrNull ? null : o.getNetworkResource().getModelVersion());
            put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUUID());
            put(valueMap, "MODEL_INSTANCE_NAME", o.getModelInstanceName());
            put(valueMap, "TOSCA_NODE_TYPE", nrNull ? null : o.getNetworkResource().getToscaNodeType());
            put(valueMap, "NETWORK_TYPE", o.getNetworkType());
            put(valueMap, "NETWORK_ROLE", o.getNetworkRole());
            put(valueMap, "NETWORK_SCOPE", o.getNetworkScope());
            put(valueMap, "NETWORK_TECHNOLOGY", o.getNetworkTechnology());

            if (isJSONValid(o.getResourceInput())) {
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
