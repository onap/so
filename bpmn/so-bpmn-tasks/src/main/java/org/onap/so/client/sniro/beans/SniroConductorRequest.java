/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sniro.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onap.so.logger.MsoLogger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public class SniroConductorRequest implements Serializable{

	private static final long serialVersionUID = 1906052095861777655L;
	private static final MsoLogger log = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SniroConductorRequest.class);

	@JsonProperty("release-locks")
	private List<Resource> resources = new ArrayList<Resource>();


	public List<Resource> getResources(){
		return resources;
	}

	@JsonInclude(Include.NON_NULL)
	public String toJsonString(){
		String json = "";
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		try{
			json = ow.writeValueAsString(this);
		}catch (Exception e){
			log.error("Unable to convert SniroConductorRequest to string", e);
		}
		return json;
	}



}
