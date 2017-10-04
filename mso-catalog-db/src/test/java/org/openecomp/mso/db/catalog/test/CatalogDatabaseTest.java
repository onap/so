/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

package org.openecomp.mso.db.catalog.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.db.catalog.beans.ServiceToResourceCustomization;
import org.openecomp.mso.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VfModuleToHeatFiles;
import org.openecomp.mso.db.catalog.beans.VnfComponent;
import org.openecomp.mso.db.catalog.beans.VnfComponentsRecipe;
import org.openecomp.mso.db.catalog.beans.VnfRecipe;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.db.catalog.utils.RecordNotFoundException;

public class CatalogDatabaseTest {

	CatalogDatabase cd = null;
	
	@Before
	public void setup(){
		cd = CatalogDatabase.getInstance();
	}
	@Test(expected = Exception.class)
	public void getAllHeatTemplatesTestException(){
		List <HeatTemplate> list = cd.getAllHeatTemplates();
	}
	
	@Test(expected = Exception.class)
	public void getHeatTemplateTestException(){
		HeatTemplate ht = cd.getHeatTemplate(10);
	}
	
	@Test(expected = Exception.class)
	public void getHeatTemplateTest2Exception(){
		HeatTemplate ht = cd.getHeatTemplate("heat123");
	}
	
	@Test(expected = Exception.class)
	public void getHeatTemplateTest3Exception(){
		HeatTemplate ht = cd.getHeatTemplate("heat123","v2");
	}
	
	@Test(expected = Exception.class)
	public void getHeatTemplateByArtifactUuidException(){
		HeatTemplate ht = cd.getHeatTemplateByArtifactUuid("123");
	}
	
	@Test(expected = Exception.class)
	public void getHeatTemplateByArtifactUuidRegularQueryException(){
		HeatTemplate ht = cd.getHeatTemplateByArtifactUuidRegularQuery("123");
	}
	
	@Test(expected = Exception.class)
	public void getParametersForHeatTemplateTestException(){
		List<HeatTemplateParam> ht = cd.getParametersForHeatTemplate("123");
	}
	
	@Test(expected = Exception.class)
	public void getHeatEnvironmentByArtifactUuidTestException(){
		HeatEnvironment ht = cd.getHeatEnvironmentByArtifactUuid("123");
	}
	
	@Test(expected = Exception.class)
	public void getServiceByInvariantUUIDTestException(){
		Service ht = cd.getServiceByInvariantUUID("123");
	}
	
	@Test(expected = Exception.class)
	public void getServiceTestException(){
		Service ht = cd.getService("123");
	}
	
	@Test(expected = Exception.class)
	public void getServiceByModelUUIDTestException(){
		Service ht = cd.getServiceByModelUUID("123");
	}
	
	@Test(expected = Exception.class)
	public void getService2TestException(){
		HashMap<String, String> map = new HashMap<>();
		map.put("serviceNameVersionId", "v2");
		Service ht = cd.getService(map, "123");
	}
	
	@Test(expected = Exception.class)
	public void getServiceByModelNameTestException(){
		Service ht = cd.getServiceByModelName("123");
	}
	
	@Test(expected = Exception.class)
	public void getServiceByVersionAndInvariantIdTestException() throws Exception{
		Service ht = cd.getServiceByVersionAndInvariantId("123","tetwe");
	}
	
	@Test(expected = Exception.class)
	public void getServiceRecipeTestException() throws Exception{
		ServiceRecipe ht = cd.getServiceRecipe("123","tetwe");
	}
	
	@Test(expected = Exception.class)
	public void getServiceRecipeByServiceModelUuidTestException() throws Exception{
		ServiceRecipe ht = cd.getServiceRecipeByServiceModelUuid("123","tetwe");
	}
	
	@Test(expected = Exception.class)
	public void getServiceRecipesTestException() throws Exception{
		List<ServiceRecipe> ht = cd.getServiceRecipes("123");
	}
	
	@Test(expected = Exception.class)
	public void getVnfComponentTestException() throws Exception{
		VnfComponent ht = cd.getVnfComponent(123,"vnf");
	}
	
	@Test(expected = Exception.class)
	public void getVnfResourceTestException() throws Exception{
		VnfResource ht = cd.getVnfResource("vnf");
	}
	
