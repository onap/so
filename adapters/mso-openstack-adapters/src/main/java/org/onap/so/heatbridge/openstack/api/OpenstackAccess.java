/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

/*-
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
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
 */

package org.onap.so.heatbridge.openstack.api;

import org.openstack4j.model.common.Identifier;

/**
 * Object handling OpenStack API access information.
 */
public class OpenstackAccess {
    private final String baseUrl;
    private final String tenantId;
    private final String user;
    private final String password;
    private final String region;
    private String domainName;
    private String projectName;

    public OpenstackAccess(OpenstackAccessBuilder builder) {
        this.baseUrl = builder.baseUrl;
        this.tenantId = builder.tenantId;
        this.user = builder.user;
        this.password = builder.password;
        this.region = builder.region;
        this.domainName = builder.domainName;
        this.projectName = builder.projectName;
    }

    public String getUrl() {
        return baseUrl;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getRegion() {
        return region;
    }

    public Identifier getDomainNameIdentifier() {
        return Identifier.byName(domainName);
    }

    public String getProjectName() {
        return projectName;
    }

    public Identifier getProjectNameIdentifier() {
        return Identifier.byName(projectName);
    }

    public static class OpenstackAccessBuilder {

        private String baseUrl;
        private String tenantId;
        private String user;
        private String password;
        private String region;
        private String domainName;
        private String projectName;

        public OpenstackAccessBuilder setBaseUrl(final String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public OpenstackAccessBuilder setTenantId(final String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public OpenstackAccessBuilder setUser(final String user) {
            this.user = user;
            return this;
        }

        public OpenstackAccessBuilder setPassword(final String password) {
            this.password = password;
            return this;
        }

        public OpenstackAccessBuilder setRegion(final String region) {
            this.region = region;
            return this;
        }

        public OpenstackAccessBuilder setDomainName(final String domainName) {
            this.domainName = domainName;
            return this;
        }

        public OpenstackAccessBuilder setProjectName(final String projectName) {
            this.projectName = projectName;
            return this;
        }

        public OpenstackAccess build() {
            return new OpenstackAccess(this);
        }
    }
}
