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
package org.openecomp.mso.adapters.catalogrest;

import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.logger.MsoLogger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "serviceNetworks")
@NoJackson
public class QueryServiceNetworks {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private List<NetworkResourceCustomization> serviceNetworks;

	public QueryServiceNetworks() { super(); serviceNetworks = new ArrayList<NetworkResourceCustomization>(); }
	public QueryServiceNetworks(List<NetworkResourceCustomization> vlist) {
		LOGGER.debug ("QueryServiceNetworks:");
		serviceNetworks = new ArrayList<NetworkResourceCustomization>();
		for (NetworkResourceCustomization o : vlist) {
			LOGGER.debug ("-- o is a  serviceNetworks ----");
			LOGGER.debug (o.toString());
			serviceNetworks.add(o);
			LOGGER.debug ("-------------------");
		}
	}

	public List<NetworkResourceCustomization> getServiceNetworks(){ return this.serviceNetworks; }
	public void setServiceNetworks(List<NetworkResourceCustomization> v) { this.serviceNetworks = v; }

	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer();

		boolean first = true;
		int i = 1;
		for (NetworkResourceCustomization o : serviceNetworks) {
			buf.append(i+"\t");
			if (!first) buf.append("\n"); first = false;
			buf.append(o);
		}
		return buf.toString();
    }

	public String toJsonString() {
		String jsonString = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonString = mapper.writeValueAsString(this);
			LOGGER.debug ("QueryServiceNetworks jsonString: "+jsonString);
		}
		catch (Exception e) {
			LOGGER.debug ("QueryServiceNetworks jsonString exception:"+e.getMessage());
		}
		return jsonString;
	}
}