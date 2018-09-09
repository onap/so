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

import java.util.Date;
import java.util.List;

import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "infraActiveRequests", path = "infraActiveRequests")
public interface InfraActiveRequestsRepository extends JpaRepository<InfraActiveRequests, String>, InfraActiveRequestsRepositoryCustom {
	
	InfraActiveRequests findOneByRequestIdOrClientRequestId(String requestId, String clientRequestId);
	InfraActiveRequests findOneByRequestIdOrClientRequestIdAndRequestType(String requestId,String clientRequestId, String requestType);
	InfraActiveRequests findOneByRequestId(String string);
	InfraActiveRequests findOneByRequestBody(String requestBody);
	List<InfraActiveRequests> findByEndTimeLessThan(Date endTime, Pageable request);
	List<InfraActiveRequests> findByStartTimeLessThanAndEndTime(Date startTime, Date endTime, Pageable request);
}
