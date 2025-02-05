/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.tasks;

import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.logging.filter.base.ONAPComponentsList;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.utils.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ExecuteBuildingBlockRainyDay {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteBuildingBlockRainyDay.class);
    public static final String HANDLING_CODE = "handlingCode";
    public static final String ROLLBACK_TARGET_STATE = "rollbackTargetState";
    public static final String RAINY_DAY_SERVICE_TYPE = "rainyDayServiceType";
    public static final String RAINY_DAY_VNF_TYPE = "rainyDayVnfType";
    public static final String RAINY_DAY_VNF_NAME = "rainyDayVnfName";

    @Autowired
    private ExceptionBuilder exceptionBuilder;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private RequestsDbClient requestDbclient;
    private static final String ASTERISK = "*";

    @Autowired
    private Environment environment;
    protected String retryDurationPath = "mso.rainyDay.retryDurationMultiplier";
    protected String defaultCode = "mso.rainyDay.defaultCode";
    protected String maxRetries = "mso.rainyDay.maxRetries";

    public void setRetryTimer(DelegateExecution execution) {
        try {
            int retryDurationMult = Integer.parseInt(this.environment.getProperty(retryDurationPath));
            int retryCount = (int) execution.getVariable("retryCount");
            int retryTimeToWait = (int) Math.pow(retryDurationMult, retryCount) * 10;
            String RetryDuration = "PT" + retryTimeToWait + "S";
            execution.setVariable("RetryDuration", RetryDuration);
        } catch (Exception e) {
            logger.error("Exception occurred", e);
            throw new BpmnError("Unknown error incrementing retry counter");
        }
    }

    public void queryRainyDayTable(DelegateExecution execution, boolean primaryPolicy) {
        try {
            ExecuteBuildingBlock ebb = (ExecuteBuildingBlock) execution.getVariable("buildingBlock");
            String bbName = ebb.getBuildingBlock().getBpmnFlowName();
            GeneralBuildingBlock gBBInput = (GeneralBuildingBlock) execution.getVariable("gBBInput");
            String requestId = (String) execution.getVariable("mso-request-id");
            Map<ResourceKey, String> lookupKeyMap = (Map<ResourceKey, String>) execution.getVariable("lookupKeyMap");
            String serviceType = ASTERISK;
            boolean aLaCarte = (boolean) execution.getVariable("aLaCarte");
            boolean suppressRollback = (boolean) execution.getVariable("suppressRollback");
            WorkflowException workflowException = (WorkflowException) execution.getVariable("WorkflowException");
            if (workflowException != null) {
                execution.setVariable("WorkflowExceptionErrorMessage", workflowException.getErrorMessage());
            } else {
                logger.debug("WorkflowException is null, unable to set WorkflowExceptionErrorMessage");
            }
            String handlingCode = "";

            if (suppressRollback) {
                handlingCode = "Abort";
            } else {
                try {
                    if (gBBInput.getCustomer() != null && gBBInput.getCustomer().getServiceSubscription() != null) {
                        serviceType = gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0)
                                .getModelInfoServiceInstance().getServiceType();
                    }
                    if (serviceType == null || serviceType.isEmpty()) {
                        serviceType = ASTERISK;
                    }
                } catch (Exception ex) {
                    // keep default serviceType value
                    logger.error("Exception in serviceType retrieval", ex);
                }
                execution.setVariable(RAINY_DAY_SERVICE_TYPE, serviceType);
                String vnfType = ASTERISK;
                String vnfName = ASTERISK;
                try {
                    if (gBBInput.getCustomer() != null && gBBInput.getCustomer().getServiceSubscription() != null) {
                        for (GenericVnf vnf : gBBInput.getCustomer().getServiceSubscription().getServiceInstances()
                                .get(0).getVnfs()) {
                            if (vnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
                                vnfType = vnf.getVnfType();
                                vnfName = vnf.getVnfName();
                            }
                        }
                    } else {
                        for (GenericVnf vnf : gBBInput.getServiceInstance().getVnfs()) {
                            if (vnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
                                vnfType = vnf.getVnfType();
                                vnfName = vnf.getVnfName();
                            }
                        }
                    }
                } catch (Exception ex) {
                    // keep default vnfType value
                    logger.error("Exception in vnfType retrieval", ex);
                }
                execution.setVariable(RAINY_DAY_VNF_TYPE, vnfType);
                execution.setVariable(RAINY_DAY_VNF_NAME, vnfName);
                String errorCode = ASTERISK;
                if (workflowException != null) {
                    errorCode = "" + workflowException.getErrorCode();
                } else {
                    logger.debug("WorkflowException is null, unable to get error code");
                }

                try {
                    errorCode = "" + (String) execution.getVariable("WorkflowExceptionCode");
                } catch (Exception ex) {
                    // keep default errorCode value
                    logger.error("Exception in errorCode retrieval", ex);
                }

                String workStep = ASTERISK;
                try {
                    workStep = workflowException.getWorkStep();
                } catch (Exception ex) {
                    // keep default workStep value
                    logger.error("Exception in workStep retrieval", ex);
                }

                String errorMessage = ASTERISK;
                try {
                    errorMessage = workflowException.getErrorMessage();
                } catch (Exception ex) {
                    // keep default workStep value
                    logger.error("Exception in errorMessage retrieval", ex);
                }

                String serviceRole = ASTERISK;
                try {
                    if (gBBInput.getCustomer() != null && gBBInput.getCustomer().getServiceSubscription() != null) {
                        serviceRole = gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0)
                                .getModelInfoServiceInstance().getServiceRole();
                    }
                    if (serviceRole == null || serviceRole.isEmpty()) {
                        serviceRole = ASTERISK;
                    }
                } catch (Exception ex) {
                    // keep default serviceRole value
                }

                RainyDayHandlerStatus rainyDayHandlerStatus;
                rainyDayHandlerStatus = catalogDbClient.getRainyDayHandlerStatus(bbName, serviceType, vnfType,
                        errorCode, workStep, errorMessage, serviceRole);

                if (rainyDayHandlerStatus == null) {
                    handlingCode = "Abort";
                } else {
                    if (primaryPolicy) {
                        handlingCode = rainyDayHandlerStatus.getPolicy();
                    } else {
                        handlingCode = rainyDayHandlerStatus.getSecondaryPolicy();
                    }
                }
                if (!primaryPolicy) {
                    try {
                        InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
                        request.setRetryStatusMessage("Retries have been exhausted.");
                        requestDbclient.updateInfraActiveRequests(request);
                    } catch (Exception ex) {
                        logger.error("Failed to update Request Db Infra Active Requests with Retry Status", ex);
                    }
                }
                if ("RollbackToAssigned".equals(handlingCode) && !aLaCarte) {
                    handlingCode = "Rollback";
                }
                if (handlingCode.startsWith("Rollback")) {
                    String targetState = "";
                    if ("RollbackToAssigned".equalsIgnoreCase(handlingCode)) {
                        targetState = Status.ROLLED_BACK_TO_ASSIGNED.toString();
                    } else if ("RollbackToCreated".equalsIgnoreCase(handlingCode)
                            || "RollbackToCreatedNoConfiguration".equalsIgnoreCase(handlingCode)) {
                        targetState = Status.ROLLED_BACK_TO_CREATED.toString();
                    } else {
                        targetState = Status.ROLLED_BACK.toString();
                    }
                    execution.setVariable(ROLLBACK_TARGET_STATE, targetState);
                    logger.debug("Rollback target state is: {}", targetState);
                }
            }
            logger.debug("RainyDayHandler Status Code is: {}", handlingCode);
            execution.setVariable(HANDLING_CODE, handlingCode);
        } catch (Exception e) {
            String code = this.environment.getProperty(defaultCode);
            logger.error("Failed to determine RainyDayHandler Status. Seting handlingCode = {}", code, e);
            execution.setVariable(HANDLING_CODE, code);
        }
        try {
            int envMaxRetries = Integer.parseInt(this.environment.getProperty(maxRetries));
            execution.setVariable("maxRetries", envMaxRetries);
        } catch (Exception ex) {
            logger.error("Could not read maxRetries from config file. Setting max to 5 retries", ex);
            execution.setVariable("maxRetries", 5);
        }
    }

    public void setHandlingStatusSuccess(DelegateExecution execution) {
        execution.setVariable(HANDLING_CODE, "Success");
    }

    public void updateExtSystemErrorSource(DelegateExecution execution) {
        try {
            String requestId = (String) execution.getVariable("mso-request-id");
            WorkflowException exception = (WorkflowException) execution.getVariable("WorkflowException");
            if (exception == null) {
                String errorMessage = (String) execution.getVariable("WorkflowExceptionMessage");
                exception =
                        exceptionBuilder.buildWorkflowException(execution, 500, errorMessage, ONAPComponents.EXTERNAL);
            }
            ONAPComponentsList extSystemErrorSource = exception.getExtSystemErrorSource();
            InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
            Boolean isRollbackFailure = (Boolean) execution.getVariable("isRollback");

            if (isRollbackFailure == null) {
                isRollbackFailure = false;
            }

            if (extSystemErrorSource != null) {
                String extSystemErrorSourceString = extSystemErrorSource.toString();
                if (isRollbackFailure) {
                    logger.debug("Updating extSystemErrorSource for isRollbackFailure to {} for request: {}",
                            extSystemErrorSourceString, requestId);
                    request.setRollbackExtSystemErrorSource(extSystemErrorSourceString);
                } else {
                    logger.debug("Updating extSystemErrorSource to {} for request: {}", extSystemErrorSourceString,
                            requestId);
                    request.setExtSystemErrorSource(extSystemErrorSourceString);
                }
            } else if (isRollbackFailure) {
                logger.debug(
                        "rollbackExtSystemErrorSource is null for isRollbackFailure. Setting rollbackExtSystemErrorSource to UNKNOWN");
                request.setRollbackExtSystemErrorSource(Components.UNKNOWN.toString());
            } else {
                logger.debug("extSystemErrorSource is null. Setting extSystemErrorSource to UNKNOWN");
                request.setExtSystemErrorSource(Components.UNKNOWN.toString());
            }

            request.setLastModifiedBy("CamundaBPMN");
            requestDbclient.updateInfraActiveRequests(request);
        } catch (Exception e) {
            logger.error("Failed to update Request db with extSystemErrorSource or rollbackExtSystemErrorSource: ", e);
        }
    }

}
