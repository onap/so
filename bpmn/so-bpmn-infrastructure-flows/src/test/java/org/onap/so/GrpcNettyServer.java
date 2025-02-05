/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.annotation.PostConstruct;
import org.junit.Rule;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GrpcNettyServer extends BluePrintProcessingServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcNettyServer.class);

    @Value("${cds.endpoint}")
    private String host;

    @Value("${cds.port}")
    private String port;

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final CountDownLatch allRequestsDelivered = new CountDownLatch(1);
    private final AtomicReference<StreamObserver<ExecutionServiceOutput>> responseObserverRef = new AtomicReference<>();
    private final List<ExecutionServiceInput> detailedMessages = new ArrayList<>();

    @PostConstruct
    public void start() throws IOException {

        final BluePrintProcessingServiceImplBase blueprintPrcessorImpl = new BluePrintProcessingServiceImplBase() {
            @Override
            public StreamObserver<ExecutionServiceInput> process(
                    StreamObserver<ExecutionServiceOutput> responseObserver) {

                responseObserverRef.set(responseObserver);

                StreamObserver<ExecutionServiceInput> requestObserver = new StreamObserver<ExecutionServiceInput>() {
                    @Override
                    public void onNext(ExecutionServiceInput message) {
                        detailedMessages.add(message);
                        logger.info("Message received: {}", message);
                        ExecutionServiceOutput executionServiceOutput = ExecutionServiceOutput.newBuilder()
                                .setActionIdentifiers(message.getActionIdentifiers())
                                .setStatus(Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_EXECUTED).build())
                                .build();

                        responseObserverRef.get().onNext(executionServiceOutput);
                        logger.info("Message sent: {}", executionServiceOutput);
                    }

                    @Override
                    public void onError(Throwable t) {
                        responseObserverRef.get().onError(t);
                    }

                    @Override
                    public void onCompleted() {
                        allRequestsDelivered.countDown();
                        responseObserverRef.get().onCompleted();
                    }
                };

                return requestObserver;
            }
        };
        grpcCleanup.register(ServerBuilder.forPort(Integer.valueOf(port)).directExecutor()
                .addService(blueprintPrcessorImpl).build().start());

    }

    public List<ExecutionServiceInput> getDetailedMessages() {
        return this.detailedMessages;
    }

    public void resetList() {
        detailedMessages.clear();
    }

}
