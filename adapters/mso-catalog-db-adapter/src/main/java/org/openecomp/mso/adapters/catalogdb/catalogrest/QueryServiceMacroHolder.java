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

import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "serviceResources")
@NoJackson
public class QueryServiceMacroHolder extends CatalogQuery {
	private ServiceMacroHolder serviceMacroHolder;
	private final String template =
		"{ \"serviceResources\"    : {\n"+
			"\t\"modelInfo\"       : {\n"+
            "\t\t\"modelName\"          : <SERVICE_MODEL_NAME>,\n"+
            "\t\t\"modelUuid\"          : <SERVICE_MODEL_UUID>,\n"+
            "\t\t\"modelInvariantUuid\" : <SERVICE_MODEL_INVARIANT_ID>,\n"+
            "\t\t\"modelVersion\"       : <SERVICE_MODEL_VERSION>\n"+	
            "\t},\n"+
            "\t\"serviceType\" : <SERVICE_TYPE>,\n"+
            "\t\"serviceRole\" : <SERVICE_ROLE>,\n"+
            "<_SERVICEVNFS_>,\n"+
            "<_SERVICENETWORKS_>,\n"+
            "<_SERVICEALLOTTEDRESOURCES_>\n"+
        "\t}}";

	public QueryServiceMacroHolder() { super(); serviceMacroHolder = new ServiceMacroHolder(); }
	public QueryServiceMacroHolder(ServiceMacroHolder vlist) { serviceMacroHolder = vlist; }

	public ServiceMacroHolder getServiceResources(){ return this.serviceMacroHolder; }
	public void setServiceResources(ServiceMacroHolder v) { this.serviceMacroHolder = v; }

	@Override
	public String toString () { return serviceMacroHolder.toString(); }

	@Override
	public String JSON2(boolean isArray, boolean x) {
		Service service = serviceMacroHolder.getService();
		if (service == null) return "\"serviceResources\": null";

		StringBuilder buf = new StringBuilder();
		Map<String, String> valueMap = new HashMap<String, String>();

		put(valueMap, "SERVICE_MODEL_NAME",         service.getModelName()); //getServiceModelName());
		put(valueMap, "SERVICE_MODEL_UUID",         service.getModelUUID()); //getServiceModelUuid());
		put(valueMap, "SERVICE_MODEL_INVARIANT_ID", service.getModelInvariantUUID()); //getServiceModelInvariantId());
		put(valueMap, "SERVICE_MODEL_VERSION",      service.getVersion()); //getServiceModelVersion());
		put(valueMap, "SERVICE_TYPE",      service.getServiceType());
		put(valueMap, "SERVICE_ROLE",      service.getServiceRole());

	    String subitem;
	    subitem = new QueryServiceVnfs(serviceMacroHolder.getVnfResourceCustomizations()).JSON2(true, true); 
	    valueMap.put("_SERVICEVNFS_",               subitem.replaceAll("(?m)^", "\t"));

		subitem = new QueryServiceNetworks(serviceMacroHolder.getNetworkResourceCustomization()).JSON2(true, true);
		valueMap.put("_SERVICENETWORKS_",           subitem.replaceAll("(?m)^", "\t"));

		subitem = new QueryAllottedResourceCustomization(serviceMacroHolder.getAllottedResourceCustomization()).JSON2(true, true);
		valueMap.put("_SERVICEALLOTTEDRESOURCES_",  subitem.replaceAll("(?m)^", "\t"));

        buf.append(this.setTemplate(template, valueMap));
		return buf.toString();
	}

}
