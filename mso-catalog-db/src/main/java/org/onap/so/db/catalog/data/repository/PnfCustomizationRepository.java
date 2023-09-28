/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.db.catalog.data.repository;

import java.util.List;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "pnfResourceCustomization", path = "pnfResourceCustomization")
public interface PnfCustomizationRepository extends JpaRepository<PnfResourceCustomization, String> {

    /**
     * Used to fetch the @{link PnfResourceCustomization} by the Model UUID.
     *
     * This operation is required by {@link org.onap.so.db.catalog.client.CatalogDbClient} to provide
     * PnfResourceCustomization based on model UUID without projection.
     *
     * @param serviceModelUuid model UUID
     * @return List of PnfResourceCustomization
     */
    @Query(value = "select b.* from pnf_resource_customization_to_service a join pnf_resource_customization b where a.RESOURCE_MODEL_CUSTOMIZATION_UUID = b.MODEL_CUSTOMIZATION_UUID and a.SERVICE_MODEL_UUID = ?1",
            nativeQuery = true)
    List<PnfResourceCustomization> findPnfResourceCustomizationByModelUuid(
            @Param("SERVICE_MODEL_UUID") String serviceModelUuid);

    List<PnfResourceCustomization> findByModelCustomizationUUID(
            @Param("MODEL_CUSTOMIZATION_UUID") String modelCustomizationUUID);
}
