/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved. 
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
package org.openecomp.mso.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.AllottedResource
import org.openecomp.mso.bpmn.core.domain.NetworkResource
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.common.recipe.ResourceInput
import org.openecomp.mso.bpmn.common.recipe.BpmnRestClient
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils;
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.List;
import java.util.UUID;

import javax.mail.Quota.Resource;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;

/**
 * This groovy class supports the <class>DoCompareModelVersions.bpmn</class> process.
 *
 * Inputs:
 * @param - model-invariant-id-target
 * @param - model-version-id-target
 * @param - model-invariant-id-original
 * @param - model-version-id-original
 *
 * Outputs:
 * @param - addResourceList
 * @param - delResourceList
 *
 */
public class DoCompareModelVersions extends AbstractServiceTaskProcessor {

	String Prefix="DCMPMDV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	CatalogDbUtils cutils = new CatalogDbUtils()

	public void preProcessRequest (Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("INFO"," ***** preProcessRequest *****",  isDebugEnabled)

		try {
			execution.setVariable("prefix", Prefix)
			
			//Inputs
			String modelInvariantUuid_target = execution.getVariable("model-invariant-id-target")
			if (isBlank(modelInvariantUuid_target)) {
				msg = "Input model-invariant-id-target is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}			
			
            String modelUuid_target = execution.getVariable("model-version-id-target")
            if (isBlank(modelUuid_target)) {
				msg = "Input model-version-id-target is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
            
            String modelInvariantUuid_original = execution.getVariable("model-invariant-id-original")
            if (isBlank(modelInvariantUuid_original)) {
				msg = "Input model-invariant-id-original is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
            
            String modelUuid_original = execution.getVariable("model-version-id-original")
            if (isBlank(modelUuid_original)) {
				msg = "Input model-version-id-original is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}  
			
			// Target and original modelInvariantUuid must to be the same
			if(modelInvariantUuid_target != modelInvariantUuid_original){
				msg = "Input model-invariant-id-target and model-invariant-id-original must to be the same"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)      
			}
			
			// Target and original modelUuid must not to be the same
			if(modelUuid_target == modelUuid_original){
				msg = "Input model-version-id-target and model-version-id-original must not to be the same"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)      
			}

		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}
	
   public void prepareDecomposeService_Target(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

        try {
            utils.log("DEBUG", " ***** Inside prepareDecomposeService_Target of update generic e2e service ***** ", isDebugEnabled)
            String modelInvariantUuid = execution.getVariable("model-invariant-id-target")
            String modelUuid = execution.getVariable("model-version-id-target")
            //here modelVersion is not set, we use modelUuid to decompose the service.
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)
            execution.setVariable("serviceModelInfo_Target", serviceModelInfo)

            utils.log("DEBUG", " ***** Completed prepareDecomposeService_Target of update generic e2e service ***** ", isDebugEnabled)
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in update generic e2e service flow. Unexpected Error from method prepareDecomposeService_Target() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
     }

    public void processDecomposition_Target(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
    
        utils.log("DEBUG", " ***** Inside processDecomposition_Target() of update generic e2e service flow ***** ", isDebugEnabled)    
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            execution.setVariable("serviceDecomposition_Target", serviceDecomposition)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in update generic e2e service flow. Unexpected Error from method processDecomposition_Target() - " + ex.getMessage()
            utils.log("DEBUG", exceptionMessage, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }
 
   public void prepareDecomposeService_Original(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

        try {
            utils.log("DEBUG", " ***** Inside prepareDecomposeService_Original of update generic e2e service ***** ", isDebugEnabled)
            String modelInvariantUuid = execution.getVariable("model-invariant-id-original")
            String modelUuid = execution.getVariable("model-version-id-original")
            //here modelVersion is not set, we use modelUuid to decompose the service.
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)
            execution.setVariable("serviceModelInfo_Original", serviceModelInfo)

            utils.log("DEBUG", " ***** Completed prepareDecomposeService_Original of update generic e2e service ***** ", isDebugEnabled)
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in update generic e2e service flow. Unexpected Error from method prepareDecomposeService_Original() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
     }

    public void processDecomposition_Original(Execution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
    
        utils.log("DEBUG", " ***** Inside processDecomposition_Original() of update generic e2e service flow ***** ", isDebugEnabled)    
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            execution.setVariable("serviceDecomposition_Original", serviceDecomposition)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in update generic e2e service flow. processDecomposition_Original() - " + ex.getMessage()
            utils.log("DEBUG", exceptionMessage, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    } 	

	public void doCompareModelVersions(execution){
	    def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", "======== Start doCompareModelVersions Process ======== ", isDebugEnabled)          

        ServiceDecomposition serviceDecomposition_Target = execution.getVariable("serviceDecomposition_Target")
        ServiceDecomposition serviceDecomposition_Original = execution.getVariable("serviceDecomposition_Original")
        
        List<Resource> allSR_target = serviceDecomposition_Target.getServiceResources();
        List<Resource> allSR_original = serviceDecomposition_Original.getServiceResources();
        
        List<Resource> addedResource = new ArrayList<String>()
        List<Resource> delResource = new ArrayList<String>()
        
        addResourceList.addAll(allSR_target)
        delResourceList.addAll(allSR_original)
        
        //Compare
        for (Resource rc_t : allSR_target){
            String muuid = rc_t.getModelInfo().getModelUuid()
            String mIuuid = rc_t.getModelInfo().getModelInvariantUuid()
            String mCuuid = rc_t.getModelInfo().getModelCustomizationUuid()
            for (Resource rc_o : allSR_original){
                if(rc_o.getModelInfo().getModelUuid() == muuid 
                && rc_o.getModelInfo().getModelInvariantUuid() == mIuuid 
                && rc_o.getModelInfo().getModelCustomizationUuid() == mCuuid) {
                    addResourceList.remove(rc_t);
                    delResourceList.remove(rc_o);
                }
            }         
        }

        execution.setVariable("addResourceList", addResourceList)
        execution.setVariable("delResourceList", delResourceList)
        utils.log("INFO", "addResourceList: " + addResourceList, isDebugEnabled)  
        utils.log("INFO", "delResourceList: " + delResourceList, isDebugEnabled)
        
        utils.log("INFO", "======== COMPLETED doCompareModelVersions Process ======== ", isDebugEnabled)  
	}

}
