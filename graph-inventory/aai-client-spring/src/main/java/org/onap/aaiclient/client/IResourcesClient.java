package org.onap.aaiclient.client;

import java.net.URI;

public interface IResourcesClient {
    <T> AAIResultWrapper<T> get(String uri, Class<T> responseType);
}
