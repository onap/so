/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 Bell Canada.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;

@RunWith(JUnit4.class)
public class CDSProcessingClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Mock
    private CDSProcessingListener listener = spy(new TestCDSProcessingListener());

    private CDSProcessingHandler handler;
    private CDSProcessingClient client;

    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private final List<String> messagesDelivered = new ArrayList<>();
    private final CountDownLatch allRequestsDelivered = new CountDownLatch(1);
    private final AtomicReference<StreamObserver<ExecutionServiceOutput>> responseObserverRef = new AtomicReference<>();

    @Before
    public void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        grpcCleanup.register(InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry)
                .directExecutor().build().start());

        handler = new CDSProcessingHandler(listener);

        client = new CDSProcessingClient(InProcessChannelBuilder.forName(serverName).directExecutor().build(), handler);

        final BluePrintProcessingServiceImplBase routeChatImpl = new BluePrintProcessingServiceImplBase() {
            @Override
            public StreamObserver<ExecutionServiceInput> process(
                    StreamObserver<ExecutionServiceOutput> responseObserver) {

                responseObserverRef.set(responseObserver);

                StreamObserver<ExecutionServiceInput> requestObserver = new StreamObserver<ExecutionServiceInput>() {
                    @Override
                    public void onNext(ExecutionServiceInput message) {
                        messagesDelivered.add(message.getActionIdentifiers().getActionName());
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {
                        allRequestsDelivered.countDown();
                    }
                };

                return requestObserver;
            }
        };

        serviceRegistry.addService(routeChatImpl);
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void testClientCst() {
        assertDoesNotThrow(() -> new CDSProcessingClient(listener));
    }

    @Test
    public void testSendMessageFail() throws Exception {

        ExecutionServiceInput fakeRequest1 = ExecutionServiceInput.newBuilder()
                .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("request1").build()).build();

        CountDownLatch finishLatch = client.sendRequest(fakeRequest1);

        responseObserverRef.get().onError(new Throwable("fail test"));
        verify(listener).onError(any(Throwable.class));

        assertTrue(finishLatch.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void testSendMessage() throws Exception {

        ExecutionServiceInput fakeRequest1 = ExecutionServiceInput.newBuilder()
                .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("request1").build()).build();

        ExecutionServiceOutput fakeResponse1 = ExecutionServiceOutput.newBuilder()
                .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("response1").build()).build();

        ExecutionServiceOutput fakeResponse2 = ExecutionServiceOutput.newBuilder()
                .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("response2").build()).build();

        CountDownLatch finishLatch = client.sendRequest(fakeRequest1);

        // request message sent and delivered for one time
        assertTrue(allRequestsDelivered.await(1, TimeUnit.SECONDS));
        assertEquals(Collections.singletonList("request1"), messagesDelivered);

        // Let the server send out two simple response messages
        // and verify that the client receives them.
        responseObserverRef.get().onNext(fakeResponse1);
        verify(listener).onMessage(fakeResponse1);
        responseObserverRef.get().onNext(fakeResponse2);
        verify(listener).onMessage(fakeResponse2);

        // let server complete.
        responseObserverRef.get().onCompleted();

        assertTrue(finishLatch.await(1, TimeUnit.SECONDS));
    }

}
