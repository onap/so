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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import org.openecomp.mso.adapters.nwrest.ContrailNetwork;
import org.openecomp.mso.adapters.nwrest.CreateNetworkError;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkError;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.NetworkExceptionResponse;
import org.openecomp.mso.adapters.nwrest.NetworkTechnology;
import org.openecomp.mso.adapters.nwrest.ProviderVlanNetwork;
import org.openecomp.mso.adapters.nwrest.QueryNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkError;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkError;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
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
import org.openecomp.mso.adapters.vnfrest.RollbackVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVolumeGroupResponse;
import org.openecomp.mso.adapters.vnfrest.VfModuleExceptionResponse;
import org.openecomp.mso.adapters.vnfrest.VfModuleRollback;
import org.openecomp.mso.adapters.vnfrest.VolumeGroupRollback;
import org.openecomp.mso.entity.MsoRequest;

public class BeanTest {

	// Test cases for code coverage
	@Test
	public void testCreateNetworkRequest() {
		CreateNetworkRequest n = new CreateNetworkRequest();
		n.setBackout(true);
		n.setCloudSiteId("test");
		ContrailNetwork contrailNetwork = new ContrailNetwork("shared", "external", new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>());
		contrailNetwork.setExternal("dgddb");
		contrailNetwork.setPolicyFqdns(new ArrayList<>());
		contrailNetwork.setRouteTableFqdns(new ArrayList<>());
		contrailNetwork.setRouteTargets(new ArrayList<>());
		contrailNetwork.setShared("test");
		n.setContrailNetwork(contrailNetwork);
		n.setFailIfExists(true);
		n.setMessageId("38829");
		n.setModelCustomizationUuid("4u838282");
		MsoRequest req = new MsoRequest();
		req.setRequestId("38849");
		req.setServiceInstanceId("3884839");
		n.setMsoRequest(req);
		n.setNetworkId("478383");
		n.setNetworkName("tetet");
		n.setNetworkParams(new HashMap<>());
		n.setNetworkTechnology("VMWARE");
		n.setNetworkType("tete");
		n.setNetworkTypeVersion("v1");
		n.setNotificationUrl("test");
		ProviderVlanNetwork providerVlanNetwork = new ProviderVlanNetwork("test", new ArrayList<>());
		providerVlanNetwork.setPhysicalNetworkName("physicalNetworkName");
		providerVlanNetwork.setVlans(new ArrayList<>());
		n.setProviderVlanNetwork(providerVlanNetwork);
		n.setSkipAAI(false);
		n.setSubnets(new ArrayList<>());
		n.setTenantId("tenantId");
		n.getBackout();
		n.getCloudSiteId();
		ContrailNetwork cn = n.getContrailNetwork();
		cn.getExternal();
		cn.getPolicyFqdns();
		cn.getRouteTableFqdns();
		cn.getRouteTargets();
		cn.getShared();
		n.getFailIfExists();
		n.getMessageId();
		n.getModelCustomizationUuid();
		n.getMsoRequest();
		n.getNetworkId();
		n.getNetworkName();
		n.getNetworkParams();
		n.getNetworkTechnology();
		n.getNetworkType();
		n.getNetworkTypeVersion();
		n.getNotificationUrl();
		n.getProviderVlanNetwork();
		n.getSkipAAI();
		n.getSubnets();
		n.getTenantId();
		n.isContrailRequest();
		n.isSynchronous();
		n.toJsonString();
		n.toXmlString();
	}

	@Test
	public void testDeleteNetworkRequest() {
		DeleteNetworkRequest r = new DeleteNetworkRequest();
		r.setCloudSiteId("test");
		r.setMessageId("messageId");
		r.setModelCustomizationUuid("modelCustomizationUuid");
		r.setMsoRequest(null);
		r.setNetworkId("networkId");
		r.setNetworkStackId("networkStackId");
		r.setNetworkType("networkType");
		r.setNotificationUrl("notificationUrl");
		r.setSkipAAI(true);
		r.setTenantId("tenantId");
		r.getCloudSiteId();
		r.getMessageId();
		r.getModelCustomizationUuid();
		r.getMsoRequest();
		r.getNetworkId();
		r.getNetworkStackId();
		r.getNetworkType();
		r.getNotificationUrl();
		r.getSkipAAI();
		r.getTenantId();
	}

