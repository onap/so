package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.domain.yang.L3Network;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.springframework.beans.factory.annotation.Autowired;

public class NetworkBBUtilsTest  extends BaseTaskTest{
	@Autowired
	private NetworkBBUtils networkBBUtils;
	
	private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/Network/";
	
	private CloudRegion cloudRegion;
	
	@Before
	public void before() {
		cloudRegion = setCloudRegion();
	}

	@Test
	public void isRelationshipRelatedToExistsTrueTest() throws Exception {
		final String aaiResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "unassignNetworkBB_queryAAIResponse_.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiResponse); 
		Optional<L3Network> l3network = aaiResultWrapper.asBean(L3Network.class);
		
		boolean isVfModule = networkBBUtils.isRelationshipRelatedToExists(l3network, "vf-module");
		assertTrue(isVfModule);
		
	}
	
	@Test
	public void isRelationshipRelatedToExistsFalseTest() throws Exception {
		final String aaiResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAAIResponse.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiResponse); 
		Optional<L3Network> l3network = aaiResultWrapper.asBean(L3Network.class);
		
		boolean isVfModule = networkBBUtils.isRelationshipRelatedToExists(l3network, "vf-module");
		assertFalse(isVfModule);
		
	}	
	
	@Test
	public void getCloudRegionSDNC25Test() throws Exception {
		cloudRegion.setCloudRegionVersion("2.5");
		
		NetworkBBUtils spyAssign = Mockito.spy(NetworkBBUtils.class);
		String cloudRegionId = spyAssign.getCloudRegion(execution, SourceSystem.SDNC);
		Mockito.verify(spyAssign).getCloudRegion(execution, SourceSystem.SDNC);

		assertEquals("AAIAIC25", cloudRegionId);
	}	
	
	@Test
	public void getCloudRegionSDNC30Test() throws Exception {
		cloudRegion.setCloudRegionVersion("3.0");
		
		NetworkBBUtils spyAssign = Mockito.spy(NetworkBBUtils.class);
		String cloudRegionId = spyAssign.getCloudRegion(execution, SourceSystem.SDNC);
		Mockito.verify(spyAssign).getCloudRegion(execution, SourceSystem.SDNC);

		assertEquals(cloudRegion.getLcpCloudRegionId(), cloudRegionId);
	}	
	
	@Test
	public void getCloudRegionPO25Test() throws Exception {
		cloudRegion.setCloudRegionVersion("2.5");
		
		NetworkBBUtils spyAssign = Mockito.spy(NetworkBBUtils.class);
		String cloudRegionId = spyAssign.getCloudRegion(execution, SourceSystem.PO);
		Mockito.verify(spyAssign).getCloudRegion(execution, SourceSystem.PO);

		assertEquals(cloudRegion.getLcpCloudRegionId(), cloudRegionId);
	}	
	
	@Test
	public void getCloudRegionPO30Test() throws Exception {
		cloudRegion.setCloudRegionVersion("3.0");
		
		NetworkBBUtils spyAssignPO = Mockito.spy(NetworkBBUtils.class);
		String cloudRegionIdPO = spyAssignPO.getCloudRegion(execution, SourceSystem.PO);
		Mockito.verify(spyAssignPO).getCloudRegion(execution, SourceSystem.PO);

		assertEquals(cloudRegion.getLcpCloudRegionId(), cloudRegionIdPO);		
	}	
}