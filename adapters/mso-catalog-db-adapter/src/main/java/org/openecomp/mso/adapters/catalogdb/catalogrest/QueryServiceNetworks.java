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

import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "serviceNetworks")
@NoJackson
public class QueryServiceNetworks extends CatalogQuery {
	private List<NetworkResourceCustomization> serviceNetworks;
	private final String template =
		"\t{\n"+
//		"\t{ \"networkResource\"            : {\n"+
			"\t\t\"modelInfo\"                : {\n"+
			"\t\t\t\"modelName\"              : <MODEL_NAME>,\n"+
			"\t\t\t\"modelUuid\"              : <MODEL_UUID>,\n"+
			"\t\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"+
			"\t\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"+
			"\t\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>,\n"+
			"\t\t\t\"modelInstanceName\"      : <MODEL_INSTANCE_NAME>\n"+
		"\t},\n"+
			"\t\t\"toscaNodeType\"            : <TOSCA_NODE_TYPE>,\n"+
			"\t\t\"networkType\"              : <NETWORK_TYPE>,\n"+
			"\t\t\"networkTechnology\"        : <NETWORK_TECHNOLOGY>,\n"+
			"\t\t\"networkRole\"              : <NETWORK_ROLE>,\n"+
			"\t\t\"networkScope\"             : <NETWORK_SCOPE>\n"+
		"\t}";
//		"\t}}";

	public QueryServiceNetworks() { super(); serviceNetworks = new ArrayList<>(); }
	public QueryServiceNetworks(List<NetworkResourceCustomization> vlist) {
		LOGGER.debug ("QueryServiceNetworks:");
		serviceNetworks = new ArrayList<>();
		for (NetworkResourceCustomization o : vlist) {
			LOGGER.debug (o.toString());
			serviceNetworks.add(o);
			LOGGER.debug ("-------------------");
		}
	}

	public List<NetworkResourceCustomization> getServiceNetworks(){ return this.serviceNetworks; }
	public void setServiceNetworks(List<NetworkResourceCustomization> v) { this.serviceNetworks = v; }

	@Override
	public String toString () {
		StringBuilder buf = new StringBuilder();

		boolean first = true;
		int i = 1;
		for (NetworkResourceCustomization o : serviceNetworks) {
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
		if (isArray) buf.append("\"serviceNetworks\": [");
		//if (isArray) buf.append("[");
		Map<String, String> valueMap = new HashMap<>();
		String sep = "";
		boolean first = true;

		for (NetworkResourceCustomization o : serviceNetworks) {
			if (first) buf.append("\n"); first = false;
			boolean nrNull = o.getNetworkResource() == null ? true : false;
		    put(valueMap, "MODEL_NAME",               nrNull ? null : o.getNetworkResource().getModelName());
		    put(valueMap, "MODEL_UUID",               nrNull ? null : o.getNetworkResource().getModelUUID());
		    put(valueMap, "MODEL_INVARIANT_ID",       nrNull ? null : o.getNetworkResource().getModelInvariantUUID());
		    put(valueMap, "MODEL_VERSION",            nrNull ? null : o.getNetworkResource().getVersion());
		    put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUuid());
		    put(valueMap, "MODEL_INSTANCE_NAME",      o.getModelInstanceName());
		    put(valueMap, "TOSCA_NODE_TYPE",             nrNull ? null : o.getNetworkResource().getToscaNodeType());
		    put(valueMap, "NETWORK_TYPE",             o.getNetworkType());
		    put(valueMap, "NETWORK_ROLE",             o.getNetworkRole());
		    put(valueMap, "NETWORK_SCOPE",             o.getNetworkScope());
		    put(valueMap, "NETWORK_TECHNOLOGY",             o.getNetworkTechnology());

            buf.append(sep+ this.setTemplate(template, valueMap));
            sep = ",\n";
		}
		if (!first) buf.append("\n");
		if (isArray) buf.append("]");
		if (!isEmbed && isArray) buf.append("}");
		return buf.toString();
	}
}
