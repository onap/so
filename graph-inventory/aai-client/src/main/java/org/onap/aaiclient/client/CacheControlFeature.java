package org.onap.aaiclient.client;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import jakarta.annotation.PreDestroy;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.cache.spi.CachingProvider;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import org.apache.cxf.jaxrs.client.cache.BytesEntity;
import org.apache.cxf.jaxrs.client.cache.CacheControlClientReaderInterceptor;
import org.apache.cxf.jaxrs.client.cache.Entry;
import org.apache.cxf.jaxrs.client.cache.Key;


@Provider
public class CacheControlFeature implements Feature, Closeable {
    static final String REQUEST_URI_PROPERTY = "org.onap.aaiclient.client.cache.requestUri";
    static final String NO_CACHE_PROPERTY = "no_client_cache";
    static final String CACHED_ENTITY_PROPERTY = "client_cached_entity";
    static final String CLIENT_ACCEPTS = "client_accepts";

    private CachingProvider provider;
    private CacheManager manager;
    private Cache<Key, Entry> cache;
    private boolean cacheResponseInputStream;
    private Factory<ExpiryPolicy> expiryPolicy;

    @Override
    public boolean configure(final FeatureContext context) {
        final Cache<Key, Entry> entryCache = createCache(context.getConfiguration().getProperties());
        context.register(new JerseyCompatibleCacheRequestFilter(entryCache));
        CacheControlClientReaderInterceptor reader = new CacheControlClientReaderInterceptor(entryCache);
        reader.setCacheResponseInputStream(cacheResponseInputStream);
        context.register(new UriProvidingReaderInterceptor(reader));
        return true;
    }

