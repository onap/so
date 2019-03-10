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

import java.util.HashMap;

import org.onap.so.apihandler.common.CommonConstants;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.utils.UUIDChecker;

public class InstanceIdMapValidation implements ValidationRule{

	@Override
	public ValidationInformation validate(ValidationInformation info) throws ValidationException{
		HashMap<String, String> instanceIdMap = info.getInstanceIdMap();
		ServiceInstancesRequest sir = info.getSir();
		if(instanceIdMap != null){
        	if(instanceIdMap.get("serviceInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("serviceInstanceId"))) {
        			throw new ValidationException ("serviceInstanceId");
        		}
        		sir.setServiceInstanceId(instanceIdMap.get("serviceInstanceId"));
        	}

        	if(instanceIdMap.get("vnfInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("vnfInstanceId"))) {
        			throw new ValidationException ("vnfInstanceId");
        		}
        		sir.setVnfInstanceId(instanceIdMap.get("vnfInstanceId"));
        	}

        	if(instanceIdMap.get("vfModuleInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("vfModuleInstanceId"))) {
        			throw new ValidationException ("vfModuleInstanceId");
        		}
        		sir.setVfModuleInstanceId(instanceIdMap.get("vfModuleInstanceId"));
        	}

        	if(instanceIdMap.get("volumeGroupInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("volumeGroupInstanceId"))) {
        			throw new ValidationException ("volumeGroupInstanceId");
        		}
        		sir.setVolumeGroupInstanceId(instanceIdMap.get("volumeGroupInstanceId"));
        	}

        	if(instanceIdMap.get("networkInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("networkInstanceId"))) {
        			throw new ValidationException ("networkInstanceId");
        		}
        		sir.setNetworkInstanceId(instanceIdMap.get("networkInstanceId"));
        	}
        	
        	if(instanceIdMap.get("configurationInstanceId") != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get ("configurationInstanceId"))) {
        			throw new ValidationException ("configurationInstanceId");
        		}
        		sir.setConfigurationId(instanceIdMap.get("configurationInstanceId"));
        	}
        	
        	if(instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID) != null){
        		if (!UUIDChecker.isValidUUID (instanceIdMap.get (CommonConstants.INSTANCE_GROUP_INSTANCE_ID))) {
        			throw new ValidationException (CommonConstants.INSTANCE_GROUP_INSTANCE_ID, true);
        		}
        		sir.setInstanceGroupId(instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID));
        	}
        }
        return info;
	}
}