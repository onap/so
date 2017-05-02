/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "vfModules")
@NoJackson
public class QueryVfModule extends CatalogQuery {
	private List<VfModule> vfModules;
	private final String template =
		"\t{ \"vfModule\"               : { \n"+
			"\t\t\"modelName\"              : <MODEL_NAME>,\n"+
			"\t\t\"modelUuid\"              : <MODEL_UUID>,\n"+
			"\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"+
			"\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"+
			"\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>,\n"+
			"\t\t\"vfModuleType\"           : <VF_MODULE_TYPE>,\n"+
			"\t\t\"isBase\"                 : <IS_BASE>,\n"+
			"\t\t\"vfModuleLabel\"          : <VF_MODULE_LABEL>,\n"+
			"\t\t\"initialCount\"           : <INITIAL_COUNT>\n"+
		"\t}}";

	public QueryVfModule() { super(); vfModules = new ArrayList<VfModule>(); }
	public QueryVfModule(List<VfModule> vlist) {
		LOGGER.debug ("QueryVfModule:");
		vfModules = new ArrayList<VfModule>();
		for (VfModule o : vlist) {
			LOGGER.debug ("-- o is a  vfModules ----");
			LOGGER.debug (o.toString());
			vfModules.add(o);
			LOGGER.debug ("-------------------");
		}
	}

	public List<VfModule> getVfModule(){ return this.vfModules; }
	public void setVfModule(List<VfModule> v) { this.vfModules = v; }

	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer();

		boolean first = true;
		int i = 1;
		for (VfModule o : vfModules) {
			buf.append(i+"\t");
			if (!first) buf.append("\n"); first = false;
			buf.append(o);
		}
		return buf.toString();
    }

	@Override
	public String JSON2(boolean isArray, boolean x) {
		StringBuffer buf = new StringBuffer();
		if (isArray) buf.append("\"vfModules\": [");
		Map<String, String> valueMap = new HashMap<String, String>();
		String sep = "";
		boolean first = true;

		for (VfModule o : vfModules) {
			if (first) buf.append("\n"); first = false;

		    put(valueMap, "MODEL_NAME",               o.getModelName());
		    put(valueMap, "MODEL_UUID",               o.getModelUuid());
		    put(valueMap, "MODEL_INVARIANT_ID",       o.getModelInvariantId());
		    put(valueMap, "MODEL_VERSION",            o.getModelVersion());
		    put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUuid());
		    put(valueMap, "VF_MODULE_TYPE",           o.getVfModuleType());
		    put(valueMap, "IS_BASE",                  new Boolean(o.isBase()? true: false));
		    put(valueMap, "VF_MODULE_LABEL",          o.getVfModuleLabel());
		    put(valueMap, "INITIAL_COUNT",            o.getInitialCount());

            buf.append(sep+ this.setTemplate(template, valueMap));
            sep = ",\n";
		}
		if (!first) buf.append("\n");
		if (isArray) buf.append("]");
		return buf.toString();
	}
}