package org.onap.aaiclient.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.cache.CacheManager;
import javax.cache.Caching;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import org.apache.cxf.jaxrs.client.cache.Key;
import org.onap.so.client.CacheProperties;

public class FlushCache implements ClientResponseFilter {

    private static final Set<String> modifyMethods =
            new HashSet<>(Arrays.asList(HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.PUT, HttpMethod.POST));

    private final CacheProperties props;

    public FlushCache(CacheProperties props) {
        this.props = props;
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

        if (responseContext.getStatus() >= 200 && responseContext.getStatus() <= 299) {
            if (FlushCache.modifyMethods.contains(requestContext.getMethod())) {

                CacheManager cacheManager = Caching.getCachingProvider().getCacheManager(
                        Caching.getCachingProvider().getDefaultURI(), Thread.currentThread().getContextClassLoader());
                cacheManager.getCache(props.getCacheName()).remove(
                        new Key(requestContext.getUri(), requestContext.getAcceptableMediaTypes().get(0).toString()));
            }
        }
    }

}
