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

import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

@RepositoryRestResource(collectionResourceRel = "operationalEnvDistributionStatus", path = "operationalEnvDistributionStatus")
public interface OperationalEnvDistributionStatusRepository extends JpaRepository<OperationalEnvDistributionStatus, String> {

	@Modifying
	@Transactional
	@Query("update OperationalEnvDistributionStatus set distributionIdStatus = :distributionIdStatus where "
					+ "distributionId = :distributionId and operationalEnvId = :operationalEnvId and serviceModelVersionId = :serviceModelVersionId")
	public int setDistributionIdStatus(@Param("distributionIdStatus") String distributionIdStatus,
										@Param("distributionId") String distributionId,
										@Param("operationalEnvId") String operationalEnvId,
										@Param("serviceModelVersionId") String serviceModelVersionId);
}
