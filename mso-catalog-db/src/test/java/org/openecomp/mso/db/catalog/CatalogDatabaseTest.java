/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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


package org.openecomp.mso.db.catalog;

import static org.junit.Assert.*;

import org.junit.Test;

public class CatalogDatabaseTest {

	@Test
	public void testGetInstance() {
	}

	@Test
	public void testClose() {
		CatalogDatabase db = CatalogDatabase.getInstance();
		db.close();
	}

	@Test
	public void testCommit() {
		CatalogDatabase db = CatalogDatabase.getInstance();
		db.commit();
	}

	@Test
	public void testRollback() {
	}

	@Test
	public void testGetAllHeatTemplates() {
	}

	@Test
	public void testGetHeatTemplateInt() {
	}

	@Test
	public void testGetHeatTemplateString() {
	}

	@Test
	public void testGetHeatTemplateStringString() {
	}

	@Test
	public void testGetHeatTemplateByArtifactUuid() {
	}

	@Test
	public void testGetHeatTemplateByArtifactUuidRegularQuery() {
	}

	@Test
	public void testGetParametersForHeatTemplate() {
	}

	@Test
	public void testGetHeatEnvironmentByArtifactUuid() {
	}

	@Test
	public void testGetServiceByInvariantUUID() {
	}

	@Test
	public void testGetServiceString() {
	}

	@Test
	public void testGetServiceByModelUUID() {
	}

	@Test
	public void testGetServiceHashMapOfStringStringString() {
	}

	@Test
	public void testGetServiceByModelName() {
	}

	@Test
	public void testGetServiceByVersionAndInvariantId() {
	}

	@Test
	public void testGetServiceRecipeIntString() {
	}

	@Test
	public void testGetServiceRecipeByServiceModelUuid() {
	}

	@Test
	public void testGetServiceRecipes() {
	}

	@Test
	public void testGetVnfComponent() {
	}

	@Test
	public void testGetVnfResourceString() {
	}

	@Test
	public void testGetVnfResourceStringString() {
	}

	@Test
	public void testGetVnfResourceByModelCustomizationId() {
	}

	@Test
	public void testGetVnfResourceCustomizationByModelCustomizationName() {
	}

	@Test
	public void testGetVnfResourceByModelInvariantId() {
	}

	@Test
	public void testGetVnfResourceById() {
	}

	@Test
	public void testGetVfModuleModelNameString() {
	}

	@Test
	public void testGetVfModuleModelNameStringString() {
	}

	@Test
	public void testGetVfModuleCustomizationByModelName() {
	}

	@Test
	public void testGetNetworkResource() {
	}

	@Test
	public void testGetVnfRecipeStringStringString() {
	}

	@Test
	public void testGetVnfRecipeStringString() {
	}

	@Test
	public void testGetVnfRecipeByVfModuleId() {
	}

	@Test
	public void testGetVfModuleTypeByUuid() {
	}

	@Test
	public void testGetVfModuleTypeString() {
	}

	@Test
	public void testGetVfModuleTypeStringString() {
	}

	@Test
	public void testGetVnfResourceByServiceUuid() {
	}

	@Test
	public void testGetVnfResourceByVnfUuid() {
	}

	@Test
	public void testGetVnfResourceByType() {
	}

	@Test
	public void testGetVfModuleByModelInvariantUuid() {
	}

	@Test
	public void testGetVfModuleByModelCustomizationUuid() {
	}

	@Test
	public void testGetVfModuleByModelInvariantUuidAndModelVersion() {
	}

	@Test
	public void testGetVfModuleCustomizationByModelCustomizationId() {
	}

	@Test
	public void testGetVfModuleByModelUuid() {
	}

	@Test
	public void testGetVnfResourceCustomizationByModelCustomizationUuid() {
	}

	@Test
	public void testGetVnfResourceCustomizationByModelVersionId() {
	}

	@Test
	public void testGetVfModuleByModelCustomizationIdAndVersion() {
	}

	@Test
	public void testGetVfModuleByModelCustomizationIdModelVersionAndModelInvariantId() {
	}

	@Test
	public void testGetVnfResourceCustomizationByModelInvariantId() {
	}

	@Test
	public void testGetVfModuleCustomizationByVnfModuleCustomizationUuid() {
	}

	@Test
	public void testGetVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId() {
	}

