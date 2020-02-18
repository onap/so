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

import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.OperationStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

@RepositoryRestResource(collectionResourceRel = "operationStatus", path = "operationStatus")
public interface OperationStatusRepository extends JpaRepository<OperationStatus, OperationStatusId> {

    OperationStatus findOneByServiceIdAndOperationId(@Param("SERVICE_ID") String serviceId,
            @Param("OPERATION_ID") String operationId);

    public OperationStatus findOneByServiceName(String serviceName);

    public OperationStatus findOneByServiceId(String serviceId);

    public List<OperationStatus> findByAccessServiceId(String accessServiceId);

    public OperationStatus findByOperationId(String operationId);
}
