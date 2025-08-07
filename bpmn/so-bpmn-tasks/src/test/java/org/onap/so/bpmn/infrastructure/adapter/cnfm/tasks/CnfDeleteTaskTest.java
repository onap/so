/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;

/**
 * @author raviteja.kaumuri@est.tech
 */
@RunWith(MockitoJUnitRunner.class)
public class CnfDeleteTaskTest {

    @Mock
    private CnfmHttpServiceProvider cnfmHttpServiceProvider;
    @Mock
    private ExceptionBuilder exceptionUtil;
    @Mock
    private ExtractPojosForBB extractPojosForBB;
    private CnfDeleteTask cnfDeleteTask;
    private static final String TERMINATE_AS_REQUEST_OBJECT = "TerminateAsRequest";
    private static final String CNFM_REQUEST_STATUS_CHECK_URL = "CnfmStatusCheckUrl";
    private final BuildingBlockExecution stubbedExecution = new StubbedBuildingBlockExecution();

    @Before
    public void setup() {
        cnfDeleteTask = new CnfDeleteTask(cnfmHttpServiceProvider, exceptionUtil, extractPojosForBB);
    }

    @Test
    public void test_createTerminateAsRequest_success() {
        cnfDeleteTask.createTerminateAsRequest(stubbedExecution);
        assertNotNull(stubbedExecution.getVariable(TERMINATE_AS_REQUEST_OBJECT));
    }

    @Test
    public void test_invokeCnfmToTerminateAsInstance_success() throws BBObjectNotFoundException {
        stubbedExecution.setVariable(TERMINATE_AS_REQUEST_OBJECT, getTerminateAsRequest());
        when(extractPojosForBB.extractByKey(Mockito.any(), Mockito.any())).thenReturn(getGenericVnf());
        when(cnfmHttpServiceProvider.invokeTerminateAsRequest(Mockito.anyString(),
                Mockito.any(TerminateAsRequest.class))).thenReturn(getURI());
        cnfDeleteTask.invokeCnfmToTerminateAsInstance(stubbedExecution);
        URI returnedContent = stubbedExecution.getVariable(CNFM_REQUEST_STATUS_CHECK_URL);
        assertEquals(getURI().orElseThrow().getPath(), returnedContent.getPath());
    }

    @Test
    public void test_invokeCnfmToTerminateAsInstance_Exception() throws BBObjectNotFoundException {
        stubbedExecution.setVariable(TERMINATE_AS_REQUEST_OBJECT, getTerminateAsRequest());
        when(extractPojosForBB.extractByKey(Mockito.any(), Mockito.any())).thenThrow(new RuntimeException());
        cnfDeleteTask.invokeCnfmToTerminateAsInstance(stubbedExecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(2002),
                any(Exception.class));
    }

    @Test
    public void test_invokeCnfmToDeleteAsInstance_success() throws BBObjectNotFoundException {
        when(extractPojosForBB.extractByKey(Mockito.any(), Mockito.any())).thenReturn(getGenericVnf());
        when(cnfmHttpServiceProvider.invokeDeleteAsRequest(Mockito.anyString())).thenReturn(Optional.of(Boolean.TRUE));
        CnfDeleteTask mockCnfDeleteTask = Mockito.spy(cnfDeleteTask);
        mockCnfDeleteTask.invokeCnfmToDeleteAsInstance(stubbedExecution);
        verify(mockCnfDeleteTask, times(1)).invokeCnfmToDeleteAsInstance(stubbedExecution);
    }

    @Test
    public void test_invokeCnfmToDeleteAsInstance_Exception() throws BBObjectNotFoundException {
        when(extractPojosForBB.extractByKey(Mockito.any(), Mockito.any())).thenThrow(new RuntimeException());
        cnfDeleteTask.invokeCnfmToDeleteAsInstance(stubbedExecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(2003),
                any(Exception.class));
    }

    private GenericVnf getGenericVnf() {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("12345");
        return genericVnf;
    }

    private TerminateAsRequest getTerminateAsRequest() {
        return new TerminateAsRequest();
    }

    private Optional<URI> getURI() {
        return Optional.of(URI.create("test_sample"));
    }
}
