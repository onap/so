package org.onap.so.adapters.requestsdb.client;


import org.onap.so.db.request.client.RequestsDbClient;
import org.springframework.stereotype.Component;
import java.net.URI;
@Component
public class RequestDbClientPortChanger extends RequestsDbClient {
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public URI getUri(String uri) {
        uri = uri.replace("8081", String.valueOf(port));
        return URI.create(uri);
    }
}

