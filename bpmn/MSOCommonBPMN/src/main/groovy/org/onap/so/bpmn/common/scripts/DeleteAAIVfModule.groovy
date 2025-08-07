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

import org.onap.so.logger.LoggingAnchor
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DeleteAAIVfModule extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( DeleteAAIVfModule.class);

	def Prefix="DAAIVfMod_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
    private MsoUtils utils = new MsoUtils()
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		execution.setVariable("DAAIVfMod_vnfId",null)
		execution.setVariable("DAAIVfMod_vnfName",null)
		execution.setVariable("DAAIVfMod_genVnfRsrcVer",null)
		execution.setVariable("DAAIVfMod_vfModuleId",null)
		execution.setVariable("DAAIVfMod_vfModRsrcVer",null)
		execution.setVariable("DAAIVfMod_moduleExists",false)
		execution.setVariable("DAAIVfMod_isBaseModule", false)
		execution.setVariable("DAAIVfMod_isLastModule", false)

		// DeleteAAIVfModule workflow response variable placeholders
		execution.setVariable("DAAIVfMod_queryGenericVnfResponseCode",null)
		execution.setVariable("DAAIVfMod_queryGenericVnfResponse","")
		execution.setVariable("DAAIVfMod_parseModuleResponse","")
		execution.setVariable("DAAIVfMod_deleteGenericVnfResponseCode",null)
		execution.setVariable("DAAIVfMod_deleteGenericVnfResponse","")
		execution.setVariable("DAAIVfMod_deleteVfModuleResponseCode",null)
		execution.setVariable("DAAIVfMod_deleteVfModuleResponse","")

	}

	// parse the incoming DELETE_VF_MODULE request and store the Generic Vnf
	// and Vf Module Ids in the flow DelegateExecution
	public void preProcessRequest(DelegateExecution execution) {
		def xml = execution.getVariable("DeleteAAIVfModuleRequest")
		logger.debug("DeleteAAIVfModule Request: " + xml)
		logger.debug("input request xml:" + xml)
		initProcessVariables(execution)
		def vnfId = utils.getNodeText(xml,"vnf-id")
		def vfModuleId = utils.getNodeText(xml,"vf-module-id")
		execution.setVariable("DAAIVfMod_vnfId", vnfId)
		execution.setVariable("DAAIVfMod_vfModuleId", vfModuleId)
	}

	// send a GET request to AA&I to retrieve the Generic Vnf/Vf Module information based on a Vnf Id
	// expect a 200 response with the information in the response body or a 404 if the Generic Vnf does not exist
	public void queryAAIForGenericVnf(DelegateExecution execution) {

		def vnfId = execution.getVariable("DAAIVfMod_vnfId")

		try {
			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
			uri.depth(Depth.ONE)
            Optional<GenericVnf> genericVnf = getAAIClient().get(GenericVnf.class, uri)

            if(genericVnf.isPresent()) {
                execution.setVariable("DAAIVfMod_queryGenericVnfResponseCode", 200)
                execution.setVariable("DAAIVfMod_queryGenericVnfResponse", genericVnf.get())

            }else{
                execution.setVariable("DAAIVfMod_queryGenericVnfResponseCode", 404)
                execution.setVariable("DAAIVfMod_queryGenericVnfResponse", "Vnf Not Found!")
            }

		} catch (Exception ex) {
			logger.debug("Exception occurred while executing AAI GET:" + ex.getMessage())
			execution.setVariable("DAAIVfMod_queryGenericVnfResponse", "AAI GET Failed:" + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during queryAAIForGenericVnf")
		}
	}

	// construct and send a DELETE request to A&AI to delete a Generic Vnf
	// note: to get here, all the modules associated with the Generic Vnf must already be deleted
	public void deleteGenericVnf(DelegateExecution execution) {

		try {
			String vnfId = execution.getVariable("DAAIVfMod_vnfId")
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
            getAAIClient().delete(uri)
			execution.setVariable("DAAIVfMod_deleteGenericVnfResponseCode", 200)
			execution.setVariable("DAAIVfMod_deleteGenericVnfResponse", "Vnf Deleted")
		} catch (Exception ex) {
			logger.debug("Exception occurred while executing AAI DELETE: {}", ex.getMessage(), ex)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during deleteGenericVnf")
		}
	}

	// construct and send a DELETE request to A&AI to delete the Base or Add-on Vf Module
	public void deleteVfModule(DelegateExecution execution) {
		try {
			String vnfId = execution.getVariable("DAAIVfMod_vnfId")
			String vfModuleId = execution.getVariable("DAAIVfMod_vfModuleId")

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId))

            getAAIClient().delete(uri)
			execution.setVariable("DAAIVfMod_deleteVfModuleResponseCode", 200)
			execution.setVariable("DAAIVfMod_deleteVfModuleResponse", "Vf Module Deleted")
		} catch (Exception ex) {
			logger.debug("Exception occurred while executing AAI PUT: {}", ex.getMessage(), ex)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during deleteVfModule")
		}
	}

	// parses the output from the result from queryAAIForGenericVnf() to determine if the Vf Module
	// to be deleted exists for the specified Generic Vnf and if it is the Base Module,
	// there are no Add-on Modules present
	public void parseForVfModule(DelegateExecution execution) {
        GenericVnf genericVnf = execution.getVariable("DAAIVfMod_queryGenericVnfResponse")

		def delModuleId = execution.getVariable("DAAIVfMod_vfModuleId")
		logger.debug("Vf Module to be deleted: " + delModuleId)

        execution.setVariable("DAAIVfMod_genVnfRsrcVer", genericVnf.getResourceVersion())

        execution.setVariable("DAAIVfMod_moduleExists", false)
        execution.setVariable("DAAIVfMod_isBaseModule", false)
        execution.setVariable("DAAIVfMod_isLastModule", false)
        if(genericVnf.getVfModules()!= null && !genericVnf.getVfModules().getVfModule().isEmpty()){
            Optional<org.onap.aai.domain.yang.VfModule> vfModule = genericVnf.getVfModules().getVfModule().stream().
                    filter{ v -> v.getVfModuleId().equals(delModuleId)}.findFirst()
            if(vfModule.isPresent()){
                execution.setVariable("DAAIVfMod_moduleExists", true)
                execution.setVariable("DAAIVfMod_vfModRsrcVer", vfModule.get().getResourceVersion())

                if (vfModule.get().isBaseVfModule  && genericVnf.getVfModules().getVfModule().size() != 1) {
                    execution.setVariable("DAAIVfMod_parseModuleResponse",
                            "Found Vf Module Id " + delModuleId + " for Generic Vnf Id " +
                                    execution.getVariable("DAAIVfMod_vnfId") + ": is Base Module, not Last Module")
                    execution.setVariable("DAAIVfMod_isBaseModule", true)
                } else {
                    if (vfModule.get().isBaseVfModule && genericVnf.getVfModules().getVfModule().size() == 1) {
                        execution.setVariable("DAAIVfMod_parseModuleResponse",
                                "Found Vf Module Id " + delModuleId + " for Generic Vnf Id " +
                                        execution.getVariable("DAAIVfMod_vnfId") + ": is Base Module and Last Module")
                        execution.setVariable("DAAIVfMod_isBaseModule", true)
                        execution.setVariable("DAAIVfMod_isLastModule", true)
                    } else {
                        if (genericVnf.getVfModules().getVfModule().size() == 1) {
                            execution.setVariable("DAAIVfMod_parseModuleResponse",
                                    "Found Vf Module Id " + delModuleId + " for Generic Vnf Id " +
                                            execution.getVariable("DAAIVfMod_vnfId") + ": is Not Base Module, is Last Module")
                            execution.setVariable("DAAIVfMod_isLastModule", true)
                        } else {
                            execution.setVariable("DAAIVfMod_parseModuleResponse",
                                    "Found Vf Module Id " + delModuleId + " for Generic Vnf Id " +
                                            execution.getVariable("DAAIVfMod_vnfId") + ": is Not Base Module and Not Last Module")
                        }
                    }
                }
                logger.debug(execution.getVariable("DAAIVfMod_parseModuleResponse"))
            }
        }
        if (execution.getVariable("DAAIVfMod_moduleExists") == false) { // (execution.getVariable("DAAIVfMod_moduleExists") == false)
            logger.debug("Vf Module Id " + delModuleId + " does not exist for Generic Vnf Id " + execution.getVariable("DAAIVfMod_vnfId"))
            execution.setVariable("DAAIVfMod_parseModuleResponse",
                    "Vf Module Id " + delModuleId + " does not exist for Generic Vnf Id " +
                            execution.getVariable("DAAIVfMod_vnfName"))
        }
	}

	// parses the output from the result from queryAAIForGenericVnf() to determine if the Vf Module
	// to be deleted exists for the specified Generic Vnf and if it is the Base Module,
	// there are no Add-on Modules present
	public void parseForResourceVersion(DelegateExecution execution) {
        GenericVnf genericVnf =  execution.getVariable("DAAIVfMod_queryGenericVnfResponse")
		execution.setVariable("DAAIVfMod_genVnfRsrcVer", genericVnf.getResourceVersion())
		logger.debug("Latest Generic VNF Resource Version: " + genericVnf.getResourceVersion())
	}


	// generates a WorkflowException if the A&AI query returns a response code other than 200
	public void handleAAIQueryFailure(DelegateExecution execution) {
		logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				"Error occurred attempting to query AAI, Response Code " + execution.getVariable("DAAIVfMod_queryGenericVnfResponseCode") + ", Error Response " + execution.getVariable("DAAIVfMod_queryGenericVnfResponse"),
				"BPMN", ErrorCode.UnknownError.getValue());
		def errorCode = 5000
		// set the errorCode to distinguish between a A&AI failure
		// and the Generic Vnf Id not found
		if (execution.getVariable("DAAIVfMod_queryGenericVnfResponseCode") == 404) {
			errorCode = 1002
		}
		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode, execution.getVariable("DAAIVfMod_queryGenericVnfResponse"))
	}

	// generates a WorkflowException if
	//		- the A&AI Vf Module DELETE returns a response code other than 200
	// 		- the Vf Module is a Base Module that is not the last Vf Module
	//		- the Vf Module does not exist for the Generic Vnf
	public void handleDeleteVfModuleFailure(DelegateExecution execution) {
		def errorCode = 2000
		def errorResponse = ""
		if (execution.getVariable("DAAIVfMod_deleteVfModuleResponseCode") != null &&
			execution.getVariable("DAAIVfMod_deleteVfModuleResponseCode") != 200) {
			logger.debug("AAI failure deleting a Vf Module: " + execution.getVariable("DAAIVfMod_deleteVfModuleResponse"))
			errorResponse = execution.getVariable("DAAIVfMod_deleteVfModuleResponse")
			logger.debug("DeleteAAIVfModule - deleteVfModuleResponse" + errorResponse)
			errorCode = 5000
		} else {
			if (execution.getVariable("DAAIVfMod_isBaseModule", true) == true &&
					execution.getVariable("DAAIVfMod_isLastModule") == false) {
				// attempt to delete a Base Module that is not the last Vf Module
				logger.debug(execution.getVariable("DAAIVfMod_parseModuleResponse"))
				errorResponse = execution.getVariable("DAAIVfMod_parseModuleResponse")
				logger.debug("DeleteAAIVfModule - parseModuleResponse" + errorResponse)
				errorCode = 1002
			} else {
				// attempt to delete a non-existant Vf Module
				if (execution.getVariable("DAAIVfMod_moduleExists") == false) {
					logger.debug(execution.getVariable("DAAIVfMod_parseModuleResponse"))
					errorResponse = execution.getVariable("DAAIVfMod_parseModuleResponse")
					logger.debug("DeleteAAIVfModule - parseModuleResponse" + errorResponse)
					errorCode = 1002
				} else {
					// if the responses get populated corerctly, we should never get here
					errorResponse = "Unknown error occurred during DeleteAAIVfModule flow"
				}
			}
		}

		logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				"Error occurred during DeleteAAIVfModule flow", "BPMN",
				ErrorCode.UnknownError.getValue(), errorResponse);
		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode, errorResponse)

	}

	// generates a WorkflowException if
	//		- the A&AI Generic Vnf DELETE returns a response code other than 200
	public void handleDeleteGenericVnfFailure(DelegateExecution execution) {
		logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				"AAI error occurred deleting the Generic Vnf", "BPMN",
				ErrorCode.UnknownError.getValue(),
				execution.getVariable("DAAIVfMod_deleteGenericVnfResponse"));
		exceptionUtil.buildAndThrowWorkflowException(execution, 5000, execution.getVariable("DAAIVfMod_deleteGenericVnfResponse"))
	}
}
