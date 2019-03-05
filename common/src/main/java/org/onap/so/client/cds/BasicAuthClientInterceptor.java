package org.onap.so.client.cds;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;

public class BasicAuthClientInterceptor implements ClientInterceptor {

    private CDSProperties props;

    public BasicAuthClientInterceptor(CDSProperties props) {
        this.props = props;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method,
        CallOptions callOptions,
        Channel channel) {

        Key<String> authHeader = Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            channel.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(authHeader, props.getBasicAuth());
                super.start(responseListener, headers);
            }
        };
    }
}
