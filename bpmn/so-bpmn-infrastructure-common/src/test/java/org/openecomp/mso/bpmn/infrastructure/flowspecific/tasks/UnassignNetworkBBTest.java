package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.springframework.beans.factory.annotation.Autowired;

public class UnassignNetworkBBTest extends BaseTaskTest {
	@Autowired
	private UnassignNetworkBB unassignNetworkBB;
	
	private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/Network/";	
	
	@Test
	public void checkRelationshipRelatedToTrueTest() throws Exception {
		expectedException.expect(BpmnError.class);
		final String aaiResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "unassignNetworkBB_queryAAIResponse_.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiResponse); 
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		unassignNetworkBB.checkRelationshipRelatedTo(execution, "vf-module");
	}	
	
	@Test
	public void checkRelationshipRelatedToFalseTest() throws Exception {
		final String aaiResponse = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "unassignNetworkBB_queryAAIResponse_.json")));
		AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiResponse); 
		execution.setVariable("l3NetworkAAIResultWrapper", aaiResultWrapper);
		
		unassignNetworkBB.checkRelationshipRelatedTo(execution, "kfc-module");
		//expected result is no exception
	}	
	
	@Test
	public void getCloudSdncRegion25Test() throws Exception {
		CloudRegion cloudRegion = setCloudRegion();
		cloudRegion.setCloudRegionVersion("2.5");
		unassignNetworkBB.getCloudSdncRegion(execution);
		assertEquals("AAIAIC25", execution.getVariable("cloudRegionSdnc"));
	}	
	
	@Test
	public void getCloudSdncRegion30Test() throws Exception {
		CloudRegion cloudRegion = setCloudRegion();
		cloudRegion.setCloudRegionVersion("3.0");
		gBBInput.setCloudRegion(cloudRegion);
		unassignNetworkBB.getCloudSdncRegion(execution);
		assertEquals(cloudRegion.getLcpCloudRegionId(), execution.getVariable("cloudRegionSdnc"));
	}	
	
	@Test
	public void errorEncounteredTest_rollback() throws Exception {
		expectedException.expect(BpmnError.class);
		execution.setVariable("ErrorUnassignNetworkBB", "Relationship's RelatedTo still exists in AAI, remove the relationship vf-module first.");
		execution.setVariable("isRollbackNeeded", true);
		unassignNetworkBB.errorEncountered(execution);
	}
	
	@Test
	public void errorEncounteredTest_noRollback() throws Exception {
		expectedException.expect(BpmnError.class);
		execution.setVariable("ErrorUnassignNetworkBB", "Relationship's RelatedTo still exists in AAI, remove the relationship vf-module first.");
		unassignNetworkBB.errorEncountered(execution);
	}	
}