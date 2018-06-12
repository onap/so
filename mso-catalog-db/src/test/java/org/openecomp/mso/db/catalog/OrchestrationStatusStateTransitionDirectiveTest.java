package org.openecomp.mso.db.catalog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.TestApplication;
import org.openecomp.mso.db.catalog.beans.OrchestrationAction;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.openecomp.mso.db.catalog.beans.ResourceType;
import org.openecomp.mso.db.catalog.data.repository.OrchestrationStatusStateTransitionDirectiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OrchestrationStatusStateTransitionDirectiveTest {
	@Autowired
	private OrchestrationStatusStateTransitionDirectiveRepository orchestrationStatusStateTransitionDirectiveRepository;

	@Test
	public void OrchestrationStatusTransitionDBSingleLookupValidationTest() {
		OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective = orchestrationStatusStateTransitionDirectiveRepository.findOneByResourceTypeAndOrchestrationStatusAndTargetAction(ResourceType.SERVICE, OrchestrationStatus.ASSIGNED, OrchestrationAction.ASSIGN);
		assertEquals(ResourceType.SERVICE, orchestrationStatusStateTransitionDirective.getResourceType());
		assertEquals(OrchestrationStatus.ASSIGNED, orchestrationStatusStateTransitionDirective.getOrchestrationStatus());
		assertEquals(OrchestrationAction.ASSIGN, orchestrationStatusStateTransitionDirective.getTargetAction());
		assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS, orchestrationStatusStateTransitionDirective.getFlowDirective());
	}
}
