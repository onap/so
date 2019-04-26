package org.onap.svnfm.simulator.config;

import java.net.InetAddress;
import java.util.Arrays;
import org.onap.svnfm.simulator.constants.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConfig implements ApplicationRunner {

    private static final String PORT = "local.server.port";

    @Autowired
    private Environment environment;

    private String baseUrl;

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        baseUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + environment.getProperty(PORT);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @Bean
    public CacheManager cacheManager() {
        Cache inlineResponse201 = new ConcurrentMapCache(Constant.IN_LINE_RESPONSE_201_CACHE);
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Arrays.asList(inlineResponse201));
        return manager;
    }
}