	@Test
	public void testCreateNetworkError() {
		CreateNetworkError e = new CreateNetworkError("message");
		e = new CreateNetworkError("message", null, true, "messageid");
		DeleteNetworkError d = new DeleteNetworkError("message");
		d = new DeleteNetworkError("message", null, false, "29102");
	}

	@Test
	public void testCreatenetworkResponse() {
		CreateNetworkResponse cnr = new CreateNetworkResponse("networkId", "neutronNetworkId", "networkStackId",
				"networkFqdn", false, null, null, "messageId");
		cnr.setMessageId("messageId");
		cnr.setNetworkCreated(true);
		cnr.setNetworkFqdn(null);
		cnr.setNetworkStackId(null);
		cnr.setNeutronNetworkId(null);
		cnr.setRollback(null);
		cnr.setNetworkStackId(null);
		cnr.setSubnetMap(null);
		cnr.getMessageId();
		cnr.getNetworkCreated();
		cnr.getNetworkFqdn();
		cnr.getNetworkId();
		cnr.getNetworkStackId();
		cnr.getNeutronNetworkId();
		cnr.getRollback();
		cnr.getSubnetMap();

		DeleteNetworkResponse dr = new DeleteNetworkResponse("networkId", true, "messageId");
		dr.setMessageId(null);
		dr.setNetworkDeleted(null);
		dr.setNetworkId(null);
		dr.getMessageId();
		dr.getNetworkDeleted();
		dr.getNetworkId();

		NetworkExceptionResponse ner = new NetworkExceptionResponse("message");
		ner = new NetworkExceptionResponse(null, null, false, null);
		ner.setCategory(null);
		ner.setMessage(null);
		ner.setRolledBack(null);
		ner.setMessageId(null);
		ner.getCategory();
		ner.getMessage();
		ner.getMessageId();
		ner.getRolledBack();

		ner.toJsonString();
		ner.toXmlString();
		NetworkTechnology nt = NetworkTechnology.NEUTRON;
		ProviderVlanNetwork pvn = new ProviderVlanNetwork(null, null);
		pvn.setPhysicalNetworkName(null);
		pvn.setVlans(null);
		pvn.getPhysicalNetworkName();
		pvn.getVlans();

		QueryNetworkResponse qnr = new QueryNetworkResponse(null, null, null, null, null);
		qnr.setNetworkExists(null);
		qnr.setNetworkId(null);
		qnr.setNetworkOutputs(null);
		qnr.setNetworkStackId(null);
		qnr.setNetworkStatus(null);
		qnr.setNeutronNetworkId(null);
		qnr.setRouteTargets(null);
		qnr.setSubnetIdMap(null);
		qnr.setVlans(null);
		qnr.getNetworkExists();
		qnr.getNetworkId();
		qnr.getNetworkOutputs();
		qnr.getNetworkStatus();
		qnr.getNeutronNetworkId();
		qnr.getRouteTargets();
		qnr.getSubnetIdMap();
		qnr.getVlans();
		qnr.toJsonString();

		UpdateNetworkRequest unr = new UpdateNetworkRequest();
		unr.setBackout(null);
		unr.setCloudSiteId(null);
		unr.setContrailNetwork(null);
		unr.setMessageId(null);
		unr.setModelCustomizationUuid(null);
		unr.setMsoRequest(null);
		unr.setNetworkId(null);
		unr.setNetworkName(null);
		unr.setNetworkParams(null);
		unr.setNetworkStackId(null);
		unr.setNetworkTechnology("VMWARE");
		unr.setNetworkType(null);
		unr.setNetworkTypeVersion(null);
		unr.setNotificationUrl(null);
		unr.setProviderVlanNetwork(null);
		unr.setSkipAAI(null);
		unr.setSubnets(null);
		unr.setTenantId(null);
		unr.getBackout();
		unr.getCloudSiteId();
		unr.getContrailNetwork();
		unr.getMessageId();
		unr.getModelCustomizationUuid();
		unr.getMsoRequest();
		unr.getNetworkId();
		unr.getNetworkName();
		unr.getNetworkParams();
		unr.getNetworkStackId();
		unr.getNetworkTechnology();
		unr.getNetworkType();
		unr.getNetworkTypeVersion();
		unr.getNotificationUrl();
		unr.getProviderVlanNetwork();
		unr.getSkipAAI();
		unr.getSubnets();
		unr.getTenantId();
		unr.isContrailRequest();

		RollbackNetworkError err = new RollbackNetworkError("message");
		err = new RollbackNetworkError(null, null, false, null);
		RollbackNetworkRequest req = new RollbackNetworkRequest();
		req.setNetworkRollback(null);
		req.getNetworkRollback();
		req.setMessageId(null);
		req.getMessageId();
		req.setNotificationUrl(null);
		req.getNotificationUrl();
		req.setSkipAAI(null);
		req.getSkipAAI();

		RollbackNetworkResponse rnr = new RollbackNetworkResponse(true, null);
		rnr.setMessageId(null);
		rnr.getMessageId();
		rnr.setNetworkRolledBack(null);
		rnr.getNetworkRolledBack();

		UpdateNetworkError error = new UpdateNetworkError(null);
		error = new UpdateNetworkError("test", null, false, null);

		UpdateVfModuleRequest uvmr = new UpdateVfModuleRequest();
		uvmr.setBackout(null);
		uvmr.setBaseVfModuleId(null);
		uvmr.setBaseVfModuleStackId(null);
		uvmr.setFailIfExists(null);
		uvmr.setMessageId(null);
		uvmr.setModelCustomizationUuid(null);
		uvmr.setMsoRequest(null);
		uvmr.setNotificationUrl(null);
		uvmr.setRequestType(null);
		uvmr.setSkipAAI(true);
		uvmr.setTenantId(null);
		uvmr.setVfModuleId(null);
		uvmr.setVfModuleName(null);
		uvmr.setVfModuleParams(null);
		uvmr.setVfModuleStackId(null);
		uvmr.setVfModuleType(null);
		uvmr.setVnfId(null);
		uvmr.setVnfType(null);
		uvmr.setVnfVersion(null);
		uvmr.setVolumeGroupId(null);
		uvmr.setVolumeGroupStackId(null);
		uvmr.getBackout();
		uvmr.getBaseVfModuleId();
		uvmr.getBaseVfModuleStackId();
		uvmr.getCloudSiteId();
		uvmr.getFailIfExists();
		uvmr.getMessageId();
		uvmr.getModelCustomizationUuid();
		uvmr.getMsoRequest();
		uvmr.getNotificationUrl();
		uvmr.getRequestType();
		uvmr.getSkipAAI();
		uvmr.getTenantId();
		uvmr.getVfModuleId();
		uvmr.getVfModuleName();
		uvmr.getVfModuleParams();
		uvmr.getVfModuleStackId();
		uvmr.getVfModuleType();
		uvmr.getVnfId();
		uvmr.getVnfType();
		uvmr.getVnfVersion();
		uvmr.getVolumeGroupId();
		uvmr.getVolumeGroupStackId();
		uvmr.setCloudSiteId(null);

		CreateVfModuleRequest cvmr = new CreateVfModuleRequest();
		cvmr.setBackout(null);
		cvmr.setBaseVfModuleId(null);
		cvmr.setBaseVfModuleStackId(null);
		cvmr.setCloudSiteId(null);
		cvmr.setFailIfExists(null);

		coverCode(CreateVfModuleRequest.class);
		CreateVfModuleResponse resp = new CreateVfModuleResponse(null, null, null, true, null, null, null);
		resp.toJsonString();
		resp.toXmlString();
		coverCode(CreateVfModuleResponse.class);

		coverCode(CreateVolumeGroupRequest.class);

		CreateVolumeGroupResponse cvgr = new CreateVolumeGroupResponse(null, null, true, null, null, null);
		coverCode(CreateVolumeGroupResponse.class);
		coverCode(DeleteVfModuleRequest.class);
		coverCode(DeleteVfModuleResponse.class);
		coverCode(DeleteVolumeGroupRequest.class);
		coverCode(DeleteVolumeGroupResponse.class);
		QueryVfModuleResponse vfmr = new QueryVfModuleResponse(null, null, null, null, null);
		coverCode(QueryVfModuleResponse.class);
		QueryVolumeGroupResponse qvgr = new QueryVolumeGroupResponse(null, null, null, null);
		coverCode(QueryVolumeGroupResponse.class);
		UpdateVfModuleResponse uvfmr = new UpdateVfModuleResponse(null, null, null, null, null);
		coverCode(UpdateVfModuleResponse.class);
		coverCode(UpdateVolumeGroupRequest.class);
		UpdateVolumeGroupResponse uvgr = new UpdateVolumeGroupResponse(null, null, null, null);
		coverCode(UpdateVolumeGroupResponse.class);
		VfModuleExceptionResponse vfmer = new VfModuleExceptionResponse(null, null, false, null);
		coverCode(VfModuleExceptionResponse.class);
		//VfModuleRollback vfmrb = new VfModuleRollback(null, null, null, null);
		VfModuleRollback vfmrb = new VfModuleRollback(null, null, null, false, null, null, null, null);
		coverCode(VfModuleRollback.class);
		//VolumeGroupRollback vgrback = new VolumeGroupRollback(null, null, null);
		VolumeGroupRollback vgrback = new VolumeGroupRollback(null, null, false, null, null, null, null);
		coverCode(VolumeGroupRollback.class);
		RollbackVolumeGroupResponse rvgresp = new RollbackVolumeGroupResponse(null, null);
		coverCode(RollbackVolumeGroupResponse.class);
	}
	
