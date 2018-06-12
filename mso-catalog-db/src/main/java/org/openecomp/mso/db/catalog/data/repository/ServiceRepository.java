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

package org.openecomp.mso.db.catalog.data.repository;

import java.util.List;

import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.data.projections.InlineService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "service", path = "service", excerptProjection = InlineService.class)
public interface ServiceRepository extends JpaRepository<Service, String> {
	List<Service> findByModelName(String modelName);

	Service findOneByModelName(String modelName);

	Service findFirstByModelNameOrderByModelVersionDesc(String modelName);

	Service findByModelNameOrderByModelVersionDesc(String modelName);

	Service findOneByModelNameAndModelVersion(String modelName, String modelVersion);

	Service findByModelNameAndModelVersion(String modelName, String modelVersion);

	Service findByServiceType(String serviceType);

	Service findFirstOneByModelUUIDOrderByModelVersionDesc(String modelUUID);

	Service findOneByModelUUIDOrderByModelVersionDesc(String modelUUID);

	Service findByModelVersionAndModelInvariantUUID(@Param("MODEL_VERSION") String modelVersion,
			@Param("MODEL_INVARIANT_UUID") String modelInvariantUUID);

	Service findFirstByModelInvariantUUIDOrderByModelVersionDesc(String modelInvariantUUID);

	List<Service> findByModelUUID(String modelUUID);

	Service findOneByModelUUID(String modelUUID);
}