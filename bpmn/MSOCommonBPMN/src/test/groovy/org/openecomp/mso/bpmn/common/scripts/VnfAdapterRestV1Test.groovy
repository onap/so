package org.openecomp.mso.bpmn.common.scripts

import org.junit.Test
import static org.junit.Assert.*;

class VnfAdapterRestV1Test {

	
	def rollbackReq = """
<rollbackVolumeGroupRequest>
   <volumeGroupRollback>
      <volumeGroupId>8a07b246-155e-4b08-b56e-76e98a3c2d66</volumeGroupId>
      <volumeGroupStackId>phmaz401me6-vpevre-VOLUMEGROUP/dbd560b6-b03f-4a17-92e7-8942459a60c1</volumeGroupStackId>
      <cloudSiteId>mtrnj1b</cloudSiteId>
      <tenantId>cfb5e0a790374c9a98a1c0d2044206a7</tenantId>
      <volumeGroupCreated>true</volumeGroupCreated>
      <msoRequest>
         <requestId>1e1a72ca-7300-4ac4-b718-30351f3b6845</requestId>
         <serviceInstanceId>15eb2c68-f771-4030-b185-cff179fdad44</serviceInstanceId>
      </msoRequest>
      <messageId>683ca1ac-2145-4a00-9484-20d48bd701aa</messageId>
   </volumeGroupRollback>
   <skipAAI>true</skipAAI>
   <notificationUrl>http://msobpel-app-e2e.ecomp.cci.att.com:8080/mso/WorkflowMessage/VNFAResponse/683ca1ac-2145-4a00-9484-20d48bd701aa</notificationUrl>
</rollbackVolumeGroupRequest>
"""
	
	@Test
	public void testGetNodeText() {
		Node root = new XmlParser().parseText(rollbackReq)
		def volGrpId = root.'volumeGroupRollback'.'volumeGroupId'.text()
		assertEquals('8a07b246-155e-4b08-b56e-76e98a3c2d66', volGrpId)
	}
	
	@Test
	public void testGetMessageId() {
		Node root = new XmlParser().parseText(rollbackReq)
		//def messageId = root.'volumeGroupRollback'.'messageId'.text()
		
		VnfAdapterRestV1 p = new VnfAdapterRestV1()
		def messageId = p.getMessageIdForVolumeGroupRollback(root)
		assertEquals('683ca1ac-2145-4a00-9484-20d48bd701aa', messageId)
	}
}