	@Test
	public void testTenantRestPackage(){
		CreateTenantError cte = new CreateTenantError(null, null, false);
		coverCode(CreateTenantError.class);
		CreateTenantRequest ctreq = new CreateTenantRequest();
		ctreq.toJsonString();
		ctreq.toXmlString();
		ctreq.toString();
		coverCode(CreateTenantRequest.class);
		CreateTenantResponse ctresp = new CreateTenantResponse(null, null, null, new TenantRollback());
		ctresp.toString();
		coverCode(CreateTenantResponse.class);
		DeleteTenantError dterr = new DeleteTenantError(null, null, false);
		coverCode(DeleteTenantError.class);
		coverCode(DeleteTenantRequest.class);
		coverCode(DeleteTenantResponse.class);
		coverCode(HealthCheckHandler.class);
		QueryTenantError qnerr = new QueryTenantError(null, null);
		coverCode(QueryTenantError.class);
		QueryTenantResponse qtresp = new QueryTenantResponse(null, null, null);
		coverCode(QueryTenantResponse.class);
		coverCode(RollbackTenantError.class);
		RollbackTenantError rollTer = new RollbackTenantError(null, null, false);
		coverCode(RollbackTenantRequest.class);
		coverCode(RollbackTenantResponse.class);
		TenantExceptionResponse resp = new TenantExceptionResponse(null, null, false);
		coverCode(TenantExceptionResponse.class);
		coverCode(TenantRollback.class);
	}

	private void coverCode(Class cls) {
		try {
			Object obj = cls.newInstance();
			Method[] methods = cls.getDeclaredMethods();
			for (Method m : methods) {
				try {
					m.setAccessible(true);
					Type[] types = m.getGenericParameterTypes();
					Object[] objs = { new Object(), new Object(), new Object(), new Object() };
					if (types.length < 1) {
						m.invoke(obj);
					} else if (types.length == 1) {
						String type = types[0].getTypeName();
						if (type.contains("<")) {
							type = type.substring(0, type.indexOf("<"));
						}
						Class paramCls = Class.forName(type);
						Object paramobj = paramCls.newInstance();
						m.invoke(obj, paramobj);
					} else if (types.length == 2) {
						// m.invoke(obj,null,null);
					}
				} catch (Exception ex) {
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
