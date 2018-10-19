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

import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.MsoLogger;
import org.onap.so.serviceinstancebeans.InstanceDirection;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.utils.UUIDChecker;

public class RelatedInstancesValidation implements ValidationRule{
    private static boolean empty(String s) {
  	  return (s == null || s.trim().isEmpty());
    }
	@Override
	public ValidationInformation validate(ValidationInformation info) throws ValidationException{
		ServiceInstancesRequest sir = info.getSir();
		Actions action = info.getAction();
		int reqVersion = info.getReqVersion();
		String requestScope = info.getRequestScope();
      	String serviceInstanceType = null;
      	String networkType = null;
      	String vnfType = null;
      	String vfModuleType = null;
      	String vfModuleModelName = null;
		ModelInfo modelInfo = info.getSir().getRequestDetails().getModelInfo();
	    MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH, RelatedInstancesValidation.class);
		RelatedInstanceList[] instanceList = sir.getRequestDetails().getRelatedInstanceList();
		String serviceModelName = null;
        String vnfModelName = null;
        String asdcServiceModelVersion = null;
        String volumeGroupId = null;
        boolean isRelatedServiceInstancePresent = false;
        boolean isRelatedVnfInstancePresent = false;
    	boolean isSourceVnfPresent = false;
      	boolean isDestinationVnfPresent = false;
      	boolean isConnectionPointPresent = false;	
		
