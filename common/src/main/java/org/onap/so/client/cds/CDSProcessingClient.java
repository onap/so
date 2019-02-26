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
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.client.PreconditionFailedException;
import org.onap.so.client.RestPropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDSProcessingClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(CDSProcessingClient.class);

    private ManagedChannel channel;
    private CDSProcessingHandler handler;

    public CDSProcessingClient() {
        CDSProperties props = RestPropertiesLoader.getInstance().getNewImpl(CDSProperties.class);
        if (props == null) {
            throw new PreconditionFailedException(
                "No RestProperty.CDSProperties implementation found on classpath, can't create client.");
        }
        this.channel = NettyChannelBuilder
            .forAddress(props.getSystemName(), props.getPort())
            .nameResolverFactory(new DnsNameResolverProvider())
            .loadBalancerFactory(new PickFirstLoadBalancerProvider())
            .usePlaintext()
            .build();
        this.handler = new CDSProcessingHandler();
        log.info("CDSProcessingClient started");
    }

    public void sendRequest(ExecutionServiceInput input) {
        handler.process(input, channel, this);
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.shutdown();
        }
        log.info("CDSProcessingClient stopped");
    }
}