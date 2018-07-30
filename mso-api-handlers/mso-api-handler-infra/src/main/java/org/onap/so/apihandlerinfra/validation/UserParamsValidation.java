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

import java.util.ArrayList;
import java.util.List;

import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;

public class UserParamsValidation implements ValidationRule{
    private static boolean empty(String s) {
  	  return (s == null || s.trim().isEmpty());
    }
	@Override
	public ValidationInformation validate(ValidationInformation info) throws ValidationException{
		Service validate = info.getUserParams();
		Actions action = info.getAction();
		
		if(validate.getModelInfo() == null){
			throw new ValidationException ("model-info in userParams");
		}else if(validate.getModelInfo().getModelVersionId() == null){
			throw new ValidationException("modelVersionId in userParams");
		}
		for(Vnfs vnf : validate.getResources().getVnfs()){
			if(vnf.getModelInfo() == null){
				throw new ValidationException ("model-info in userParams vnf resources");
			}else if(vnf.getModelInfo().getModelCustomizationId() == null){
				throw new ValidationException ("modelCustomizationId in userParams vnf resources");
			}else if(vnf.getModelInfo().getModelVersionId() == null){
				throw new ValidationException("modelVersionId in userParams vnf resources");
			}
			if(vnf.getCloudConfiguration() == null){
				throw new ValidationException ("cloudConfiguration in userParams vnf resources");
			}
			if(action == Action.createInstance || action == Action.assignInstance){
				if(vnf.getPlatform() == null){
					throw new ValidationException ("platform in userParams vnf resources");
				}if(vnf.getProductFamilyId() == null){
					throw new ValidationException ("productFamilyId in userParams vnf resources");
				}
			}
			if (vnf.getPlatform() != null && vnf.getPlatform().getPlatformName() == null){
				throw new ValidationException ("platformName in userParams vnf resources");
			}
			if(vnf.getVfModules().isEmpty()){
				throw new ValidationException ("vfModules in userParams vnf resources");
			}
			for(VfModules vfModules : vnf.getVfModules()){
				if(vfModules.getModelInfo() == null){
					throw new ValidationException ("model-info in userParams vfModules resources");
				}else if(vfModules.getModelInfo().getModelCustomizationId() == null){
					throw new ValidationException ("modelCustomizationId in userParams vfModule resources");
				}else if(vfModules.getModelInfo().getModelVersionId() == null){
					throw new ValidationException("modelVersionId in userParams vfModule resources");
				}
			}
		}
		
		List<Networks> validateNetworks = new ArrayList<>();
		validateNetworks = validate.getResources().getNetworks();
		if(validateNetworks != null){
			for(Networks networks : validateNetworks){
				if(networks.getModelInfo() == null){
					throw new ValidationException ("model-info in userParams network resources");
				}else if(networks.getModelInfo().getModelCustomizationId() == null){
					throw new ValidationException ("modelCustomizationId in userParams network resources");
				}else if(networks.getModelInfo().getModelVersionId() == null){
					throw new ValidationException("modelVersionId in userParams network resources");
				}
				if(networks.getCloudConfiguration() == null){
					throw new ValidationException ("cloudConfiguration in userParams network resources");
				}
			}
		} 
		return info;
	}
}