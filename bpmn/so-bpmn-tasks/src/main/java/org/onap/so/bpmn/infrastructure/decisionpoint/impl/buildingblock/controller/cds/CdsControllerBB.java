/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.cds;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Status;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.controller.ControllerPreparable;
import org.onap.so.client.PreconditionFailedException;
import org.onap.so.client.RestPropertiesLoader;
import org.onap.so.client.cds.CDSProcessingClient;
import org.onap.so.client.cds.CDSProcessingListener;
import org.onap.so.client.cds.CDSProperties;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This component is used to run against CDS controller.
 *
 * It's similiar to {@link org.onap.so.client.cds.AbstractCDSProcessingBBUtils} for demo purpose.
 */
@Component
public class CdsControllerBB implements ControllerRunnable<BuildingBlockExecution>, CDSProcessingListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String SUCCESS = "Success";
    private static final String FAILED = "Failed";
    private static final String PROCESSING = "Processing";

    /**
     * indicate exception thrown.
     */
    private static final String EXCEPTION = "Exception";


    private final AtomicReference<String> cdsResponse = new AtomicReference<>();

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Autowired(required = false)
    private List<ControllerPreparable<BuildingBlockExecution>> prepareList;

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> context) {
        return context.getControllerActor().equalsIgnoreCase("cds");
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> context) {
        prepareList.stream().filter(prepare -> prepare.understand(context))
                .forEach(prepare -> prepare.prepare(context));
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> context) {
        constructExecutionServiceInputObject(context.getExecution());
        sendRequestToCDSClient(context.getExecution());
    }

    /**
     * Extracting data from execution object and building the ExecutionServiceInput Object
     *
     * @param execution DelegateExecution object
     */
    public void constructExecutionServiceInputObject(BuildingBlockExecution execution) {
        logger.trace("Start AbstractCDSProcessingBBUtils.preProcessRequest ");

        try {
            AbstractCDSPropertiesBean executionObject =
                    (AbstractCDSPropertiesBean) execution.getVariable("executionObject");

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

            ExecutionServiceInput executionServiceInput =
                    ExecutionServiceInput.newBuilder().setCommonHeader(commonHeader)
                            .setActionIdentifiers(actionIdentifiers).setPayload(struct.build()).build();

            execution.setVariable("executionServiceInput", executionServiceInput);

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * get the executionServiceInput object from execution and send a request to CDS Client and wait for TIMEOUT period
     *
     * @param execution DelegateExecution object
     */
    public void sendRequestToCDSClient(BuildingBlockExecution execution) {

        logger.trace("Start AbstractCDSProcessingBBUtils.sendRequestToCDSClient ");
        try {
            CDSProperties props = RestPropertiesLoader.getInstance().getNewImpl(CDSProperties.class);
            if (props == null) {
                throw new PreconditionFailedException(
                        "No RestProperty.CDSProperties implementation found on classpath, can't create client.");
            }

            ExecutionServiceInput executionServiceInput =
                    (ExecutionServiceInput) execution.getVariable("executionServiceInput");

            try (CDSProcessingClient cdsClient = new CDSProcessingClient(this)) {
                CountDownLatch countDownLatch = cdsClient.sendRequest(executionServiceInput);
                countDownLatch.await(props.getTimeout(), TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                logger.error("Caught exception in sendRequestToCDSClient in AbstractCDSProcessingBBUtils : ", ex);
                Thread.currentThread().interrupt();
            }

            if (cdsResponse != null) {
                String cdsResponseStatus = cdsResponse.get();
                execution.setVariable("CDSStatus", cdsResponseStatus);

                /**
                 * throw CDS failed exception.
                 */
                if (cdsResponseStatus != SUCCESS) {
                    throw new BadResponseException("CDS call failed with status: " + cdsResponseStatus);
                }
            }

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * Get Response from CDS Client
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
        cdsResponse.set(EXCEPTION);
    }
}
