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
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;

public class UserParamsValidation implements ValidationRule{
	@Override
	public ValidationInformation validate(ValidationInformation info) throws ValidationException{
		Service validate = info.getUserParams();
		Actions action = info.getAction();
		
		if(validate.getModelInfo() == null){
			throw new ValidationException ("modelInfo in userParams", true);
		}else if(validate.getModelInfo().getModelType() == null){
			throw new ValidationException("modelType in userParams service modelInfo", true);
		}else if(validate.getModelInfo().getModelVersionId() == null){
			throw new ValidationException("modelVersionId in userParams service modelInfo", true);
		}
		modelInfoValidation(info.getSir().getRequestDetails().getModelInfo(), validate.getModelInfo());
		if(validate.getInstanceName() != null && info.getRequestInfo().getInstanceName() != null){
			instanceNameValidation(info, validate);
		}
		for(Vnfs vnf : validate.getResources().getVnfs()){
			if(vnf.getModelInfo() == null){
				throw new ValidationException ("modelInfo in userParams vnf resources", true);
			}else if(vnf.getModelInfo().getModelCustomizationId() == null){
				throw new ValidationException ("modelCustomizationId in userParams vnf resources", true);
			}else if(vnf.getModelInfo().getModelVersionId() == null){
				throw new ValidationException("modelVersionId in userParams vnf resources", true);
			}
			if(vnf.getCloudConfiguration() == null){
				throw new ValidationException ("cloudConfiguration in userParams vnf resources", true);
			}
			if(action == Action.createInstance || action == Action.assignInstance){
				if(vnf.getPlatform() == null){
					throw new ValidationException ("platform in userParams vnf resources", true);
				}if(vnf.getProductFamilyId() == null){
					throw new ValidationException ("productFamilyId in userParams vnf resources", true);
				}
			}
			if (vnf.getPlatform() != null && vnf.getPlatform().getPlatformName() == null){
				throw new ValidationException ("platformName in userParams vnf resources", true);
			}
			if(vnf.getVfModules().isEmpty()){
				throw new ValidationException ("vfModules in userParams vnf resources", true);
			}
			for(VfModules vfModules : vnf.getVfModules()){
				if(vfModules.getModelInfo() == null){
					throw new ValidationException ("modelInfo in userParams vfModules resources", true);
				}else if(vfModules.getModelInfo().getModelCustomizationId() == null){
					throw new ValidationException ("modelCustomizationId in userParams vfModule resources", true);
				}else if(vfModules.getModelInfo().getModelVersionId() == null){
					throw new ValidationException("modelVersionId in userParams vfModule resources", true);
				}
			}
		}
		
		List<Networks> validateNetworks = new ArrayList<>();
		validateNetworks = validate.getResources().getNetworks();
		if(validateNetworks != null){
			for(Networks networks : validateNetworks){
				if(networks.getModelInfo() == null){
					throw new ValidationException ("modelInfo in userParams network resources", true);
				}else if(networks.getModelInfo().getModelCustomizationId() == null){
					throw new ValidationException ("modelCustomizationId in userParams network resources", true);
				}else if(networks.getModelInfo().getModelVersionId() == null){
					throw new ValidationException("modelVersionId in userParams network resources", true);
				}
				if(networks.getCloudConfiguration() == null){
					throw new ValidationException ("cloudConfiguration in userParams network resources", true);
				}
			}
		} 
		return info;
	}
	public void instanceNameValidation(ValidationInformation info, Service validate) throws ValidationException{
		if(!info.getRequestInfo().getInstanceName().equals(validate.getInstanceName())){
			throw new ValidationException("instanceName in requestInfo", "instanceName in userParams service");
		}
	}
	public void modelInfoValidation(ModelInfo info, ModelInfo userParamInfo) throws ValidationException{
		if(!info.getModelType().equals(userParamInfo.getModelType())){
			throw new ValidationException("modelType in modelInfo", "modelType in userParams service");
		}
		if((info.getModelInvariantId() != null && userParamInfo.getModelInvariantId() != null) &&
				(!info.getModelInvariantId().equals(userParamInfo.getModelInvariantId()))){
			throw new ValidationException("modelInvariantId in modelInfo", "modelInvariantId in userParams service");
		}
		if(!info.getModelVersionId().equals(userParamInfo.getModelVersionId())){
			throw new ValidationException("modelVersionId in modelInfo", "modelVersionId in userParams service");
		}
		if((info.getModelName() != null && userParamInfo.getModelName() != null) &&
				(!info.getModelName().equals(userParamInfo.getModelName()))){
			throw new ValidationException("modelName in modelInfo", "modelName in userParams service");
		}
		if((info.getModelVersion() != null && userParamInfo.getModelVersion() != null) &&
			(!info.getModelVersion().equals(userParamInfo.getModelVersion()))){
			throw new ValidationException("modelVersion in modelInfo", "modelVersion in userParams service");
		}
		if((info.getModelCustomizationId() != null && userParamInfo.getModelCustomizationId() != null) &&
			(!info.getModelCustomizationId().equals(userParamInfo.getModelCustomizationId()))){
			throw new ValidationException("modelCustomizationId in modelInfo", "modelCustomizationId in userParams service");
		}
	}
}