/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.client.policy;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.UriBuilder;
import org.openecomp.mso.client.ResponseExceptionMapper;
import org.openecomp.mso.client.policy.entities.PolicyServiceType;
import org.springframework.stereotype.Service;

@Service
public class PolicyRestClient extends RestClient {

    private static final String X_ECOMP_REQUESTID = String.valueOf(UUID.randomUUID());
    private final PolicyRestProperties properties;

    public PolicyRestClient(PolicyRestProperties props, PolicyServiceType serviceType) {
        super(props, Optional.of(UriBuilder.fromPath(serviceType.toString()).build()));
        this.properties = props;
        this.getClient();
    }

    @Override
    protected void initializeHeaderMap(Map<String, String> headerMap) {
        headerMap.put("ClientAuth", properties.getClientAuth());
        headerMap.put("Authorization", properties.getAuth());
        headerMap.put("Environment", properties.getEnvironment());
        this.addRequestId(X_ECOMP_REQUESTID);
    }

    @Override
    protected Optional<ClientResponseFilter> addResponseFilter() {
        return Optional.of(new ResponseExceptionMapper());
    }

    @Override
    public void addRequestId(String requestId) {
        this.headerMap.put("X-ECOMP-RequestID", requestId);
    }
}