    @PreDestroy // TODO: check it is called
    public void close() {
        for (final Closeable c : Arrays.asList(cache, manager, provider)) {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    private Cache<Key, Entry> createCache(final Map<String, Object> properties) {
        final Properties props = new Properties();
        props.putAll(properties);

        final String prefix = this.getClass().getName() + ".";
        final String uri = props.getProperty(prefix + "config-uri");
        final String name = props.getProperty(prefix + "name", this.getClass().getName());

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        provider = Caching.getCachingProvider();
        try {
            synchronized (contextClassLoader) {
                manager = provider.getCacheManager(uri == null ? provider.getDefaultURI() : new URI(uri),
                        contextClassLoader, props);
                if (manager.getCache(name) == null) {
                    final MutableConfiguration<Key, Entry> configuration = new MutableConfiguration<Key, Entry>()
                            .setReadThrough("true".equalsIgnoreCase(props.getProperty(prefix + "readThrough", "false")))
                            .setWriteThrough(
                                    "true".equalsIgnoreCase(props.getProperty(prefix + "writeThrough", "false")))
                            .setManagementEnabled(
                                    "true".equalsIgnoreCase(props.getProperty(prefix + "managementEnabled", "false")))
                            .setStatisticsEnabled(
                                    "true".equalsIgnoreCase(props.getProperty(prefix + "statisticsEnabled", "false")))
                            .setStoreByValue(
                                    "true".equalsIgnoreCase(props.getProperty(prefix + "storeByValue", "false")));

                    final String loader = props.getProperty(prefix + "loaderFactory");
                    if (loader != null) {
                        @SuppressWarnings("unchecked")
                        Factory<? extends CacheLoader<Key, Entry>> f =
                                newInstance(contextClassLoader, loader, Factory.class);
                        configuration.setCacheLoaderFactory(f);
                    }
                    final String writer = props.getProperty(prefix + "writerFactory");
                    if (writer != null) {
                        @SuppressWarnings("unchecked")
                        Factory<? extends CacheWriter<Key, Entry>> f =
                                newInstance(contextClassLoader, writer, Factory.class);
                        configuration.setCacheWriterFactory(f);
                    }
                    if (expiryPolicy != null) {

                        configuration.setExpiryPolicyFactory(expiryPolicy);
                    }
                    configuration.addCacheEntryListenerConfiguration(new MutableCacheEntryListenerConfiguration(
                            FactoryBuilder.factoryOf(new CacheLogger()), null, true, true));

                    cache = manager.createCache(name, configuration);
                } else {
                    cache = manager.getCache(name);
                }
                return cache;
            }
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(final ClassLoader contextClassLoader, final String clazz, final Class<T> cast) {
        try {
            return (T) contextClassLoader.loadClass(clazz).newInstance();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setCacheResponseInputStream(boolean cacheStream) {
        this.cacheResponseInputStream = cacheStream;
    }

    public void setExpiryPolicyFactory(Factory<ExpiryPolicy> f) {
        this.expiryPolicy = f;
    }

    @jakarta.annotation.Priority(jakarta.ws.rs.Priorities.USER - 1)
    private static class JerseyCompatibleCacheRequestFilter implements ClientRequestFilter {
        private final Cache<Key, Entry> cache;

        JerseyCompatibleCacheRequestFilter(Cache<Key, Entry> cache) {
            this.cache = cache;
        }

        @Override
        public void filter(ClientRequestContext request) throws IOException {
            request.setProperty(REQUEST_URI_PROPERTY, request.getUri());

            if (!"GET".equals(request.getMethod())) {
                request.setProperty(NO_CACHE_PROPERTY, "true");
                return;
            }
            final URI uri = request.getUri();
            final String accepts = request.getHeaderString(jakarta.ws.rs.core.HttpHeaders.ACCEPT);
            final Key key = new Key(uri, accepts);
            Entry entry = cache.get(key);
            if (entry != null) {
                if (entry.isOutDated()) {
                    java.util.Map<String, String> cacheHeaders = entry.getCacheHeaders();
                    String ifNoneMatch =
                            cacheHeaders != null ? cacheHeaders.get(jakarta.ws.rs.core.HttpHeaders.IF_NONE_MATCH)
                                    : null;
                    String ifModifiedSince =
                            cacheHeaders != null ? cacheHeaders.get(jakarta.ws.rs.core.HttpHeaders.IF_MODIFIED_SINCE)
                                    : null;
                    if ((ifNoneMatch == null || ifNoneMatch.isEmpty())
                            && (ifModifiedSince == null || ifModifiedSince.isEmpty())) {
                        cache.remove(key, entry);
                    } else {
                        if (ifNoneMatch != null) {
                            request.getHeaders().add(jakarta.ws.rs.core.HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
                        }
                        if (ifModifiedSince != null) {
                            request.getHeaders().add(jakarta.ws.rs.core.HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince);
                        }
                        request.setProperty(CACHED_ENTITY_PROPERTY, entry.getData());
                    }
                } else {
                    Object cachedEntity = entry.getData();
                    jakarta.ws.rs.core.Response.ResponseBuilder ok;
                    if (cachedEntity instanceof BytesEntity) {
                        BytesEntity bytesEntity = (BytesEntity) cachedEntity;
                        ok = jakarta.ws.rs.core.Response.ok(new ByteArrayInputStream(bytesEntity.getEntity()));
                    } else if (cachedEntity instanceof byte[]) {
                        ok = jakarta.ws.rs.core.Response.ok(new ByteArrayInputStream((byte[]) cachedEntity));
                    } else {
                        ok = jakarta.ws.rs.core.Response.ok(cachedEntity);
                    }
                    if (entry.getHeaders() != null) {
                        for (java.util.Map.Entry<String, java.util.List<String>> h : entry.getHeaders().entrySet()) {
                            for (Object instance : h.getValue()) {
                                ok = ok.header(h.getKey(), instance);
                            }
                        }
                    }
                    request.setProperty(CACHED_ENTITY_PROPERTY, cachedEntity);
                    request.abortWith(ok.build());
                }
            }
            request.setProperty(CLIENT_ACCEPTS, accepts);
        }
    }

    private static class UriProvidingReaderInterceptor implements ReaderInterceptor {
        private final CacheControlClientReaderInterceptor delegate;

        UriProvidingReaderInterceptor(CacheControlClientReaderInterceptor delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context)
                throws IOException, jakarta.ws.rs.WebApplicationException {
            URI requestUri = (URI) context.getProperty(REQUEST_URI_PROPERTY);
            if (requestUri != null) {
                injectUriInfo(delegate, requestUri);
            }
            return delegate.aroundReadFrom(context);
        }

        private void injectUriInfo(CacheControlClientReaderInterceptor interceptor, URI uri) {
            try {
                java.lang.reflect.Field field = CacheControlClientReaderInterceptor.class.getDeclaredField("uriInfo");
                field.setAccessible(true);
                field.set(interceptor, new SimpleUriInfo(uri));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to inject UriInfo into CacheControlClientReaderInterceptor", e);
            }
        }
    }

    private static class SimpleUriInfo implements UriInfo {
        private final URI requestUri;

        SimpleUriInfo(URI requestUri) {
            this.requestUri = requestUri;
        }

        @Override
        public String getPath() {
            return requestUri.getPath();
        }

        @Override
        public String getPath(boolean decode) {
            return requestUri.getPath();
        }

        @Override
        public java.util.List<jakarta.ws.rs.core.PathSegment> getPathSegments() {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.List<jakarta.ws.rs.core.PathSegment> getPathSegments(boolean decode) {
            return java.util.Collections.emptyList();
        }

        @Override
        public URI getRequestUri() {
            return requestUri;
        }

        @Override
        public jakarta.ws.rs.core.UriBuilder getRequestUriBuilder() {
            return jakarta.ws.rs.core.UriBuilder.fromUri(requestUri);
        }

        @Override
        public URI getAbsolutePath() {
            return requestUri;
        }

        @Override
        public jakarta.ws.rs.core.UriBuilder getAbsolutePathBuilder() {
            return jakarta.ws.rs.core.UriBuilder.fromUri(requestUri);
        }

        @Override
        public URI getBaseUri() {
            return requestUri;
        }

        @Override
        public jakarta.ws.rs.core.UriBuilder getBaseUriBuilder() {
            return jakarta.ws.rs.core.UriBuilder.fromUri(requestUri);
        }

        @Override
        public jakarta.ws.rs.core.MultivaluedMap<String, String> getPathParameters() {
            return new jakarta.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public jakarta.ws.rs.core.MultivaluedMap<String, String> getPathParameters(boolean decode) {
            return new jakarta.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public jakarta.ws.rs.core.MultivaluedMap<String, String> getQueryParameters() {
            return new jakarta.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public jakarta.ws.rs.core.MultivaluedMap<String, String> getQueryParameters(boolean decode) {
            return new jakarta.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public java.util.List<String> getMatchedURIs() {
            return java.util.Collections.emptyList();
        }

        @Override
        public String getMatchedResourceTemplate() {
            return null;
        }

        @Override
        public java.util.List<String> getMatchedURIs(boolean decode) {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.List<Object> getMatchedResources() {
            return java.util.Collections.emptyList();
        }

        @Override
        public URI resolve(URI uri) {
            return requestUri.resolve(uri);
        }

        @Override
        public URI relativize(URI uri) {
            return requestUri.relativize(uri);
        }
    }
}
