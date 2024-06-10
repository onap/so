package org.onap.so.db.request.configuration;

import org.onap.so.db.request.beans.RequestProcessingData;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Component
public class ExposeEntityIdResourceConfiguration implements RepositoryRestConfigurer {

    /**
     * Spring Data Rest hides the ID by default, in order to have it in the JSON you have to manually configure that for
     * your entity
     *
     * @param config
     */
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(RequestProcessingData.class);
    }
}
