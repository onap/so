package org.onap.so.client;

import java.io.IOException;
import java.util.Collections;
import jakarta.annotation.Priority;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class AddCacheHeaders implements ClientResponseFilter {

    private final CacheProperties props;

    public AddCacheHeaders(CacheProperties props) {
        this.props = props;
    }

    public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
        if (request.getMethod().equalsIgnoreCase("GET")) {
            response.getHeaders().putIfAbsent("Cache-Control",
                    Collections.singletonList("public, max-age=" + (props.getMaxAge() / 1000)));
        }

    }
}
