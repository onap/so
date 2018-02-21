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
package org.openecomp.mso.adapters.catalogdb.catalogrest;

import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "vfModules")
@NoJackson
public class QueryVfModule extends CatalogQuery {
	private List<VfModuleCustomization> vfModules;
	private final String template =
		"\t{\n"+
//		"\t{ \"vfModule\"               : { \n"+
		"\t\t\"modelInfo\"               : { \n"+
			"\t\t\t\"modelName\"              : <MODEL_NAME>,\n"+
			"\t\t\t\"modelUuid\"              : <MODEL_UUID>,\n"+
			"\t\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"+
			"\t\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"+
			"\t\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>\n"+
			"\t\t},"+
			"\t\t\"isBase\"                 : <IS_BASE>,\n"+
			"\t\t\"vfModuleLabel\"          : <VF_MODULE_LABEL>,\n"+
			"\t\t\"initialCount\"           : <INITIAL_COUNT>,\n"+
			"\t\t\"hasVolumeGroup\"           : <HAS_VOLUME_GROUP>\n"+
		"\t}";
//		"\t}}";

	public QueryVfModule() { super(); vfModules = new ArrayList<>(); }
	public QueryVfModule(List<VfModuleCustomization> vlist) { 
		LOGGER.debug ("QueryVfModule:");
		vfModules = new ArrayList<>();
		if (vlist != null) {
			for (VfModuleCustomization o : vlist) {
			LOGGER.debug ("-- o is a  vfModules ----");
			LOGGER.debug (o.toString());
			vfModules.add(o);
			LOGGER.debug ("-------------------");
		}
	}
	}

	public List<VfModuleCustomization> getVfModule(){ return this.vfModules; }
	public void setVfModule(List<VfModuleCustomization> v) { this.vfModules = v; }

	@Override
	public String toString () {
		StringBuilder buf = new StringBuilder();

		boolean first = true;
		int i = 1;
		for (VfModuleCustomization o : vfModules) {
			buf.append(i+"\t");
			if (!first) buf.append("\n"); first = false;
			buf.append(o);
		}
		return buf.toString();
    }

	@Override
	public String JSON2(boolean isArray, boolean x) {
		StringBuilder buf = new StringBuilder();
		if (isArray) buf.append("\"vfModules\": [");
		Map<String, String> valueMap = new HashMap<>();
		String sep = "";
		boolean first = true;

		for (VfModuleCustomization o : vfModules) {
			if (first) buf.append("\n"); first = false;

			boolean vfNull = o.getVfModule() == null ? true : false;
			boolean hasVolumeGroup = false;
			String envt = o.getHeatEnvironmentArtifactUuid();
			if (envt != null && !"".equals(envt)) {
				hasVolumeGroup = true;
			}

		    put(valueMap, "MODEL_NAME",               vfNull ? null : o.getVfModule().getModelName());
		    put(valueMap, "MODEL_UUID",               vfNull ? null : o.getVfModule().getModelUUID());
		    put(valueMap, "MODEL_INVARIANT_ID",       vfNull ? null : o.getVfModule().getModelInvariantUuid());
		    put(valueMap, "MODEL_VERSION",            vfNull ? null : o.getVfModule().getVersion());
		    put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUuid());
		    put(valueMap, "IS_BASE",                  vfNull ? false : Boolean
				.valueOf(o.getVfModule().isBase() ? true : false));
		    put(valueMap, "VF_MODULE_LABEL",          o.getLabel());
		    put(valueMap, "INITIAL_COUNT",            o.getInitialCount());
		    put(valueMap, "HAS_VOLUME_GROUP", Boolean.valueOf(hasVolumeGroup));

            buf.append(sep+ this.setTemplate(template, valueMap));
            sep = ",\n";
		}
		if (!first) buf.append("\n");
		if (isArray) buf.append("]");
		return buf.toString();
	}
}
