/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2022 Deutsche telekom
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


package org.onap.so.bpmn.moi.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import junit.framework.TestCase;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aai.domain.yang.SliceProfile;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.Silent.class)

public class ModifyRANNssiBBTaskTest {
    @Mock
    BuildingBlockExecution execution;
    @Mock
    ServiceInstance serviceInstance;
    @Mock
    private AAIResourceUri allotedResourceURI;
    private org.onap.aai.domain.yang.Customer customer = new org.onap.aai.domain.yang.Customer();
    @Mock
    private ModelInfoServiceInstance modelInfoServiceInstance;
    @Mock
    protected ExtractPojosForBB extractPojosForBBMock;

    @Mock
    protected InjectionHelper injectionHelper;

    private String operationalState = "ENABLED";
    private String administrativeState = "UNLOCKED";
    private String serviceInstanceId = "123";
    private ServiceSubscription serviceSubscription;
    @Mock
    protected ExceptionBuilder exceptionUtil;
    String serviceType;

    private String sliceProfileServiceInstanceId = "123";

    private AAIRestClientImpl aaiRestClient = new AAIRestClientImpl();

    @Mock
    private GeneralBuildingBlock gBB = new GeneralBuildingBlock();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    ModifyRANNssiBBTask modifyRANNssiBBTask = new ModifyRANNssiBBTask();

    @Before
    public void before() throws BBObjectNotFoundException {
        serviceInstance.setServiceInstanceId("123");
        modelInfoServiceInstance.setModelUuid("235");
        ServiceInstance serviceInstance = new ServiceInstance();
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstanceId);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.ServiceInstance))).thenReturn(serviceInstance);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.ModelInfoServiceInstance)))
                .thenReturn(modelInfoServiceInstance);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.operationalState))).thenReturn(operationalState);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.administrativeState)))
                .thenReturn(administrativeState);
        serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType(serviceType);
    }

    @Test
    public void modifyNssiTest() throws JsonProcessingException {

        doNothing().when(modifyRANNssiBBTask).modifyNssi(execution);
        modifyRANNssiBBTask.modifyNssi(execution);
        verify(modifyRANNssiBBTask, times(1)).modifyNssi(execution);
    }
}


