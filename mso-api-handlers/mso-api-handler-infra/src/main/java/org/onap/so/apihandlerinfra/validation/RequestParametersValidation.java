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
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestParameters;

public class RequestParametersValidation implements ValidationRule{
	private static boolean empty(String s) {
  	  return (s == null || s.trim().isEmpty());
    }
	@Override
	public ValidationInformation validate(ValidationInformation info) throws ValidationException{
		int reqVersion = info.getReqVersion();
		String requestScope = info.getRequestScope();
		Actions action = info.getAction();
		RequestParameters requestParameters = info.getReqParameters(); 
	      
		if (requestScope.equalsIgnoreCase(ModelType.service.name()) && (action == Action.createInstance || action == Action.assignInstance)) {
        	if (requestParameters == null) {
        		throw new ValidationException ("requestParameters");
        	}
        	if (empty (requestParameters.getSubscriptionServiceType())) {
        		throw new ValidationException ("subscriptionServiceType");
        	}
        }
		if(reqVersion >= 4){
        	if(Action.addRelationships.equals(action) || Action.removeRelationships.equals(action)) {
        		if(requestParameters == null || requestParameters.getALaCarte() == null) {
        			throw new ValidationException ("aLaCarte in requestParameters");
        		}
        	}
        }
		if(requestParameters == null && !requestScope.equalsIgnoreCase(ModelType.service.name())){
			info.setALaCarteFlag(true);
		}
        if(requestParameters != null){
        	if(requestScope.equalsIgnoreCase(ModelType.vnf.name())){
        		if(action == Action.updateInstance){
        			if(requestParameters.isUsePreload() == null){
        				requestParameters.setUsePreload(true);
        			}
        		}
        		if(action == Action.replaceInstance){
        			if(requestParameters.getRebuildVolumeGroups() == null){
        				requestParameters.setRebuildVolumeGroups(false);
        			}
        		}
        	}
        	if(requestScope.equalsIgnoreCase(ModelType.vfModule.name())){
        		if(action == Action.createInstance || action == Action.updateInstance){        			
        			if(requestParameters.isUsePreload() == null){        				
        				if(reqVersion >= 4){       					
        					if (requestParameters.getALaCarte() == null || requestParameters.getALaCarte() == false) {        						
        						requestParameters.setUsePreload(false);
        					}
        					else {        						
        						requestParameters.setUsePreload(true);
        					}
        				}
        				else {        				
        					requestParameters.setUsePreload(true);
        				}
        			}
        		}
        	}
        	if(reqVersion >= 4){
 		       if(requestParameters.getALaCarte() != null){
 		        	info.setALaCarteFlag(requestParameters.getALaCarte());
 		       }else if(requestScope.equalsIgnoreCase(ModelType.service.name())){
 		    	   if(action == Action.createInstance || action == Action.deleteInstance || action == Action.activateInstance || action == Action.deactivateInstance){
 		    		   if(requestParameters.getALaCarte() == null){
 		    			   requestParameters.setaLaCarte(false);
 		    			   info.setALaCarteFlag(requestParameters.getALaCarte());
 		    		   }
 		    	   }
 		       }else{
 		    	   info.setALaCarteFlag(true);
 		       }
        	}else{
        		info.setALaCarteFlag(true);
        	}
        }
		return info;
	}
}