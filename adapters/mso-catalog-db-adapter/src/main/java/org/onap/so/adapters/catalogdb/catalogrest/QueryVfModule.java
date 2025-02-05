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

package org.onap.so.adapters.catalogdb.catalogrest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.VfModuleCustomization;

@XmlRootElement(name = "vfModules")
public class QueryVfModule extends CatalogQuery {
    private List<VfModuleCustomization> vfModules;
    private static final String TEMPLATE = "\t{\n" + "\t\t\"modelInfo\"               : { \n"
            + "\t\t\t\"modelName\"              : <MODEL_NAME>,\n"
            + "\t\t\t\"modelUuid\"              : <MODEL_UUID>,\n"
            + "\t\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"
            + "\t\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"
            + "\t\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>\n" + "\t\t},"
            + "\t\t\"isBase\"                 : <IS_BASE>,\n" + "\t\t\"vfModuleLabel\"          : <VF_MODULE_LABEL>,\n"
            + "\t\t\"initialCount\"           : <INITIAL_COUNT>,\n"
            + "\t\t\"hasVolumeGroup\"           : <HAS_VOLUME_GROUP>\n" + "\t}";

    public QueryVfModule() {
        super();
        vfModules = new ArrayList<>();
    }

    public QueryVfModule(List<VfModuleCustomization> vlist) {
        vfModules = new ArrayList<>();
        if (vlist != null) {
            for (VfModuleCustomization o : vlist) {
                if (logger.isDebugEnabled())
                    logger.debug(o.toString());
                vfModules.add(o);
            }
        }
    }

    public List<VfModuleCustomization> getVfModule() {
        return this.vfModules;
    }

    public void setVfModule(List<VfModuleCustomization> v) {
        this.vfModules = v;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        int i = 1;
        for (VfModuleCustomization o : vfModules) {
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
            sb.append("\"vfModules\": [");
        Map<String, String> valueMap = new HashMap<>();
        String sep = "";
        boolean first = true;

        for (VfModuleCustomization o : vfModules) {
            if (first)
                sb.append("\n");
            first = false;

            boolean vfNull = o.getVfModule() == null;
            boolean hasVolumeGroup = false;
            HeatEnvironment envt = o.getVolumeHeatEnv();
            if (envt != null) {
                hasVolumeGroup = true;
            }

            put(valueMap, "MODEL_NAME", vfNull ? null : o.getVfModule().getModelName());
            put(valueMap, "MODEL_UUID", vfNull ? null : o.getVfModule().getModelUUID());
            put(valueMap, "MODEL_INVARIANT_ID", vfNull ? null : o.getVfModule().getModelInvariantUUID());
            put(valueMap, "MODEL_VERSION", vfNull ? null : o.getVfModule().getModelVersion());
            put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUUID());
            put(valueMap, "IS_BASE", !vfNull && (o.getVfModule().getIsBase()));
            put(valueMap, "VF_MODULE_LABEL", o.getLabel());
            put(valueMap, "INITIAL_COUNT", o.getInitialCount());
            put(valueMap, "HAS_VOLUME_GROUP", hasVolumeGroup);

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
