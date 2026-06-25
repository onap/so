package org.onap.so.adapters.requestsdb;

import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.onap.so.db.request.beans.CloudApiRequests;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RequestDbRepositoryConfiguration implements RepositoryRestConfigurer {

    @Autowired
    private EntityManager entityManager;

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(entityManager.getMetamodel().getEntities().stream().map(e -> e.getJavaType())
                .collect(Collectors.toList()).toArray(new Class[0]));
    }

    @Override
    public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
        objectMapper.addMixIn(InfraActiveRequests.class, InfraActiveRequestsDeserializationMixIn.class);
    }

    abstract static class InfraActiveRequestsDeserializationMixIn {
        @JsonIgnore
        abstract List<CloudApiRequests> getCloudApiRequests();

        @JsonIgnore
        abstract void setCloudApiRequests(List<CloudApiRequests> cloudApiRequests);
    }
}
