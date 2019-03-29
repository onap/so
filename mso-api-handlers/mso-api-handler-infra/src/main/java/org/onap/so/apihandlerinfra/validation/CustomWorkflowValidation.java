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

package org.onap.so.apihandlerinfra.validation;

import java.util.List;
import java.util.Map;

import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.RequestParameters;

public class CustomWorkflowValidation implements ValidationRule{
    private static boolean empty(String s) {
  	  return (s == null || s.trim().isEmpty());
    }
	@Override
	public ValidationInformation validate(ValidationInformation info) throws ValidationException{
    	RequestParameters requestParameters = info.getSir().getRequestDetails().getRequestParameters();
    	CloudConfiguration cloudConfiguration = info.getSir().getRequestDetails ().getCloudConfiguration();
    	String workflowUuid = info.getInstanceIdMap().get("workflowUuid");
    	
		if (cloudConfiguration == null) {
			throw new ValidationException ("cloudConfiguration");
		}else if (empty (cloudConfiguration.getCloudOwner ())) {
    		throw new ValidationException ("cloudOwner");			
		}else if (empty (cloudConfiguration.getLcpCloudRegionId ())) {
    		throw new ValidationException ("lcpCloudRegionId");
    	}else if (empty (cloudConfiguration.getTenantId ())) {
    		throw new ValidationException ("tenantId");
    	}
    	if(requestParameters == null){
    		throw new ValidationException("requestParameters");
    	}
    	
    	List<Map<String, Object>> userParams = requestParameters.getUserParams();
    	if (!validateCustomUserParams(userParams, workflowUuid)) {
    		throw new ValidationException("userParams");
    	}
        return info;
	}
	
	private boolean validateCustomUserParams(List<Map<String, Object>> userParams, String workflowUuid) {
		return true;
	}
}