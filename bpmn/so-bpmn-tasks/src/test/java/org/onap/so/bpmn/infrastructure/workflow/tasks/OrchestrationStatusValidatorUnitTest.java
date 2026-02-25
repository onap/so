/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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


        assertThat(execution.getVariable("orchestrationStatusValidationResult"),
                equalTo(OrchestrationStatusValidationDirective.VALIDATION_SKIPPED));
    }

}
