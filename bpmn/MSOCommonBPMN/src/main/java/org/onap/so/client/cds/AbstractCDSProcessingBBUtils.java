/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.ccsdk.apps.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.apps.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.apps.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.so.client.PreconditionFailedException;
import org.onap.so.client.RestPropertiesLoader;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.util.JsonFormat;

import io.grpc.Status;

/**
 * Util class to support Call to CDS client
 *
 */
@Component
public class AbstractCDSProcessingBBUtils implements CDSProcessingListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCDSProcessingBBUtils.class);

    private static final String SUCCESS = "Success";
    private static final String FAILED = "Failed";
    private static final String PROCESSING = "Processing";

    private final AtomicReference<String> cdsResponse = new AtomicReference<>();

    @Autowired
    private ExceptionBuilder exceptionUtil;

    /**
     * Extracting data from execution object and building the ExecutionServiceInput
     * Object
     * 
     * @param execution
     *            DelegateExecution object
     */
    public void constructExecutionServiceInputObject(DelegateExecution execution) {
        logger.trace("Start AbstractCDSProcessingBBUtils.preProcessRequest ");

        try {
            AbstractCDSPropertiesBean executionObject = (AbstractCDSPropertiesBean) execution
                    .getVariable("executionObject");

            String payload = executionObject.getRequestObject();

            CommonHeader commonHeader = CommonHeader.newBuilder().setOriginatorId(executionObject.getOriginatorId())
                    .setRequestId(executionObject.getRequestId()).setSubRequestId(executionObject.getSubRequestId())
                    .build();
            ActionIdentifiers actionIdentifiers = ActionIdentifiers.newBuilder()
                    .setBlueprintName(executionObject.getBlueprintName())
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

            ExecutionServiceInput executionServiceInput = ExecutionServiceInput.newBuilder()
                    .setCommonHeader(commonHeader).setActionIdentifiers(actionIdentifiers).setPayload(struct.build())
                    .build();

            execution.setVariable("executionServiceInput", executionServiceInput);

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * get the executionServiceInput object from execution and send a request to CDS
     * Client and wait for TIMEOUT period
     * 
     * @param execution
     *            DelegateExecution object
     */
    public void sendRequestToCDSClient(DelegateExecution execution) {

        logger.trace("Start AbstractCDSProcessingBBUtils.sendRequestToCDSClient ");
        try {
            CDSProperties props = RestPropertiesLoader.getInstance().getNewImpl(CDSProperties.class);
            if (props == null) {
                throw new PreconditionFailedException(
                        "No RestProperty.CDSProperties implementation found on classpath, can't create client.");
            }

            ExecutionServiceInput executionServiceInput = (ExecutionServiceInput) execution
                    .getVariable("executionServiceInput");

            CDSProcessingListener cdsProcessingListener = new AbstractCDSProcessingBBUtils();

            CDSProcessingClient cdsClient = new CDSProcessingClient(cdsProcessingListener);
            CountDownLatch countDownLatch = cdsClient.sendRequest(executionServiceInput);

            try {
                countDownLatch.await(props.getTimeout(), TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                logger.error("Caught exception in sendRequestToCDSClient in AbstractCDSProcessingBBUtils : ", ex);
            } finally {
                cdsClient.close();
            }

            if (cdsResponse != null) {
                execution.setVariable("CDSStatus", cdsResponse.get());
            }

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * Get Response from CDS Client
     * 
     */
    @Override
    public void onMessage(ExecutionServiceOutput message) {
        logger.info("Received notification from CDS: {}", message);
        EventType eventType = message.getStatus().getEventType();

        switch (eventType) {

        case EVENT_COMPONENT_FAILURE:
            // failed processing with failure
            cdsResponse.set(FAILED);
            break;
        case EVENT_COMPONENT_PROCESSING:
            // still processing
            cdsResponse.set(PROCESSING);
            break;
        case EVENT_COMPONENT_EXECUTED:
            // done with async processing
            cdsResponse.set(SUCCESS);
            break;
        default:
            cdsResponse.set(FAILED);
            break;
        }

    }

    /**
     * On error at CDS, log the error
     */
    @Override
    public void onError(Throwable t) {
        Status status = Status.fromThrowable(t);
        logger.error("Failed processing blueprint {}", status, t);
    }

}
