/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
 *
 * Copyright (C) 2019 IBM.
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

import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "groups")
public class QueryGroups extends CatalogQuery {

    private List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizations;
    private static final String TEMPLATE = "\n" + "\t{ \"modelInfo\"                    : {\n"
            + "\t\t\"modelName\"              : <MODEL_NAME>,\n" + "\t\t\"modelUuid\"              : <MODEL_UUID>,\n"
            + "\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"
            + "\t\t\"modelVersion\"           : <MODEL_VERSION>\n" + "\t\t},\n" + "<_VNFCS_>\n" + "\t}";

    public QueryGroups() {
        super();
        vnfcInstanceGroupCustomizations = new ArrayList<>();

    }

    public QueryGroups(List<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizations) {
        this.vnfcInstanceGroupCustomizations = new ArrayList<>();
        if (vnfcInstanceGroupCustomizations != null) {
            for (VnfcInstanceGroupCustomization g : vnfcInstanceGroupCustomizations) {
                if (logger.isDebugEnabled()) {
                    logger.debug(g.toString());
                }
                this.vnfcInstanceGroupCustomizations.add(g);
            }
        }
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        StringBuilder sb = new StringBuilder();
        if (!isEmbed && isArray)
            sb.append("{ ");
        if (isArray)
            sb.append("\"groups\": [");
        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        for (VnfcInstanceGroupCustomization o : vnfcInstanceGroupCustomizations) {
            if (first)
                sb.append("\n");
            first = false;

            boolean vnfcCustomizationNull = o.getVnfcCustomizations() == null;
            InstanceGroup instanceGroup = o.getInstanceGroup();

            if (instanceGroup != null) {
                put(valueMap, "MODEL_NAME", instanceGroup.getModelName());
                put(valueMap, "MODEL_UUID", instanceGroup.getModelUUID());
                put(valueMap, "MODEL_INVARIANT_ID", instanceGroup.getModelInvariantUUID());
                put(valueMap, "MODEL_VERSION", instanceGroup.getModelVersion());
            }

            String subItem = new QueryVnfcs(vnfcCustomizationNull ? null : o.getVnfcCustomizations()).JSON2(true, true);
            valueMap.put("_VNFCS_", subItem.replaceAll("(?m)^", "\t\t"));
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
