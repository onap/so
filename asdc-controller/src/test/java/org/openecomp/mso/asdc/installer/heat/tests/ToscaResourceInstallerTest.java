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

package org.openecomp.mso.asdc.installer.heat.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.asdc.client.tests.ASDCControllerTest;
import org.openecomp.mso.asdc.installer.ToscaResourceStructure;
import org.openecomp.mso.asdc.installer.VfResourceStructure;
import org.openecomp.mso.asdc.installer.heat.ToscaResourceInstaller;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceToAllottedResources;
import org.openecomp.mso.db.catalog.beans.ServiceToNetworks;
import org.openecomp.mso.db.catalog.beans.ServiceToResourceCustomization;
import org.openecomp.mso.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VfModuleToHeatFiles;
import org.openecomp.mso.db.catalog.beans.VnfResCustomToVfModuleCustom;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.impl.FilterType;
import org.openecomp.sdc.tosca.parser.impl.SdcTypes;
import org.openecomp.sdc.toscaparser.api.CapabilityAssignment;
import org.openecomp.sdc.toscaparser.api.CapabilityAssignments;
import org.openecomp.sdc.toscaparser.api.Group;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.RequirementAssignments;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;
import org.openecomp.sdc.toscaparser.api.parameters.Input;
import org.openecomp.sdc.utils.DistributionActionResultEnum;

import mockit.Mock;
import mockit.MockUp;

public class ToscaResourceInstallerTest {

	private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

	private static String heatExample;
	private static String heatExampleMD5HashBase64;

	private static INotificationData iNotif;

	private static IDistributionClientDownloadResult downloadResult;
	private static IDistributionClientDownloadResult downloadCorruptedResult;

	private static IDistributionClientResult successfulClientInitResult;
	private static IDistributionClientResult unsuccessfulClientInitResult;

	private static IDistributionClient distributionClient;

	private static IArtifactInfo artifactInfo1;

	private static IResourceInstance resource1;

	private static VfResourceStructure vrs;

	public static final String ASDC_PROP = MsoJavaProperties.class.getClassLoader().getResource("mso.json").toString()
			.substring(5);
	public static final String ASDC_PROP2 = MsoJavaProperties.class.getClassLoader().getResource("mso2.json").toString()
			.substring(5);
	public static final String ASDC_PROP3 = MsoJavaProperties.class.getClassLoader().getResource("mso3.json").toString()
			.substring(5);
	public static final String ASDC_PROP_BAD = MsoJavaProperties.class.getClassLoader().getResource("mso-bad.json")
			.toString().substring(5);
	public static final String ASDC_PROP_WITH_NULL = MsoJavaProperties.class.getClassLoader()
			.getResource("mso-with-NULL.json").toString().substring(5);

	@BeforeClass
	public static final void prepareMockNotification() throws MsoPropertiesException, IOException, URISyntaxException,
			NoSuchAlgorithmException, ArtifactInstallerException {

		heatExample = new String(Files.readAllBytes(Paths.get(
				ASDCControllerTest.class.getClassLoader().getResource("resource-examples/autoscaling.yaml").toURI())));
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] md5Hash = md.digest(heatExample.getBytes());
		heatExampleMD5HashBase64 = Base64.encodeBase64String(md5Hash);

		iNotif = Mockito.mock(INotificationData.class);

		// Create fake ArtifactInfo
		artifactInfo1 = Mockito.mock(IArtifactInfo.class);
		Mockito.when(artifactInfo1.getArtifactChecksum())
				.thenReturn(ToscaResourceInstallerTest.heatExampleMD5HashBase64);

		Mockito.when(artifactInfo1.getArtifactName()).thenReturn("artifact1");
		Mockito.when(artifactInfo1.getArtifactType()).thenReturn(ASDCConfiguration.HEAT);
		Mockito.when(artifactInfo1.getArtifactURL())
				.thenReturn("https://localhost:8080/v1/catalog/services/srv1/2.0/resources/aaa/1.0/artifacts/aaa.yml");
		Mockito.when(artifactInfo1.getArtifactUUID()).thenReturn("UUID1");
		Mockito.when(artifactInfo1.getArtifactDescription()).thenReturn("testos artifact1");

		distributionClient = Mockito.mock(IDistributionClient.class);

		// Now provision the NotificationData mock
		List<IArtifactInfo> listArtifact = new ArrayList<IArtifactInfo>();
		listArtifact.add(artifactInfo1);

		// Create fake resource Instance
		resource1 = Mockito.mock(IResourceInstance.class);
		// Mockito.when(resource1.getResourceType()).thenReturn("VF");
		Mockito.when(resource1.getResourceName()).thenReturn("resourceName");
		Mockito.when(resource1.getArtifacts()).thenReturn(listArtifact);

