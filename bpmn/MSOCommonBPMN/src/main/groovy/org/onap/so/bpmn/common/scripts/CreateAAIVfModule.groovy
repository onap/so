/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.common.scripts

import org.apache.commons.lang.StringUtils
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.GenericVnfs
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.db.catalog.beans.OrchestrationStatus
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class CreateAAIVfModule extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( CreateAAIVfModule.class);

	def prefix="CAAIVfMod_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable("prefix",prefix)
		execution.setVariable("CAAIVfMod_vnfId",null)
		execution.setVariable("CAAIVfMod_vnfName",null)
		execution.setVariable("CAAIVfMod_vnfType",null)
		execution.setVariable("CAAIVfMod_serviceId",null)
		execution.setVariable("CAAIVfMod_personaId",null)
		execution.setVariable("CAAIVfMod_personaVer",null)
		execution.setVariable("CAAIVfMod_modelCustomizationId",null)
		execution.setVariable("CAAIVfMod_vnfPersonaId",null)
		execution.setVariable("CAAIVfMod_vnfPersonaVer",null)
		execution.setVariable("CAAIVfMod_isBaseVfModule", false)
		execution.setVariable("CAAIVfMod_moduleName",null)
		execution.setVariable("CAAIVfMod_moduleModelName",null)
		execution.setVariable("CAAIVfMod_newGenericVnf",false)
		execution.setVariable("CAAIVfMod_genericVnfGetEndpoint",null)
		execution.setVariable("CAAIVfMod_genericVnfPutEndpoint",null)
		execution.setVariable("CAAIVfMod_aaiNamespace",null)
		execution.setVariable("CAAIVfMod_moduleExists",false)
		execution.setVariable("CAAIVfMod_baseModuleConflict", false)
		execution.setVariable("CAAIVfMod_vnfNameFromAAI", null)


		// CreateAAIVfModule workflow response variable placeholders
		execution.setVariable("CAAIVfMod_queryGenericVnfResponseCode",null)
		execution.setVariable("CAAIVfMod_queryGenericVnfResponse","")
		execution.setVariable("CAAIVfMod_createGenericVnfResponseCode",null)
		execution.setVariable("CAAIVfMod_createGenericVnfResponse","")
		execution.setVariable("CAAIVfMod_createVfModuleResponseCode",null)
		execution.setVariable("CAAIVfMod_createVfModuleResponse","")
		execution.setVariable("CAAIVfMod_parseModuleResponse","")
		execution.setVariable("CAAIVfMod_deleteGenericVnfResponseCode",null)
		execution.setVariable("CAAIVfMod_deleteGenericVnfResponse","")
		execution.setVariable("CAAIVfMod_deleteVfModuleResponseCode",null)
		execution.setVariable("CAAIVfMod_deleteVfModuleResponse","")
		execution.setVariable("CreateAAIVfModuleResponse","")
		execution.setVariable("RollbackData", null)

	}

	// parse the incoming CREATE_VF_MODULE request and store the Generic VNF
	// and VF Module data in the flow DelegateExecution
	public void preProcessRequest(DelegateExecution execution) {
		initProcessVariables(execution)

		def vnfId = execution.getVariable("vnfId")
		if (vnfId == null || vnfId.isEmpty()) {
			execution.setVariable("CAAIVfMod_newGenericVnf", true)
			execution.setVariable("CAAIVfMod_vnfId","")
		}
		else {
			execution.setVariable("CAAIVfMod_vnfId",vnfId)
		}

		def vnfName = execution.getVariable("vnfName")
		execution.setVariable("CAAIVfMod_vnfName", vnfName)

		String vnfType = execution.getVariable("vnfType")
        execution.setVariable("CAAIVfMod_vnfType", StringUtils.defaultString(vnfType))

		execution.setVariable("CAAIVfMod_serviceId", execution.getVariable("serviceId"))

		String personaModelId = execution.getVariable("personaModelId")
        execution.setVariable("CAAIVfMod_personaId",StringUtils.defaultString(personaModelId))

		String personaModelVersion = execution.getVariable("personaModelVersion")
        execution.setVariable("CAAIVfMod_personaVer", StringUtils.defaultString(personaModelVersion))

		String modelCustomizationId = execution.getVariable("modelCustomizationId")
        execution.setVariable("CAAIVfMod_modelCustomizationId",StringUtils.defaultString(modelCustomizationId))

		String vnfPersonaModelId = execution.getVariable("vnfPersonaModelId")
        execution.setVariable("CAAIVfMod_vnfPersonaId", StringUtils.defaultString(vnfPersonaModelId))

		String vnfPersonaModelVersion = execution.getVariable("vnfPersonaModelVersion")
		execution.setVariable("CAAIVfMod_vnfPersonaVer", StringUtils.defaultString(vnfPersonaModelVersion))

		//isBaseVfModule
		Boolean isBaseVfModule = false
		String isBaseVfModuleString = execution.getVariable("isBaseVfModule")
		if (isBaseVfModuleString != null && isBaseVfModuleString.equals("true")) {
				isBaseVfModule = true
		}
		execution.setVariable("CAAIVfMod_isBaseVfModule", isBaseVfModule)

		String isVidRequest = execution.getVariable("isVidRequest")
		if (isVidRequest != null && "true".equals(isVidRequest)) {
			logger.debug("VID Request received")
		}

		execution.setVariable("CAAIVfMod_moduleName",execution.getVariable("vfModuleName"))
		execution.setVariable("CAAIVfMod_moduleModelName",execution.getVariable("vfModuleModelName"))

		AaiUtil aaiUriUtil = new AaiUtil(this)
		String aaiNamespace = aaiUriUtil.getNamespace()
		logger.debug('AAI namespace is: ' + aaiNamespace)

		execution.setVariable("CAAIVfMod_aaiNamespace",aaiNamespace)

	}

	// send a GET request to AA&I to retrieve the Generic VNF/VF Module information based on a Vnf Name
	// expect a 200 response with the information in the response body or a 404 if the Generic VNF does not exist
	public void queryAAIForGenericVnf(DelegateExecution execution) {

		AAIResourceUri uri
		def vnfId = execution.getVariable("CAAIVfMod_vnfId")
		def vnfName = execution.getVariable("CAAIVfMod_vnfName")
        Optional<GenericVnf> genericVnfOp
		if (vnfId == null || vnfId.isEmpty()) {
			genericVnfOp = getAAIClient().getFirst(GenericVnfs.class, GenericVnf.class, AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs()).queryParam("vnf-name", vnfName).depth(Depth.ONE))
		} else {
			genericVnfOp = getAAIClient().get(GenericVnf.class,  AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)))
		}
		try {
            if(genericVnfOp.isPresent()){
                execution.setVariable("CAAIVfMod_queryGenericVnfResponseCode", 200)
                execution.setVariable("CAAIVfMod_queryGenericVnfResponse", genericVnfOp.get())
            }else{
                execution.setVariable("CAAIVfMod_queryGenericVnfResponseCode", 404)
                execution.setVariable("CAAIVfMod_queryGenericVnfResponse", "Generic Vnf not Found!")
            }
		} catch (Exception ex) {
			logger.debug("Exception occurred while executing AAI GET:" + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in queryAAIForGenericVnf.")

		}
	}

	// process the result from queryAAIForGenericVnf()
	// note: this method is primarily for logging as the actual decision logic is embedded in the bpmn flow
	public void processAAIGenericVnfQuery(DelegateExecution execution) {
		if (execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 404 &&
			execution.getVariable("CAAIVfMod_vnfId").isEmpty()) {
			logger.debug("New Generic VNF requested and it does not already exist")
		} else if (execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 200 &&
				!execution.getVariable("CAAIVfMod_vnfId").isEmpty()) {
			logger.debug("Adding module to existing Generic VNF")
		} else if (execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 200 &&
				execution.getVariable("CAAIVfMod_vnfId").isEmpty()) {
			logger.debug("Invalid request for new Generic VNF which already exists")
			execution.setVariable("CAAIVfMod_queryGenericVnfResponse",
				"Invalid request for new Generic VNF which already exists, Vnf Name=" +
				 execution.getVariable("CAAIVfMod_vnfName"))
		} else { // execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 404 &&
			   // !execution.getVariable("CAAIVfMod_vnfId").isEmpty())
			logger.debug("Invalid request for Add-on Module requested for non-existant Generic VNF")
			execution.setVariable("CAAIVfMod_createVfModuleResponse",
				"Invalid request for Add-on Module requested for non-existant Generic VNF, VNF Id=" +
				execution.getVariable("CAAIVfMod_vnfId"))
		}
	}

	// construct and send a PUT request to A&AI to create a new Generic VNF
	// note: to get here, the vnf-id in the original CREATE_VF_MODULE request was absent or ""
	public void createGenericVnf(DelegateExecution execution) {
		// TBD - is this how we want to generate the Id for the new Generic VNF?
		def newVnfId = UUID.randomUUID().toString()
		execution.setVariable("CAAIVfMod_vnfId",newVnfId)

        GenericVnf genericVnf = new GenericVnf()
        genericVnf.setVnfId(newVnfId)
        genericVnf.setVnfName(execution.getVariable("CAAIVfMod_vnfName"))
        genericVnf.setVnfType(execution.getVariable("CAAIVfMod_vnfType"))
        genericVnf.setServiceId(execution.getVariable("CAAIVfMod_serviceId"))
        genericVnf.setOrchestrationStatus(OrchestrationStatus.ACTIVE.toString())
        genericVnf.setModelInvariantId(execution.getVariable("CAAIVfMod_vnfPersonaId"))
        genericVnf.setModelVersionId(execution.getVariable("CAAIVfMod_vnfPersonaVer"))

		try {
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(newVnfId))
            getAAIClient().create(uri,genericVnf)
			execution.setVariable("CAAIVfMod_createGenericVnfResponseCode", 201)
			execution.setVariable("CAAIVfMod_createGenericVnfResponse", "Vnf Created")
		} catch (Exception ex) {
			logger.debug("Exception occurred while executing AAI PUT: {}", ex.getMessage(), ex)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in createGenericVnf.")
		}
	}

	// construct and send a PUT request to A&AI to create a Base or Add-on VF Module
	public void createVfModule(DelegateExecution execution, Boolean isBaseModule) {
		// TBD - is this how we want to generate the Id for the new (Base) VF Module?

		// Generate the new VF Module ID here if it has not been provided by the parent process
		def newModuleId = execution.getVariable('newVfModuleId')
		if (newModuleId == null || newModuleId.isEmpty()) {
			newModuleId = UUID.randomUUID().toString()
		}
		
		String vnfId = execution.getVariable("CAAIVfMod_vnfId")
		
		int moduleIndex = 0
		if (!isBaseModule) {
            GenericVnf aaiVnfResponse = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
            moduleIndex = getLowestUnusedVfModuleIndexFromAAIVnfResponse(aaiVnfResponse,execution)
		}

		// if we get to this point, we may be about to create the Vf Module,
		// add rollback information about the Generic VNF for this base/add-on module
		def rollbackData = execution.getVariable("RollbackData")
		if (rollbackData == null) {
			rollbackData = new RollbackData();
		}
		rollbackData.put("VFMODULE", "vnfId", execution.getVariable("CAAIVfMod_vnfId"))
		rollbackData.put("VFMODULE", "vnfName", execution.getVariable("CAAIVfMod_vnfName"))
		rollbackData.put("VFMODULE", "isBaseModule", isBaseModule.toString())
		execution.setVariable("RollbackData", rollbackData)
		logger.debug("RollbackData:" + rollbackData)

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule()
        vfModule.setVfModuleId(newModuleId)
        vfModule.setVfModuleName(execution.getVariable("CAAIVfMod_moduleName"))
        vfModule.setModelInvariantId(execution.getVariable("CAAIVfMod_personaId"))
        vfModule.setModelVersionId(execution.getVariable("CAAIVfMod_personaVer"))
        vfModule.setModelCustomizationId(execution.getVariable("CAAIVfMod_modelCustomizationId"))
        vfModule.setIsBaseVfModule(isBaseModule)
        vfModule.setOrchestrationStatus(OrchestrationStatus.PENDING_CREATE.toString())
        vfModule.setModuleIndex(moduleIndex)

		try {

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(newModuleId))
            getAAIClient().create(uri,vfModule)
            def statusCode = 201
			execution.setVariable("CAAIVfMod_createVfModuleResponseCode", statusCode)
			execution.setVariable("CAAIVfMod_createVfModuleResponse", "Vf Module Created")


			// the base or add-on VF Module was successfully created,
			// add the module name to the rollback data and the response
			if (isOneOf(statusCode, 200, 201)) {
				rollbackData.put("VFMODULE", "vfModuleId", newModuleId)
				rollbackData.put("VFMODULE", "vfModuleName", execution.getVariable("CAAIVfMod_moduleName"))
				execution.setVariable("RollbackData", rollbackData)
				logger.debug("RollbackData:" + rollbackData)

				String responseOut = ""

				String isVidRequest = execution.getVariable("isVidRequest")
				def moduleIndexString = String.valueOf(moduleIndex)
				if (isBaseModule && (isVidRequest == null || "false".equals(isVidRequest))) {

					responseOut = """<CreateAAIVfModuleResponse>
											<vnf-id>${MsoUtils.xmlEscape(execution.getVariable("CAAIVfMod_vnfId"))}</vnf-id>
											<vf-module-id>${MsoUtils.xmlEscape(newModuleId)}</vf-module-id>
											<vf-module-index>${MsoUtils.xmlEscape(moduleIndexString)}</vf-module-index>
										</CreateAAIVfModuleResponse>""" as String
				}
				else {
					responseOut = """<CreateAAIVfModuleResponse>
											<vnf-name>${MsoUtils.xmlEscape(execution.getVariable("CAAIVfMod_vnfNameFromAAI"))}</vnf-name>
											<vnf-id>${MsoUtils.xmlEscape(execution.getVariable("CAAIVfMod_vnfId"))}</vnf-id>
											<vf-module-id>${MsoUtils.xmlEscape(newModuleId)}</vf-module-id>
											<vf-module-index>${MsoUtils.xmlEscape(moduleIndexString)}</vf-module-index>
										</CreateAAIVfModuleResponse>""" as String
				}
				
				execution.setVariable("CreateAAIVfModuleResponse", responseOut)
				logger.debug("CreateAAIVfModuleResponse:" + System.lineSeparator()+responseOut)
				logger.debug("CreateAAIVfModule Response /n " + responseOut)
			}
		} catch (Exception ex) {
            execution.setVariable("CAAIVfMod_createVfModuleResponseCode", 500)
            execution.setVariable("CAAIVfMod_createVfModuleResponse", ex.getMessage())
			logger.debug("Exception occurred while executing AAI PUT:" + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in createVfModule.")
		}
	}

    private int getLowestUnusedVfModuleIndexFromAAIVnfResponse(GenericVnf genericVnf,DelegateExecution execution){
        String personaModelId = execution.getVariable("CAAIVfMod_personaId")
        if(genericVnf!=null && genericVnf.getVfModules()!= null &&
                !genericVnf.getVfModules().getVfModule().isEmpty()){
            Set<Integer> moduleIndices = new TreeSet<>()
            for(org.onap.aai.domain.yang.VfModule vfModule in genericVnf.getVfModules().getVfModule()){
                if(genericVnf.getModelInvariantId()==null){
                    if(vfModule.getPersonaModelVersion().equals(personaModelId) && vfModule.getModuleIndex()!=null)
                        moduleIndices.add(vfModule.getModuleIndex())
                }else{
                    if(vfModule.getModelInvariantId().equals(personaModelId) && vfModule.getModuleIndex()!=null)
                        moduleIndices.add(vfModule.getModuleIndex())
                }
            }
            for(i in 0..moduleIndices.size()-1){
                if(moduleIndices.getAt(i) != i){
                    return i;
                }
            }
            return moduleIndices.size()
        }else{
            return 0
        }

    }

	// parses the output from the result from queryAAIForGenericVnf() to determine if the vf-module-name
	// requested for an Add-on VF Module does not already exist for the specified Generic VNF
	// also retrieves VNF name from AAI response for existing VNF
	public void parseForAddOnModule(DelegateExecution execution) {
		GenericVnf genericVnf = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
		def vnfNameFromAAI = genericVnf.getVnfName()
		execution.setVariable("CAAIVfMod_vnfNameFromAAI", vnfNameFromAAI)
		logger.debug("Obtained vnf-name from AAI for existing VNF: " + vnfNameFromAAI)
		def newModuleName = execution.getVariable("CAAIVfMod_moduleName")
		logger.debug("VF Module to be added: " + newModuleName)
		execution.setVariable("CAAIVfMod_moduleExists", false)
		if (genericVnf !=null && genericVnf.getVfModules()!=null && !genericVnf.getVfModules().getVfModule().isEmpty()) {
            def qryModuleList =  genericVnf.getVfModules().getVfModule()
			logger.debug("Existing VF Module List: " + qryModuleList)
			for (org.onap.aai.domain.yang.VfModule qryModule : qryModuleList) {
                def qryModuleName = qryModule.getVfModuleName()
				if (newModuleName.equals(qryModuleName)) {
					// a module with the requested name already exists - failure
					logger.debug("VF Module " + qryModuleName + " already exists for Generic VNF " + vnfNameFromAAI)
					execution.setVariable("CAAIVfMod_moduleExists", true)
					execution.setVariable("CAAIVfMod_parseModuleResponse",
						"VF Module " + qryModuleName + " already exists for Generic VNF " + vnfNameFromAAI)
					break
				}
			}
		}
		if (execution.getVariable("CAAIVfMod_moduleExists") == false) {
			logger.debug("VF Module " + execution.getVariable("CAAIVfMod_moduleName") + " does not exist for Generic VNF " + vnfNameFromAAI)
			execution.setVariable("CAAIVfMod_parseModuleResponse",
				"VF Module " + newModuleName + " does not exist for Generic VNF " + vnfNameFromAAI)
		}
	}

	// parses the output from the result from queryAAIForGenericVnf() to determine if the vf-module-name
	// requested for an Add-on VF Module does not already exist for the specified Generic VNF;
	// also retrieves VNF name from AAI response for existing VNF
	public void parseForBaseModule(DelegateExecution execution) {
        GenericVnf genericVnf = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
		def vnfNameFromAAI = genericVnf.getVnfName()
		execution.setVariable("CAAIVfMod_vnfNameFromAAI", vnfNameFromAAI)
		logger.debug("Obtained vnf-name from AAI for existing VNF: " + vnfNameFromAAI)
		def newModuleName = execution.getVariable("CAAIVfMod_moduleName")
		logger.debug("VF Module to be added: " + newModuleName)
		def qryModuleList = genericVnf !=null ? genericVnf.getVfModules():null;
		execution.setVariable("CAAIVfMod_moduleExists", false)
		if (qryModuleList != null && !qryModuleList.getVfModule().isEmpty()) {
            def qryModules = qryModuleList.getVfModule()
			logger.debug("Existing VF Module List: " + qryModules)
			for (org.onap.aai.domain.yang.VfModule qryModule : qryModules) {
				if (newModuleName.equals(qryModule.getVfModuleName())) {
					// a module with the requested name already exists - failure
					logger.debug("VF Module " + qryModule.getVfModuleName() + " already exists for Generic VNF " + vnfNameFromAAI)
					execution.setVariable("CAAIVfMod_baseModuleConflict", true)
					execution.setVariable("CAAIVfMod_parseModuleResponse",
						"VF Module " + qryModule.getVfModuleName() + " already exists for Generic VNF " + vnfNameFromAAI)
					break
				}
			}
		}

		if (qryModuleList != null && !qryModuleList.getVfModule().isEmpty() && !execution.getVariable("CAAIVfMod_baseModuleConflict")) {
			def qryModules = qryModuleList.getVfModule()
			for (org.onap.aai.domain.yang.VfModule qryModule : qryModules) {
				if (qryModule.isBaseVfModule) {
					// a base module already exists in this VNF - failure
					logger.debug("Base VF Module already exists for Generic VNF " + vnfNameFromAAI)
					execution.setVariable("CAAIVfMod_baseModuleConflict", true)
					execution.setVariable("CAAIVfMod_parseModuleResponse",
						"Base VF Module already exists for Generic VNF " + vnfNameFromAAI)
					break
				}
			}

		}
		if (execution.getVariable("CAAIVfMod_baseModuleConflict") == false) {
			logger.debug("VF Module " + execution.getVariable("CAAIVfMod_moduleName") + " does not exist for Generic VNF " + execution.getVariable("CAAIVfMod_vnfNameFromAAI"))
			execution.setVariable("CAAIVfMod_parseModuleResponse",
				"VF Module " + newModuleName + " does not exist for Generic VNF " + vnfNameFromAAI)
		}
	}

	// generates a WorkflowException when the A&AI query returns a response code other than 200 or 404
	public void handleAAIQueryFailure(DelegateExecution execution) {
		logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				"Error occurred attempting to query AAI, Response Code " + execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") + ", Error Response " + execution.getVariable("CAAIVfMod_queryGenericVnfResponse"),
				"BPMN", ErrorCode.UnknownError.getValue());
		int code = execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode")
		exceptionUtil.buildAndThrowWorkflowException(execution, code, "Error occurred attempting to query AAI")

	}

	// generates a WorkflowException if
	//		- the A&AI Generic VNF PUT returns a response code other than 200 or 201
	//		- the requested Generic VNF already exists but vnf-id == null
	//		- the requested Generic VNF does not exist but vnf-id != null
	// 		- the A&AI VF Module PUT returns a response code other than 200 or 201
	//		- the requested VF Module already exists for the Generic VNF
	public void handleCreateVfModuleFailure(DelegateExecution execution) {
		def errorCode
		def errorResponse
		if (execution.getVariable("CAAIVfMod_createGenericVnfResponseCode") != null &&
				!isOneOf(execution.getVariable("CAAIVfMod_createGenericVnfResponseCode"), 200, 201)) {
			logger.debug("Failure creating Generic VNF: " + execution.getVariable("CAAIVfMod_createGenericVnfResponse"))
			errorResponse = execution.getVariable("CAAIVfMod_createGenericVnfResponse")
			errorCode = 5000
		} else if (execution.getVariable("CAAIVfMod_queryGenericVnfResponse") != null &&
				execution.getVariable("CAAIVfMod_newGenericVnf") == true) {
			// attempted to create a Generic VNF that already exists but vnf-id == null
			logger.debug(execution.getVariable("CAAIVfMod_queryGenericVnfResponse"))
			errorResponse = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
			errorCode = 1002
		} else if (execution.getVariable("CAAIVfMod_queryGenericVnfResponseCode") == 404 &&
				execution.getVariable("CAAIVfMod_newGenericVnf") == false) {
			// attempted to create a Generic VNF where vnf-name does not exist but vnf-id != null
			logger.debug(execution.getVariable("CAAIVfMod_queryGenericVnfResponse"))
			errorResponse = execution.getVariable("CAAIVfMod_queryGenericVnfResponse")
			errorCode = 1002
		} else if (execution.getVariable("CAAIVfMod_createVfModuleResponseCode") != null) {
			logger.debug("Failed to add VF Module: " + execution.getVariable("CAAIVfMod_createVfModuleResponse"))
			errorResponse = execution.getVariable("CAAIVfMod_createVfModuleResponse")
			errorCode = 5000
		} else if (execution.getVariable("CAAIVfMod_moduleExists") == true) {
			logger.debug("Attempting to add VF Module that already exists: " + execution.getVariable("CAAIVfMod_parseModuleResponse"))
			errorResponse = execution.getVariable("CAAIVfMod_parseModuleResponse")
			errorCode = 1002
		} else if (execution.getVariable("CAAIVfMod_baseModuleConflict") == true) {
			logger.debug("Attempting to add Base VF Module to VNF that already has a Base VF Module: " + execution.getVariable("CAAIVfMod_parseModuleResponse"))
			errorResponse = execution.getVariable("CAAIVfMod_parseModuleResponse")
			errorCode = 1002
		} else {
			// if the responses get populated corerctly, we should never get here
			errorResponse = "Unknown error occurred during CreateAAIVfModule flow"
			errorCode = 2000
		}

		logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				"Error occurred during CreateAAIVfModule flow", "BPMN",
				ErrorCode.UnknownError.getValue(), errorResponse);
		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode, errorResponse)
		logger.debug("Workflow exception occurred in CreateAAIVfModule: " + errorResponse)
	}
}
