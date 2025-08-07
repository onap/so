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

import org.onap.so.db.catalog.beans.NetworkResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "networkResource", path = "networkResource")
public interface NetworkResourceRepository extends JpaRepository<NetworkResource, String> {

    /**
     * This method will not work for versions greater than 255, as it is utilizing an ip address function to do the
     * sorting
     * 
     * @param modelName
     * @return
     */
    @Query(value = "SELECT * FROM network_resource WHERE MODEL_NAME = ?1 ORDER BY INET_ATON(SUBSTRING_INDEX(CONCAT(MODEL_VERSION,'.0.0.0'),'.',4)) DESC LIMIT 1;",
            nativeQuery = true)
    NetworkResource findFirstByModelNameOrderByModelVersionDesc(@Param("modelName") String modelName);

    NetworkResource findResourceByModelUUID(String modelUUID);

    NetworkResource findOneByModelName(String modelName);
}
