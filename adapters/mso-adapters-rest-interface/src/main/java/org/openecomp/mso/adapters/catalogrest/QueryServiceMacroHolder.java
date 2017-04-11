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

import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;
import org.openecomp.mso.logger.MsoLogger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.annotations.providers.NoJackson;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "serviceResources")
@NoJackson
public class QueryServiceMacroHolder {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private ServiceMacroHolder serviceMacroHolder;

	public QueryServiceMacroHolder() { super(); serviceMacroHolder = new ServiceMacroHolder(); }
	public QueryServiceMacroHolder(ServiceMacroHolder vlist) { serviceMacroHolder = vlist; }

	public ServiceMacroHolder getServiceResources(){ return this.serviceMacroHolder; }
	public void setServiceResources(ServiceMacroHolder v) { this.serviceMacroHolder = v; }

	@Override
	public String toString () { return serviceMacroHolder.toString(); }

	public String toJsonString() {
		String jsonString = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonString = mapper.writeValueAsString(this);
			LOGGER.debug ("QueryServiceMacroHolder jsonString: "+jsonString);
		}
		catch (Exception e) {
			LOGGER.debug ("QueryServiceMacroHolder jsonString exception:"+e.getMessage());
		}
		return jsonString;
	}
}