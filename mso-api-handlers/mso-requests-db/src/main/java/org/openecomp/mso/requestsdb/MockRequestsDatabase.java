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

package org.openecomp.mso.requestsdb;



import java.util.HashMap;
import java.util.Map;

public class MockRequestsDatabase {
	
	private Map<String, InfraActiveRequests> activeRequests;
	
	public MockRequestsDatabase() {
		activeRequests = new HashMap<>();
	}
	
	public void addRecord(InfraActiveRequests record) {
		String serviceType = record.getServiceType();
		String serviceInstanceId = record.getServiceInstanceId();
		String key = serviceType + "::" + serviceInstanceId;
		activeRequests.put(key, record);
	}
	
	public void deleteRecord(String serviceType, String serviceInstanceId) {
		String key = serviceType + "::" + serviceInstanceId;
		activeRequests.remove(key);
	}
	
	public InfraActiveRequests getRecord(String serviceType, String serviceInstanceId) {
		String key = serviceType + "::" + serviceInstanceId;
		InfraActiveRequests record = activeRequests.get(key);
		return record;
	}
	
	public InfraActiveRequests checkDuplicate(String serviceType, String serviceInstanceId) {
		return getRecord(serviceType, serviceInstanceId);
	}
	
	public InfraActiveRequests checkRetry(String serviceType, String serviceInstanceId) {
		InfraActiveRequests record = getRecord(serviceType, serviceInstanceId);
		InfraActiveRequests returnRecord = null;
		if (record != null) {
			String requestAction = record.getRequestAction();
			if (!"GetLayer3ServiceDetailsRequest".equals(requestAction)) {
				String status = record.getRequestStatus();
				if ("COMPLETED".equals(status)) {
					returnRecord = record;
				}
			}
		}
		return returnRecord;
	}
}
