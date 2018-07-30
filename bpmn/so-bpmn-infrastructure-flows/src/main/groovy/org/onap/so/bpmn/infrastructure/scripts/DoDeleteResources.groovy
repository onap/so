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
package org.onap.so.bpmn.infrastructure.scripts

import org.json.JSONArray;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*

import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.rest.APIResponse;
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig

import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import com.fasterxml.jackson.jaxrs.json.annotation.JSONP.Def;
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

/**
 * This groovy class supports the <class>DoDeleteResources.bpmn</class> process.
 * 
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId - O
 * @param - subscriptionServiceType - O
 * @param - serviceInstanceId
 * @param - serviceInstanceName - O
 * @param - serviceInputParams (should contain aic_zone for serviceTypes TRANSPORT,ATM)
 * @param - sdncVersion 
 * @param - failNotFound - TODO
 * @param - serviceInputParams - TODO 
 *
 * @param - delResourceList
 * @param - serviceRelationShip
 *
 * Outputs:
 * @param - WorkflowException
 * 
 * Rollback - Deferred
 */
public class DoDeleteResources extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteResources.class);

	String Prefix="DDELR_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		msoLogger.trace("preProcessRequest ")
		String msg = ""
		
		List<ServiceInstance> realNSRessources = new ArrayList<ServiceInstance>() 
        
        // related ns from AAI
        String serviceRelationShip = execution.getVariable("serviceRelationShip")
        def jsonSlurper = new JsonSlurper()
        def jsonOutput = new JsonOutput()        
        List<String> nsSequence = new ArrayList<String>() 
        List relationShipList =  jsonSlurper.parseText(serviceRelationShip)
        if (relationShipList != null) {
           relationShipList.each {
               String resourceType = it.resourceType
               nsSequence.add(resourceType)
           }
        }       
     
        execution.setVariable("currentNSIndex", 0)
        execution.setVariable("nsSequence", nsSequence)
        execution.setVariable("realNSRessources", realNSRessources)
        msoLogger.info("nsSequence: " + nsSequence) 

		msoLogger.trace("Exit preProcessRequest ")
    }

   public void getCurrentNS(execution){
       msoLogger.trace("Start getCurrentNS Process ")    
       
       def currentIndex = execution.getVariable("currentNSIndex")
       List<String> nsSequence = execution.getVariable("nsSequence") 
       String nsResourceType =  nsSequence.get(currentIndex)
       
       // GET AAI by Name, not ID, for process convenient
       execution.setVariable("GENGS_type", "service-instance")
       execution.setVariable("GENGS_serviceInstanceId", "") 
       execution.setVariable("GENGS_serviceInstanceName", nsResourceType)       

       msoLogger.trace("COMPLETED getCurrentNS Process ")  
   }	
	
	public void postProcessAAIGET(DelegateExecution execution) {
		msoLogger.trace("postProcessAAIGET2 ")
		String msg = ""

		try {
			String nsResourceName = execution.getVariable("GENGS_serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				msoLogger.info("Error getting Service-instance from AAI in postProcessAAIGET", + nsResourceName)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				msoLogger.debug("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
					msoLogger.info(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
			else
			{
				boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
				if(foundInAAI){
					String aaiService = execution.getVariable("GENGS_service")
					if (!isBlank(aaiService)) {
					    String svcId = utils.getNodeText(aaiService, "service-instance-id")
						//String mn = utils.getNodeText(aaiService, "model-name")						
						String mIuuid = utils.getNodeText(aaiService, "model-invariant-id")
						String muuid = utils.getNodeText(aaiService, "model-version-id")
						String mCuuid = utils.getNodeText(aaiService, "model-customization-uuid")
						ServiceInstance rc = new ServiceInstance()
						ModelInfo modelInfo = new ModelInfo()
						//modelInfo.setModelName(mn)
						modelInfo.setModelUuid(muuid)						
						modelInfo.setModelInvariantUuid(mIuuid)
						modelInfo.getModelCustomizationUuid(mCuuid)
						rc.setModelInfo(modelInfo)
						rc.setInstanceId(svcId)
						rc.setInstanceName(nsResourceName)

						List<ServiceInstance> realNSRessources = execution.getVariable("realNSRessources")
						realNSRessources.add(rc)
						execution.setVariable("realNSRessources", realNSRessources)
						
						msoLogger.info("Found Service-instance in AAI.serviceInstanceName:" + execution.getVariable("serviceInstanceName"))
					}
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoDeleteResources.postProcessAAIGET " + ex.getMessage()
			msoLogger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit postProcessAAIGET ")
	}
	
    public void parseNextNS(execution){
       msoLogger.trace("Start parseNextNS Process ")    
       def currentIndex = execution.getVariable("currentNSIndex")
       def nextIndex =  currentIndex + 1
       execution.setVariable("currentNSIndex", nextIndex)
       List<String> nsSequence = execution.getVariable("nsSequence")    
       if(nextIndex >= nsSequence.size()){
           execution.setVariable("allNsFinished", "true")
       }else{
           execution.setVariable("allNsFinished", "false")
       }
       msoLogger.trace("COMPLETED parseNextNS Process ")            
   }   

   
   public void sequenceResource(execution){
       msoLogger.trace("STARTED sequenceResource Process ")
       List<String> nsResources = new ArrayList<String>()
       List<String> wanResources = new ArrayList<String>()
       List<String> resourceSequence = new  ArrayList<String>()
       
       // get delete resource list and order list
       List<Resource> delResourceList = execution.getVariable("delResourceList")
       // existing resource list
       List<ServiceInstance> existResourceList = execution.getVariable("realNSRessources")
       
       for(ServiceInstance rc_e : existResourceList){
       
           String muuid = rc_e.getModelInfo().getModelUuid()
           String mIuuid = rc_e.getModelInfo().getModelInvariantUuid()
           String mCuuid = rc_e.getModelInfo().getModelCustomizationUuid()
           rcType = rc_e.getInstanceName()
           
           for(Resource rc_d : delResourceList){                 
           
                if(rc_d.getModelInfo().getModelUuid() == muuid 
                && rc_d.getModelInfo().getModelInvariantUuid() == mIuuid 
                && rc_d.getModelInfo().getModelCustomizationUuid() == mCuuid) {
                
                   if(StringUtils.containsIgnoreCase(rcType, "overlay") 
                   || StringUtils.containsIgnoreCase(rcType, "underlay")){                   
                       wanResources.add(rcType)
                   }else{
                       nsResources.add(rcType)
                   }
                   
               }
           }
           
       }
  
       resourceSequence.addAll(wanResources)
       resourceSequence.addAll(nsResources)
       String isContainsWanResource = wanResources.isEmpty() ? "false" : "true"
       execution.setVariable("isContainsWanResource", isContainsWanResource)
       execution.setVariable("currentResourceIndex", 0)
       execution.setVariable("resourceSequence", resourceSequence)
       msoLogger.info("resourceSequence: " + resourceSequence)  
       execution.setVariable("wanResources", wanResources)
       msoLogger.trace("END sequenceResource Process ")
   }
   
   public void getCurrentResource(execution){
       msoLogger.trace("Start getCurrentResoure Process ")    
       def currentIndex = execution.getVariable("currentResourceIndex")
       List<String> resourceSequence = execution.getVariable("resourceSequence")  
       List<String> wanResources = execution.getVariable("wanResources")  
       String resourceName =  resourceSequence.get(currentIndex)
       execution.setVariable("resourceType",resourceName)
       if(wanResources.contains(resourceName)){
           execution.setVariable("controllerInfo", "SDN-C")
       }else{
           execution.setVariable("controllerInfo", "VF-C")
       }
       msoLogger.trace("COMPLETED getCurrentResoure Process ")  
   }
   
   /**
    * prepare delete parameters
    */
   public void preResourceDelete(execution, resourceName){

       msoLogger.trace("STARTED preResourceDelete Process ")
       
       List<ServiceInstance> existResourceList = execution.getVariable("realNSRessources")
           
       for(ServiceInstance rc_e : existResourceList){
           
            if(StringUtils.containsIgnoreCase(rc_e.getInstanceName(), resourceName)) {
                
				   String resourceInstanceUUID = rc_e.getInstanceId()
				   String resourceTemplateUUID = rc_e.getModelInfo().getModelUuid()
				   execution.setVariable("resourceInstanceId", resourceInstanceUUID)
				   execution.setVariable("resourceTemplateId", resourceTemplateUUID)				   
				   execution.setVariable("resourceType", resourceName)
			       msoLogger.info("Delete Resource Info resourceTemplate Id :" + resourceTemplateUUID + "  resourceInstanceId: " + resourceInstanceUUID + " resourceType: " + resourceName)
           }
       }       
    
       msoLogger.trace("END preResourceDelete Process ")
   }
   
   public void parseNextResource(execution){
       msoLogger.trace("Start parseNextResource Process ")    
       def currentIndex = execution.getVariable("currentResourceIndex")
       def nextIndex =  currentIndex + 1
       execution.setVariable("currentResourceIndex", nextIndex)
       List<String> resourceSequence = execution.getVariable("resourceSequence")    
       if(nextIndex >= resourceSequence.size()){
           execution.setVariable("allResourceFinished", "true")
       }else{
           execution.setVariable("allResourceFinished", "false")
       }
       msoLogger.trace("COMPLETED parseNextResource Process ")            
   }
   
     /**
      * post config request.
      */
     public void postConfigRequest(execution){
         //to do
     } 
   
}
 