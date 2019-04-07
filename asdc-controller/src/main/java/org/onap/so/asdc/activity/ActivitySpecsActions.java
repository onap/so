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

import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.onap.so.asdc.activity.beans.ActivitySpec;
import org.onap.so.asdc.activity.beans.ActivitySpecCreateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ActivitySpecsActions {
	
	private static final String ACTIVITY_SPEC_URI = "/v1.0/activity-spec";
	private static final String ACTIVITY_SPEC_URI_SUFFIX = "/versions/latest/actions";
	private static final String CERTIFY_ACTIVITY_PAYLOAD = "{\"action\": \"CERTIFY\"}";
		
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
			
			HttpClient client = HttpClientBuilder.create().build();
					
			String url = UriBuilder.fromUri(hostname).path(ACTIVITY_SPEC_URI).build().toString();
		
			HttpPost post = new HttpPost(url);
			StringEntity input = new StringEntity(payload, ContentType.APPLICATION_JSON);
			post.setEntity(input);
						
			HttpResponse response = client.execute(post);			
			StatusLine statusLine = response.getStatusLine();
			HttpEntity responseEntity = response.getEntity();
			
			if (response.getStatusLine() != null) {				
				if (statusLine.getStatusCode() != 200) {
					logger.warn("{} {} {}", "Error creating activity spec", activitySpec.getName(), statusLine.toString());
				}
				else {
					if (responseEntity != null) {
						ActivitySpecCreateResponse activitySpecCreateResponse = mapper.readValue(responseEntity.getContent(), ActivitySpecCreateResponse.class);
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
			else {
				logger.warn("Empty response from the remote endpoint");
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
			HttpClient client = HttpClientBuilder.create().build();
			String path = ACTIVITY_SPEC_URI + "/" + activitySpecId + ACTIVITY_SPEC_URI_SUFFIX;
					
			String url = UriBuilder.fromUri(hostname).path(path).build().toString();			
			HttpPut put = new HttpPut(url);		
			
			StringEntity input = new StringEntity(CERTIFY_ACTIVITY_PAYLOAD, ContentType.APPLICATION_JSON);			
			put.setEntity(input);				
			
			HttpResponse response = client.execute(put);
			StatusLine statusLine = response.getStatusLine();
			
			if (statusLine != null) {
				if (statusLine.getStatusCode() != 200) {
					logger.warn("{} {} {}", "Error certifying activity", activitySpecId, statusLine.toString());
				}
				else {
					certificationResult = true;
				}
			}
			else {
				logger.warn("Empty response from the remote endpoint");
			}			
	            
		} catch (Exception e) {				
			logger.warn("{} {}", "Exception certifying activitySpec", e.getMessage());
		}
		
		return certificationResult;			
	}	
}
