/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra
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

package org.onap.so.client.cds;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Status;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.PreconditionFailedException;
import org.onap.so.client.RestPropertiesLoader;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Util class to support Call to CDS client
 */
@Component
public class AbstractCDSProcessingBBUtils {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCDSProcessingBBUtils.class);

    private static final String SUCCESS = "Success";
    private static final String FAILED = "Failed";
    private static final String PROCESSING = "Processing";
    private static final String RESPONSE_PAYLOAD = "CDSResponsePayload";
    private static final String CDS_STATUS = "CDSStatus";
    private static final String EXEC_INPUT = "executionServiceInput";
    private static final String EXECUTION_OBJECT = "executionObject";
    private static final String EXCEPTION = "Exception";

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    /**
     * Extracting data from execution object and building the ExecutionServiceInput Object
     *
     * @param execution DelegateExecution object
     */
    public void constructExecutionServiceInputObject(DelegateExecution execution) {
        logger.trace("Start AbstractCDSProcessingBBUtils.preProcessRequest for DelegateExecution object.");

        try {
            AbstractCDSPropertiesBean executionObject =
                    (AbstractCDSPropertiesBean) execution.getVariable(EXECUTION_OBJECT);

            ExecutionServiceInput executionServiceInput = prepareExecutionServiceInput(executionObject);

            execution.setVariable(EXEC_INPUT, executionServiceInput);

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * Extracting data from execution object and building the ExecutionServiceInput Object
     *
     * @param execution BuildingBlockExecution object
     */
    public void constructExecutionServiceInputObject(BuildingBlockExecution execution) {
        logger.trace("Start AbstractCDSProcessingBBUtils.preProcessRequest for BuildingBlockExecution object.");

        try {
            AbstractCDSPropertiesBean executionObject = execution.getVariable(EXECUTION_OBJECT);

            ExecutionServiceInput executionServiceInput = prepareExecutionServiceInput(executionObject);

            execution.setVariable(EXEC_INPUT, executionServiceInput);

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * get the executionServiceInput object from execution and send a request to CDS Client and wait for TIMEOUT period
     *
     * @param execution DelegateExecution object
     */
    public void sendRequestToCDSClient(DelegateExecution execution) {

        logger.trace("Start AbstractCDSProcessingBBUtils.sendRequestToCDSClient for DelegateExecution object.");
        try {
            ExecutionServiceInput executionServiceInput = (ExecutionServiceInput) execution.getVariable(EXEC_INPUT);
            CDSResponse cdsResponse = getCdsResponse(executionServiceInput);
            execution.setVariable(CDS_STATUS, cdsResponse.status);

            if (cdsResponse.payload != null) {
                String payload = JsonFormat.printer().print(cdsResponse.payload);
                execution.setVariable(RESPONSE_PAYLOAD, payload);
            }

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * get the executionServiceInput object from execution and send a request to CDS Client and wait for TIMEOUT period
     *
     * @param execution BuildingBlockExecution object
     */
    public void sendRequestToCDSClient(BuildingBlockExecution execution) {

        logger.trace("Start AbstractCDSProcessingBBUtils.sendRequestToCDSClient for BuildingBlockExecution object.");
        try {
            ExecutionServiceInput executionServiceInput = execution.getVariable(EXEC_INPUT);
            CDSResponse cdsResponse = getCdsResponse(executionServiceInput);
            execution.setVariable(CDS_STATUS, cdsResponse.status);

            if (cdsResponse.payload != null) {
                String payload = JsonFormat.printer().print(cdsResponse.payload);
                execution.setVariable(RESPONSE_PAYLOAD, payload);
            }

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    private CDSResponse getCdsResponse(ExecutionServiceInput executionServiceInput) throws BadResponseException {
        CDSProperties props = RestPropertiesLoader.getInstance().getNewImpl(CDSProperties.class);
        if (props == null) {
            throw new PreconditionFailedException(
                    "No RestProperty.CDSProperties implementation found on classpath, can't create client.");
        }

        CDSResponse cdsResponse = new CDSResponse();

        try (CDSProcessingClient cdsClient = new CDSProcessingClient(new ResponseHandler(cdsResponse))) {
            CountDownLatch countDownLatch = cdsClient.sendRequest(executionServiceInput);
            countDownLatch.await(props.getTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            logger.error("Caught exception in sendRequestToCDSClient in AbstractCDSProcessingBBUtils : ", ex);
            Thread.currentThread().interrupt();
        }

        String cdsResponseStatus = cdsResponse.status;

        /**
         * throw CDS failed exception.
         */
        if (!cdsResponseStatus.equals(SUCCESS)) {
            throw new BadResponseException("CDS call failed with status: " + cdsResponse.status + " and errorMessage: "
                    + cdsResponse.errorMessage);
        }
        return cdsResponse;
    }

    private ExecutionServiceInput prepareExecutionServiceInput(AbstractCDSPropertiesBean executionObject) {
        String payload = executionObject.getRequestObject();

        CommonHeader commonHeader = CommonHeader.newBuilder().setOriginatorId(executionObject.getOriginatorId())
                .setRequestId(executionObject.getRequestId()).setSubRequestId(executionObject.getSubRequestId())
                .build();
        ActionIdentifiers actionIdentifiers =
                ActionIdentifiers.newBuilder().setBlueprintName(executionObject.getBlueprintName())
                        .setBlueprintVersion(executionObject.getBlueprintVersion())
                        .setActionName(executionObject.getActionName()).setMode(executionObject.getMode()).build();

        Builder struct = Struct.newBuilder();
        try {
            JsonFormat.parser().merge(payload, struct);
        } catch (InvalidProtocolBufferException e) {
            logger.error("Failed to parse received message. blueprint({}:{}) for action({}). {}",
                    executionObject.getBlueprintVersion(), executionObject.getBlueprintName(),
                    executionObject.getActionName(), e);
        }

        return ExecutionServiceInput.newBuilder().setCommonHeader(commonHeader).setActionIdentifiers(actionIdentifiers)
                .setPayload(struct.build()).build();
    }

    private class ResponseHandler implements CDSProcessingListener {

        private CDSResponse cdsResponse;

        ResponseHandler(CDSResponse cdsResponse) {
            this.cdsResponse = cdsResponse;
        }

        /**
         * Get Response from CDS Client
         */
        @Override
        public void onMessage(ExecutionServiceOutput message) {
            logger.info("Received notification from CDS: {}", message);
            EventType eventType = message.getStatus().getEventType();

            switch (eventType) {
                case EVENT_COMPONENT_PROCESSING:
                    cdsResponse.status = PROCESSING;
                    break;
                case EVENT_COMPONENT_EXECUTED:
                    cdsResponse.status = SUCCESS;
                    break;
                default:
                    cdsResponse.status = FAILED;
                    cdsResponse.errorMessage = message.getStatus().getErrorMessage();
                    break;
            }
            cdsResponse.payload = message.getPayload();
        }

        /**
         * On error at CDS, log the error
         */
        @Override
        public void onError(Throwable t) {
            Status status = Status.fromThrowable(t);
            logger.error("Failed processing blueprint {}", status, t);
            cdsResponse.status = EXCEPTION;
        }
    }

    private class CDSResponse {

        String status;
        String errorMessage;
        Struct payload;

        @Override
        public String toString() {
            return "CDSResponse{" + "status='" + status + '\'' + ", errorMessage='" + errorMessage + '\'' + ", payload="
                    + payload + '}';
        }
    }
}