		List<IResourceInstance> resources = new ArrayList<>();
		resources.add(resource1);

		Mockito.when(iNotif.getResources()).thenReturn(resources);
		Mockito.when(iNotif.getDistributionID()).thenReturn("distributionID1");
		Mockito.when(iNotif.getServiceName()).thenReturn("serviceName1");
		Mockito.when(iNotif.getServiceUUID()).thenReturn("serviceNameUUID1");
		Mockito.when(iNotif.getServiceVersion()).thenReturn("1.0");

		downloadResult = Mockito.mock(IDistributionClientDownloadResult.class);
		Mockito.when(downloadResult.getArtifactPayload()).thenReturn(heatExample.getBytes());
		Mockito.when(downloadResult.getDistributionActionResult()).thenReturn(DistributionActionResultEnum.SUCCESS);
		Mockito.when(downloadResult.getDistributionMessageResult()).thenReturn("Success");

		downloadCorruptedResult = Mockito.mock(IDistributionClientDownloadResult.class);
		Mockito.when(downloadCorruptedResult.getArtifactPayload()).thenReturn((heatExample + "badone").getBytes());
		Mockito.when(downloadCorruptedResult.getDistributionActionResult())
				.thenReturn(DistributionActionResultEnum.SUCCESS);
		Mockito.when(downloadCorruptedResult.getDistributionMessageResult()).thenReturn("Success");

		vrs = new VfResourceStructure(iNotif, resource1);
		try {
			vrs.addArtifactToStructure(distributionClient, artifactInfo1, downloadResult);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			vrs.createVfModuleStructures();
		} catch (ArtifactInstallerException e) {
			e.printStackTrace();
		}
		vrs.getNotification();
		vrs.getArtifactsMapByUUID();
		vrs.getCatalogNetworkResourceCustomization();
		vrs.getCatalogResourceCustomization();
		vrs.getCatalogService();
		vrs.getCatalogServiceToAllottedResources();
		vrs.getCatalogServiceToNetworks();
		vrs.getCatalogVnfResource();
		vrs.getResourceInstance();
		vrs.getVfModulesStructureList();
		vrs.getVfModuleStructure();
		vrs.setCatalogNetworkResourceCustomization(new NetworkResourceCustomization());
		vrs.setCatalogResourceCustomization(new AllottedResourceCustomization());
		vrs.setCatalogService(new Service());
		vrs.setCatalogServiceToAllottedResources(new ServiceToAllottedResources());
		vrs.setCatalogServiceToNetworks(new ServiceToNetworks());
		vrs.setCatalogVnfResource(new VnfResource());
		vrs.setSuccessfulDeployment();

		AllottedResourceCustomization arc = new AllottedResourceCustomization();
		arc.setModelCustomizationUuid("modelCustomizationUuid");
		List<AllottedResourceCustomization> allottedResources = new ArrayList<>();
		allottedResources.add(arc);

		NetworkResourceCustomization nrc = new NetworkResourceCustomization();
		nrc.setModelCustomizationUuid("modelCustomizationUuid");
		List<NetworkResourceCustomization> networkResources = new ArrayList<>();
		networkResources.add(nrc);

		new MockUp<CatalogDatabase>() {
			@Mock
			public List<AllottedResourceCustomization> getAllAllottedResourcesByServiceModelUuid(
					String serviceModelUuid) {
				return allottedResources;
			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public List<NetworkResourceCustomization> getAllNetworksByServiceModelUuid(String serviceModelUuid) {
				return networkResources;
			}
		};

		// Mock now the ASDC distribution client behavior
		successfulClientInitResult = Mockito.mock(IDistributionClientResult.class);
		Mockito.when(successfulClientInitResult.getDistributionActionResult())
				.thenReturn(DistributionActionResultEnum.SUCCESS);

		unsuccessfulClientInitResult = Mockito.mock(IDistributionClientResult.class);
		Mockito.when(unsuccessfulClientInitResult.getDistributionActionResult())
				.thenReturn(DistributionActionResultEnum.GENERAL_ERROR);

	}

