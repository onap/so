/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.catalogdb;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.onap.so.adapters.catalogdb.rest.CatalogDbAdapterRest;
import org.onap.so.adapters.catalogdb.rest.ServiceRestImpl;
import org.onap.so.logging.jaxrs.filter.JaxRsFilterLogging;
import org.springframework.context.annotation.Configuration;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

@Configuration
@ApplicationPath("/ecomp/mso/catalog")
public class JerseyConfiguration extends ResourceConfig {

    @PostConstruct
    public void setUp() {
        register(CatalogDbAdapterRest.class);
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);
        register(JaxRsFilterLogging.class);
        register(ServiceRestImpl.class);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[] {"https"});
        beanConfig.setBasePath("/ecomp/mso/catalog");
        beanConfig.setResourcePackage("org.onap.so.adapters.catalogdb.rest");
        beanConfig.setPrettyPrint(true);
        beanConfig.setScan(true);
    }
}