	@Test
	public void testGetAllVfModuleCustomizationsString() {
	}

	@Test
	public void testGetVnfResourceByModelUuid() {
	}

	@Test
	public void testGetVnfResCustomToVfModule() {
	}

	@Test
	public void testGetVfModulesForVnfResourceVnfResource() {
	}

	@Test
	public void testGetVfModulesForVnfResourceString() {
	}

	@Test
	public void testGetServiceByUuid() {
	}

	@Test
	public void testGetNetworkResourceByIdInteger() {
	}

	@Test
	public void testGetNetworkResourceByIdString() {
	}

	@Test
	public void testIsEmptyOrNull() {
	}

	@Test
	public void testGetSTR() {
	}

	@Test
	public void testGetVRCtoVFMC() {
	}

	@Test
	public void testGetTempNetworkHeatTemplateLookup() {
	}

	@Test
	public void testGetAllNetworksByServiceModelUuid() {
	}

	@Test
	public void testGetAllNetworksByServiceModelInvariantUuidString() {
	}

	@Test
	public void testGetAllNetworksByServiceModelInvariantUuidStringString() {
	}

	@Test
	public void testGetAllNetworksByNetworkModelCustomizationUuid() {
	}

	@Test
	public void testGetAllNetworksByNetworkType() {
	}

	@Test
	public void testGetAllVfmcForVrc() {
	}

	@Test
	public void testGetAllVnfsByServiceModelUuid() {
	}

	@Test
	public void testGetAllVnfsByServiceModelInvariantUuidString() {
	}

	@Test
	public void testGetAllVnfsByServiceModelInvariantUuidStringString() {
	}

	@Test
	public void testGetAllVnfsByServiceNameStringString() {
	}

	@Test
	public void testGetAllVnfsByServiceNameString() {
	}

	@Test
	public void testGetAllVnfsByVnfModelCustomizationUuid() {
	}

	@Test
	public void testGetAllAllottedResourcesByServiceModelUuid() {
	}

	@Test
	public void testGetAllAllottedResourcesByServiceModelInvariantUuidString() {
	}

	@Test
	public void testGetAllAllottedResourcesByServiceModelInvariantUuidStringString() {
	}

	@Test
	public void testGetAllAllottedResourcesByArModelCustomizationUuid() {
	}

	@Test
	public void testGetAllottedResourceByModelUuid() {
	}

	@Test
	public void testGetAllResourcesByServiceModelUuid() {
	}

	@Test
	public void testGetAllResourcesByServiceModelInvariantUuidString() {
	}

	@Test
	public void testGetAllResourcesByServiceModelInvariantUuidStringString() {
	}

	@Test
	public void testGetSingleNetworkByModelCustomizationUuid() {
	}

	@Test
	public void testGetSingleAllottedResourceByModelCustomizationUuid() {
	}

	@Test
	public void testGetSingleVnfResourceByModelCustomizationUuid() {
	}

	@Test
	public void testGetVfModuleRecipe() {

	}

	@Test
	public void testGetVfModuleStringStringStringStringString() {

	}

	@Test
	public void testGetVnfComponentsRecipeStringStringStringStringStringString() {

	}

	@Test
	public void testGetVnfComponentsRecipeByVfModule() {

	}

	@Test
	public void testGetAllVnfResources() {

	}

	@Test
	public void testGetVnfResourcesByRole() {

	}

	@Test
	public void testGetVnfResourceCustomizationsByRole() {

	}

	@Test
	public void testGetAllNetworkResources() {

	}

	@Test
	public void testGetAllNetworkResourceCustomizations() {

	}

	@Test
	public void testGetAllVfModules() {

	}

	@Test
	public void testGetAllVfModuleCustomizations() {

	}

	@Test
	public void testGetAllHeatEnvironment() {

	}

	@Test
	public void testGetHeatEnvironmentInt() {

	}

	@Test
	public void testGetNestedTemplatesInt() {

	}

	@Test
	public void testGetNestedTemplatesString() {

	}

	@Test
	public void testGetHeatFilesInt() {

	}

	@Test
	public void testGetHeatFilesForVfModuleInt() {

	}

	@Test
	public void testGetVfModuleToHeatFilesEntry() {

	}

	@Test
	public void testGetServiceToResourceCustomization() {

	}

	@Test
	public void testGetHeatFilesForVfModuleString() {

	}

