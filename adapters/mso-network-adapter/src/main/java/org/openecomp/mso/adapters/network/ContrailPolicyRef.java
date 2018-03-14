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


import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContrailPolicyRef {
	private static MsoLogger logger = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	 
	@JsonProperty("network_policy_refs_data_sequence")
	private ContrailPolicyRefSeq seq;
	
	public JsonNode toJsonNode()
	{
		JsonNode node = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper(); 
			node = mapper.convertValue(this, JsonNode.class);
		}
		catch (Exception e)
		{
			logger.error (MessageEnum.RA_MARSHING_ERROR, "Error creating JsonString for Contrail Policy Ref", "", "", MsoLogger.ErrorCode.SchemaError, "Exception creating JsonString for Contrail Policy Ref", e);
		}
		
		return node;
	}
	
	public String toJsonString()
	{
		String jsonString = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper(); 
			jsonString = mapper.writeValueAsString(this);
		}
		catch (Exception e)
		{
			logger.error (MessageEnum.RA_MARSHING_ERROR, "Error creating JsonString for Contrail Policy Ref", "", "", MsoLogger.ErrorCode.SchemaError, "Exception creating JsonString for Contrail Policy Ref", e);
		}
		
		return jsonString;
	}
	
	public void populate(String major, String minor)
	{
		seq = new ContrailPolicyRefSeq(major, minor);
		return;
	}
	
}
