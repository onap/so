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

import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "serviceAllottedResources")
public class QueryAllottedResourceCustomization extends CatalogQuery {
	private List<AllottedResourceCustomization> allottedResourceCustomization;
	private final String template =
		"\t{\n"+
//		"\t{ \"allottedResource\"       : {\n"+
		"\t\t\"modelInfo\"       : {\n"+
			"\t\t\t\"modelName\"              : <MODEL_NAME>,\n"+
			"\t\t\t\"modelUuid\"              : <MODEL_UUID>,\n"+
			"\t\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"+
			"\t\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"+
			"\t\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>,\n"+
			"\t\t\t\"modelInstanceName\"      : <MODEL_INSTANCE_NAME>\n"+
	        "\t\t},\n"+
	        "\t\t\"toscaNodeType\"                      : <TOSCA_NODE_TYPE>,\n"+ 
	        "\t\t\"allottedResourceType\"               : <ALLOTTED_RESOURCE_TYPE>,\n"+ 
	        "\t\t\"allottedResourceRole\"               : <ALLOTTED_RESOURCE_ROLE>,\n"+ 
	        "\t\t\"providingServiceModelName\"          : <PROVIDING_SERVICE_MODEL_NAME>,\n"+ 
	        "\t\t\"providingServiceModelInvariantUuid\" : <PROVIDING_SERVICE_MODEL_INVARIANT_UUID>,\n"+ 
	        "\t\t\"providingServiceModelUuid\"          : <PROVIDING_SERVICE_MODEL_UUID>,\n"+ 
	        "\t\t\"nfFunction\"                         : <NF_FUNCTION>,\n"+ 
	        "\t\t\"nfType\"                             : <NF_TYPE>,\n"+ 
	        "\t\t\"nfRole\"                             : <NF_ROLE>,\n"+ 
	        "\t\t\"nfNamingCode\"                       : <NF_NAMING_CODE>\n"+ 
	    "\t}";			
//		"\t}}";

	public QueryAllottedResourceCustomization() { super(); allottedResourceCustomization = new ArrayList<>(); }
	public QueryAllottedResourceCustomization(List<AllottedResourceCustomization> vlist) { allottedResourceCustomization = vlist; }

	public List<AllottedResourceCustomization> getServiceAllottedResources(){ return this.allottedResourceCustomization; }
	public void setServiceAllottedResources(List<AllottedResourceCustomization> v) { this.allottedResourceCustomization = v; }

	@Override
	public String toString () {
		StringBuilder buf = new StringBuilder();

		boolean first = true;
		int i = 1;
		for (AllottedResourceCustomization o : allottedResourceCustomization) {
			buf.append(i+"\t");
			if (!first) buf.append("\n"); first = false;
			buf.append(o);
		}
		return buf.toString();
    }

	@Override
	public String JSON2(boolean isArray, boolean isEmbed) {
		StringBuilder buf = new StringBuilder();
		if (!isEmbed && isArray) buf.append("{ ");
		if (isArray) buf.append("\"serviceAllottedResources\": [");
		Map<String, String> valueMap = new HashMap<>();
		String sep = "";
		boolean first = true;

		if (this.allottedResourceCustomization != null) {
		for (AllottedResourceCustomization o : allottedResourceCustomization) {
			if (first) buf.append("\n"); first = false;

				boolean arNull = o.getAllottedResource() == null ? true : false;
	
			    put(valueMap, "MODEL_NAME",               arNull ? null : o.getAllottedResource().getModelName());
			    put(valueMap, "MODEL_UUID",               arNull ? null : o.getAllottedResource().getModelUuid());
			    put(valueMap, "MODEL_INVARIANT_ID",       arNull ? null : o.getAllottedResource().getModelInvariantUuid());
			    put(valueMap, "MODEL_VERSION",            arNull ? null : o.getAllottedResource().getModelVersion());
		    put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUuid());
		    put(valueMap, "MODEL_INSTANCE_NAME",      o.getModelInstanceName());
			    put(valueMap, "TOSCA_NODE_TYPE",      arNull ? null : o.getAllottedResource().getToscaNodeType());
			    put(valueMap, "ALLOTTED_RESOURCE_TYPE",     arNull ? null : o.getAllottedResource().getSubcategory());
			    put(valueMap, "ALLOTTED_RESOURCE_ROLE",     o.getTargetNetworkRole());
			    put(valueMap, "NF_TYPE",     o.getNfType());
			    put(valueMap, "NF_ROLE",     o.getNfRole());
			    put(valueMap, "NF_FUNCTION",     o.getNfFunction());
			    put(valueMap, "NF_NAMING_CODE",     o.getNfNamingCode());
			    put(valueMap, "PROVIDING_SERVICE_MODEL_INVARIANT_UUID",     o.getProvidingServiceModelInvariantUuid());
			    put(valueMap, "PROVIDING_SERVICE_MODEL_UUID",     o.getProvidingServiceModelUuid());
			    put(valueMap, "PROVIDING_SERVICE_MODEL_NAME",     o.getProvidingServiceModelName());

            buf.append(sep+ this.setTemplate(template, valueMap));
            sep = ",\n";
		}
		}
		if (!first) buf.append("\n");
		if (isArray) buf.append("]");
		if (!isEmbed && isArray) buf.append("}");
		return buf.toString();
	}

}
