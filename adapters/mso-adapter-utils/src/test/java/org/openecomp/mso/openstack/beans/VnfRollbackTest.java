package org.openecomp.mso.openstack.beans;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class VnfRollbackTest extends BaseTest {
	@Autowired
	private VnfRollback vnfRollback;
	
	private String vnfId = "testVnfId";
	private String tenantId = "testTenantId";
	private String cloudSiteId = "testCloudSiteId";
	private boolean tenantCreated = true;
	private boolean vnfCreated = true;
	private MsoRequest msoRequest = new MsoRequest();
	private String volumeGroupName = "testVolumeGroupName";
	private String volumeGroupId = "testVolumeGroupId";
	private String requestType = "testRequestType";
	private String modelCustomizationUuid = "testModelCustimizationUuid";
	private String orchestrationMode = "testOrchestrationMode";
	private static final String VNF_ROLLBACK_STRING = "VnfRollback: cloud=testCloudSiteId, tenant=testTenantId, vnf=testVnfId, "
			+ "tenantCreated=true, vnfCreated=true, requestType = testRequestType, modelCustomizationUuid=testModelCustimizationUuid, mode=testOrchestrationMode";
	
	@Test
	public void VnfRollbackInstantiationTest() {
		vnfRollback = new VnfRollback(vnfId, tenantId, cloudSiteId, tenantCreated, vnfCreated,
				msoRequest, volumeGroupName, volumeGroupId, requestType, modelCustomizationUuid);
		
		assertEquals(vnfId, vnfRollback.getVnfId());
		assertEquals(tenantId, vnfRollback.getTenantId());
		assertEquals(cloudSiteId, vnfRollback.getCloudSiteId());
		assertEquals(tenantCreated, vnfRollback.getTenantCreated());
		assertEquals(vnfCreated, vnfRollback.getVnfCreated());
		assertEquals(msoRequest, vnfRollback.getMsoRequest());
		assertEquals(volumeGroupName, vnfRollback.getVolumeGroupName());
		assertEquals(volumeGroupId, vnfRollback.getVolumeGroupId());
		assertEquals(requestType, vnfRollback.getRequestType());
		assertEquals(modelCustomizationUuid, vnfRollback.getModelCustomizationUuid());
	}
	
	@Test
	public void VnfRollbackInstantiationOrchestrationModeTest() {
		vnfRollback = new VnfRollback(vnfId, tenantId, cloudSiteId, tenantCreated, vnfCreated,
				msoRequest, volumeGroupName, volumeGroupId, requestType, modelCustomizationUuid, orchestrationMode);
		
		assertEquals(vnfId, vnfRollback.getVnfId());
		assertEquals(tenantId, vnfRollback.getTenantId());
		assertEquals(cloudSiteId, vnfRollback.getCloudSiteId());
		assertEquals(tenantCreated, vnfRollback.getTenantCreated());
		assertEquals(vnfCreated, vnfRollback.getVnfCreated());
		assertEquals(msoRequest, vnfRollback.getMsoRequest());
		assertEquals(volumeGroupName, vnfRollback.getVolumeGroupName());
		assertEquals(volumeGroupId, vnfRollback.getVolumeGroupId());
		assertEquals(requestType, vnfRollback.getRequestType());
		assertEquals(modelCustomizationUuid, vnfRollback.getModelCustomizationUuid());
		assertEquals(orchestrationMode, vnfRollback.getMode());
	}
	
	@Test
	public void toStringTest() {
		vnfRollback = new VnfRollback(vnfId, tenantId, cloudSiteId, tenantCreated, vnfCreated,
				msoRequest, volumeGroupName, volumeGroupId, requestType, modelCustomizationUuid, orchestrationMode);
		
		assertEquals(VNF_ROLLBACK_STRING, vnfRollback.toString());
	}
}
