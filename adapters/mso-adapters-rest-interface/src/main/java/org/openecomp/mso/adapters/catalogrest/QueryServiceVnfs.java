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
/* should be called QueryVnfResource.java */

import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.logger.MsoLogger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "serviceVnfs")
@NoJackson
public class QueryServiceVnfs {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private List<VnfResource> serviceVnfs;

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

	public String toJsonString() {
		String jsonString = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonString = mapper.writeValueAsString(this);
			LOGGER.debug ("QuerySrviceVnfs jsonString: "+jsonString);
		}
		catch (Exception e) {
			LOGGER.debug ("QuerySrviceVnfs jsonString exception:"+e.getMessage());
		}
		return jsonString;
	}
}