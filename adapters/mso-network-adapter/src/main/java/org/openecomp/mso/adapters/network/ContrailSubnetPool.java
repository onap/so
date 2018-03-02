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

package org.openecomp.mso.adapters.network;


import org.openecomp.mso.openstack.beans.Pool;
import com.fasterxml.jackson.annotation.JsonProperty;
public class ContrailSubnetPool {
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_allocation_pools_start")
	private String start;
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_allocation_pools_end")
	private String end;

	public ContrailSubnetPool() {
		/* Empty constructor */
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}
	
	public void populateWith(Pool pool)
	{
		if (pool != null)
		{
			start = pool.getStart();
			end = pool.getEnd();
		}
	}

	@Override
	public String toString() {
		return "ContrailSubnetPool [start=" + start + ", end=" + end + "]";
	}
	
}
