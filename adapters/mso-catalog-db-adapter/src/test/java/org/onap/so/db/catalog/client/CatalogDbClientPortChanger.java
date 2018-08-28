package org.onap.so.db.catalog.client;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component()
public class CatalogDbClientPortChanger extends CatalogDbClient {

    public String wiremockPort;

    protected URI getUri(String template) {
        URI uri = URI.create(template);
        String path = uri.getPath();
        String prefix = "http://localhost:" + wiremockPort;
        String query = uri.getQuery();

        return URI.create(prefix + path + (query == null || query.isEmpty()?"":"?"+query));
    }
}
