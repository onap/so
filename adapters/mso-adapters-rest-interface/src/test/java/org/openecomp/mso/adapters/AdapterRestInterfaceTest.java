/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Intellectual Property. All rights reserved.
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
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.SerializerProvider;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.adapters.json.MapDeserializer;
import org.openecomp.mso.adapters.json.MapSerializer;
import org.openecomp.mso.adapters.nwrest.ContrailNetwork;
import org.openecomp.mso.adapters.nwrest.CreateNetworkError;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkError;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.NetworkExceptionResponse;
import org.openecomp.mso.adapters.nwrest.NetworkRequestCommon;
import org.openecomp.mso.adapters.nwrest.ProviderVlanNetwork;
import org.openecomp.mso.adapters.nwrest.QueryNetworkError;
import org.openecomp.mso.adapters.nwrest.QueryNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkError;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkError;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.VfModuleRollback;
import org.openecomp.mso.adapters.vnfrest.VolumeGroupRollback;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.NetworkRollback;
import org.openecomp.mso.openstack.beans.NetworkStatus;
import org.openecomp.mso.openstack.beans.Subnet;
import org.openecomp.mso.openstack.beans.VnfRollback;

public class AdapterRestInterfaceTest {
	@Test(expected = Exception.class)
	public final void mapDeserializerTest() {
		MapDeserializer mapDeserializer = new MapDeserializer();
		JsonParser jsonParser = Mockito.mock(JsonParser.class);
		DeserializationContext deserializationContext = Mockito.mock(DeserializationContext.class);
		try {
			mapDeserializer.deserialize(jsonParser, deserializationContext);
		} catch (IOException e) {
		}
	}

