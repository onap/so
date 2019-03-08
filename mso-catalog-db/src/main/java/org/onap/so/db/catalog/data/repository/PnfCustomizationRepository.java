/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "pnfResourceCustomization", path = "pnfResourceCustomization")
public interface PnfCustomizationRepository extends JpaRepository<PnfResourceCustomization, String> {

    List<PnfResourceCustomization> findByModelCustomizationUUID(String modelCustomizationUUID);

    PnfResourceCustomization findOneByModelCustomizationUUID(String modelCustomizationUuid);

    @Query(value = "SELECT * FROM pnf_resource_customization WHERE MODEL_INSTANCE_NAME = ?1 AND PNF_RESOURCE_MODEL_UUID = ?2 LIMIT 1;", nativeQuery = true)
    PnfResourceCustomization findByModelInstanceNameAndPnfResources(String modelInstanceName,
        String pnfResourceModelUUID);
}