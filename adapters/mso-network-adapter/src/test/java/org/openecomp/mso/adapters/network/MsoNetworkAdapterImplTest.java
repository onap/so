/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.network;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.ws.Holder;

import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.adapters.network.exceptions.NetworkException;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.openstack.beans.NetworkInfo;
import org.openecomp.mso.openstack.beans.NetworkRollback;
import org.openecomp.mso.openstack.beans.NetworkStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.beans.Subnet;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.openstack.utils.MsoHeatUtilsWithUpdate;
import org.openecomp.mso.openstack.utils.MsoNeutronUtils;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class MsoNetworkAdapterImplTest {

	@Test
	public void createNetworkImplTest_CloudSiteNotPresent() throws NetworkException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(null);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_NeutronMode_QueryNetworkThrowsException()
			throws NetworkException, MsoException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		MsoException exception = Mockito.mock(MsoException.class);
		when(impl.neutron.queryNetwork(any(), any(), any())).thenThrow(exception);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_NeutronMode_QueryNetworkExists_FailIfExistsTrue()
			throws NetworkException, MsoException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		NetworkInfo netInfo = Mockito.mock(NetworkInfo.class);
		when(impl.neutron.queryNetwork(any(), any(), any())).thenReturn(netInfo);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_NeutronMode_QueryNetworkExists_FailIfExistsNotTrue()
			throws NetworkException, MsoException {
		Holder<String> networkId = new Holder<>();
		Holder<String> neutronNetworkId = new Holder<>();
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		NetworkInfo netInfo = Mockito.mock(NetworkInfo.class);
		when(impl.neutron.queryNetwork(any(), any(), any())).thenReturn(netInfo);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, false, true, null, null, networkId, neutronNetworkId, subnetIdMap,
					rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_NeutronMode_NetInfoDoesntExist_CreateNetworkException()
			throws NetworkException, MsoException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		NetworkInfo netInfo = null;
		MsoException exception = Mockito.mock(MsoException.class);
		when(impl.neutron.queryNetwork(any(), any(), any())).thenReturn(netInfo);
		when(impl.neutron.createNetwork(any(), any(), any(), any(), any(), any())).thenThrow(exception);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_NeutronMode_NetInfoDoesntExist_CreateNetwork()
			throws NetworkException, MsoException {
		Holder<String> networkId = new Holder<>();
		Holder<String> neutronNetworkId = new Holder<>();
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		NetworkInfo netInfo = Mockito.mock(NetworkInfo.class);
		when(netInfo.getId()).thenReturn("Id");
		when(impl.neutron.queryNetwork(any(), any(), any())).thenReturn(null);
		when(impl.neutron.createNetwork(any(), any(), any(), any(), any(), anyList())).thenReturn(netInfo);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, networkId, neutronNetworkId, subnetIdMap,
					rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_NeutronMode_NetInfoExists()
			throws NetworkException, MsoException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		NetworkInfo netInfo = null;
		when(impl.neutron.queryNetwork(any(), any(), any())).thenReturn(netInfo);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_HeatMode_HeatTemplateNull() throws NetworkException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_HeatMode_HeatTemplateNotNull_ThrowMsoPropsException()
			throws NetworkException, MsoPropertiesException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		HeatTemplate heatTemplate = Mockito.mock(HeatTemplate.class);
		when(heatTemplate.toString()).thenReturn("heatTemplate");
		when(heatTemplate.getHeatTemplate()).thenReturn("heatTemplate");
		when(catalogDB.getHeatTemplateByArtifactUuidRegularQuery(any())).thenReturn(heatTemplate);
		MsoPropertiesException exception = Mockito.mock(MsoPropertiesException.class);
		when(impl.msoPropertiesFactory.getMsoJavaProperties(any())).thenThrow(exception);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_HeatMode_HeatTemplateNotNull()
			throws NetworkException, MsoPropertiesException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		HeatTemplate heatTemplate = Mockito.mock(HeatTemplate.class);
		when(heatTemplate.toString()).thenReturn("heatTemplate");
		when(heatTemplate.getHeatTemplate()).thenReturn("heatTemplateaic");
		when(catalogDB.getHeatTemplateByArtifactUuidRegularQuery(any())).thenReturn(heatTemplate);
		MsoPropertiesException exception = Mockito.mock(MsoPropertiesException.class);
		MsoJavaProperties props = Mockito.mock(MsoJavaProperties.class);
		when(impl.msoPropertiesFactory.getMsoJavaProperties(any())).thenReturn(props);
		when(props.getProperty(anyString(), anyString())).thenReturn("aic");
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_HeatMode_HeatTemplateNotNull_QueryStackThrowsException()
			throws NetworkException, MsoPropertiesException, MsoException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		HeatTemplate heatTemplate = Mockito.mock(HeatTemplate.class);
		when(heatTemplate.toString()).thenReturn("heatTemplate");
		when(heatTemplate.getHeatTemplate()).thenReturn("heatTemplateaic");
		when(catalogDB.getHeatTemplateByArtifactUuidRegularQuery(any())).thenReturn(heatTemplate);
		MsoJavaProperties props = Mockito.mock(MsoJavaProperties.class);
		when(impl.msoPropertiesFactory.getMsoJavaProperties(any())).thenReturn(props);
		when(props.getProperty(anyString(), anyString())).thenReturn("aic");
		MsoException exception = Mockito.mock(MsoException.class);
		impl.heat = Mockito.mock(MsoHeatUtils.class);
		when(impl.heat.queryStack(anyString(), anyString(), anyString())).thenThrow(exception);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_HeatMode_HeatTemplateNotNull_QueryStack_HeatStackNull()
			throws NetworkException, MsoPropertiesException, MsoException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		HeatTemplate heatTemplate = Mockito.mock(HeatTemplate.class);
		when(heatTemplate.toString()).thenReturn("heatTemplate");
		when(heatTemplate.getHeatTemplate()).thenReturn("heatTemplateaic");
		when(catalogDB.getHeatTemplateByArtifactUuidRegularQuery(any())).thenReturn(heatTemplate);
		MsoJavaProperties props = Mockito.mock(MsoJavaProperties.class);
		when(impl.msoPropertiesFactory.getMsoJavaProperties(any())).thenReturn(props);
		when(props.getProperty(anyString(), anyString())).thenReturn("aic");
		StackInfo heatStack = null;
		impl.heat = Mockito.mock(MsoHeatUtils.class);
		when(impl.heat.queryStack(anyString(), anyString(), anyString())).thenReturn(heatStack);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_HeatMode_HeatTemplateNotNull_QueryStack_HeatStackNotNull_FailIfExists()
			throws NetworkException, MsoPropertiesException, MsoException {
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		HeatTemplate heatTemplate = Mockito.mock(HeatTemplate.class);
		when(heatTemplate.toString()).thenReturn("heatTemplate");
		when(heatTemplate.getHeatTemplate()).thenReturn("heatTemplateaic");
		when(catalogDB.getHeatTemplateByArtifactUuidRegularQuery(any())).thenReturn(heatTemplate);
		MsoJavaProperties props = Mockito.mock(MsoJavaProperties.class);
		when(impl.msoPropertiesFactory.getMsoJavaProperties(any())).thenReturn(props);
		when(props.getProperty(anyString(), anyString())).thenReturn("aic");
		StackInfo heatStack = Mockito.mock(StackInfo.class);
		impl.heat = Mockito.mock(MsoHeatUtils.class);
		when(impl.heat.queryStack(anyString(), anyString(), anyString())).thenReturn(heatStack);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, true, true, null, null, null, null, null, null);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_HeatMode_HeatTemplateNotNull_QueryStack_HeatStackNotNull_DontFailIfExists()
			throws NetworkException, MsoPropertiesException, MsoException {
		Holder<String> networkId = new Holder<>();
		Holder<String> neutronNetworkId = new Holder<>();
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		HeatTemplate heatTemplate = Mockito.mock(HeatTemplate.class);
		when(heatTemplate.toString()).thenReturn("heatTemplate");
		when(heatTemplate.getHeatTemplate()).thenReturn("heatTemplateaic");
		when(catalogDB.getHeatTemplateByArtifactUuidRegularQuery(any())).thenReturn(heatTemplate);
		MsoJavaProperties props = Mockito.mock(MsoJavaProperties.class);
		when(impl.msoPropertiesFactory.getMsoJavaProperties(any())).thenReturn(props);
		when(props.getProperty(anyString(), anyString())).thenReturn("aic");
		StackInfo heatStack = Mockito.mock(StackInfo.class);
		Map<String, Object> outputs = new HashMap<>();
		outputs.put("subnet", "");
		when(heatStack.getOutputs()).thenReturn(outputs);
		impl.heat = Mockito.mock(MsoHeatUtils.class);
		when(impl.heat.queryStack(anyString(), anyString(), anyString())).thenReturn(heatStack);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, false, true, null, null, networkId, neutronNetworkId, subnetIdMap,
					rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void createNetworkImplTest_CloudSitePresent_HeatMode_HeatTemplateNotNull_QueryStack_HeatStackNotNull_DontFailIfExists_Validate()
			throws NetworkException, MsoPropertiesException, MsoException {
		List<Subnet> subnets = new ArrayList<>();
		Holder<String> networkId = new Holder<>();
		Holder<String> neutronNetworkId = new Holder<>();
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		HeatTemplate heatTemplate = Mockito.mock(HeatTemplate.class);
		when(heatTemplate.toString()).thenReturn("heatTemplate");
		when(heatTemplate.getHeatTemplate()).thenReturn("heatTemplateaic");
		when(catalogDB.getHeatTemplateByArtifactUuidRegularQuery(any())).thenReturn(heatTemplate);
		MsoJavaProperties props = Mockito.mock(MsoJavaProperties.class);
		when(impl.msoPropertiesFactory.getMsoJavaProperties(any())).thenReturn(props);
		when(props.getProperty(anyString(), anyString())).thenReturn("aic");
		StackInfo heatStack = Mockito.mock(StackInfo.class);
		Map<String, Object> outputs = new HashMap<>();
		outputs.put("subnet", "");
		when(heatStack.getOutputs()).thenReturn(outputs);
		impl.heat = Mockito.mock(MsoHeatUtils.class);
		when(impl.heat.queryStack(anyString(), anyString(), anyString())).thenReturn(heatStack);
		try {
			impl.createNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
					"physicalNetworkName", null, false, true, subnets, null, networkId, neutronNetworkId, subnetIdMap,
					rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void updateNetworkImplTest_CloudSiteNotPresent() throws NetworkException {
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(null);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		try {
			impl.updateNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
					"networkName", "physicalNetworkName", null, null, null, subnetIdMap, rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void updateNetworkImplTest_CloudSitePresent_NeutronMode_QueryNetworkThrowsException()
			throws NetworkException, MsoException {
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		MsoException exception = Mockito.mock(MsoException.class);
		when(impl.neutron.queryNetwork(any(), any(), any())).thenThrow(exception);
		try {
			impl.updateNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
					"networkName", "physicalNetworkName", null, null, null, subnetIdMap, rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void updateNetworkImplTest_CloudSitePresent_NeutronMode_QueryNetworkReturnsNull()
			throws NetworkException, MsoException {
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		when(impl.neutron.queryNetwork(any(), any(), any())).thenReturn(null);
		try {
			impl.updateNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
					"networkName", "physicalNetworkName", null, null, null, subnetIdMap, rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void updateNetworkImplTest_CloudSitePresent_NeutronMode_QueryNetworkDoesntExist_UpdateNetworkException()
			throws NetworkException, MsoException {
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		NetworkInfo netInfo = new NetworkInfo(null);
		MsoException exception = Mockito.mock(MsoException.class);
		when(impl.neutron.queryNetwork(any(), any(), any())).thenReturn(netInfo);
		when(impl.neutron.updateNetwork(any(), any(), any(), any(), any(), anyList())).thenThrow(exception);
		try {
			impl.updateNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
					"networkName", "physicalNetworkName", null, null, null, subnetIdMap, rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void updateNetworkImplTest_CloudSitePresent_NeutronMode_QueryNetworkDoesntExist_UpdateNetwork()
			throws NetworkException, MsoException {
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.neutron = Mockito.mock(MsoNeutronUtils.class);
		NetworkInfo netInfo = new NetworkInfo(null);
		NetworkInfo mockedNetworkInfo = Mockito.mock(NetworkInfo.class);
		when(mockedNetworkInfo.getId()).thenReturn("Id");
		when(impl.neutron.queryNetwork(any(), any(), any())).thenReturn(netInfo);
		when(impl.neutron.updateNetwork(any(), any(), any(), any(), any(), anyList())).thenReturn(mockedNetworkInfo);
		try {
			impl.updateNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
					"networkName", "physicalNetworkName", null, null, null, subnetIdMap, rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void updateNetworkImplTest_CloudSitePresent_HeatMode_QueryStackThrowException()
			throws NetworkException, MsoException {
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.heatWithUpdate = Mockito.mock(MsoHeatUtilsWithUpdate.class);
		MsoException exception = Mockito.mock(MsoException.class);
		when(impl.heatWithUpdate.queryStack(any(), any(), any())).thenThrow(exception);
		try {
			impl.updateNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
					"networkName", "physicalNetworkName", null, null, null, subnetIdMap, rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void updateNetworkImplTest_CloudSitePresent_HeatMode_QueryStackReturnNull()
			throws NetworkException, MsoException {
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.heatWithUpdate = Mockito.mock(MsoHeatUtilsWithUpdate.class);
		MsoException exception = Mockito.mock(MsoException.class);
		when(impl.heatWithUpdate.queryStack(any(), any(), any())).thenReturn(null);
		try {
			impl.updateNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
					"networkName", "physicalNetworkName", null, null, null, subnetIdMap, rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void updateNetworkImplTest_CloudSitePresent_HeatMode_QueryStackReturnInfo()
			throws NetworkException, MsoException {
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		Holder<NetworkRollback> rollback = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("HEAT");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.heatWithUpdate = Mockito.mock(MsoHeatUtilsWithUpdate.class);
		MsoException exception = Mockito.mock(MsoException.class);
		StackInfo stackInfo = Mockito.mock(StackInfo.class);
		when(impl.heatWithUpdate.queryStack(any(), any(), any())).thenReturn(stackInfo);
		try {
			impl.updateNetwork("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
					"networkName", "physicalNetworkName", null, null, null, subnetIdMap, rollback);
		} catch (Exception e) {

		}
	}

	@Test
	public void queryNetworkImplTest_CloudSiteNotPresent() throws NetworkException {
		Holder<Boolean> networkExists = new Holder<>();
		Holder<String> networkId = new Holder<>();
		Holder<String> neutronNetworkId = new Holder<>();
		Holder<NetworkStatus> status = new Holder<>();
		Holder<List<Integer>> vlans = new Holder<>();
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(null);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		try {
			impl.queryNetwork("cloudSiteId", "tenantId", "networkNameOrId", null, networkExists, networkId,
					neutronNetworkId, status, vlans, subnetIdMap);
		} catch (Exception e) {

		}
	}

	@Test
	public void queryNetworkImplTest_CloudSitePresent_NeutronMode_QueryNetworkThrowsException()
			throws NetworkException, MsoException {
		Holder<Boolean> networkExists = new Holder<>();
		Holder<String> networkId = new Holder<>();
		Holder<String> neutronNetworkId = new Holder<>();
		Holder<NetworkStatus> status = new Holder<>();
		Holder<List<Integer>> vlans = new Holder<>();
		Holder<Map<String, String>> subnetIdMap = new Holder<>();
		MsoNetworkAdapterImpl impl = Mockito.spy(MsoNetworkAdapterImpl.class);
		impl.cloudConfigFactory = Mockito.mock(CloudConfigFactory.class);
		CloudConfig cloudConfig = Mockito.mock(CloudConfig.class);
		when(impl.cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
		CloudSite cloudSite = new CloudSite();
		Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(cloudSite);
		when(cloudConfig.getCloudSite(any())).thenReturn(cloudSiteOpt);
		impl.msoPropertiesFactory = Mockito.mock(MsoPropertiesFactory.class);
		CatalogDatabase catalogDB = Mockito.mock(CatalogDatabase.class);
		NetworkResource networkResource = Mockito.mock(NetworkResource.class);
		when(networkResource.getOrchestrationMode()).thenReturn("NEUTRON");
		when(networkResource.getNeutronNetworkType()).thenReturn("BASIC");
		doReturn(catalogDB).when(impl).getCatalogDB();
		doReturn(networkResource).when(impl).networkCheck(any(), anyLong(), anyString(), anyString(), anyString(),
				anyString(), anyList(), anyList(), any());
		impl.heat = Mockito.mock(MsoHeatUtils.class);
		MsoException exception = Mockito.mock(MsoException.class);
		when(impl.heat.queryStack(any(), any(), any())).thenThrow(exception);
		try {
			impl.queryNetwork("cloudSiteId", "tenantId", "networkNameOrId", null, networkExists, networkId,
					neutronNetworkId, status, vlans, subnetIdMap);
		} catch (Exception e) {

		}
	}
}