      	if(requestScope.equalsIgnoreCase(ModelType.service.name())){
			serviceInstanceType = modelInfo.getModelName();
			info.setServiceInstanceType(serviceInstanceType);
	    }
	    if(requestScope.equalsIgnoreCase(ModelType.network.name())){
	        networkType = modelInfo.getModelName();
	        info.setNetworkType(networkType);
	    }
	    if (instanceList != null) {
	       	for(RelatedInstanceList relatedInstanceList : instanceList){
	        	RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();

	        	ModelInfo relatedInstanceModelInfo = relatedInstance.getModelInfo ();
				if (relatedInstanceModelInfo == null) {
	          		throw new ValidationException ("modelInfo in relatedInstance");
	          	}

	          	if (relatedInstanceModelInfo.getModelType () == null) {
	          		throw new ValidationException ("modelType in relatedInstance");
	          	}

	          	if(empty(relatedInstance.getInstanceName ()) && ModelType.pnf.equals(relatedInstanceModelInfo.getModelType())) {
	          		throw new ValidationException ("instanceName in relatedInstance for pnf modelType");
	          	}
	          	
	        	if (!empty (relatedInstance.getInstanceName ())) {
	            	if (!relatedInstance.getInstanceName ().matches (Constants.VALID_INSTANCE_NAME_FORMAT)) {
	            		throw new ValidationException ("instanceName format in relatedInstance");
	            	}
	            }

	          	if (empty (relatedInstance.getInstanceId ()) && !ModelType.pnf.equals(relatedInstanceModelInfo.getModelType())) {
	          		throw new ValidationException ("instanceId in relatedInstance");
	          	}

	          	if (!empty(relatedInstance.getInstanceId ()) && !UUIDChecker.isValidUUID (relatedInstance.getInstanceId ())) {
	          		throw new ValidationException ("instanceId format in relatedInstance");
	          	}


	          	if (action != Action.deleteInstance) {
	          		if(!(	relatedInstanceModelInfo.getModelType().equals(ModelType.volumeGroup) || 
	          				relatedInstanceModelInfo.getModelType().equals(ModelType.connectionPoint) ||
	          				relatedInstanceModelInfo.getModelType().equals(ModelType.pnf) ||
	          				relatedInstanceModelInfo.getModelType().equals(ModelType.networkInstanceGroup))) {

	          			if(empty (relatedInstanceModelInfo.getModelInvariantId ())) {
	          				throw new ValidationException ("modelInvariantId in relatedInstance");
	          			} else if(reqVersion >= 4 && empty(relatedInstanceModelInfo.getModelVersionId ())) {
	          				throw new ValidationException("modelVersionId in relatedInstance");
	          			} else if(empty(relatedInstanceModelInfo.getModelName ())) {
	          				throw new ValidationException ("modelName in relatedInstance");
	          			} else if (empty (relatedInstanceModelInfo.getModelVersion ())) {
	          				throw new ValidationException ("modelVersion in relatedInstance");
	          			}
	          		}

		          	if (!empty (relatedInstanceModelInfo.getModelInvariantId ()) &&
		          			!UUIDChecker.isValidUUID (relatedInstanceModelInfo.getModelInvariantId ())) {
		          		throw new ValidationException ("modelInvariantId format in relatedInstance");
		          	}
		          	
		          	if(ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
		          		if(InstanceDirection.source.equals(relatedInstance.getInstanceDirection()) && relatedInstanceModelInfo.getModelType().equals(ModelType.vnf)) {
		          			isSourceVnfPresent = true;
		          		} else if(InstanceDirection.destination.equals(relatedInstance.getInstanceDirection()) && 
		          				(relatedInstanceModelInfo.getModelType().equals(ModelType.vnf) || (relatedInstanceModelInfo.getModelType().equals(ModelType.pnf) && reqVersion == 6))) {
		          			isDestinationVnfPresent = true;
		          		}
		          	}
		          	
		          	if(ModelType.connectionPoint.equals(relatedInstanceModelInfo.getModelType()) && ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
		          		isConnectionPointPresent = true;
		          	}
		        }

	          	if (empty (relatedInstanceModelInfo.getModelCustomizationName ()) && relatedInstanceModelInfo.getModelType ().equals (ModelType.vnf) ) {
	          		if(reqVersion >=4 && empty (relatedInstanceModelInfo.getModelCustomizationId()) && action != Action.deleteInstance) {
	          			throw new ValidationException ("modelCustomizationName or modelCustomizationId in relatedInstance of vnf");
	          		}
	          	}

	          	if(relatedInstanceModelInfo.getModelType().equals(ModelType.service)) {
	          		isRelatedServiceInstancePresent = true;
	          		if (!relatedInstance.getInstanceId ().equals (sir.getServiceInstanceId ())) {
	          			throw new ValidationException ("serviceInstanceId matching the serviceInstanceId in request URI");
	          		}
	          		serviceModelName = relatedInstanceModelInfo.getModelName ();
	          		asdcServiceModelVersion = relatedInstanceModelInfo.getModelVersion ();
	          	} else if(relatedInstanceModelInfo.getModelType().equals(ModelType.vnf) && !(ModelType.configuration.name().equalsIgnoreCase(requestScope))) {
	          		isRelatedVnfInstancePresent = true;
	          		if (!relatedInstance.getInstanceId ().equals (sir.getVnfInstanceId ())) {
	          			throw new ValidationException ("vnfInstanceId matching the vnfInstanceId in request URI");
	          		}
	          		vnfModelName = relatedInstanceModelInfo.getModelCustomizationName();
	          	} else if(relatedInstanceModelInfo.getModelType().equals(ModelType.volumeGroup)) {	          		
	           		volumeGroupId = relatedInstance.getInstanceId ();
	          	}
          	}

	       	if(action == Action.createInstance && ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
		       	if(!isSourceVnfPresent) {
		       		throw new ValidationException ("source vnf relatedInstance for Port Configuration");
		       	} 
		       	
		       	if(!isDestinationVnfPresent) {
		       		throw new ValidationException ("destination vnf relatedInstance for Port Configuration");
		       	}
        	}

	       	if((action == Action.enablePort || action == Action.disablePort) && ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
	       		if(!isConnectionPointPresent) {
	       			throw new ValidationException ("connectionPoint relatedInstance for Port Configuration");
	       		}
	       	}
	       	
	        if(requestScope.equalsIgnoreCase (ModelType.volumeGroup.name ())) {
	        	if (!isRelatedServiceInstancePresent) {
	        		throw new ValidationException ("related service instance for volumeGroup request");
	        	}
	        	if (!isRelatedVnfInstancePresent) {
	        		throw new ValidationException ("related vnf instance for volumeGroup request");
	        	}
	          	serviceInstanceType = serviceModelName;
	          	vnfType = serviceModelName + "/" + vnfModelName;
	          	info.setServiceInstanceType(serviceInstanceType);
	          	info.setVnfType(vnfType);
	        }
	        else if(requestScope.equalsIgnoreCase(ModelType.vfModule.name ())) {
	        	if (!isRelatedServiceInstancePresent) {
	        		throw new ValidationException ("related service instance for vfModule request");
	        	}
	        	if (!isRelatedVnfInstancePresent) {
	        		throw new ValidationException ("related vnf instance for vfModule request");
	        	}
	        	vfModuleModelName = modelInfo.getModelName ();
	          	serviceInstanceType = serviceModelName;
	          	vnfType = serviceModelName + "/" + vnfModelName;
	          	vfModuleType = vnfType + "::" + vfModuleModelName;
	          	sir.setVolumeGroupInstanceId (volumeGroupId);
	          	info.setVfModuleModelName(vfModuleModelName);
	          	info.setVnfType(vnfType);
	          	info.setServiceInstanceType(serviceInstanceType);
	          	info.setVfModuleType(vfModuleType);
	        }
	        else if (requestScope.equalsIgnoreCase (ModelType.vnf.name ())) {
	        	if (!isRelatedServiceInstancePresent) {
	        		throw new ValidationException ("related service instance for vnf request");
	        	}
	        	vnfType = serviceModelName + "/" + sir.getRequestDetails().getModelInfo().getModelCustomizationName();
	        	info.setVnfType(vnfType);
	       }
        }
        else if ((( requestScope.equalsIgnoreCase(ModelType.vnf.name ()) || requestScope.equalsIgnoreCase(ModelType.volumeGroup.name ()) || requestScope.equalsIgnoreCase(ModelType.vfModule.name ()) 
        			|| requestScope.equalsIgnoreCase(ModelType.configuration.name())) && (action == Action.createInstance || action == Action.enablePort || action == Action.disablePort)) ||
        		(reqVersion >= 4 && (requestScope.equalsIgnoreCase(ModelType.volumeGroup.name ()) || requestScope.equalsIgnoreCase(ModelType.vfModule.name ())) && action == Action.updateInstance ||
        		(requestScope.equalsIgnoreCase(ModelType.vfModule.name ()) && action == Action.scaleOut)) ||
        			(requestScope.equalsIgnoreCase(ModelType.service.name()) && (action.equals(Action.addRelationships) || action.equals(Action.removeRelationships)))){
        	 msoLogger.debug ("related instance exception");
        	throw new ValidationException ("related instances");
        }
    	info.setVfModuleModelName(vfModuleModelName);
      	info.setServiceInstanceType(serviceInstanceType);
      	info.setVnfType(vnfType);
      	info.setAsdcServiceModelVersion(asdcServiceModelVersion);
      	info.setVfModuleType(vfModuleType);
		return info;
	}
}