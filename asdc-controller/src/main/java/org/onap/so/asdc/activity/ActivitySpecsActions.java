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

package org.onap.so.asdc.activity;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;


import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.onap.so.asdc.activity.beans.ActivitySpec;
import org.onap.so.asdc.activity.beans.ActivitySpecCreateResponse;
import org.onap.so.client.HttpClient;
import org.onap.so.client.HttpClientFactory;
import org.onap.so.utils.TargetEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;

@Component
public class ActivitySpecsActions {
	
	private static final String ACTIVITY_SPEC_URI = "/v1.0/activity-spec";
	private static final String ACTIVITY_SPEC_URI_SUFFIX = "/versions/latest/actions";
	private static final String CERTIFY_ACTIVITY_PAYLOAD = "{\"action\": \"CERTIFY\"}";
	
	private final HttpClientFactory httpClientFactory = new HttpClientFactory();
	protected static final Logger logger = LoggerFactory.getLogger(ActivitySpecsActions.class);
	   
	public String createActivitySpec(String hostname, ActivitySpec activitySpec) {
		if (activitySpec == null) {
			return null;
		}
		
		String activitySpecId = null;
	
		try {		
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			String payload = mapper.writer().writeValueAsString(activitySpec);
			
			String urlString = UriBuilder.fromUri(hostname).path(ACTIVITY_SPEC_URI).build().toString();
			URL url = new URL(urlString);
			
			HttpClient httpClient = httpClientFactory.newJsonClient(url, TargetEntity.SDC);			
			httpClient.addAdditionalHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
					
			Response response = httpClient.post(payload);
			
			int statusCode = response.getStatus();
			if (statusCode != HttpStatus.SC_OK) {
				logger.warn("{} {} {}", "Error creating activity spec", activitySpec.getName(), statusCode);
			}
			else {
				if (response.getEntity() != null) {
					ActivitySpecCreateResponse activitySpecCreateResponse = response.readEntity(ActivitySpecCreateResponse.class);
					if (activitySpecCreateResponse != null) {
						activitySpecId = activitySpecCreateResponse.getId();
					}
					else {
						logger.warn("{} {}", "Unable to read activity spec", activitySpec.getName());
					}
				}
				else {
					logger.warn("{} {}", "No activity spec response returned", activitySpec.getName());
				}
			}			
		}
		catch (Exception e) {
			logger.warn("{} {}", "Exception creating activitySpec", e.getMessage());
		}	            
		
		return activitySpecId;		
	}	
	
	public boolean certifyActivitySpec(String hostname, String activitySpecId) {
		boolean certificationResult = false;
		if (activitySpecId == null) {
			return false;
		}
			
		try {			
			String path = ACTIVITY_SPEC_URI + "/" + activitySpecId + ACTIVITY_SPEC_URI_SUFFIX;
					
			String urlString = UriBuilder.fromUri(hostname).path(path).build().toString();
			URL url = new URL(urlString);
			
			HttpClient httpClient = httpClientFactory.newJsonClient(url, TargetEntity.SDC);			
			httpClient.addAdditionalHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
						
			Response response = httpClient.put(CERTIFY_ACTIVITY_PAYLOAD);
			
			int statusCode = response.getStatus();
		
			if (statusCode != HttpStatus.SC_OK) {
					logger.warn("{} {} {}", "Error certifying activity", activitySpecId, statusCode);
			}
			else {
				certificationResult = true;
			}		
			 
		} catch (Exception e) {				
			logger.warn("{} {}", "Exception certifying activitySpec", e.getMessage());
		}
		
		return certificationResult;			
	}	
}
