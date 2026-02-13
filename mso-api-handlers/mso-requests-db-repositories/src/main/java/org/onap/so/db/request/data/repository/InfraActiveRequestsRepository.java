/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2026 Deutsche telekom
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

import java.util.Date;
import java.util.List;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "infraActiveRequests", path = "infraActiveRequests")
public interface InfraActiveRequestsRepository
        extends JpaRepository<InfraActiveRequests, String>, InfraActiveRequestsRepositoryCustom {

    InfraActiveRequests findOneByRequestId(String string);

    InfraActiveRequests findOneByRequestBody(String requestBody);

    List<InfraActiveRequests> findByEndTimeLessThan(Date endTime, Pageable request);

    List<InfraActiveRequests> findByStartTimeLessThanAndEndTime(Date startTime, Date endTime, Pageable request);

    @Query(value = "SELECT * FROM infra_active_requests WHERE request_status = 'IN_PROGRESS' AND (request_scope = 'volumeGroup' OR request_scope = 'vfModule') AND start_time < (NOW() - INTERVAL 2 MINUTE)"
            + " ORDER BY start_time DESC", nativeQuery = true)
    List<InfraActiveRequests> getInProgressVolumeGroupsAndVfModules();

    @Query(value = "SELECT * FROM infra_active_requests WHERE request_url like '%serviceInstances%' AND original_request_id is null AND request_status = 'IN_PROGRESS'"
            + " ORDER BY start_time DESC", nativeQuery = true)
    List<InfraActiveRequests> getListOfRequestsInProgress();
}
