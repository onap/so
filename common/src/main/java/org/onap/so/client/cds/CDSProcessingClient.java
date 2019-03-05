/*
 * Copyright (C) 2019 Bell Canada.
 *
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
 */
package org.onap.so.client.cds;

import io.grpc.ManagedChannel;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import io.grpc.netty.NettyChannelBuilder;
import java.util.concurrent.CountDownLatch;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.client.PreconditionFailedException;
import org.onap.so.client.RestPropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The CDS processing client is using gRPC for communication between SO and CDS.
 * That communication is configured to use a streaming approach, meaning that
 * client can send an event to which server can reply will multiple sub-responses,
 * until full completion of the processing.
 * </p>
 * <p>
 * In order for the caller to manage the callback, it is the responsibility of the
 * caller to implement and provide a {@link CDSProcessingListener} so received messages
 * can be handled appropriately.
 * </p>
 *
 * Here is an example of implementation of such listener:
 * <pre>
 * new CDSProcessingListener {
 *
 *     &#64;Override
 *     public void onMessage(ExecutionServiceOutput message) {
 *         log.info("Received notification from CDS: {}", message);
 *     }
 *
 *     &#64;Override
 *     public void onError(Throwable t) {
 *         Status status = Status.fromThrowable(t);
 *         log.error("Failed processing blueprint {}", status, t);
 *     }
 * }
 * </pre>
 */
public class CDSProcessingClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(CDSProcessingClient.class);

    private ManagedChannel channel;
    private CDSProcessingHandler handler;

    public CDSProcessingClient(final CDSProcessingListener listener) {
        CDSProperties props = RestPropertiesLoader.getInstance().getNewImpl(CDSProperties.class);
        if (props == null) {
            throw new PreconditionFailedException(
                "No RestProperty.CDSProperties implementation found on classpath, can't create client.");
        }
        this.channel = NettyChannelBuilder
            .forAddress(props.getHost(), props.getPort())
            .nameResolverFactory(new DnsNameResolverProvider())
            .loadBalancerFactory(new PickFirstLoadBalancerProvider())
            .intercept(new BasicAuthClientInterceptor(props))
            .usePlaintext()
            .build();
        this.handler = new CDSProcessingHandler(listener);
        log.info("CDSProcessingClient started");
    }

    CDSProcessingClient(final ManagedChannel channel, final CDSProcessingHandler handler) {
        this.channel = channel;
        this.handler = handler;
    }

    /**
     * Sends a request to the CDS backend micro-service.
     *
     * The caller will be returned a CountDownLatch that can be used
     * to define how long the processing can wait. The CountDownLatch is
     * initiated with just 1 count. When the client receives an #onCompleted callback,
     * the counter will decrement.
     *
     * It is the user responsibility to close the client.
     *
     * @param input request to send
     * @return CountDownLatch instance that can be use to #await for
     * completeness of processing
     */
    public CountDownLatch sendRequest(ExecutionServiceInput input) {
        return handler.process(input, channel);
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.shutdown();
        }
        log.info("CDSProcessingClient stopped");
    }
}