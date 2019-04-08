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

package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.VnfResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "vnfResource", path = "vnfResource")
public interface VnfResourceRepository extends JpaRepository<VnfResource, String> {

    VnfResource findResourceByModelNameAndModelUUID(String modelName, String modelUUID);

    VnfResource findByModelName(String modelName);

    VnfResource findByModelNameAndModelVersion(String modelName, String modelVersion);

    VnfResource findResourceByModelUUID(String modelUUID);

    VnfResource findResourceByModelInvariantUUID(String modelInvariantUUID);

    VnfResource findFirstResourceByModelInvariantUUIDAndModelVersion(String modelInvariantUUID, String modelVersion);
}
