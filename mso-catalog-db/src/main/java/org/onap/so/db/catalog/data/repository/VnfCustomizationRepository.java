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

import java.util.List;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "vnfResourceCustomization", path = "vnfResourceCustomization")
public interface VnfCustomizationRepository extends JpaRepository<VnfResourceCustomization, String> {

    List<VnfResourceCustomization> findByModelCustomizationUUID(String modelCustomizationUUID);

    VnfResourceCustomization findOneByModelCustomizationUUID(String modelCustomizationUuid);

    @Query(value = "SELECT * FROM vnf_resource_customization WHERE MODEL_INSTANCE_NAME = ?1 AND VNF_RESOURCE_MODEL_UUID = ?2 LIMIT 1;", nativeQuery = true)
    VnfResourceCustomization findByModelInstanceNameAndVnfResources(String modelInstanceName,
        String vnfResourceModelUUID);

}