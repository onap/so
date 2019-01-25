package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.OrchestrationAction;
import org.onap.so.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.onap.so.db.catalog.beans.ResourceType;
import org.onap.so.db.catalog.client.CatalogDbClient;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationStatusValidatorUnitTest {

	@Mock
	private CatalogDbClient catalogDbClient;
	
	@InjectMocks
	private OrchestrationStatusValidator validator;
	@Test
	public void skipValidationTest() {
		BuildingBlockDetail bbDetail = new BuildingBlockDetail();
		bbDetail.setBuildingBlockName("customBB");
		bbDetail.setResourceType(ResourceType.NO_VALIDATE);
		bbDetail.setTargetAction(OrchestrationAction.CUSTOM);
		when(catalogDbClient.getBuildingBlockDetail("customBB")).thenReturn(bbDetail);
		BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
		execution.setVariable("flowToBeCalled", "customBB");
		execution.setVariable("aLaCarte", false);
		validator.validateOrchestrationStatus(execution);
		
		
		assertThat(execution.getVariable("orchestrationStatusValidationResult"), equalTo(OrchestrationStatusValidationDirective.VALIDATION_SKIPPED));
	}
	
}
