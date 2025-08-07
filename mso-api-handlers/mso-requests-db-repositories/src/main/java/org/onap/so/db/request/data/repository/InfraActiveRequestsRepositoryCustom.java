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

package org.onap.so.db.request.data.repository;

import java.util.List;
import java.util.Map;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.stereotype.Repository;

@Repository
public interface InfraActiveRequestsRepositoryCustom {

    public boolean healthCheck();

    public InfraActiveRequests getRequestFromInfraActive(String requestId);

    public InfraActiveRequests checkInstanceNameDuplicate(Map<String, String> instanceIdMap, String instanceName,
            String requestScope);

    public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive(Map<String, List<String>> orchestrationMap);

    // Added this method for Tenant Isolation project ( 1802-295491a) to query the mso_requests DB
    // (infra_active_requests table) for operationalEnvId and OperationalEnvName
    public List<InfraActiveRequests> getCloudOrchestrationFiltersFromInfraActive(Map<String, String> orchestrationMap);

    public List<InfraActiveRequests> getRequestListFromInfraActive(String queryAttributeName, String queryValue,
            String requestType);

    public InfraActiveRequests getRequestFromInfraActive(String requestId, String requestType);

    public InfraActiveRequests checkVnfIdStatus(String operationalEnvironmentId);

    List<InfraActiveRequests> getInfraActiveRequests(final Map<String, String[]> filters, final long startTime,
            final long endTime, final Integer maxResult);

}
