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

import org.onap.so.db.catalog.beans.VnfVfmoduleCvnfcConfigurationCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "vnfVfmoduleCvnfcConfigurationCustomization", path = "vnfVfmoduleCvnfcConfigurationCustomization")
public interface VnfVfmoduleCvnfcConfigurationCustomizationRepository extends JpaRepository<VnfVfmoduleCvnfcConfigurationCustomization, Integer> {
	List<VnfVfmoduleCvnfcConfigurationCustomization> findByModelCustomizationUUID(String modelCustomizationUUID);
	
	@Query(value = "SELECT * FROM vnf_vfmodule_cvnfc_configuration_customization WHERE VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID = ?1 AND VF_MODULE_MODEL_CUSTOMIZATION_UUID = ?2 AND CVNFC_MODEL_CUSTOMIZATION_UUID = ?3", nativeQuery = true)
	VnfVfmoduleCvnfcConfigurationCustomization findOneByVnfResourceCustomizationAndVfModuleCustomizationAndCvnfcCustomization (
			@Param("VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID") String vnfResourceCustomizationUuid, 
			@Param("VF_MODULE_MODEL_CUSTOMIZATION_UUID") String vfModuleCustomizationUuid,
			@Param("CVNFC_MODEL_CUSTOMIZATION_UUID") String cvnfcCustomizationUuid);
}