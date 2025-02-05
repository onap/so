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
package org.onap.so.apihandlerinfra;

import java.net.URI;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "mso.health")
@Validated
public class HealthCheckConfig {

    @NotNull
    private List<Endpoint> endpoints;

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("endpoints", this.endpoints).toString();
    }

    @Validated
    public static class Endpoint {
        @NotNull
        private Subsystem subsystem;
        @NotNull
        private URI uri;

        public Endpoint() {

        }

        public Endpoint(Subsystem subsystem, URI uri) {
            this.subsystem = subsystem;
            this.uri = uri;
        }

        public Subsystem getSubsystem() {
            return subsystem;
        }

        public void setSubsystem(Subsystem subsystem) {
            this.subsystem = subsystem;
        }

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }
    }
}
