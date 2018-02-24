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
/* should be called QueryVnfResource.java */

import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "serviceVnfs")
@NoJackson
public class QueryServiceVnfs extends CatalogQuery {
	private List<VnfResourceCustomization> serviceVnfs;
	private final String template =
        "\n"+
//        "\t{ \"vnfResource\"                    : {\n"+
        "\t{ \"modelInfo\"                    : {\n"+
			"\t\t\"modelName\"              : <MODEL_NAME>,\n"+
			"\t\t\"modelUuid\"              : <MODEL_UUID>,\n"+
			"\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"+
			"\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"+
			"\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>,\n"+
			"\t\t\"modelInstanceName\"      : <MODEL_INSTANCE_NAME>\n"+
        "\t\t},\n"+
			"\t\"toscaNodeType\"            : <TOSCA_NODE_TYPE>,\n"+
			"\t\"nfFunction\"           	: <NF_FUNCTION>,\n"+
			"\t\"nfType\"              		: <NF_TYPE>,\n"+
			"\t\"nfRole\"              		: <NF_ROLE>,\n"+
			"\t\"nfNamingCode\"         	: <NF_NAMING_CODE>,\n"+
//        "\t}\n"+
			"<_VFMODULES_>\n" + 
			"\t}";
//			"\t}}";

	public QueryServiceVnfs() { super(); serviceVnfs = new ArrayList<>(); }
	public QueryServiceVnfs(List<VnfResourceCustomization> vlist) { 
		LOGGER.debug ("QueryServiceVnfs:");
		serviceVnfs = new ArrayList<>();
		for (VnfResourceCustomization o : vlist) {
			LOGGER.debug ("-- o is a  serviceVnfs ----");
			LOGGER.debug (o.toString());
			serviceVnfs.add(o);
			LOGGER.debug ("-------------------");
		}
	}

	public List<VnfResourceCustomization> getServiceVnfs(){ return this.serviceVnfs; }
	public void setServiceVnfs(List<VnfResourceCustomization> v) { this.serviceVnfs = v; }

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		int i = 1;
		for (VnfResourceCustomization o : serviceVnfs) {
			sb.append(i).append("\t");
			if (!first) sb.append("\n"); first = false;
			sb.append(o);
		}
		return sb.toString();
    }

	@Override
	public String JSON2(boolean isArray, boolean isEmbed) {
		StringBuilder sb = new StringBuilder();
		if (!isEmbed && isArray) sb.append("{ ");
		if (isArray) sb.append("\"serviceVnfs\": [");
		Map<String, String> valueMap = new HashMap<>();
		String sep = "";
		boolean first = true;

		for (VnfResourceCustomization o : serviceVnfs) {
			if (first) sb.append("\n"); first = false;

			boolean vrNull = o.getVnfResource() == null ? true : false;

		    put(valueMap, "MODEL_NAME",               vrNull ? null : o.getVnfResource().getModelName());
		    put(valueMap, "MODEL_UUID",               vrNull ? null : o.getVnfResource().getModelUuid());
		    put(valueMap, "MODEL_INVARIANT_ID",       vrNull ? null : o.getVnfResource().getModelInvariantId());
		    put(valueMap, "MODEL_VERSION",            vrNull ? null : o.getVnfResource().getVersion());
		    put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUuid());
		    put(valueMap, "MODEL_INSTANCE_NAME",      o.getModelInstanceName());
		    put(valueMap, "TOSCA_NODE_TYPE",     vrNull ? null : o.getVnfResource().getToscaNodeType());
		    put(valueMap, "NF_FUNCTION", o.getNfFunction());
		    put(valueMap, "NF_TYPE", o.getNfType());
		    put(valueMap, "NF_ROLE", o.getNfRole());
		    put(valueMap, "NF_NAMING_CODE", o.getNfNamingCode());

		    String subitem = new QueryVfModule(vrNull ? null : o.getVfModuleCustomizations()).JSON2(true, true); 
		    valueMap.put("_VFMODULES_",               subitem.replaceAll("(?m)^", "\t\t"));

            sb.append(sep).append(this.setTemplate(template, valueMap));
            sep = ",\n";
		}
		if (!first) sb.append("\n");
		if (isArray) sb.append("]");
		if (!isEmbed && isArray) sb.append("}");
		return sb.toString();
	}
}
