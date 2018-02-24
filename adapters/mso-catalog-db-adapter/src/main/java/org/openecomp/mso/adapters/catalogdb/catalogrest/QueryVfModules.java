/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.annotations.providers.NoJackson;

import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.logger.MsoLogger;

@XmlRootElement(name = "vfModules")
@NoJackson
public class QueryVfModules {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private List<VfModule> vfModules;

	public QueryVfModules() {
		super();
		vfModules = new ArrayList<>();
	}
	public QueryVfModules(List<VfModule> vlist) { 
		LOGGER.debug ("QueryVfModules:");
		vfModules = new ArrayList<>();
		for (VfModule o : vlist) {
			LOGGER.debug ("-- o is a vfModules ----");
			LOGGER.debug (o.toString());
			vfModules.add(o);
			LOGGER.debug ("-------------------");
		}
	}

	public List<VfModule> getVfModules(){ return this.vfModules; }

	public void setVfModules(List<VfModule> v) { this.vfModules = v; }
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		int i = 1;
		for (VfModule o : vfModules) {
			sb.append(i).append("\t");
			if (!first) sb.append("\n");
			first = false;
			sb.append(o);
		}
		return sb.toString();
    }
	
	public String toJsonString() {
		String jsonString = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonString = mapper.writeValueAsString(this);
			LOGGER.debug ("QueryVfModules jsonString: "+jsonString);
		}
		catch (Exception e) {
		    LOGGER.debug ("Exception:", e);
			LOGGER.debug ("QueryVfModules jsonString exception:"+e.getMessage()); 
		}
		return jsonString;
	}
}