	@Before
	public final void initBeforeEachTest() throws MsoPropertiesException {
		// load the config
		msoPropertiesFactory.removeAllMsoProperties();
		msoPropertiesFactory.initializeMsoProperties(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
	}

	@AfterClass
	public static final void kill() throws MsoPropertiesException {

		msoPropertiesFactory.removeMsoProperties(ASDCConfiguration.MSO_PROP_ASDC);

	}

	@Test
	public void isResourceAlreadyDeployedAllotedResourceTest() {
		Mockito.when(resource1.getResourceType()).thenReturn("VF");
		Mockito.when(resource1.getCategory()).thenReturn("Allotted Resource");
		ToscaResourceInstaller tri = new ToscaResourceInstaller();

		try {
			tri.isResourceAlreadyDeployed(vrs);
		} catch (ArtifactInstallerException e) {
		}
	}

	@Test(expected=Exception.class)
	public void installTheResourceTest() {

		ToscaResourceStructure trs = new ToscaResourceStructure();
		trs.getAllottedResource();
		trs.getAllottedList();
		trs.getCatalogAllottedResourceCustomization();
		trs.getCatalogAllottedServiceToResourceCustomization();
		trs.getCatalogNetworkResource();
		trs.getCatalogNetworkResourceCustomization();
		trs.getCatalogResourceCustomization();
		trs.getCatalogService();
		trs.getCatalogTempNetworkHeatTemplateLookup();
		trs.getCatalogToscaCsar();
		trs.getCatalogVfModule();
		trs.getCatalogVfModuleCustomization();
		trs.getCatalogVfModuleToHeatFiles();
		trs.getCatalogVfServiceToResourceCustomization();
		trs.getCatalogVlServiceToResourceCustomization();
		trs.getCatalogVnfResCustomToVfModuleCustom();
		trs.getCatalogVnfResource();
		trs.getCatalogVnfResourceCustomization();
		trs.getEnvHeatTemplateUUID();
		trs.getHeatFilesUUID();
		trs.getHeatTemplateUUID();
		trs.getNetworkTypes();
		trs.getSdcCsarHelper();
		trs.getServiceMetadata();
		trs.getServiceToResourceCustomization();
		trs.getServiceVersion();
		trs.getToscaArtifact();
		trs.getVfTypes();
		trs.getVolHeatEnvTemplateUUID();
		trs.getVolHeatTemplateUUID();

		NodeTemplate nodeTemplate = Mockito.mock(NodeTemplate.class);
		List<NodeTemplate> alnt = new ArrayList<>();
		trs.setAllottedList(alnt);
		trs.setAllottedResource(new AllottedResource());
		trs.setCatalogAllottedResourceCustomization(new AllottedResourceCustomization());
		trs.setCatalogAllottedServiceToResourceCustomization(new ServiceToResourceCustomization());
		trs.setCatalogNetworkResource(new NetworkResource());
		trs.setCatalogNetworkResourceCustomization(new NetworkResourceCustomization());
		trs.setCatalogResourceCustomization(new AllottedResourceCustomization());
		trs.setCatalogService(new Service());
		trs.setCatalogTempNetworkHeatTemplateLookup(new TempNetworkHeatTemplateLookup());
		trs.setCatalogToscaCsar(new ToscaCsar());
		trs.setCatalogVfModule(new VfModule());
		trs.setCatalogVfModuleCustomization(new VfModuleCustomization());
		trs.setCatalogVfModuleToHeatFiles(new VfModuleToHeatFiles());
		trs.setCatalogVfServiceToResourceCustomization(new ServiceToResourceCustomization());
		trs.setCatalogVlServiceToResourceCustomization(new ServiceToResourceCustomization());
		trs.setCatalogVnfResCustomToVfModuleCustom(new VnfResCustomToVfModuleCustom());
		trs.setCatalogVnfResource(new VnfResource());
		trs.setCatalogVnfResourceCustomization(new VnfResourceCustomization());
		trs.setEnvHeatTemplateUUID("envHeatTemplateUUID");
		trs.setHeatFilesUUID("heatFilesUUID");
		trs.setHeatTemplateUUID("heatTemplateUUID");
		trs.setNetworkTypes(alnt);
		trs.setVolHeatTemplateUUID("volHeatTemplateUUID");
		trs.setSdcCsarHelper(new ISdcCsarHelper() {

			@Override
			public boolean hasTopology(NodeTemplate arg0) {
				return false;
			}

			@Override
			public NodeTemplate getVnfConfig(String arg0) {
				return null;
			}

			@Override
			public List<NodeTemplate> getVfcListByVf(String arg0) {
				return null;
			}

			@Override
			public List<Group> getVfModulesByVf(String arg0) {
				return null;
			}

			@Override
			public String getTypeOfNodeTemplate(NodeTemplate arg0) {
				return null;
			}

			@Override
			public List<NodeTemplate> getServiceVlList() {
				return null;
			}

			@Override
			public List<NodeTemplate> getServiceVfList() {
				return null;
			}

			@Override
			public String getServiceSubstitutionMappingsTypeName() {
				return null;
			}

			@Override
			public List<NodeTemplate> getServiceNodeTemplatesByType(String arg0) {
				return null;
			}

			@Override
			public List<NodeTemplate> getServiceNodeTemplates() {
				return null;
			}

			@Override
			public List<NodeTemplate> getServiceNodeTemplateBySdcType(SdcTypes arg0) {
				return null;
			}

			@Override
			public Map<String, Object> getServiceMetadataProperties() {
				return null;
			}

			@Override
			public Metadata getServiceMetadata() {
				return null;
			}

			@Override
			public List<Input> getServiceInputs() {
				return null;
			}

			@Override
			public Object getServiceInputLeafValueOfDefaultAsObject(String arg0) {
				return null;
			}

			@Override
			public String getServiceInputLeafValueOfDefault(String arg0) {
				return null;
			}

			@Override
			public String getNodeTemplatePropertyLeafValue(NodeTemplate arg0, String arg1) {
				return null;
			}

			@Override
			public Object getNodeTemplatePropertyAsObject(NodeTemplate arg0, String arg1) {
				return null;
			}

			@Override
			public List<Pair<NodeTemplate, NodeTemplate>> getNodeTemplatePairsByReqName(List<NodeTemplate> arg0,
					List<NodeTemplate> arg1, String arg2) {
				return null;
			}

			@Override
			public String getNodeTemplateCustomizationUuid(NodeTemplate arg0) {
				return null;
			}

			@Override
			public List<NodeTemplate> getNodeTemplateChildren(NodeTemplate arg0) {
				return null;
			}

			@Override
			public List<NodeTemplate> getNodeTemplateBySdcType(NodeTemplate arg0, SdcTypes arg1) {
				return null;
			}

			@Override
			public String getMetadataPropertyValue(Metadata arg0, String arg1) {
				return null;
			}

			@Override
			public List<NodeTemplate> getMembersOfVfModule(NodeTemplate arg0, Group arg1) {
				return null;
			}

			@Override
			public String getGroupPropertyLeafValue(Group arg0, String arg1) {
				return null;
			}

			@Override
			public Object getGroupPropertyAsObject(Group arg0, String arg1) {
				return null;
			}

			@Override
			public Map<String, Map<String, Object>> getCpPropertiesFromVfcAsObject(NodeTemplate arg0) {
				return null;
			}

			@Override
			public Map<String, Map<String, Object>> getCpPropertiesFromVfc(NodeTemplate arg0) {
				return null;
			}

			@Override
			public List<NodeTemplate> getCpListByVf(String arg0) {
				return null;
			}

			@Override
			public String getConformanceLevel() {
				return null;
			}

			@Override
			public List<NodeTemplate> getAllottedResources() {
				return null;
			}

			@Override
			public Map<String, String> filterNodeTemplatePropertiesByValue(NodeTemplate arg0, FilterType arg1,
					String arg2) {
				return null;
			}

			@Override
			public CapabilityAssignments getCapabilitiesOf(NodeTemplate arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getCapabilityPropertyLeafValue(CapabilityAssignment arg0, String arg1) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Metadata getNodeTemplateMetadata(NodeTemplate arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public RequirementAssignments getRequirementsOf(NodeTemplate arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Map<String, String> getServiceMetadataAllProperties() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public NodeTemplate getServiceNodeTemplateByNodeName(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		// trs.setServiceMetadata(new Metadata(new HashMap<>()));
		trs.setServiceToResourceCustomization(new ServiceToResourceCustomization());
		trs.setServiceVersion("1.0");
		trs.setToscaArtifact(new IArtifactInfo() {

			@Override
			public List<IArtifactInfo> getRelatedArtifacts() {
				return null;
			}

			@Override
			public IArtifactInfo getGeneratedArtifact() {
				return null;
			}

			@Override
			public String getArtifactVersion() {
				return null;
			}

			@Override
			public String getArtifactUUID() {
				return null;
			}

			@Override
			public String getArtifactURL() {
				return null;
			}

			@Override
			public String getArtifactType() {
				return null;
			}

			@Override
			public Integer getArtifactTimeout() {
				return null;
			}

			@Override
			public String getArtifactName() {
				return null;
			}

			@Override
			public String getArtifactDescription() {
				return null;
			}

			@Override
			public String getArtifactChecksum() {
				return null;
			}
		});
		trs.setVfTypes(alnt);
		trs.setVnfAlreadyInstalled(true);
		trs.setVolHeatEnvTemplateUUID("volHeatEnvTemplateUUID");
		trs.isVnfAlreadyInstalled();

		try{
		trs.updateResourceStructure(artifactInfo1);
		
		}catch(Exception e){}
		
		ToscaResourceInstaller tri = new ToscaResourceInstaller();

		try {
			tri.installTheResource(trs, vrs);
		} catch (ArtifactInstallerException e) {
		}
	}
}
