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
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.logging.filter.base.ONAPComponentsList;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
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
                    ErrorCode.UnknownError.getValue(), msg.toString());
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
                    ErrorCode.UnknownError.getValue(), msg.toString());
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
                    ErrorCode.UnknownError.getValue(), msg.toString());
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
                    ErrorCode.UnknownError.getValue(), msg.toString());
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

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage) {
        String processKey = getProcessKey(execution);
        logger.info("Building a WorkflowException for Subflow");

        WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
        execution.setVariable("WorkflowException", exception);
        execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
        logger.info("Outgoing WorkflowException is {}", exception);
        logger.info("Throwing MSOWorkflowException");
        throw new BpmnError("MSOWorkflowException");
    }

    public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage,
            ONAPComponentsList extSystemErrorSource) {
        String processKey = getProcessKey(execution);
        logger.info("Building a WorkflowException for Subflow");

        WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage, extSystemErrorSource);
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
        String auditListString = (String) execution.getVariable("auditInventoryResult");
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
                        .filter(auditObject -> auditObject.getAaiObjectType().equals(AAIObjectType.VSERVER.typeName())
                                || auditObject.getAaiObjectType().equals(AAIObjectType.L_INTERFACE.typeName()));
                List<AAIObjectAudit> filteredAuditStream =
                        vServerLInterfaceAuditStream.filter(a -> !a.isDoesObjectExist()).collect(Collectors.toList());

                for (AAIObjectAudit object : filteredAuditStream) {
                    if (object.getAaiObjectType().equals(AAIObjectType.L_INTERFACE.typeName())) {
                        LInterface li = objectMapper.getMapper().convertValue(object.getAaiObject(), LInterface.class);
                        errorMessage = errorMessage
                                .append(AAIObjectType.L_INTERFACE.typeName() + " " + li.getInterfaceId() + ", ");
                    } else {
                        Vserver vs = objectMapper.getMapper().convertValue(object.getAaiObject(), Vserver.class);
                        errorMessage =
                                errorMessage.append(AAIObjectType.VSERVER.typeName() + " " + vs.getVserverId() + ", ");
                    }
                }

                if (errorMessage.length() > 0) {
                    errorMessage.setLength(errorMessage.length() - 2);
                    errorMessage = errorMessage.append(".");
                }

            } catch (IOException | BBObjectNotFoundException e) {
                errorMessage = errorMessage.append("process objects in AAI. ");
            }

            if (flowShouldContinue) {
                execution.setVariable("StatusMessage", errorMessage.toString());
            } else {
                WorkflowException exception =
                        new WorkflowException(processKey, 400, errorMessage.toString(), ONAPComponents.SO);
                execution.setVariable("WorkflowException", exception);
                execution.setVariable("WorkflowExceptionErrorMessage", errorMessage.toString());
                logger.info("Outgoing WorkflowException is {}", exception);
                logger.info("Throwing MSOWorkflowException");
                throw new BpmnError("AAIInventoryFailure");
            }

        } else {
            String errorMessage = "Unable to process audit results due to auditInventoryResult being null";
            WorkflowException exception = new WorkflowException(processKey, 400, errorMessage, ONAPComponents.SO);
            execution.setVariable("WorkflowException", exception);
            execution.setVariable("WorkflowExceptionErrorMessage", errorMessage);
            logger.info("Outgoing WorkflowException is {}", exception);
            logger.info("Throwing MSOWorkflowException");
            throw new BpmnError("AAIInventoryFailure");
        }
    }

}
