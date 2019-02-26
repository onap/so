/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.core.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper encapsulates needed JSON functionality 
 * to be extended by MSO service decomposition objects
 * providing ways to convert to and from JSON
 *
 */
@JsonInclude(Include.NON_NULL)
public abstract class JsonWrapper implements Serializable  {

	private static final Logger logger = LoggerFactory.getLogger(JsonWrapper.class);
	@JsonInclude(Include.NON_NULL)
	public String toJsonString(){

		String jsonString = "";
		//convert with Jackson
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

		mapper.setSerializationInclusion(Include.NON_NULL);

		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		try {
			jsonString = ow.writeValueAsString(this);
		} catch (Exception e){

			logger.debug("Exception :",e);
		}
		return jsonString;
	}

	@JsonInclude(Include.NON_NULL)
	public JSONObject toJsonObject(){

		ObjectMapper mapper = new ObjectMapper();   
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);    
		JSONObject json = new JSONObject();
		try {
			json = new JSONObject(mapper.writeValueAsString(this));
		} catch (JsonGenerationException e) {
			logger.debug("Exception :",e);
		} catch (JsonMappingException e) {
			logger.debug("Exception :",e);
		} catch (JSONException e) {
			logger.debug("Exception :",e);
		} catch (IOException e) {
			logger.debug("Exception :",e);
		}
		return json; 
	}

	public String listToJson(List list) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(list);
		} catch (JsonGenerationException e) {
			logger.debug("Exception :",e);
		} catch (JsonMappingException e) {
			logger.debug("Exception :",e);
		} catch (IOException e) {
			logger.debug("Exception :",e);
		}
		return jsonString;
	}

	@JsonInclude(Include.NON_NULL)
	public String toJsonStringNoRootName(){

		String jsonString = "";
		//convert with Jackson
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		try {
			jsonString = ow.writeValueAsString(this);
		} catch (Exception e){

			logger.debug("Exception :",e);
		}
		return jsonString;
	}

	/**
	 * Returns a string representation of this object.
	 */
	public String toString() {
		return this.toJsonString();
	}
}
