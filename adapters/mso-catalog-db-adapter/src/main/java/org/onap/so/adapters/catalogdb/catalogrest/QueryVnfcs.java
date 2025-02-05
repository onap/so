/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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
import org.onap.so.db.catalog.beans.VnfcCustomization;

@XmlRootElement(name = "vnfcs")
public class QueryVnfcs extends CatalogQuery {
    private List<VnfcCustomization> vnfcCustomizations;
    private static final String TEMPLATE =
            "\t{\n" + "\t\t\"modelInfo\"               : { \n" + "\t\t\t\"modelName\"              : <MODEL_NAME>,\n"
                    + "\t\t\t\"modelUuid\"              : <MODEL_UUID>,\n"
                    + "\t\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"
                    + "\t\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"
                    + "\t\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>\n" + "\t\t},\n"
                    + "\t\t\"resourceInput\"            : <RESOURCE_INPUT>\n" + "\t}";

    public QueryVnfcs() {
        super();
        vnfcCustomizations = new ArrayList();
    }

    public QueryVnfcs(List<VnfcCustomization> vnfcCustomizations) {
        this.vnfcCustomizations = new ArrayList();
        if (vnfcCustomizations != null) {
            for (VnfcCustomization vnfcCustomization : vnfcCustomizations) {
                if (logger.isDebugEnabled()) {
                    logger.debug(vnfcCustomization.toString());
                }
                this.vnfcCustomizations.add(vnfcCustomization);
            }
        }
    }

    public List<VnfcCustomization> getVnfcCustomizations() {
        return vnfcCustomizations;
    }

    public void setVnfcCustomizations(List<VnfcCustomization> vnfcCustomizations) {
        this.vnfcCustomizations = vnfcCustomizations;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        int i = 1;
        for (VnfcCustomization o : vnfcCustomizations) {
            sb.append(i).append("\t");
            if (!first) {
                sb.append("\n");
            }
            first = false;
            sb.append(o);
        }
        return sb.toString();
    }

    @Override
    public String JSON2(boolean isArray, boolean isEmbed) {
        StringBuilder sb = new StringBuilder();
        if (!isEmbed && isArray) {
            sb.append("{");
        }

        if (isArray) {
            sb.append("\"vnfcs\": [");
        }

        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        for (VnfcCustomization o : vnfcCustomizations) {
            if (first)
                sb.append("\n");
            first = false;

            put(valueMap, "MODEL_NAME", o.getModelName());
            put(valueMap, "MODEL_UUID", o.getModelUUID());
            put(valueMap, "MODEL_INVARIANT_ID", o.getModelInvariantUUID());
            put(valueMap, "MODEL_VERSION", o.getModelVersion());
            put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUUID());

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
