package org.onap.so.client;

public interface CacheProperties {


    default Long getMaxAge() {
        return 60000L;
    }

    default String getCacheName() {
        return "default-http-cache";
    }
}