	@Test
	public void testGetHeatTemplateStringStringString() {

	}

	@Test
	public void testSaveHeatTemplateHeatTemplateSetOfHeatTemplateParam() {

	}

	@Test
	public void testGetHeatEnvironmentStringStringString() {

	}

	@Test
	public void testGetHeatEnvironmentStringString() {
	}

	@Test
	public void testSaveHeatEnvironment() {

	}

	@Test
	public void testSaveHeatTemplateHeatTemplate() {

	}

	@Test
	public void testSaveHeatFile() {

	}

	@Test
	public void testSaveVnfRecipe() {

	}

	@Test
	public void testSaveVnfComponentsRecipe() {

	}

	@Test
	public void testSaveOrUpdateVnfResource() {

	}

	@Test
	public void testSaveVnfResourceCustomization() {

	}

	@Test
	public void testSaveAllottedResourceCustomization() {

	}

	@Test
	public void testSaveAllottedResource() {

	}

	@Test
	public void testSaveNetworkResource() {

	}

	@Test
	public void testSaveToscaCsar() {

	}

	@Test
	public void testGetToscaCsar() {

	}

	@Test
	public void testSaveTempNetworkHeatTemplateLookup() {

	}

	@Test
	public void testSaveVfModuleToHeatFilesVfModuleToHeatFiles() {

	}

	@Test
	public void testSaveVnfResourceToVfModuleCustomization() {

	}

	@Test
	public void testSaveNetworkResourceCustomization() {

	}

	@Test
	public void testSaveServiceToNetworks() {

	}

	@Test
	public void testSaveServiceToResourceCustomization() {

	}

	@Test
	public void testSaveServiceToAllottedResources() {

	}

	@Test
	public void testSaveService() {

	}

	@Test
	public void testSaveOrUpdateVfModule() {

	}

	@Test
	public void testSaveOrUpdateVfModuleCustomization() {

	}

	@Test
	public void testGetNestedHeatTemplateIntInt() {

	}

	@Test
	public void testGetNestedHeatTemplateStringString() {

	}

	@Test
	public void testSaveNestedHeatTemplateIntHeatTemplateString() {

	}

	@Test
	public void testSaveNestedHeatTemplateStringHeatTemplateString() {

	}

	@Test
	public void testGetHeatFilesIntStringStringString() {

	}

	@Test
	public void testGetHeatFilesString() {

	}

	@Test
	public void testSaveHeatFiles() {

	}

	@Test
	public void testSaveVfModuleToHeatFilesIntHeatFiles() {

	}

	@Test
	public void testSaveVfModuleToHeatFilesStringHeatFiles() {

	}

	@Test
	public void testGetNetworkResourceByModelUuid() {

	}

	@Test
	public void testGetNetworkRecipeStringStringString() {

	}

	@Test
	public void testGetNetworkRecipeStringString() {

	}

	@Test
	public void testGetNetworkResourceByModelCustUuid() {

	}

	@Test
	public void testGetVnfComponentsRecipeStringStringStringString() {

	}

	@Test
	public void testGetVnfComponentsRecipeByVfModuleModelUUId() {

	}

	@Test
	public void testGetVnfComponentRecipes() {

	}

	@Test
	public void testSaveOrUpdateVnfComponent() {

	}

	@Test
	public void testGetVfModuleString() {

	}

	@Test
	public void testGetVfModuleByModelUUID() {

	}

	@Test
	public void testGetServiceRecipeByModelUUID() {

	}

	@Test
	public void testGetServiceRecipeStringString() {

	}

	@Test
	public void testGetModelRecipe() {

	}

	@Test
	public void testHealthCheck() {

	}

	@Test
	public void testExecuteQuerySingleRow() {

	}

	@Test
	public void testExecuteQueryMultipleRows() {

	}

	@Test
	public void testObject() {

	}

	@Test
	public void testGetClass() {

	}

	@Test
	public void testHashCode() {

	}

	@Test
	public void testEquals() {

	}

	@Test
	public void testClone() {

	}

	@Test
	public void testToString() {

	}

	@Test
	public void testNotify() {

	}

	@Test
	public void testNotifyAll() {

	}

	@Test
	public void testWaitLong() {

	}

	@Test
	public void testWaitLongInt() {

	}

	@Test
	public void testWait() {

	}

	@Test
	public void testFinalize() {

	}

}
