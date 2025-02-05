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

package org.onap.so.client.exception;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.Vserver;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.logging.filter.base.ONAPComponentsList;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.onap.so.utils.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ExceptionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionBuilder.class);


    protected ExtractPojosForBB getExtractPojosForBB() {
        return new ExtractPojosForBB();
    }

    public void buildAndThrowWorkflowException(BuildingBlockExecution execution, int errorCode, Exception exception) {
        String msg = "Exception in %s.%s ";
        try {
            logger.error("Exception occurred", exception);

            String errorVariable = "Error%s%s";

            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement traceElement : trace) {
                if (!traceElement.getClassName().equals(this.getClass().getName())
                        && !traceElement.getClassName().equals(Thread.class.getName())) {
                    msg = String.format(msg, traceElement.getClassName(), traceElement.getMethodName());
                    String shortClassName =
                            traceElement.getClassName().substring(traceElement.getClassName().lastIndexOf(".") + 1);
                    errorVariable = String.format(errorVariable, shortClassName, traceElement.getMethodName());
                    break;
                }
            }

            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue(), msg);
            execution.setVariable(errorVariable, exception.getMessage());
        } catch (Exception ex) {
            // log trace, allow process to complete gracefully
            logger.error("Exception occurred", ex);
        }

        if (exception.getMessage() != null)
            msg = msg.concat(exception.getMessage());
        buildAndThrowWorkflowException(execution, errorCode, msg);
    }

    public void buildAndThrowWorkflowException(BuildingBlockExecution execution, int errorCode, Exception exception,
            ONAPComponentsList extSystemErrorSource) {
        String msg = "Exception in %s.%s ";
        try {
            logger.error("Exception occurred", exception);

            String errorVariable = "Error%s%s";

            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement traceElement : trace) {
                if (!traceElement.getClassName().equals(this.getClass().getName())
                        && !traceElement.getClassName().equals(Thread.class.getName())) {
                    msg = String.format(msg, traceElement.getClassName(), traceElement.getMethodName());
                    String shortClassName =
                            traceElement.getClassName().substring(traceElement.getClassName().lastIndexOf(".") + 1);
                    errorVariable = String.format(errorVariable, shortClassName, traceElement.getMethodName());
                    break;
                }
            }

            logger.error("{} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue(), msg);
            execution.setVariable(errorVariable, exception.getMessage());
        } catch (Exception ex) {
            // log trace, allow process to complete gracefully
            logger.error("Exception occurred", ex);
        }

        if (exception.getMessage() != null)
            msg = msg.concat(exception.getMessage());
        buildAndThrowWorkflowException(execution, errorCode, msg, extSystemErrorSource);
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, Exception exception) {
        String msg = "Exception in %s.%s ";
        try {
            logger.error("Exception occurred", exception);

            String errorVariable = "Error%s%s";

            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement traceElement : trace) {
                if (!traceElement.getClassName().equals(this.getClass().getName())
                        && !traceElement.getClassName().equals(Thread.class.getName())) {
                    msg = String.format(msg, traceElement.getClassName(), traceElement.getMethodName());
                    String shortClassName =
                            traceElement.getClassName().substring(traceElement.getClassName().lastIndexOf(".") + 1);
                    errorVariable = String.format(errorVariable, shortClassName, traceElement.getMethodName());
                    break;
                }
            }
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue(), msg);
            execution.setVariable(errorVariable, exception.getMessage());
        } catch (Exception ex) {
            // log trace, allow process to complete gracefully
            logger.error("Exception occurred", ex);
        }

        if (exception.getMessage() != null)
            msg = msg.concat(exception.getMessage());
        buildAndThrowWorkflowException(execution, errorCode, msg);
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, Exception exception,
            ONAPComponentsList extSystemErrorSource) {
        String msg = "Exception in %s.%s ";
        try {
            logger.error("Exception occurred", exception);

            String errorVariable = "Error%s%s";

            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement traceElement : trace) {
                if (!traceElement.getClassName().equals(this.getClass().getName())
                        && !traceElement.getClassName().equals(Thread.class.getName())) {
                    msg = String.format(msg, traceElement.getClassName(), traceElement.getMethodName());
                    String shortClassName =
                            traceElement.getClassName().substring(traceElement.getClassName().lastIndexOf(".") + 1);
                    errorVariable = String.format(errorVariable, shortClassName, traceElement.getMethodName());
                    break;
                }
            }
            logger.error("{} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue(), msg);
            execution.setVariable(errorVariable, exception.getMessage());
        } catch (Exception ex) {
            // log trace, allow process to complete gracefully
            logger.error("Exception occurred", ex);
        }

        if (exception.getMessage() != null)
            msg = msg.concat(exception.getMessage());
        buildAndThrowWorkflowException(execution, errorCode, msg, extSystemErrorSource);
    }

    /**
     * @deprecated Please utilize method that specifies where the failure occured
     */
    @Deprecated
    public void buildAndThrowWorkflowException(BuildingBlockExecution execution, int errorCode, String errorMessage) {
        if (execution instanceof DelegateExecutionImpl) {
            buildAndThrowWorkflowException(((DelegateExecutionImpl) execution).getDelegateExecution(), errorCode,
                    errorMessage);
        }
    }

    public void buildAndThrowWorkflowException(BuildingBlockExecution execution, int errorCode, String errorMessage,
            ONAPComponentsList extSystemErrorSource) {
        if (execution instanceof DelegateExecutionImpl) {
            buildAndThrowWorkflowException(((DelegateExecutionImpl) execution).getDelegateExecution(), errorCode,
                    errorMessage, extSystemErrorSource);
        }
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage)
            throws BpmnError {

        buildWorkflowException(execution, errorCode, errorMessage);
        logger.info("Throwing MSOWorkflowException");
        throw new BpmnError("MSOWorkflowException");
    }

    public void buildWorkflowException(DelegateExecution execution, int errorCode, String errorMessage) {
        String processKey = getProcessKey(execution);
        logger.info("Building a WorkflowException");

        WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
        execution.setVariable("WorkflowException", exception);
        execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
        logger.info("Outgoing WorkflowException is {}", exception);
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage,
            ONAPComponentsList extSystemErrorSource) {

        buildWorkflowException(execution, errorCode, errorMessage, extSystemErrorSource);
        logger.info("Throwing MSOWorkflowException");
        throw new BpmnError("MSOWorkflowException");
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage,
            ONAPComponentsList extSystemErrorSource, String workStep) {
        String processKey = getProcessKey(execution);
        logger.info("Building a WorkflowException for Subflow");

        WorkflowException exception =
                new WorkflowException(processKey, errorCode, errorMessage, workStep, extSystemErrorSource);
        execution.setVariable("WorkflowException", exception);
        execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
        logger.info("Outgoing WorkflowException is {}", exception);
        logger.info("Throwing MSOWorkflowException");
        throw new BpmnError("MSOWorkflowException");
    }

    public WorkflowException buildWorkflowException(DelegateExecution execution, int errorCode, String errorMessage,
            ONAPComponentsList extSystemErrorSource) {
        String processKey = getProcessKey(execution);
        logger.info("Building a WorkflowException for Subflow");

        WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage, extSystemErrorSource);
        execution.setVariable("WorkflowException", exception);
        execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
        logger.info("Outgoing WorkflowException is {}", exception);
        return exception;
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, String errorCode, String errorMessage) {
        execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
        throw new BpmnError(errorCode, errorMessage);
    }

    public String getProcessKey(DelegateExecution execution) {
        String testKey = (String) execution.getVariable("testProcessKey");
        if (testKey != null) {
            return testKey;
        }
        return execution.getProcessEngineServices().getRepositoryService()
                .getProcessDefinition(execution.getProcessDefinitionId()).getKey();
    }

    public void processAuditException(DelegateExecutionImpl execution, boolean flowShouldContinue) {
        logger.debug("Processing Audit Results");
        String auditListString = execution.getVariable("auditInventoryResult");
        String processKey = getProcessKey(execution.getDelegateExecution());
        if (auditListString != null) {
            StringBuilder errorMessage = new StringBuilder();
            try {
                ExtractPojosForBB extractPojosForBB = getExtractPojosForBB();
                VfModule module = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
                String cloudRegionId = execution.getGeneralBuildingBlock().getCloudRegion().getLcpCloudRegionId();

                GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
                AAIObjectAuditList auditList =
                        objectMapper.getMapper().readValue(auditListString, AAIObjectAuditList.class);

                errorMessage = errorMessage.append(auditList.getAuditType() + " VF-Module " + module.getVfModuleId()
                        + " failed due to incomplete AAI vserver inventory population after stack "
                        + auditList.getHeatStackName() + " was successfully " + auditList.getAuditType()
                        + "d in cloud region " + cloudRegionId + ". MSO Audit indicates that the following was not "
                        + auditList.getAuditType() + "d in AAI: ");

                Stream<AAIObjectAudit> vServerLInterfaceAuditStream = auditList.getAuditList().stream()
                        .filter(auditObject -> auditObject.getAaiObjectType().equals(Types.VSERVER.typeName())
                                || auditObject.getAaiObjectType().equals(Types.L_INTERFACE.typeName()));
                List<AAIObjectAudit> filteredAuditStream =
                        vServerLInterfaceAuditStream.filter(a -> !a.isDoesObjectExist()).collect(Collectors.toList());

                for (AAIObjectAudit object : filteredAuditStream) {
                    if (object.getAaiObjectType().equals(Types.L_INTERFACE.typeName())) {
                        LInterface li = objectMapper.getMapper().convertValue(object.getAaiObject(), LInterface.class);
                        errorMessage =
                                errorMessage.append(Types.L_INTERFACE.typeName() + " " + li.getInterfaceId() + ", ");
                    } else {
                        Vserver vs = objectMapper.getMapper().convertValue(object.getAaiObject(), Vserver.class);
                        errorMessage = errorMessage.append(Types.VSERVER.typeName() + " " + vs.getVserverId() + ", ");
                    }
                }

                if (errorMessage.length() > 0) {
                    errorMessage.setLength(errorMessage.length() - 2);
                    errorMessage = errorMessage.append(".");
                }

            } catch (IOException | BBObjectNotFoundException e) {
                errorMessage = errorMessage.append("process objects in AAI. ");
                logger.error("Exception occurred in processAuditException", e);
            }

            if (flowShouldContinue) {
                execution.setVariable("StatusMessage", errorMessage.toString());
            } else {
                WorkflowException exception =
                        new WorkflowException(processKey, 400, errorMessage.toString(), ONAPComponents.SO);
                execution.setVariable("WorkflowException", exception);
                execution.setVariable("WorkflowExceptionErrorMessage", errorMessage.toString());
                logger.info("Outgoing WorkflowException is {}", exception);
                logger.info("Throwing AAIInventoryFailure");
                throw new BpmnError("AAIInventoryFailure");
            }

        } else {
            String errorMessage = "Unable to process audit results due to auditInventoryResult being null";
            WorkflowException exception = new WorkflowException(processKey, 400, errorMessage, ONAPComponents.SO);
            execution.setVariable("WorkflowException", exception);
            execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
            logger.info("Outgoing WorkflowException is {}", exception);
            logger.info("Throwing AAIInventoryFailure");
            throw new BpmnError("AAIInventoryFailure");
        }
    }

    public void processOpenstackAdapterException(DelegateExecution execution) {
        StringBuilder workflowExceptionMessage = new StringBuilder();
        logger.debug("Processing Vnf Adapter Exception");
        try {
            String errorMessage = (String) execution.getVariable("openstackAdapterErrorMessage");
            boolean openstackRollbackPollSuccess = (boolean) execution.getVariable("OpenstackPollSuccess");
            boolean rollbackPerformed = (boolean) execution.getVariable("rollbackPerformed");
            boolean openstackRollbackSuccess = (boolean) execution.getVariable("OpenstackRollbackSuccess");
            boolean pollRollbackStatus = (boolean) execution.getVariable("PollRollbackStatus");

            workflowExceptionMessage.append("Exception occured during vnf adapter: " + errorMessage + ".");

            boolean rollbackCompleted = false;
            if (rollbackPerformed) {
                if (openstackRollbackSuccess && !pollRollbackStatus) {
                    rollbackCompleted = true;
                } else if (openstackRollbackSuccess && pollRollbackStatus) {
                    if (openstackRollbackPollSuccess) {
                        rollbackCompleted = true;
                    }
                }
                workflowExceptionMessage
                        .append(" The resource was rollbacked in openstack: " + rollbackCompleted + ".");
            }
        } catch (Exception e) {
            logger.debug("Error while Processing Vnf Adapter Exception", e);
        }
        buildWorkflowException(execution, 500, workflowExceptionMessage.toString(), Components.OPENSTACK);
        throw new BpmnError("MSOWorkflowException");
    }

    public void processSDNCException(DelegateExecution execution) {
        logger.debug("Processing SDNC Exception");
        String errorMessage = "";
        try {
            errorMessage = (String) execution.getVariable("errorMessage");
        } catch (Exception e) {
            logger.debug("Error while Processing SDNC Exception", e);
        }
        buildWorkflowException(execution, 500, errorMessage, ONAPComponents.SDNC);
        throw new BpmnError("MSOWorkflowException");
    }

    public void processInventoryException(DelegateExecution execution) {
        String errorMessage = "";
        logger.debug("Processing Inventory Exception");
        try {
            errorMessage = (String) execution.getVariable("inventoryErrorMessage");
        } catch (Exception e) {
            logger.debug("Error while Processing Inventory Exception", e);
        }
        buildWorkflowException(execution, 500, errorMessage, Components.OPENSTACK);
        throw new BpmnError("MSOWorkflowException");


    }

}
