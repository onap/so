package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.mockito.ArgumentCaptor
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.MsoGroovyTest
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.junit.Before
import org.junit.Ignore;
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(MockitoJUnitRunner.class)
class DoCreateVfModuleVolumeV2Test extends MsoGroovyTest {

	def String volumeRollbackRequest = """
<rollbackVolumeGroupRequest>
   <volumeGroupRollback>
      <volumeGroupId>171907d6-cdf0-4e08-953d-81ee104005a7</volumeGroupId>
      <volumeGroupStackId>{{VOLUMEGROUPSTACKID}}</volumeGroupStackId>
      <cloudSiteId>mtwnj1a</cloudSiteId>
      <tenantId>c2141e3fcae940fcb4797ec9115e5a7a</tenantId>
      <volumeGroupCreated>true</volumeGroupCreated>
      <msoRequest>
         <requestId>230fd6ac-2a39-4be4-9b1e-7b7e1cc039b5</requestId>
         <serviceInstanceId>88c871d6-be09-4982-8490-96b1d243fb34</serviceInstanceId>
      </msoRequest>
      <messageId>9a5a91e8-3b79-463c-81c3-874a78f5b567</messageId>
   </volumeGroupRollback>
   <skipAAI>true</skipAAI>
   <messageId>9a5a91e8-3b79-463c-81c3-874a78f5b567</messageId>
   <notificationUrl>http://msobpel-app-e2e.ecomp.cci.att.com:8080/mso/WorkflowMessage/VNFAResponse/9a5a91e8-3b79-463c-81c3-874a78f5b567</notificationUrl>
</rollbackVolumeGroupRequest>
	"""
	
	def String volumeRollbackRequestWithStackId = """
<rollbackVolumeGroupRequest>
   <volumeGroupRollback>
      <volumeGroupId>171907d6-cdf0-4e08-953d-81ee104005a7</volumeGroupId>
      <volumeGroupStackId>mdt22avrr_volume01/0f1aaae8-efe3-45ce-83e1-bfad01db58d8</volumeGroupStackId>
      <cloudSiteId>mtwnj1a</cloudSiteId>
      <tenantId>c2141e3fcae940fcb4797ec9115e5a7a</tenantId>
      <volumeGroupCreated>true</volumeGroupCreated>
      <msoRequest>
         <requestId>230fd6ac-2a39-4be4-9b1e-7b7e1cc039b5</requestId>
         <serviceInstanceId>88c871d6-be09-4982-8490-96b1d243fb34</serviceInstanceId>
      </msoRequest>
      <messageId>9a5a91e8-3b79-463c-81c3-874a78f5b567</messageId>
   </volumeGroupRollback>
   <skipAAI>true</skipAAI>
   <messageId>9a5a91e8-3b79-463c-81c3-874a78f5b567</messageId>
   <notificationUrl>http://msobpel-app-e2e.ecomp.cci.att.com:8080/mso/WorkflowMessage/VNFAResponse/9a5a91e8-3b79-463c-81c3-874a78f5b567</notificationUrl>
</rollbackVolumeGroupRequest>
	"""
	
	
	
	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	@Test
	public void testBuildRollbackVolumeGroupRequestXml() {
		DoCreateVfModuleVolumeV2 process = new DoCreateVfModuleVolumeV2()
		String xml = process.buildRollbackVolumeGroupRequestXml(
			"171907d6-cdf0-4e08-953d-81ee104005a7", 	// volumeGroupId
			"mtwnj1a", 									// cloudSiteId
			"c2141e3fcae940fcb4797ec9115e5a7a", 		// tenantId
			"230fd6ac-2a39-4be4-9b1e-7b7e1cc039b5", 	// requestId
			"88c871d6-be09-4982-8490-96b1d243fb34", 	// serviceInstanceId
			"9a5a91e8-3b79-463c-81c3-874a78f5b567", 	// messageId
			"http://msobpel-app-e2e.ecomp.cci.att.com:8080/mso/WorkflowMessage/VNFAResponse/9a5a91e8-3b79-463c-81c3-874a78f5b567")	// notificationUrl

		assertEquals(volumeRollbackRequest.replaceAll("\\s", ""), xml.replaceAll("\\s", ""))
	}
	
	
	@Test
	public void testUpdateRollbackVolumeGroupRequestXml() {
		DoCreateVfModuleVolumeV2 process = new DoCreateVfModuleVolumeV2()
		String updatedXml = process.updateRollbackVolumeGroupRequestXml(volumeRollbackRequest, "mdt22avrr_volume01/0f1aaae8-efe3-45ce-83e1-bfad01db58d8")
		assertEquals(volumeRollbackRequestWithStackId.replaceAll("\\s", ""), updatedXml.replaceAll("\\s", ""))
	}
}
