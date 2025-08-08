/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.common.name.generation.AAIObjectInstanceNameGenerator;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.so.client.exception.BBObjectNotFoundException;

public class AssignVnfTest extends BaseTaskTest {
    @InjectMocks
    private AssignVnf assignVnf = new AssignVnf();

    @Mock
    private AAIObjectInstanceNameGenerator aaiObjectInstanceNameGenerator = new AAIObjectInstanceNameGenerator();

    private InstanceGroup instanceGroup1;
    private InstanceGroup instanceGroup2;
    private InstanceGroup instanceGroup3;
    private InstanceGroup instanceGroup4;
    private GenericVnf genericVnf;

    @Before
    public void before() throws BBObjectNotFoundException {
        ModelInfoInstanceGroup modelVnfc = new ModelInfoInstanceGroup();
        modelVnfc.setType("VNFC");
        modelVnfc.setFunction("function");

        ModelInfoInstanceGroup modelNetworkInstanceGroup = new ModelInfoInstanceGroup();
        modelNetworkInstanceGroup.setType("L3-NETWORK");
        modelNetworkInstanceGroup.setFunction("function");

        instanceGroup1 = new InstanceGroup();
        instanceGroup1.setId("test-001");
        instanceGroup1.setModelInfoInstanceGroup(modelVnfc);

        instanceGroup2 = new InstanceGroup();
        instanceGroup2.setId("test-002");
        instanceGroup2.setModelInfoInstanceGroup(modelVnfc);

        instanceGroup3 = new InstanceGroup();
        instanceGroup3.setId("test-003");
        instanceGroup3.setModelInfoInstanceGroup(modelNetworkInstanceGroup);

        instanceGroup4 = new InstanceGroup();
        instanceGroup4.setId("test-004");
        instanceGroup4.setModelInfoInstanceGroup(modelNetworkInstanceGroup);

        genericVnf = setGenericVnf();
        genericVnf.setVnfName("vnfName");


        doNothing().when(aaiInstanceGroupResources).createInstanceGroup(isA(InstanceGroup.class));
        doNothing().when(aaiInstanceGroupResources).connectInstanceGroupToVnf(isA(InstanceGroup.class),
                isA(GenericVnf.class));
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID)))
                .thenReturn(genericVnf);
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
    }

    @Test
    public void createInstanceGroupsSunnyDayTest() {

        List<InstanceGroup> instanceGroupList = genericVnf.getInstanceGroups();
        instanceGroupList.add(instanceGroup1);
        instanceGroupList.add(instanceGroup2);
        instanceGroupList.add(instanceGroup3);
        instanceGroupList.add(instanceGroup4);


        assignVnf.createInstanceGroups(execution);
        verify(aaiInstanceGroupResources, times(1)).createInstanceGroup(instanceGroup1);
        verify(aaiInstanceGroupResources, times(1)).createInstanceGroup(instanceGroup2);
        verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup1, genericVnf,
                AAIEdgeLabel.BELONGS_TO);
        verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup2, genericVnf,
                AAIEdgeLabel.BELONGS_TO);
        verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup3, genericVnf,
                AAIEdgeLabel.USES);
        verify(aaiInstanceGroupResources, times(1)).connectInstanceGroupToVnf(instanceGroup4, genericVnf,
                AAIEdgeLabel.USES);
    }

    @Test
    public void createVnfcInstanceGroupNoneTest() {
        assignVnf.createInstanceGroups(execution);


        verify(aaiInstanceGroupResources, times(0)).createInstanceGroup(any(InstanceGroup.class));
        verify(aaiInstanceGroupResources, times(0)).connectInstanceGroupToVnf(any(InstanceGroup.class),
                any(GenericVnf.class));
    }

    @Test
    public void createVnfcInstanceGroupExceptionTest() {
        List<InstanceGroup> instanceGroupList = genericVnf.getInstanceGroups();
        instanceGroupList.add(instanceGroup1);
        instanceGroupList.add(instanceGroup2);
        instanceGroupList.add(instanceGroup3);
        instanceGroupList.add(instanceGroup4);
        doThrow(RuntimeException.class).when(aaiInstanceGroupResources).createInstanceGroup(isA(InstanceGroup.class));
        expectedException.expect(BpmnError.class);

        genericVnf.setVnfId("test-999");
        assignVnf.createInstanceGroups(execution);
    }
}
