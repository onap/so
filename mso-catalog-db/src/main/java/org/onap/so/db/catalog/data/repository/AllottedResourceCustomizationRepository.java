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

import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "allottedResourceCustomization", path = "allottedResourceCustomization")
public interface AllottedResourceCustomizationRepository extends JpaRepository<AllottedResourceCustomization, String> {

	@Query(value = "SELECT * FROM ALLOTTED_RESOURCE_CUSTOMIZATION WHERE PROVIDING_SERVICE_MODEL_UUID =?0", nativeQuery = true)
	List<AllottedResourceCustomization> queryByProvidingServiceModelUUID(String providingServiceModelUUID);

	List<AllottedResourceCustomization> findByModelCustomizationUUID(String modelCustomizationUUID);
	AllottedResourceCustomization findOneByModelCustomizationUUID(String modelCustomizationUUID);
}