	@Test(expected = Exception.class)
	public void getVnfResource2TestException() throws Exception{
		VnfResource ht = cd.getVnfResource("vnf","3992");
	}
	
	@Test(expected = Exception.class)
	public void getVnfResourceByModelCustomizationIdTestException() throws Exception{
		VnfResource ht = cd.getVnfResourceByModelCustomizationId("3992");
	}

    @Test(expected = Exception.class)
	public void getServiceRecipeTest2Exception() throws Exception{
		ServiceRecipe ht = cd.getServiceRecipe(1001,"3992");
	}
    
    @Test(expected = Exception.class)
    public void getVnfResourceCustomizationByModelCustomizationNameTestException(){
    	VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelCustomizationName("test", "test234");
    }
    
    @Test(expected = Exception.class)
    public void getVnfResourceByModelInvariantIdTestException(){
    	VnfResource vnf = cd.getVnfResourceByModelInvariantId("test", "test234");
    }
    
    @Test(expected = Exception.class)
    public void getVnfResourceByIdTestException(){
    	VnfResource vnf = cd.getVnfResourceById(19299);
    }
    
    @Test(expected = Exception.class)
    public void getVfModuleModelNameTestException(){
    	VfModule vnf = cd.getVfModuleModelName("tetes");
    }
    
    @Test(expected = Exception.class)
    public void getVfModuleModelName2TestException(){
    	VfModule vnf = cd.getVfModuleModelName("tetes","4kidsl");
    }
    
    @Test(expected = Exception.class)
    public void ggetVfModuleCustomizationByModelNameTestException(){
    	VfModuleCustomization vnf = cd.getVfModuleCustomizationByModelName("tetes");
    }
    
    @Test(expected = Exception.class)
    public void getNetworkResourceTestException(){
    	NetworkResource vnf = cd.getNetworkResource("tetes");
    }
    
    @Test(expected = Exception.class)
    public void getVnfRecipeTestException(){
    	VnfRecipe vnf = cd.getVnfRecipe("tetes","ergfedrf","4993493");
    }
    
    @Test(expected = Exception.class)
    public void getVnfRecipe2TestException(){
    	VnfRecipe vnf = cd.getVnfRecipe("tetes","4993493");
    }
    
    @Test(expected = Exception.class)
    public void getVnfRecipeByVfModuleIdTestException(){
    	VnfRecipe vnf = cd.getVnfRecipeByVfModuleId("tetes","4993493","vnf");
    }
    
    @Test(expected = Exception.class)
    public void getVfModuleTypeTestException(){
    	VfModule vnf = cd.getVfModuleType("4993493");
    }
    
