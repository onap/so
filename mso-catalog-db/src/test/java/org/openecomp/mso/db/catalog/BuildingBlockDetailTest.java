package org.openecomp.mso.db.catalog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.TestApplication;
import org.openecomp.mso.db.catalog.beans.BuildingBlockDetail;
import org.openecomp.mso.db.catalog.beans.OrchestrationAction;
import org.openecomp.mso.db.catalog.beans.ResourceType;
import org.openecomp.mso.db.catalog.data.repository.BuildingBlockDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BuildingBlockDetailTest {
	@Autowired
	private BuildingBlockDetailRepository buildingBlockDetailRepository;
	
	@Test
	public void BuildingBlockDetailSingleLookupValidationTest() {
		String buildingBlockName = "AssignServiceInstanceBB";
		
		BuildingBlockDetail buildingBlockDetail = buildingBlockDetailRepository.findOneByBuildingBlockName(buildingBlockName);
		assertEquals(buildingBlockName, buildingBlockDetail.getBuildingBlockName());
		assertEquals(ResourceType.SERVICE, buildingBlockDetail.getResourceType());
		assertEquals(OrchestrationAction.ASSIGN, buildingBlockDetail.getTargetAction());
	}
}
