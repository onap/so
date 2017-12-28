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

package org.openecomp.mso.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.adapters.json.MapSerializer;
import org.openecomp.mso.adapters.nwrest.ContrailNetwork;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.NetworkExceptionResponse;
import org.openecomp.mso.adapters.nwrest.NetworkRequestCommon;
import org.openecomp.mso.adapters.nwrest.ProviderVlanNetwork;
import org.openecomp.mso.adapters.nwrest.QueryNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.adapters.sdncrest.RequestInformation;
import org.openecomp.mso.adapters.sdncrest.SDNCErrorCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCEvent;
import org.openecomp.mso.adapters.sdncrest.SDNCRequestCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCResponseCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceError;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceRequest;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceResponse;
import org.openecomp.mso.adapters.sdncrest.ServiceInformation;
import org.openecomp.mso.adapters.tenantrest.CreateTenantError;
import org.openecomp.mso.adapters.tenantrest.CreateTenantRequest;
import org.openecomp.mso.adapters.tenantrest.CreateTenantResponse;
import org.openecomp.mso.adapters.tenantrest.DeleteTenantError;
import org.openecomp.mso.adapters.tenantrest.DeleteTenantRequest;
import org.openecomp.mso.adapters.tenantrest.DeleteTenantResponse;
import org.openecomp.mso.adapters.tenantrest.HealthCheckHandler;
import org.openecomp.mso.adapters.tenantrest.QueryTenantError;
import org.openecomp.mso.adapters.tenantrest.QueryTenantResponse;
import org.openecomp.mso.adapters.tenantrest.RollbackTenantError;
import org.openecomp.mso.adapters.tenantrest.RollbackTenantRequest;
import org.openecomp.mso.adapters.tenantrest.RollbackTenantResponse;
import org.openecomp.mso.adapters.tenantrest.TenantExceptionResponse;
import org.openecomp.mso.adapters.tenantrest.TenantRequestCommon;
import org.openecomp.mso.adapters.tenantrest.TenantRollback;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.QueryVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.QueryVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.VfModuleExceptionResponse;
import org.openecomp.mso.adapters.vnfrest.VfModuleRollback;
import org.openecomp.mso.adapters.vnfrest.VfRequestCommon;
import org.openecomp.mso.adapters.vnfrest.VfResponseCommon;
import org.openecomp.mso.adapters.vnfrest.VolumeGroupExceptionResponse;
import org.openecomp.mso.adapters.vnfrest.VolumeGroupRollback;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.NetworkRollback;
import org.openecomp.mso.openstack.beans.NetworkStatus;
import org.openecomp.mso.openstack.beans.VnfRollback;

public class AdapterRestInterfaceTest {

