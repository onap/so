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
/* should be called QueryVnfResource.java */

import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "serviceVnfs")
@NoJackson
public class QueryServiceVnfs extends CatalogQuery {
	private List<VnfResource> serviceVnfs;
	private final String template =
        "\t{ \"vnf\"                    : {\n"+
			"\t\t\"modelName\"              : <MODEL_NAME>,\n"+
			"\t\t\"modelUuid\"              : <MODEL_UUID>,\n"+
			"\t\t\"modelInvariantUuid\"     : <MODEL_INVARIANT_ID>,\n"+
			"\t\t\"modelVersion\"           : <MODEL_VERSION>,\n"+
			"\t\t\"modelCustomizationUuid\" : <MODEL_CUSTOMIZATION_UUID>,\n"+
			"\t\t\"modelInstanceName\"      : <MODEL_INSTANCE_NAME>,\n"+
			"<_VFMODULES_>\n"+
			"\t}}";

	public QueryServiceVnfs() { super(); serviceVnfs = new ArrayList<VnfResource>(); }
	public QueryServiceVnfs(List<VnfResource> vlist) {
		LOGGER.debug ("QueryServiceVnfs:");
		serviceVnfs = new ArrayList<VnfResource>();
		for (VnfResource o : vlist) {
			LOGGER.debug ("-- o is a  serviceVnfs ----");
			LOGGER.debug (o.toString());
			serviceVnfs.add(o);
			LOGGER.debug ("-------------------");
		}
	}

	public List<VnfResource> getServiceVnfs(){ return this.serviceVnfs; }
	public void setServiceVnfs(List<VnfResource> v) { this.serviceVnfs = v; }

	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer();

		boolean first = true;
		int i = 1;
		for (VnfResource o : serviceVnfs) {
			buf.append(i+"\t");
			if (!first) buf.append("\n"); first = false;
			buf.append(o);
		}
		return buf.toString();
    }

	@Override
	public String JSON2(boolean isArray, boolean isEmbed) {
		StringBuffer buf = new StringBuffer();
		if (!isEmbed && isArray) buf.append("{ ");
		if (isArray) buf.append("\"serviceVnfs\": [");
		Map<String, String> valueMap = new HashMap<String, String>();
		String sep = "";
		boolean first = true;

		for (VnfResource o : serviceVnfs) {
			if (first) buf.append("\n"); first = false;

		    put(valueMap, "MODEL_NAME",               o.getModelName());
		    put(valueMap, "MODEL_UUID",               o.getModelUuid());
		    put(valueMap, "MODEL_INVARIANT_ID",       o.getModelInvariantId());
		    put(valueMap, "MODEL_VERSION",            o.getModelVersion());
		    put(valueMap, "MODEL_CUSTOMIZATION_UUID", o.getModelCustomizationUuid());
		    put(valueMap, "MODEL_INSTANCE_NAME",      o.getModelInstanceName());

		    String subitem = new QueryVfModule(o.getVfModules()).JSON2(true, true);
		    valueMap.put("_VFMODULES_",               subitem.replaceAll("(?m)^", "\t\t"));

            buf.append(sep+ this.setTemplate(template, valueMap));
            sep = ",\n";
		}
		if (!first) buf.append("\n");
		if (isArray) buf.append("]");
		if (!isEmbed && isArray) buf.append("}");
		return buf.toString();
	}
}