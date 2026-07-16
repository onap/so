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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
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
    // Thread-safe: surefire runs the workflow ITs with <parallel>classes</parallel> in a single reused
    // fork, so several tests append to this shared singleton concurrently from gRPC worker threads.
    private final List<ExecutionServiceInput> detailedMessages = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void start() throws IOException {

        final BluePrintProcessingServiceImplBase blueprintPrcessorImpl = new BluePrintProcessingServiceImplBase() {
            @Override
            public StreamObserver<ExecutionServiceInput> process(
                    StreamObserver<ExecutionServiceOutput> responseObserver) {

                responseObserverRef.set(responseObserver);

                // Use the per-stream responseObserver captured here, NOT the shared responseObserverRef:
                // concurrent CDS calls (multiple grpc worker threads) each overwrite the shared ref, so a
                // callback could target another stream's already-completed observer, which under newer
                // gRPC throws "sendHeaders has already been called".
                StreamObserver<ExecutionServiceInput> requestObserver = new StreamObserver<ExecutionServiceInput>() {
                    @Override
                    public void onNext(ExecutionServiceInput message) {
                        detailedMessages.add(message);
                        logger.info("Message received: {}", message);
                        ExecutionServiceOutput executionServiceOutput = ExecutionServiceOutput.newBuilder()
                                .setActionIdentifiers(message.getActionIdentifiers())
                                .setStatus(Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_EXECUTED).build())
                                .build();

                        responseObserver.onNext(executionServiceOutput);
                        logger.info("Message sent: {}", executionServiceOutput);
                    }

                    @Override
                    public void onError(Throwable t) {
                        responseObserver.onError(t);
                    }

                    @Override
                    public void onCompleted() {
                        allRequestsDelivered.countDown();
                        responseObserver.onCompleted();
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

    /**
     * Returns only the messages received for the given mso-request-id. Tests must use this rather than
     * {@link #getDetailedMessages()} for their size/ordering assertions: the backing list is a shared singleton and,
     * under surefire's parallel-classes execution, concurrent workflow ITs interleave their messages into it. Filtering
     * by request id makes each test see exactly its own messages.
     */
    public List<ExecutionServiceInput> getDetailedMessagesForRequestId(String requestId) {
        return this.detailedMessages.stream().filter(eSI -> requestId.equals(eSI.getCommonHeader().getRequestId()))
                .collect(Collectors.toList());
    }

}