	@Test
	public final void mapSerializerTest() {
		MapSerializer mapSerializer = new MapSerializer();
		mapSerializer.isUnwrappingSerializer();
		mapSerializer.toString();
		mapSerializer.unwrappingSerializer();
		JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);
		SerializerProvider serializerProvider = Mockito
				.mock(SerializerProvider.class);
		try {
			mapSerializer.serialize(new HashMap(), jsonGenerator, serializerProvider);
		} catch (IOException e) {
		}
	}

	/**
	 * Test case for coverage
	 */
	@Test
	public final void contrailNetworkPOJOTest() {
		try {
			ContrailNetwork contrailNetwork = new ContrailNetwork();
			contrailNetwork.getExternal();
			contrailNetwork.setExternal("external");
			contrailNetwork.setPolicyFqdns(new ArrayList<>());
			contrailNetwork.setRouteTableFqdns(new ArrayList<>());
			contrailNetwork.setRouteTargets(new ArrayList<>());
			contrailNetwork.setShared("shared");
			contrailNetwork.getShared();
			contrailNetwork.getPolicyFqdns();
			contrailNetwork.getRouteTableFqdns();
			contrailNetwork.getRouteTargets();

			UpdateNetworkRequest updateNetworkRequest = new UpdateNetworkRequest();
			updateNetworkRequest.setBackout(false);
			updateNetworkRequest.setCloudSiteId("cloudSiteId");
			updateNetworkRequest.setContrailNetwork(new ContrailNetwork());
			updateNetworkRequest.setMessageId("messageId");
			updateNetworkRequest
					.setModelCustomizationUuid("modelCustomizationUuid");
			updateNetworkRequest.setMsoRequest(new MsoRequest());
			updateNetworkRequest.setNetworkId("networkId");
			updateNetworkRequest.setNetworkName("networkName");
			updateNetworkRequest
					.setNetworkParams(new HashMap<>());
			updateNetworkRequest.setNetworkStackId("networkStackId");
			updateNetworkRequest.setNetworkTechnology("networkTechnology");
			updateNetworkRequest.setNetworkType("networkType");
			updateNetworkRequest.setNetworkTypeVersion("networkTypeVersion");
			updateNetworkRequest.setNotificationUrl("notificationUrl");
			updateNetworkRequest
					.setProviderVlanNetwork(new ProviderVlanNetwork());
			updateNetworkRequest.setSkipAAI(true);
			updateNetworkRequest.setSubnets(new ArrayList<>());
			updateNetworkRequest.setTenantId("tenantId");
			updateNetworkRequest.isContrailRequest();
			updateNetworkRequest.getCloudSiteId();
			updateNetworkRequest.getContrailNetwork();
			updateNetworkRequest.getMessageId();
			updateNetworkRequest.getNetworkId();
			updateNetworkRequest.getModelCustomizationUuid();
			updateNetworkRequest.getMsoRequest();
			updateNetworkRequest.getNetworkName();
			updateNetworkRequest.getNetworkParams();
			updateNetworkRequest.getNetworkStackId();
			updateNetworkRequest.getNetworkTechnology();
			updateNetworkRequest.getNetworkType();
			updateNetworkRequest.getNetworkTypeVersion();
			updateNetworkRequest.getNotificationUrl();
			updateNetworkRequest.getProviderVlanNetwork();
			updateNetworkRequest.getSkipAAI();
			updateNetworkRequest.getSubnets();
			updateNetworkRequest.getTenantId();
			updateNetworkRequest.getBackout();

			CreateNetworkRequest createNetworkRequest = new CreateNetworkRequest();
			createNetworkRequest.setBackout(false);
			createNetworkRequest.setCloudSiteId("cloudSiteId");
			createNetworkRequest.setContrailNetwork(new ContrailNetwork());
			createNetworkRequest.setFailIfExists(false);
			createNetworkRequest.setMessageId("messageId");
			createNetworkRequest
					.setModelCustomizationUuid("modelCustomizationUuid");
			createNetworkRequest.setMsoRequest(new MsoRequest());
			createNetworkRequest.setNetworkId("networkId");
			createNetworkRequest.setNetworkName("networkName");
			createNetworkRequest.setNetworkType("networkType");
			createNetworkRequest.setNetworkTypeVersion("networkTypeVersion");
			createNetworkRequest.setNotificationUrl("notificationUrl");
			createNetworkRequest
					.setProviderVlanNetwork(new ProviderVlanNetwork());
			createNetworkRequest.setSkipAAI(true);
			createNetworkRequest.setSubnets(new ArrayList<>());
			createNetworkRequest.setTenantId("tenantId");
			createNetworkRequest
					.setNetworkParams(new HashMap<>());
			createNetworkRequest.setNetworkTechnology("VMWARE");
			createNetworkRequest.getBackout();
			createNetworkRequest.getCloudSiteId();
			createNetworkRequest.getContrailNetwork();
			createNetworkRequest.getFailIfExists();
			createNetworkRequest.getMessageId();
			createNetworkRequest.getModelCustomizationUuid();
			createNetworkRequest.getMsoRequest();
			createNetworkRequest.getNetworkId();
			createNetworkRequest.getNetworkName();
			createNetworkRequest.getNetworkParams();
			createNetworkRequest.getNetworkTechnology();
			createNetworkRequest.getNetworkType();
			createNetworkRequest.getNetworkTypeVersion();
			createNetworkRequest.getNotificationUrl();
			createNetworkRequest.getProviderVlanNetwork();
			createNetworkRequest.getSkipAAI();
			createNetworkRequest.getSubnets();
			createNetworkRequest.getTenantId();
			createNetworkRequest.isContrailRequest();

			QueryNetworkResponse queryNetworkResponse = new QueryNetworkResponse();
			queryNetworkResponse.setNetworkExists(true);
			queryNetworkResponse.setNetworkId("networkId");
			queryNetworkResponse
					.setNetworkOutputs(new HashMap<>());
			queryNetworkResponse.setNetworkStackId("networkStackId");
			queryNetworkResponse.setNetworkStatus(NetworkStatus.ACTIVE);
			queryNetworkResponse.setNeutronNetworkId("neutronNetworkId");
			queryNetworkResponse.setRouteTargets(new ArrayList<>());
			queryNetworkResponse.setSubnetIdMap(new HashMap<>());
			queryNetworkResponse.setVlans(new ArrayList<>());
			queryNetworkResponse.getNetworkExists();
			queryNetworkResponse.getNetworkId();
			queryNetworkResponse.getNetworkOutputs();
			queryNetworkResponse.getNetworkStackId();
			queryNetworkResponse.getNetworkStatus();
			queryNetworkResponse.getNeutronNetworkId();
			queryNetworkResponse.getRouteTargets();
			queryNetworkResponse.getSubnetIdMap();
			queryNetworkResponse.getVlans();
			queryNetworkResponse.toJsonString();

			CreateNetworkResponse createNetworkResponse = new CreateNetworkResponse();
			createNetworkResponse.setMessageId("messageId");
			createNetworkResponse.setNetworkCreated(true);
			createNetworkResponse.setNetworkFqdn("networkFqdn");
			createNetworkResponse.setNetworkId("networkId");
			createNetworkResponse.setNetworkStackId("networkStackId");
			createNetworkResponse.setNeutronNetworkId("neutronNetworkId");
			createNetworkResponse.setRollback(new NetworkRollback());
			createNetworkResponse.setSubnetMap(new HashMap<>());
			createNetworkResponse.toJsonString();
			createNetworkResponse.toXmlString();
			createNetworkResponse.getMessageId();
			createNetworkResponse.getNetworkCreated();
			createNetworkResponse.getNetworkFqdn();
			createNetworkResponse.getNetworkId();
			createNetworkResponse.getNetworkStackId();
			createNetworkResponse.getNeutronNetworkId();
			createNetworkResponse.getRollback();
			createNetworkResponse.getSubnetMap();

			NetworkRequestCommon networkRequestCommon = new CreateNetworkRequest();
			networkRequestCommon.getMessageId();
			networkRequestCommon.getNotificationUrl();
			networkRequestCommon.getSkipAAI();
			networkRequestCommon.setMessageId("messageId");
			networkRequestCommon.setNotificationUrl("notificationUrl");
			networkRequestCommon.setSkipAAI(false);
			networkRequestCommon.isSynchronous();
			networkRequestCommon.toXmlString();
			networkRequestCommon.toJsonString();

			DeleteNetworkRequest deleteNetworkRequest = new DeleteNetworkRequest();
			deleteNetworkRequest.setCloudSiteId("cloudSiteId");
			deleteNetworkRequest.setMessageId("messageId");
			deleteNetworkRequest
					.setModelCustomizationUuid("modelCustomizationUuid");
			deleteNetworkRequest.setMsoRequest(new MsoRequest());
			deleteNetworkRequest.setNetworkId("networkId");
			deleteNetworkRequest.setNetworkStackId("networkStackId");
			deleteNetworkRequest.setNetworkType("networkType");
			deleteNetworkRequest.setNotificationUrl("notificationUrl");
			deleteNetworkRequest.setSkipAAI(true);
			deleteNetworkRequest.setTenantId("tenantId");
			deleteNetworkRequest.getCloudSiteId();
			deleteNetworkRequest.getMessageId();
			deleteNetworkRequest.getModelCustomizationUuid();
			deleteNetworkRequest.getMsoRequest();
			deleteNetworkRequest.getNetworkId();
			deleteNetworkRequest.getNetworkStackId();
			deleteNetworkRequest.getNetworkType();
			deleteNetworkRequest.getNotificationUrl();
			deleteNetworkRequest.getSkipAAI();
			deleteNetworkRequest.getTenantId();

			NetworkExceptionResponse networkExceptionResponse = new NetworkExceptionResponse();
			networkExceptionResponse.setCategory(null);
			networkExceptionResponse.setMessage("message");
			networkExceptionResponse.setMessageId("messageId");
			networkExceptionResponse.setRolledBack(true);
			networkExceptionResponse.getCategory();
			networkExceptionResponse.getMessage();
			networkExceptionResponse.getMessageId();
			networkExceptionResponse.getRolledBack();

			UpdateNetworkResponse updateNetworkResponse = new UpdateNetworkResponse();
			updateNetworkResponse.setMessageId("messageId");
			updateNetworkResponse.setNetworkId("networkId");
			updateNetworkResponse.setNeutronNetworkId("");
			updateNetworkResponse.setSubnetMap(null);
			updateNetworkResponse.getNetworkId();
			updateNetworkResponse.getMessageId();
			updateNetworkResponse.getNeutronNetworkId();
			updateNetworkResponse.getSubnetMap();

			DeleteNetworkResponse deleteNetworkResponse = new DeleteNetworkResponse();
			deleteNetworkResponse.setNetworkDeleted(false);
			deleteNetworkResponse.setNetworkId("networkId");
			deleteNetworkResponse.getNetworkDeleted();
			deleteNetworkResponse.getNetworkId();

			ProviderVlanNetwork providerVlanNetwork = new ProviderVlanNetwork();
			providerVlanNetwork.setPhysicalNetworkName("");
			providerVlanNetwork.setVlans(null);
			providerVlanNetwork.getPhysicalNetworkName();
			providerVlanNetwork.getVlans();

			RollbackNetworkResponse rollbackNetworkResponse = new RollbackNetworkResponse();
			rollbackNetworkResponse.setNetworkRolledBack(false);
			rollbackNetworkResponse.getNetworkRolledBack();

			RollbackNetworkRequest rollbackNetworkRequest = new RollbackNetworkRequest();
			rollbackNetworkRequest.setNetworkRollback(null);
			rollbackNetworkRequest.getNetworkRollback();

			UpdateVfModuleRequest updateVfModuleRequest = new UpdateVfModuleRequest();
			updateVfModuleRequest.setBackout(false);
			updateVfModuleRequest.setBaseVfModuleId("");
			updateVfModuleRequest.setBaseVfModuleStackId("");
			updateVfModuleRequest.setCloudSiteId("");
			updateVfModuleRequest.setFailIfExists(false);
			updateVfModuleRequest.setMessageId("");
			updateVfModuleRequest.setModelCustomizationUuid("");
			updateVfModuleRequest.setMsoRequest(null);
			updateVfModuleRequest.setNotificationUrl("");
			updateVfModuleRequest.setRequestType("");
			updateVfModuleRequest.setSkipAAI(false);
			updateVfModuleRequest.setTenantId("");
			updateVfModuleRequest.setVfModuleId("");
			updateVfModuleRequest.setVfModuleName("");
			updateVfModuleRequest.setVfModuleParams(null);
			updateVfModuleRequest.setVfModuleStackId("");
			updateVfModuleRequest.setVfModuleType("");
			updateVfModuleRequest.setVnfId("");
			updateVfModuleRequest.setVnfType("");
			updateVfModuleRequest.setVnfVersion("");
			updateVfModuleRequest.setVolumeGroupId("");
			updateVfModuleRequest.setVolumeGroupStackId("");
			updateVfModuleRequest.getBackout();
			updateVfModuleRequest.getBaseVfModuleId();
			updateVfModuleRequest.getBaseVfModuleStackId();
			updateVfModuleRequest.getCloudSiteId();
			updateVfModuleRequest.getFailIfExists();
			updateVfModuleRequest.getMessageId();
			updateVfModuleRequest.getModelCustomizationUuid();
			updateVfModuleRequest.getMsoRequest();
			updateVfModuleRequest.getNotificationUrl();
			updateVfModuleRequest.getRequestType();
			updateVfModuleRequest.getSkipAAI();
			updateVfModuleRequest.getTenantId();
			updateVfModuleRequest.getVfModuleId();
			updateVfModuleRequest.getVfModuleName();
			updateVfModuleRequest.getVfModuleParams();
			updateVfModuleRequest.getVfModuleStackId();
			updateVfModuleRequest.getVfModuleType();
			updateVfModuleRequest.getVnfId();
			updateVfModuleRequest.getVnfType();
			updateVfModuleRequest.getVnfVersion();
			updateVfModuleRequest.getVolumeGroupId();
			updateVfModuleRequest.getVolumeGroupStackId();

			CreateVfModuleRequest createVfModuleRequest = new CreateVfModuleRequest();
			createVfModuleRequest.setBackout(false);
			createVfModuleRequest.setBaseVfModuleId("");
			createVfModuleRequest.setBaseVfModuleStackId("");
			createVfModuleRequest.setCloudSiteId("");
			createVfModuleRequest.setFailIfExists(false);
			createVfModuleRequest.setModelCustomizationUuid("");
			createVfModuleRequest.setMsoRequest(null);
			createVfModuleRequest.setNotificationUrl("");
			createVfModuleRequest.setRequestType("");
			createVfModuleRequest.setSkipAAI(false);
			createVfModuleRequest.setTenantId("");
			createVfModuleRequest.setVfModuleId("");
			createVfModuleRequest.setVfModuleName("");
			createVfModuleRequest.setVfModuleParams(null);
			createVfModuleRequest.setVfModuleType("");
			createVfModuleRequest.setVnfId("");
			createVfModuleRequest.setVnfType("");
			createVfModuleRequest.setVnfVersion("");
			createVfModuleRequest.setVolumeGroupId("volumeGroupId");
			createVfModuleRequest.setVolumeGroupStackId("volumeGroupStackId");
			createVfModuleRequest.getBackout();
			createVfModuleRequest.getBaseVfModuleId();
			createVfModuleRequest.getBaseVfModuleStackId();
			createVfModuleRequest.getCloudSiteId();
			createVfModuleRequest.getFailIfExists();
			createVfModuleRequest.getModelCustomizationUuid();
			createVfModuleRequest.getMsoRequest();
			createVfModuleRequest.getNotificationUrl();
			createVfModuleRequest.getRequestType();
			createVfModuleRequest.getSkipAAI();
			createVfModuleRequest.getTenantId();
			createVfModuleRequest.getVfModuleId();
			createVfModuleRequest.getVfModuleName();
			createVfModuleRequest.getVfModuleParams();
			createVfModuleRequest.getVfModuleType();
			createVfModuleRequest.getVnfId();
			createVfModuleRequest.getVnfType();
			createVfModuleRequest.getVnfVersion();
			createVfModuleRequest.getVolumeGroupId();
			createVfModuleRequest.getVolumeGroupStackId();

			VnfRollback vnfRollback = new VnfRollback();
			vnfRollback.setBaseGroupHeatStackId("");
			vnfRollback.setCloudSiteId("");
			vnfRollback.setIsBase(false);
			vnfRollback.setModelCustomizationUuid("");
			vnfRollback.setMsoRequest(null);
			vnfRollback.setRequestType("");
			vnfRollback.setTenantCreated(false);
			vnfRollback.setTenantId("");
			vnfRollback.setVfModuleStackId("");
			vnfRollback.setVnfCreated(false);
			vnfRollback.setVnfId("");
			vnfRollback.setVolumeGroupHeatStackId("");
			vnfRollback.setVolumeGroupId("");
			vnfRollback.setVolumeGroupName("");

			VfModuleRollback vfModuleRollback = new VfModuleRollback();
			vfModuleRollback.setCloudSiteId("");
			vfModuleRollback.setMsoRequest(null);
			vfModuleRollback.setTenantId("");
			vfModuleRollback.setVfModuleCreated(false);
			vfModuleRollback.setVfModuleId("");
			vfModuleRollback.setVfModuleStackId("");
			vfModuleRollback.setVnfId("");
			vfModuleRollback.getCloudSiteId();
			vfModuleRollback.getMsoRequest();
			vfModuleRollback.getTenantId();
			vfModuleRollback.getVfModuleId();
			vfModuleRollback.getVfModuleStackId();
			vfModuleRollback.getVnfId();

			VolumeGroupRollback volumeGroupRollback = new VolumeGroupRollback();
			volumeGroupRollback.setCloudSiteId("cloudSiteId");
			volumeGroupRollback.setMsoRequest(null);
			volumeGroupRollback.setTenantId("");
			volumeGroupRollback.setVolumeGroupCreated(false);
			volumeGroupRollback.setVolumeGroupId("");
			volumeGroupRollback.setVolumeGroupStackId("");
			volumeGroupRollback.setMessageId("messageId");
			volumeGroupRollback.getMessageId();
			volumeGroupRollback.getCloudSiteId();
			volumeGroupRollback.getMsoRequest();
			volumeGroupRollback.getTenantId();
			volumeGroupRollback.getVolumeGroupId();
			volumeGroupRollback.getVolumeGroupStackId();

			CreateVolumeGroupRequest createVolumeGroupRequest = new CreateVolumeGroupRequest();
			createVolumeGroupRequest.setCloudSiteId("");
			createVolumeGroupRequest.setFailIfExists(false);
			createVolumeGroupRequest.setMessageId("messageId");
			createVolumeGroupRequest
					.setModelCustomizationUuid("modelCustomizationUuid");
			createVolumeGroupRequest.setMsoRequest(null);
			createVolumeGroupRequest.setNotificationUrl("");
			createVolumeGroupRequest.setSkipAAI(false);
			createVolumeGroupRequest.setSuppressBackout(false);
			createVolumeGroupRequest.setTenantId("");
			createVolumeGroupRequest.setVfModuleType("");
			createVolumeGroupRequest.setVnfType("");
			createVolumeGroupRequest.setVnfVersion("");
			createVolumeGroupRequest.setVolumeGroupId("");
			createVolumeGroupRequest.setVolumeGroupName("");
			createVolumeGroupRequest.setVolumeGroupParams(null);
			createVolumeGroupRequest.getCloudSiteId();
			createVolumeGroupRequest.getFailIfExists();
			createVolumeGroupRequest.getMessageId();
			createVolumeGroupRequest.getModelCustomizationUuid();
			createVolumeGroupRequest.getMsoRequest();
			createVolumeGroupRequest.getNotificationUrl();
			createVolumeGroupRequest.getSkipAAI();
			createVolumeGroupRequest.getSuppressBackout();
			createVolumeGroupRequest.getTenantId();
			createVolumeGroupRequest.getVfModuleType();
			createVolumeGroupRequest.getVnfType();
			createVolumeGroupRequest.getVnfVersion();
			createVolumeGroupRequest.getVolumeGroupId();
			createVolumeGroupRequest.getVolumeGroupName();
			createVolumeGroupRequest.getVolumeGroupParams();

			CreateVfModuleResponse createVfModuleResponse = new CreateVfModuleResponse();
			createVfModuleResponse.setMessageId("");
			createVfModuleResponse.setRollback(null);
			createVfModuleResponse.setVfModuleCreated(false);
			createVfModuleResponse.setVfModuleId("");
			createVfModuleResponse.setVfModuleOutputs(null);
			createVfModuleResponse.setVfModuleStackId("");
			createVfModuleResponse.setVnfId("");
			createVfModuleResponse.getMessageId();
			createVfModuleResponse.getRollback();
			createVfModuleResponse.getVfModuleCreated();
			createVfModuleResponse.getVfModuleId();
			createVfModuleResponse.getVfModuleOutputs();
			createVfModuleResponse.getVfModuleStackId();
			createVfModuleResponse.getVnfId();

			UpdateVolumeGroupRequest updateVolumeGroupRequest = new UpdateVolumeGroupRequest();
			updateVolumeGroupRequest.setCloudSiteId("");
			updateVolumeGroupRequest.setMessageId("");
			updateVolumeGroupRequest.setModelCustomizationUuid("");
			updateVolumeGroupRequest.setMsoRequest(null);
			updateVolumeGroupRequest.setNotificationUrl("");
			updateVolumeGroupRequest.setSkipAAI(false);
			updateVolumeGroupRequest.setTenantId("");
			updateVolumeGroupRequest.setVfModuleType("");
			updateVolumeGroupRequest.setVnfType("");
			updateVolumeGroupRequest.setVnfVersion("");
			updateVolumeGroupRequest.setVolumeGroupId("");
			updateVolumeGroupRequest.setVolumeGroupParams(null);
			updateVolumeGroupRequest.setVolumeGroupStackId("");
			updateVolumeGroupRequest.getCloudSiteId();
			updateVolumeGroupRequest.getMessageId();
			updateVolumeGroupRequest.getModelCustomizationUuid();
			updateVolumeGroupRequest.getMsoRequest();
			updateVolumeGroupRequest.getNotificationUrl();
			updateVolumeGroupRequest.getSkipAAI();
			updateVolumeGroupRequest.getTenantId();
			updateVolumeGroupRequest.getVfModuleType();
			updateVolumeGroupRequest.getVnfType();
			updateVolumeGroupRequest.getVnfVersion();
			updateVolumeGroupRequest.getVolumeGroupId();
			updateVolumeGroupRequest.getVolumeGroupParams();
			updateVolumeGroupRequest.getVolumeGroupStackId();

			QueryVfModuleResponse queryVfModuleResponse = new QueryVfModuleResponse();
			queryVfModuleResponse.setVfModuleId("");
			queryVfModuleResponse.setVfModuleOutputs(null);
			queryVfModuleResponse.setVfModuleStackId("");
			queryVfModuleResponse.setVnfId("");
			queryVfModuleResponse.setVnfStatus(null);
			queryVfModuleResponse.getVfModuleId();
			queryVfModuleResponse.getVfModuleOutputs();
			queryVfModuleResponse.getVfModuleStackId();
			queryVfModuleResponse.getVnfId();
			queryVfModuleResponse.getVnfStatus();

			CreateVolumeGroupResponse createVolumeGroupResponse = new CreateVolumeGroupResponse();
			createVolumeGroupResponse.setMessageId("");
			createVolumeGroupResponse.setVolumeGroupCreated(false);
			createVolumeGroupResponse.setVolumeGroupId("");
			createVolumeGroupResponse.setVolumeGroupOutputs(null);
			createVolumeGroupResponse.setVolumeGroupRollback(null);
			createVolumeGroupResponse.setVolumeGroupStackId("");
			createVolumeGroupResponse.getMessageId();
			createVolumeGroupResponse.getVolumeGroupCreated();
			createVolumeGroupResponse.getVolumeGroupId();
			createVolumeGroupResponse.getVolumeGroupOutputs();

			VfResponseCommon vfResponseCommon = new CreateVfModuleResponse();
			vfResponseCommon.setMessageId("");
			vfResponseCommon.toJsonString();
			vfResponseCommon.getMessageId();
			vfResponseCommon.toXmlString();

			QueryVolumeGroupResponse queryVolumeGroupResponse = new QueryVolumeGroupResponse();
			queryVolumeGroupResponse.setVolumeGroupId("");
			queryVolumeGroupResponse.setVolumeGroupOutputs(null);
			queryVolumeGroupResponse.setVolumeGroupStackId("");
			queryVolumeGroupResponse.setVolumeGroupStatus(null);
			queryVolumeGroupResponse.getVolumeGroupId();
			queryVolumeGroupResponse.getVolumeGroupOutputs();
			queryVolumeGroupResponse.getVolumeGroupStackId();
			queryVolumeGroupResponse.getVolumeGroupStatus();
			queryVolumeGroupResponse.toString();
			queryVolumeGroupResponse.toJsonString();

			DeleteVfModuleResponse deleteVfModuleResponse = new DeleteVfModuleResponse();
			deleteVfModuleResponse.setMessageId("");
			deleteVfModuleResponse.setVfModuleDeleted(false);
			deleteVfModuleResponse.setVfModuleId("");
			deleteVfModuleResponse.setVfModuleOutputs(null);
			deleteVfModuleResponse.setVnfId("");
			deleteVfModuleResponse.getMessageId();
			deleteVfModuleResponse.getVfModuleDeleted();
			deleteVfModuleResponse.getVfModuleId();
			deleteVfModuleResponse.getVfModuleOutputs();
			deleteVfModuleResponse.getVnfId();

			UpdateVfModuleResponse updateVfModuleResponse = new UpdateVfModuleResponse();
			updateVfModuleResponse.setMessageId("");
			updateVfModuleResponse.setVfModuleId("");
			updateVfModuleResponse.setVfModuleOutputs(null);
			updateVfModuleResponse.setVfModuleStackId("");
			updateVfModuleResponse.setVnfId("");
			updateVfModuleResponse.getMessageId();
			updateVfModuleResponse.getVfModuleId();
			updateVfModuleResponse.getVfModuleOutputs();
			updateVfModuleResponse.getVfModuleStackId();
			updateVfModuleResponse.getVnfId();

			DeleteVfModuleRequest deleteVfModuleRequest = new DeleteVfModuleRequest();
			deleteVfModuleRequest.setCloudSiteId("");
			deleteVfModuleRequest.setMessageId("");
			deleteVfModuleRequest.setMsoRequest(null);
			deleteVfModuleRequest.setNotificationUrl("");
			deleteVfModuleRequest.setSkipAAI(false);
			deleteVfModuleRequest.setTenantId("");
			deleteVfModuleRequest.setVfModuleId("");
			deleteVfModuleRequest.setVfModuleStackId("");
			deleteVfModuleRequest.setVnfId("");
			deleteVfModuleRequest.getCloudSiteId();
			deleteVfModuleRequest.getMessageId();
			deleteVfModuleRequest.getMsoRequest();
			deleteVfModuleRequest.getNotificationUrl();
			deleteVfModuleRequest.getSkipAAI();
			deleteVfModuleRequest.getTenantId();
			deleteVfModuleRequest.getVfModuleId();
			deleteVfModuleRequest.getVfModuleStackId();
			deleteVfModuleRequest.getVnfId();

			VfModuleExceptionResponse vfModuleExceptionResponse = new VfModuleExceptionResponse();
			vfModuleExceptionResponse.setCategory(null);
			vfModuleExceptionResponse.setMessage("");
			vfModuleExceptionResponse.setMessageId("");
			vfModuleExceptionResponse.setRolledBack(false);
			vfModuleExceptionResponse.getCategory();
			vfModuleExceptionResponse.getMessage();
			vfModuleExceptionResponse.getMessageId();
			vfModuleExceptionResponse.getRolledBack();

			DeleteVolumeGroupRequest deleteVolumeGroupRequest = new DeleteVolumeGroupRequest();
			deleteVolumeGroupRequest.setCloudSiteId("");
			deleteVolumeGroupRequest.setMessageId("");
			deleteVolumeGroupRequest.setMsoRequest(null);
			deleteVolumeGroupRequest.setNotificationUrl("");
			deleteVolumeGroupRequest.setSkipAAI(false);
			deleteVolumeGroupRequest.setTenantId("");
			deleteVolumeGroupRequest.setVolumeGroupId("");
			deleteVolumeGroupRequest.setVolumeGroupStackId("");
			deleteVolumeGroupRequest.getCloudSiteId();
			deleteVolumeGroupRequest.getMessageId();
			deleteVolumeGroupRequest.getMsoRequest();
			deleteVolumeGroupRequest.getNotificationUrl();
			deleteVolumeGroupRequest.getSkipAAI();
			deleteVolumeGroupRequest.getTenantId();
			deleteVolumeGroupRequest.getVolumeGroupId();
			deleteVolumeGroupRequest.getVolumeGroupStackId();

			// 1
			UpdateVolumeGroupResponse updateVolumeGroupResponse = new UpdateVolumeGroupResponse();
			updateVolumeGroupResponse.setMessageId("");
			updateVolumeGroupResponse.setVolumeGroupId("");
			updateVolumeGroupResponse.setVolumeGroupOutputs(null);
			updateVolumeGroupResponse.setVolumeGroupStackId("");
			updateVolumeGroupResponse.getMessageId();
			updateVolumeGroupResponse.getVolumeGroupId();
			updateVolumeGroupResponse.getVolumeGroupOutputs();
			updateVolumeGroupResponse.getVolumeGroupStackId();

			VfRequestCommon vfRequestCommon = new CreateVfModuleRequest();
			vfRequestCommon.setMessageId("");
			vfRequestCommon.setNotificationUrl("");
			vfRequestCommon.setSkipAAI(false);
			vfRequestCommon.getMessageId();
			vfRequestCommon.getNotificationUrl();
			vfRequestCommon.getSkipAAI();

			DeleteVolumeGroupResponse deleteVolumeGroupResponse = new DeleteVolumeGroupResponse();
			deleteVolumeGroupResponse.setMessageId("");
			deleteVolumeGroupResponse.setVolumeGroupDeleted(false);
			deleteVolumeGroupResponse.getMessageId();
			deleteVolumeGroupResponse.getVolumeGroupDeleted();
			deleteVolumeGroupResponse.toJsonString();
			deleteVolumeGroupResponse.toXmlString();

			RollbackVfModuleResponse rollbackVfModuleResponse = new RollbackVfModuleResponse();

			rollbackVfModuleResponse.setMessageId("");
			rollbackVfModuleResponse.setVfModuleRolledback(false);
			rollbackVfModuleResponse.getMessageId();
			rollbackVfModuleResponse.getVfModuleRolledback();

			RollbackVolumeGroupResponse rollbackVolumeGroupResponse = new RollbackVolumeGroupResponse();
			rollbackVolumeGroupResponse.setMessageId("");
			rollbackVolumeGroupResponse.setVolumeGroupRolledBack(false);
			rollbackVolumeGroupResponse.getMessageId();
			rollbackVolumeGroupResponse.getVolumeGroupRolledBack();

			VolumeGroupExceptionResponse volumeGroupExceptionResponse = new VolumeGroupExceptionResponse();
			volumeGroupExceptionResponse.setCategory(null);
			volumeGroupExceptionResponse.setMessage("");
			volumeGroupExceptionResponse.setMessageId("");
			volumeGroupExceptionResponse.setRolledBack(false);
			volumeGroupExceptionResponse.getCategory();
			volumeGroupExceptionResponse.getMessage();
			volumeGroupExceptionResponse.getMessageId();
			volumeGroupExceptionResponse.getRolledBack();
			volumeGroupExceptionResponse.toJsonString();
			volumeGroupExceptionResponse.toXmlString();

			RollbackVfModuleRequest rollbackVfModuleRequest = new RollbackVfModuleRequest();
			rollbackVfModuleRequest.setMessageId("");
			rollbackVfModuleRequest.setNotificationUrl("");
			rollbackVfModuleRequest.setSkipAAI(false);
			rollbackVfModuleRequest.setVfModuleRollback(null);
			rollbackVfModuleRequest.getMessageId();
			rollbackVfModuleRequest.getNotificationUrl();
			rollbackVfModuleRequest.getSkipAAI();
			rollbackVfModuleRequest.getVfModuleRollback();
			rollbackVfModuleRequest.toJsonString();
			rollbackVfModuleRequest.toXmlString();

			SDNCResponseCommon SDNCResponseCommon = new SDNCServiceResponse();
			SDNCResponseCommon.setAckFinalIndicator("");
			SDNCResponseCommon.setResponseCode("");
			SDNCResponseCommon.setResponseMessage("");
			SDNCResponseCommon.setSDNCRequestId("");
			SDNCResponseCommon.getAckFinalIndicator();
			SDNCResponseCommon.getResponseCode();
			SDNCResponseCommon.getResponseMessage();
			SDNCResponseCommon.getSDNCRequestId();
			SDNCResponseCommon.toJson();
			// 2

			SDNCServiceResponse sDNCServiceResponse = new SDNCServiceResponse();
			sDNCServiceResponse.setAckFinalIndicator("");
			sDNCServiceResponse.setParams(null);
			sDNCServiceResponse.setResponseCode("");
			sDNCServiceResponse.setResponseMessage("");
			sDNCServiceResponse.setSDNCRequestId("");
			sDNCServiceResponse.getAckFinalIndicator();
			sDNCServiceResponse.getParams();
			sDNCServiceResponse.getResponseCode();
			sDNCServiceResponse.getSDNCRequestId();
			sDNCServiceResponse.getResponseMessage();

			RollbackVolumeGroupRequest rollbackVolumeGroupRequest = new RollbackVolumeGroupRequest();
			rollbackVolumeGroupRequest.setMessageId("");
			rollbackVolumeGroupRequest.setNotificationUrl("");
			rollbackVolumeGroupRequest.setSkipAAI(false);
			rollbackVolumeGroupRequest.setVolumeGroupRollback(null);
			rollbackVolumeGroupRequest.getMessageId();
			rollbackVolumeGroupRequest.getNotificationUrl();
			rollbackVolumeGroupRequest.getSkipAAI();
			rollbackVolumeGroupRequest.getVolumeGroupRollback();
			rollbackVolumeGroupRequest.toJsonString();
			rollbackVolumeGroupRequest.toXmlString();

			RequestInformation requestInformation = new RequestInformation();
			requestInformation.setNotificationUrl("");
			requestInformation.setRequestId("");
			requestInformation.setSource("");
			requestInformation.getNotificationUrl();
			requestInformation.getRequestId();
			requestInformation.getSource();

			SDNCErrorCommon sDNCErrorCommon = new SDNCServiceError();
			sDNCErrorCommon.setAckFinalIndicator("");
			sDNCErrorCommon.setResponseCode("");
			sDNCErrorCommon.setResponseMessage("");
			sDNCErrorCommon.setSDNCRequestId("");
			sDNCErrorCommon.getAckFinalIndicator();
			sDNCErrorCommon.getResponseCode();
			sDNCErrorCommon.getResponseMessage();
			sDNCErrorCommon.getSDNCRequestId();

			SDNCEvent sDNCEvent = new SDNCEvent();
			sDNCEvent.setEventCorrelator("");
			sDNCEvent.setEventCorrelatorType("");
			sDNCEvent.setEventType("");
			sDNCEvent.setParams(null);
			sDNCEvent.getEventCorrelator();
			sDNCEvent.getEventCorrelatorType();
			sDNCEvent.getEventType();
			sDNCEvent.getParams();
			sDNCEvent.addParam("", "");
			sDNCEvent.toJson();

			SDNCRequestCommon sDNCRequestCommon = new SDNCServiceRequest();
			sDNCRequestCommon.setBPNotificationUrl("");
			sDNCRequestCommon.setBPTimeout("");
			sDNCRequestCommon.setSDNCRequestId("");
			sDNCRequestCommon.getBPNotificationUrl();
			sDNCRequestCommon.getBPTimeout();
			sDNCRequestCommon.getSDNCRequestId();
			sDNCRequestCommon.toJson();
			sDNCRequestCommon.isSynchronous();

			SDNCServiceError sDNCServiceError = new SDNCServiceError();
			sDNCServiceError.setAckFinalIndicator("");
			sDNCServiceError.setResponseCode("");
			sDNCServiceError.setResponseMessage("");
			sDNCServiceError.setSDNCRequestId("");
			sDNCServiceError.getAckFinalIndicator();
			sDNCServiceError.getResponseCode();
			sDNCServiceError.getResponseMessage();
			sDNCServiceError.getSDNCRequestId();

			SDNCServiceRequest sDNCServiceRequest = new SDNCServiceRequest();
			sDNCServiceRequest.setBPNotificationUrl("");
			sDNCServiceRequest.setBPTimeout("");
			sDNCServiceRequest.setRequestInformation(null);
			sDNCServiceRequest.setSDNCOperation("");
			sDNCServiceRequest.setSDNCRequestId("");
			sDNCServiceRequest.setSDNCService("");
			sDNCServiceRequest.setSDNCServiceData("");
			sDNCServiceRequest.setSDNCServiceDataType("");
			sDNCServiceRequest.setServiceInformation(null);
			sDNCServiceRequest.getBPNotificationUrl();
			sDNCServiceRequest.getBPTimeout();
			sDNCServiceRequest.getRequestInformation();
			sDNCServiceRequest.getSDNCOperation();
			sDNCServiceRequest.getSDNCRequestId();
			sDNCServiceRequest.getSDNCService();
			sDNCServiceRequest.getSDNCServiceData();
			sDNCServiceRequest.getSDNCServiceDataType();
			sDNCServiceRequest.getServiceInformation();

			// 3
			ServiceInformation serviceInformation = new ServiceInformation();
			serviceInformation.setServiceInstanceId("");
			serviceInformation.setServiceType("");
			serviceInformation.setSubscriberGlobalId("");
			serviceInformation.setSubscriberName("");
			serviceInformation.getServiceInstanceId();
			serviceInformation.getServiceType();
			serviceInformation.getSubscriberGlobalId();
			serviceInformation.getSubscriberName();

			CreateTenantError createTenantError = new CreateTenantError();
			createTenantError.setCategory(null);
			createTenantError.setMessage("");
			createTenantError.setRolledBack(false);
			createTenantError.getCategory();
			createTenantError.getMessage();
			createTenantError.getRolledBack();

			CreateTenantRequest createTenantRequest = new CreateTenantRequest();
			createTenantRequest.setBackout(false);
			createTenantRequest.setCloudSiteId("");
			createTenantRequest.setFailIfExists(false);
			createTenantRequest.setMetadata(null);
			createTenantRequest.setMsoRequest(null);
			createTenantRequest.setTenantName("");
			createTenantRequest.getBackout();
			createTenantRequest.getCloudSiteId();
			createTenantRequest.getFailIfExists();
			createTenantRequest.getMetadata();
			createTenantRequest.getMsoRequest();
			createTenantRequest.getTenantName();
			createTenantRequest.toString();

			CreateTenantResponse createTenantResponse = new CreateTenantResponse();
			createTenantResponse.setCloudSiteId("");
			createTenantResponse.setTenantCreated(false);
			createTenantResponse.setTenantId("");
			createTenantResponse.setTenantRollback(new TenantRollback());
			createTenantResponse.getCloudSiteId();
			createTenantResponse.getTenantCreated();
			createTenantResponse.getTenantId();
			createTenantResponse.getTenantRollback();
			createTenantResponse.toString();

			DeleteTenantError deleteTenantError = new DeleteTenantError();
			deleteTenantError.setCategory(null);
			deleteTenantError.setMessage("");
			deleteTenantError.setRolledBack(false);
			deleteTenantError.getCategory();
			deleteTenantError.getMessage();
			deleteTenantError.getRolledBack();

			DeleteTenantRequest deleteTenantRequest = new DeleteTenantRequest();
			deleteTenantRequest.setCloudSiteId("");
			deleteTenantRequest.setMsoRequest(null);
			deleteTenantRequest.setTenantId("");
			deleteTenantRequest.getCloudSiteId();
			deleteTenantRequest.getMsoRequest();
			deleteTenantRequest.getTenantId();

			DeleteTenantResponse deleteTenantResponse = new DeleteTenantResponse();
			deleteTenantResponse.setTenantDeleted(false);
			deleteTenantResponse.getTenantDeleted();

			HealthCheckHandler healthCheckHandler = new HealthCheckHandler();
			healthCheckHandler.healthcheck();

			QueryTenantError queryTenantError = new QueryTenantError();
			queryTenantError.setCategory(null);
			queryTenantError.setMessage("");
			queryTenantError.getCategory();
			queryTenantError.getMessage();

			QueryTenantResponse queryTenantResponse = new QueryTenantResponse();
			queryTenantResponse.setMetadata(null);
			queryTenantResponse.setTenantId("");
			queryTenantResponse.setTenantName("");
			queryTenantResponse.getMetadata();
			queryTenantResponse.getTenantId();
			queryTenantResponse.getTenantName();

			RollbackTenantError rollbackTenantError = new RollbackTenantError();
			rollbackTenantError.setCategory(null);
			rollbackTenantError.setMessage("");
			rollbackTenantError.setRolledBack(false);
			rollbackTenantError.getCategory();
			rollbackTenantError.getMessage();
			rollbackTenantError.getRolledBack();

			RollbackTenantRequest rollbackTenantRequest = new RollbackTenantRequest();
			rollbackTenantRequest.setTenantRollback(null);
			rollbackTenantRequest.getTenantRollback();

			RollbackTenantResponse rollbackTenantResponse = new RollbackTenantResponse();
			rollbackTenantResponse.setTenantRolledback(false);
			rollbackTenantResponse.getTenantRolledback();

			TenantExceptionResponse tenantExceptionResponse = new TenantExceptionResponse();
			tenantExceptionResponse.setCategory(null);
			tenantExceptionResponse.setMessage("");
			tenantExceptionResponse.setRolledBack(false);
			tenantExceptionResponse.getCategory();
			tenantExceptionResponse.getMessage();
			tenantExceptionResponse.getRolledBack();

			TenantRequestCommon tenantRequestCommon = new TenantRequestCommon();
			tenantRequestCommon.toJsonString();
			tenantRequestCommon.toXmlString();

			TenantRollback tenantRollback = new TenantRollback();
			tenantRollback.toString();
			tenantRollback.setCloudId("");
			tenantRollback.setMsoRequest(null);
			tenantRollback.setTenantCreated(false);
			tenantRollback.setTenantId("");
			tenantRollback.getCloudId();
			tenantRollback.getMsoRequest();
			tenantRollback.getTenantCreated();
			tenantRollback.getTenantId();

		} catch (Exception e) {
			assert (false);

		}
	}
}