    @Test(expected = Exception.class)
    public void getVfModuleType2TestException(){
    	VfModule vnf = cd.getVfModuleType("4993493","vnf");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceByServiceUuidTestException(){
    	VnfResource vnf = cd.getVnfResourceByServiceUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceByVnfUuidTestException(){
    	VnfResource vnf = cd.getVnfResourceByVnfUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVfModuleByModelInvariantUuidTestException(){
    	VfModule vnf = cd.getVfModuleByModelInvariantUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVfModuleByModelCustomizationUuidTestException(){
    	VfModuleCustomization vnf = cd.getVfModuleByModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVfModuleByModelInvariantUuidAndModelVersionTestException(){
    	VfModule vnf = cd.getVfModuleByModelInvariantUuidAndModelVersion("4993493","vnf");
    }
    @Test(expected = Exception.class)
    public void getVfModuleCustomizationByModelCustomizationIdTestException(){
    	VfModuleCustomization vnf = cd.getVfModuleCustomizationByModelCustomizationId("4993493");
    }
    @Test(expected = Exception.class)
    public void getVfModuleByModelUuidTestException(){
    	VfModule vnf = cd.getVfModuleByModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceCustomizationByModelCustomizationUuidTestException(){
    	VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceCustomizationByModelVersionIdTestException(){
    	VnfResourceCustomization vnf = cd.getVnfResourceCustomizationByModelVersionId("4993493");
    }
    @Test(expected = Exception.class)
    public void getVfModuleByModelCustomizationIdAndVersionTestException(){
    	cd.getVfModuleByModelCustomizationIdAndVersion("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getVfModuleByModelCustomizationIdModelVersionAndModelInvariantIdTestException(){
    	cd.getVfModuleByModelCustomizationIdModelVersionAndModelInvariantId("4993493","vnf","test");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceCustomizationByModelInvariantIdTest(){
    	cd.getVnfResourceCustomizationByModelInvariantId("4993493","vnf","test");
    }
    @Test(expected = Exception.class)
    public void getVfModuleCustomizationByVnfModuleCustomizationUuidTest(){
    	cd.getVfModuleCustomizationByVnfModuleCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionIdTest(){
    	cd.getVnfResourceCustomizationByVnfModelCustomizationNameAndModelVersionId("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllVfModuleCustomizationstest(){
    	cd.getAllVfModuleCustomizations("4993493");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceByModelUuidTest(){
    	cd.getVnfResourceByModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVnfResCustomToVfModuleTest(){
    	cd.getVnfResCustomToVfModule("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getVfModulesForVnfResourceTest(){
    	VnfResource vnfResource = new VnfResource();
    	vnfResource.setModelUuid("48839");
    	cd.getVfModulesForVnfResource(vnfResource);
    }
    @Test(expected = Exception.class)
    public void getVfModulesForVnfResource2Test(){
    	cd.getVfModulesForVnfResource("4993493");
    }
    @Test(expected = Exception.class)
    public void getServiceByUuidTest(){
    	cd.getServiceByUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getNetworkResourceById2Test(){
    	cd.getNetworkResourceById(4993493);
    }
    @Test(expected = Exception.class)
    public void getNetworkResourceByIdTest(){
        cd.getVfModuleTypeByUuid("4993493");
    }
    @Test
    public void isEmptyOrNullTest(){
    	boolean is = cd.isEmptyOrNull("4993493");
    	assertFalse(is);
    }
    @Test(expected = Exception.class)
    public void getSTRTest(){
    	cd.getSTR("4993493","test","vnf");
    }
    @Test(expected = Exception.class)
    public void getVRCtoVFMCTest(){
    	cd.getVRCtoVFMC("4993493","388492");
    }
    @Test(expected = Exception.class)
    public void getVfModuleTypeByUuidTestException(){
    	cd.getVfModuleTypeByUuid("4993493");
    }
    
    @Test(expected = Exception.class)
    public void getTempNetworkHeatTemplateLookupTest(){
    	cd.getTempNetworkHeatTemplateLookup("4993493");
    }
    
    @Test(expected = Exception.class)
    public void getAllNetworksByServiceModelUuidTest(){
    	cd.getAllNetworksByServiceModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllNetworksByServiceModelInvariantUuidTest(){
    	cd.getAllNetworksByServiceModelInvariantUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllNetworksByServiceModelInvariantUuid2Test(){
    	cd.getAllNetworksByServiceModelInvariantUuid("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllNetworksByNetworkModelCustomizationUuidTest(){
    	cd.getAllNetworksByNetworkModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllNetworksByNetworkTypeTest(){
    	cd.getAllNetworksByNetworkType("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllVfmcForVrcTest(){
    	VnfResourceCustomization re = new VnfResourceCustomization();
    	re.setModelCustomizationUuid("377483");
    	cd.getAllVfmcForVrc(re);
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceModelUuidTest(){
    	cd.getAllVnfsByServiceModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceModelInvariantUuidTest(){
    	cd.getAllVnfsByServiceModelInvariantUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceModelInvariantUuid2Test(){
    	cd.getAllVnfsByServiceModelInvariantUuid("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceNameTest(){
    	cd.getAllVnfsByServiceName("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByServiceName2Test(){
    	cd.getAllVnfsByServiceName("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllVnfsByVnfModelCustomizationUuidTest(){
    	cd.getAllVnfsByVnfModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllAllottedResourcesByServiceModelUuidTest(){
    	cd.getAllAllottedResourcesByServiceModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllAllottedResourcesByServiceModelInvariantUuidTest(){
    	cd.getAllAllottedResourcesByServiceModelInvariantUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllAllottedResourcesByServiceModelInvariantUuid2Test(){
    	cd.getAllAllottedResourcesByServiceModelInvariantUuid("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getAllAllottedResourcesByArModelCustomizationUuidTest(){
    	cd.getAllAllottedResourcesByArModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllottedResourceByModelUuidTest(){
    	cd.getAllottedResourceByModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllResourcesByServiceModelUuidTest(){
    	cd.getAllResourcesByServiceModelUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllResourcesByServiceModelInvariantUuidTest(){
    	cd.getAllResourcesByServiceModelInvariantUuid("4993493");
    }
    
    @Test(expected = Exception.class)
    public void getAllResourcesByServiceModelInvariantUuid2Test(){
    	cd.getAllResourcesByServiceModelInvariantUuid("4993493","test");
    }
    @Test(expected = Exception.class)
    public void getSingleNetworkByModelCustomizationUuidTest(){
    	cd.getSingleNetworkByModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getSingleAllottedResourceByModelCustomizationUuidTest(){
    	cd.getSingleAllottedResourceByModelCustomizationUuid("4993493");
    }
    @Test(expected = Exception.class)
    public void getVfModuleRecipeTest(){
    	cd.getVfModuleRecipe("4993493","test","get");
    }
    @Test(expected = Exception.class)
    public void getVfModuleTest(){
    	cd.getVfModule("4993493","test","get","v2","vnf");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentsRecipeTest(){
    	cd.getVnfComponentsRecipe("4993493","test","v2","vnf","get","3992");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentsRecipeByVfModuleTest(){
    	List <VfModule> resultList = new ArrayList<>();
    	VfModule m = new VfModule();
    	resultList.add(m);
    	cd.getVnfComponentsRecipeByVfModule(resultList,"4993493");
    }
    @Test(expected = Exception.class)
    public void getAllVnfResourcesTest(){
    	cd.getAllVnfResources();
    }
    @Test(expected = Exception.class)
    public void getVnfResourcesByRoleTest(){
    	cd.getVnfResourcesByRole("4993493");
    }
    @Test(expected = Exception.class)
    public void getVnfResourceCustomizationsByRoleTest(){
    	cd.getVnfResourceCustomizationsByRole("4993493");
    }
    @Test(expected = Exception.class)
    public void getAllNetworkResourcesTest(){
    	cd.getAllNetworkResources();
    }
    @Test(expected = Exception.class)
    public void getAllNetworkResourceCustomizationsTest(){
    	cd.getAllNetworkResourceCustomizations();
    }
    @Test(expected = Exception.class)
    public void getAllVfModulesTest(){
    	cd.getAllVfModules();
    }
    @Test(expected = Exception.class)
    public void getAllVfModuleCustomizationsTest(){
    	cd.getAllVfModuleCustomizations();
    }
    @Test(expected = Exception.class)
    public void getAllHeatEnvironmentTest(){
    	cd.getAllHeatEnvironment();
    }
    @Test(expected = Exception.class)
    public void getHeatEnvironment2Test(){
    	cd.getHeatEnvironment(4993493);
    }
    @Test(expected = Exception.class)
    public void getNestedTemplatesTest(){
    	cd.getNestedTemplates(4993493);
    }
    @Test(expected = Exception.class)
    public void getNestedTemplates2Test(){
    	cd.getNestedTemplates("4993493");
    }
    @Test(expected = Exception.class)
    public void getHeatFilesTest(){
    	cd.getHeatFiles(4993493);
    }
    @Test(expected = Exception.class)
    public void getVfModuleToHeatFilesEntryTest(){
    	cd.getVfModuleToHeatFilesEntry("4993493","49959499");
    }
    @Test(expected = Exception.class)
    public void getServiceToResourceCustomization(){
    	cd.getServiceToResourceCustomization("4993493","599349","49900");
    }
    @Test(expected = Exception.class)
    public void getHeatFilesForVfModuleTest(){
    	cd.getHeatFilesForVfModule("4993493");
    }
    @Test(expected = Exception.class)
    public void getHeatTemplateTest(){
    	cd.getHeatTemplate("4993493","test","heat");
    }
    
    @Test(expected = Exception.class)
    public void saveHeatTemplateTest(){
    	HeatTemplate heat = new HeatTemplate();
    	Set <HeatTemplateParam> paramSet = new HashSet<HeatTemplateParam>();
    	cd.saveHeatTemplate(heat,paramSet);
    }
    @Test(expected = Exception.class)
    public void getHeatEnvironmentTest(){
    	cd.getHeatEnvironment("4993493","test","heat");
    }
    @Test(expected = Exception.class)
    public void getHeatEnvironment3Test(){
    	cd.getHeatEnvironment("4993493","test");
    }
    @Test(expected = Exception.class)
    public void saveHeatEnvironmentTest(){
    	HeatEnvironment en = new HeatEnvironment();
    	cd.saveHeatEnvironment(en);
    }
    @Test(expected = Exception.class)
    public void saveHeatTemplate2Test(){
    	HeatTemplate heat = new HeatTemplate();
    	cd.saveHeatTemplate(heat);
    }
    @Test(expected = Exception.class)
    public void saveHeatFileTest(){
    	HeatFiles hf = new HeatFiles();
    	cd.saveHeatFile(hf);
    }
    @Test(expected = Exception.class)
    public void saveVnfRecipeTest(){
    	VnfRecipe vr = new VnfRecipe();
    	cd.saveVnfRecipe(vr);
    }
    @Test(expected = Exception.class)
    public void saveVnfComponentsRecipe(){
    	VnfComponentsRecipe vr = new VnfComponentsRecipe();
    	cd.saveVnfComponentsRecipe(vr);
    }
    @Test(expected = Exception.class)
    public void saveOrUpdateVnfResourceTest(){
    	VnfResource vr = new VnfResource();
    	cd.saveOrUpdateVnfResource(vr);
    }
    @Test(expected = Exception.class)
    public void saveVnfResourceCustomizationTest(){
    	VnfResourceCustomization vr = new VnfResourceCustomization();
    	cd.saveVnfResourceCustomization(vr);
    }
    @Test(expected = Exception.class)
    public void saveAllottedResourceCustomizationTest(){
    	AllottedResourceCustomization arc = new AllottedResourceCustomization();
    	cd.saveAllottedResourceCustomization(arc);
    }
    @Test(expected = Exception.class)
    public void saveAllottedResourceTest(){
    	AllottedResource ar = new AllottedResource();
    	cd.saveAllottedResource(ar);
    }
    @Test(expected = Exception.class)
    public void saveNetworkResourceTest() throws RecordNotFoundException {
    	NetworkResource nr = new NetworkResource();
    	cd.saveNetworkResource(nr);
    }
    @Test(expected = Exception.class)
    public void saveToscaCsarTest()throws RecordNotFoundException {
    	ToscaCsar ts = new ToscaCsar();
    	cd.saveToscaCsar(ts);
    }
    @Test(expected = Exception.class)
    public void getToscaCsar(){
    	cd.getToscaCsar("4993493");
    }
    @Test(expected = Exception.class)
    public void saveTempNetworkHeatTemplateLookupTest(){
    	TempNetworkHeatTemplateLookup t = new TempNetworkHeatTemplateLookup();
    	cd.saveTempNetworkHeatTemplateLookup(t);
    }
    @Test(expected = Exception.class)
    public void saveVfModuleToHeatFiles(){
    	VfModuleToHeatFiles v = new VfModuleToHeatFiles();
    	cd.saveVfModuleToHeatFiles(v);
    }
    @Test(expected = Exception.class)
    public void saveVnfResourceToVfModuleCustomizationTest() throws RecordNotFoundException {
    	VnfResourceCustomization v =new VnfResourceCustomization();
    	VfModuleCustomization vm = new VfModuleCustomization();
    	cd.saveVnfResourceToVfModuleCustomization(v, vm);
    }
    @Test(expected = Exception.class)
    public void saveNetworkResourceCustomizationTest() throws RecordNotFoundException {
    	NetworkResourceCustomization nrc = new NetworkResourceCustomization();
    	cd.saveNetworkResourceCustomization(nrc);
    }
    
    @Test(expected = Exception.class)
    public void saveServiceToNetworksTest(){
    	AllottedResource ar = new AllottedResource();
    	cd.saveAllottedResource(ar);
    }
    @Test(expected = Exception.class)
    public void saveServiceToResourceCustomizationTest(){
    	ServiceToResourceCustomization ar = new ServiceToResourceCustomization();
    	cd.saveServiceToResourceCustomization(ar);
    }
    @Test(expected = Exception.class)
    public void saveServiceTest(){
    	Service ar = new Service();
    	cd.saveService(ar);
    }
    @Test(expected = Exception.class)
    public void saveOrUpdateVfModuleTest(){
    	VfModule ar = new VfModule();
    	cd.saveOrUpdateVfModule(ar);
    }
    @Test(expected = Exception.class)
    public void saveOrUpdateVfModuleCustomizationTest(){
    	VfModuleCustomization ar = new VfModuleCustomization();
    	cd.saveOrUpdateVfModuleCustomization(ar);
    }
    
    @Test(expected = Exception.class)
    public void getNestedHeatTemplateTest(){
    	cd.getNestedHeatTemplate(101,201);
    }
    @Test(expected = Exception.class)
    public void getNestedHeatTemplate2Test(){
    	cd.getNestedHeatTemplate("1002","1002");
    }
    @Test(expected = Exception.class)
    public void saveNestedHeatTemplateTest(){
    	HeatTemplate ar = new HeatTemplate();
    	cd.saveNestedHeatTemplate("1001",ar,"test");
    }
    @Test(expected = Exception.class)
    public void getHeatFiles2Test(){
    	VfModuleCustomization ar = new VfModuleCustomization();
    	cd.getHeatFiles(101,"test","1001","v2");
    }
    @Test(expected = Exception.class)
    public void getHeatFiles3Test(){
    	VfModuleCustomization ar = new VfModuleCustomization();
    	cd.getHeatFiles("200192");
    }
    @Test(expected = Exception.class)
    public void saveHeatFilesTest(){
    	HeatFiles ar = new HeatFiles();
    	cd.saveHeatFiles(ar);
    }
    @Test(expected = Exception.class)
    public void saveVfModuleToHeatFilesTest(){
    	HeatFiles ar = new HeatFiles();
    	cd.saveVfModuleToHeatFiles("3772893",ar);
    }
    @Test
    public void getNetworkResourceByModelUuidTest(){
    	
    	cd.getNetworkResourceByModelUuid("3899291");
    }
    @Test(expected = Exception.class)
    public void getNetworkRecipeTest(){
    	
    	cd.getNetworkRecipe("test","test1","test2");
    }
    @Test(expected = Exception.class)
    public void getNetworkRecipe2Test(){
    	
    	cd.getNetworkRecipe("test","test1");
    }
    @Test
    public void getNetworkResourceByModelCustUuidTest(){
    	
    	cd.getNetworkResourceByModelCustUuid("test");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentsRecipe2Test(){
    	
    	cd.getVnfComponentsRecipe("test1","test2","test3","test4");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentsRecipeByVfModuleModelUUIdTest(){
    	
    	cd.getVnfComponentsRecipeByVfModuleModelUUId("test1","test2","test3");
    }
    @Test(expected = Exception.class)
    public void getVnfComponentRecipesTest(){
    	
    	cd.getVnfComponentRecipes("test");
    }
    @Test(expected = Exception.class)
    public void saveOrUpdateVnfComponentTest(){
    	VnfComponent ar = new VnfComponent();
    	cd.saveOrUpdateVnfComponent(ar);
    }
    
    @Test(expected = Exception.class)
    public void getVfModule2Test(){
    	
    	cd.getVfModule("test");
    }
    @Test(expected = Exception.class)
    public void getVfModuleByModelUUIDTest(){
    	
    	cd.getVfModuleByModelUUID("test");
    }
    @Test(expected = Exception.class)
    public void getServiceRecipeByModelUUIDTest(){
    	
    	cd.getServiceRecipeByModelUUID("test1","test2");
    }
    @Test(expected = Exception.class)
    public void getModelRecipeTest(){
    	
    	cd.getModelRecipe("test1","test2","test3");
    }
    @Test(expected = Exception.class)
    public void healthCheck(){
    	
    	cd.healthCheck();
    }
    @Test(expected = Exception.class)
    public void executeQuerySingleRow(){
    	VnfComponent ar = new VnfComponent();
    	HashMap<String, String> variables = new HashMap<String, String>();
    	cd.executeQuerySingleRow("tets",variables,false);
    }
    @Test(expected = Exception.class)
    public void executeQueryMultipleRows(){
    	HashMap<String, String> variables = new HashMap<String, String>();
    	cd.executeQueryMultipleRows("select",variables,false);
    }
}