	@Test
	public final void mapSerializerTest() {
		MapSerializer mapSerializer = new MapSerializer();
		mapSerializer.isUnwrappingSerializer();
		mapSerializer.toString();
		mapSerializer.unwrappingSerializer();
		JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);
		SerializerProvider serializerProvider = Mockito.mock(SerializerProvider.class);
		try {
			mapSerializer.serialize(new HashMap<String, String>(), jsonGenerator, serializerProvider);
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
			ContrailNetwork contrailNetwork2 = new ContrailNetwork("", "", new ArrayList<String>(),
					new ArrayList<String>(), new ArrayList<String>());
			contrailNetwork.getExternal();
			contrailNetwork.setExternal("external");
			contrailNetwork.setPolicyFqdns(new ArrayList<String>());
			contrailNetwork.setRouteTableFqdns(new ArrayList<String>());
			contrailNetwork.setRouteTargets(new ArrayList<String>());
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
			updateNetworkRequest.setModelCustomizationUuid("modelCustomizationUuid");
			updateNetworkRequest.setMsoRequest(new MsoRequest());
			updateNetworkRequest.setNetworkId("networkId");
			updateNetworkRequest.setNetworkName("networkName");
			updateNetworkRequest.setNetworkParams(new HashMap<String, String>());
			updateNetworkRequest.setNetworkStackId("networkStackId");
			updateNetworkRequest.setNetworkTechnology("networkTechnology");
			updateNetworkRequest.setNetworkType("networkType");
			updateNetworkRequest.setNetworkTypeVersion("networkTypeVersion");
			updateNetworkRequest.setNotificationUrl("notificationUrl");
			updateNetworkRequest.setProviderVlanNetwork(new ProviderVlanNetwork());
			updateNetworkRequest.setSkipAAI(true);
			updateNetworkRequest.setSubnets(new ArrayList<Subnet>());
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
			createNetworkRequest.setModelCustomizationUuid("modelCustomizationUuid");
			createNetworkRequest.setMsoRequest(new MsoRequest());
			createNetworkRequest.setNetworkId("networkId");
			createNetworkRequest.setNetworkName("networkName");
			createNetworkRequest.setNetworkType("networkType");
			createNetworkRequest.setNetworkTypeVersion("networkTypeVersion");
			createNetworkRequest.setNotificationUrl("notificationUrl");
			createNetworkRequest.setProviderVlanNetwork(new ProviderVlanNetwork());
			createNetworkRequest.setSkipAAI(true);
			createNetworkRequest.setSubnets(new ArrayList<Subnet>());
			createNetworkRequest.setTenantId("tenantId");
			createNetworkRequest.setNetworkParams(new HashMap<String, String>());
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
			QueryNetworkResponse queryNetworkResponse2 = new QueryNetworkResponse("", "", "", NetworkStatus.ACTIVE,
					new HashMap<String, String>());
			queryNetworkResponse.setNetworkExists(true);
			queryNetworkResponse.setNetworkId("networkId");
			queryNetworkResponse.setNetworkOutputs(new HashMap<String, String>());
			queryNetworkResponse.setNetworkStackId("networkStackId");
			queryNetworkResponse.setNetworkStatus(NetworkStatus.ACTIVE);
			queryNetworkResponse.setNeutronNetworkId("neutronNetworkId");
			queryNetworkResponse.setRouteTargets(new ArrayList<String>());
			queryNetworkResponse.setSubnetIdMap(new HashMap<String, String>());
			queryNetworkResponse.setVlans(new ArrayList<Integer>());
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
			CreateNetworkResponse createNetworkResponse2 = new CreateNetworkResponse("", "", "", "", true,
					new HashMap<String, String>(), new NetworkRollback(), "");
			createNetworkResponse.setMessageId("messageId");
			createNetworkResponse.setNetworkCreated(true);
			createNetworkResponse.setNetworkFqdn("networkFqdn");
			createNetworkResponse.setNetworkId("networkId");
			createNetworkResponse.setNetworkStackId("networkStackId");
			createNetworkResponse.setNeutronNetworkId("neutronNetworkId");
			createNetworkResponse.setRollback(new NetworkRollback());
			createNetworkResponse.setSubnetMap(new HashMap<String, String>());
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
			deleteNetworkRequest.setModelCustomizationUuid("modelCustomizationUuid");
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
			NetworkExceptionResponse networkExceptionResponse2 = new NetworkExceptionResponse("", null, false, "");
			NetworkExceptionResponse networkExceptionResponse3 = new NetworkExceptionResponse("");
			networkExceptionResponse.setCategory(null);
			networkExceptionResponse.setMessage("message");
			networkExceptionResponse.setMessageId("messageId");
			networkExceptionResponse.setRolledBack(true);
			networkExceptionResponse.getCategory();
			networkExceptionResponse.getMessage();
			networkExceptionResponse.getMessageId();
			networkExceptionResponse.getRolledBack();

			UpdateNetworkResponse updateNetworkResponse = new UpdateNetworkResponse();
			UpdateNetworkResponse updateNetworkResponse2 = new UpdateNetworkResponse("", "", null, "");
			updateNetworkResponse.setMessageId("messageId");
			updateNetworkResponse.setNetworkId("networkId");
			updateNetworkResponse.setNeutronNetworkId("");
			updateNetworkResponse.setSubnetMap(null);
			updateNetworkResponse.getNetworkId();
			updateNetworkResponse.getMessageId();
			updateNetworkResponse.getNeutronNetworkId();
			updateNetworkResponse.getSubnetMap();

			DeleteNetworkResponse deleteNetworkResponse = new DeleteNetworkResponse();
			DeleteNetworkResponse deleteNetworkResponse2 = new DeleteNetworkResponse("", false, "");
			deleteNetworkResponse.setNetworkDeleted(false);
			deleteNetworkResponse.setNetworkId("networkId");
			deleteNetworkResponse.getNetworkDeleted();
			deleteNetworkResponse.getNetworkId();

			ProviderVlanNetwork providerVlanNetwork = new ProviderVlanNetwork();
			ProviderVlanNetwork providerVlanNetwork2 = new ProviderVlanNetwork("", null);
			providerVlanNetwork.setPhysicalNetworkName("");
			providerVlanNetwork.setVlans(null);
			providerVlanNetwork.getPhysicalNetworkName();
			providerVlanNetwork.getVlans();

			RollbackNetworkResponse rollbackNetworkResponse = new RollbackNetworkResponse();
			RollbackNetworkResponse rollbackNetworkResponse2 = new RollbackNetworkResponse(false, "");
			rollbackNetworkResponse.setNetworkRolledBack(false);
			rollbackNetworkResponse.getNetworkRolledBack();

			CreateNetworkError createNetworkError = new CreateNetworkError();
			CreateNetworkError createNetworkError2 = new CreateNetworkError("");
			CreateNetworkError createNetworkError3 = new CreateNetworkError("", null, false, "");

			DeleteNetworkError deleteNetworkError = new DeleteNetworkError();
			DeleteNetworkError deleteNetworkError2 = new DeleteNetworkError("");
			DeleteNetworkError deleteNetworkError3 = new DeleteNetworkError("", null, false, "");

			RollbackNetworkError rollbackNetworkError = new RollbackNetworkError();
			RollbackNetworkError rollbackNetworkError2 = new RollbackNetworkError("");
			RollbackNetworkError rollbackNetworkError3 = new RollbackNetworkError("", null, false, "");

			UpdateNetworkError updateNetworkError = new UpdateNetworkError();
			UpdateNetworkError updateNetworkError2 = new UpdateNetworkError("");
			UpdateNetworkError updateNetworkError3 = new UpdateNetworkError("", null, false, "");

			RollbackNetworkRequest rollbackNetworkRequest = new RollbackNetworkRequest();
			rollbackNetworkRequest.setNetworkRollback(null);
			rollbackNetworkRequest.getNetworkRollback();

			QueryNetworkError queryNetworkError = new QueryNetworkError();

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
			VfModuleRollback vfModuleRollback2 = new VfModuleRollback(vnfRollback, "", "", "");
			VfModuleRollback vfModuleRollback3 = new VfModuleRollback("", "", "", false, "", "", null, "");
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
			VolumeGroupRollback volumeGroupRollback2 = new VolumeGroupRollback(volumeGroupRollback, "", "");
			VolumeGroupRollback volumeGroupRollback3 = new VolumeGroupRollback("", "", false, "", "", null, "");

			CreateVolumeGroupRequest createVolumeGroupRequest = new CreateVolumeGroupRequest();
			createVolumeGroupRequest.setCloudSiteId("");
			createVolumeGroupRequest.setFailIfExists(false);
			createVolumeGroupRequest.setMessageId("messageId");
			createVolumeGroupRequest.setModelCustomizationUuid("modelCustomizationUuid");
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
		} catch (Exception e) {
			assert (false);

		}
	}
}
