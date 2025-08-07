/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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


package org.onap.so.bpmn.servicedecomposition.tasks;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Pnfs;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class BBInputSetupPnfTest {

    @Mock
    private Pnfs pnfs;

    @Mock
    private ModelInfo modelInfo;

    @Test
    public void populatePnfShouldSetRequiredFields() {
        final String pnfId = "PNF_id1";
        final String pnfName = "PNF_name1";
        final String modelCustomizationId = "8421fe03-fd1b-4bf7-845a-c3fe91edb031";
        final String modelInvariantId = "3360a2a5-22ff-44c7-8935-08c8e5ecbd06";
        final String modelVersionId = "b80c3a52-abd4-436c-a22e-9c5da768781a";

        doReturn(modelCustomizationId).when(modelInfo).getModelCustomizationId();
        doReturn(modelInvariantId).when(modelInfo).getModelInvariantId();
        doReturn(modelVersionId).when(modelInfo).getModelVersionId();
        doReturn(pnfName).when(pnfs).getInstanceName();
        doReturn(modelInfo).when(pnfs).getModelInfo();

        ServiceInstance serviceInstance = new ServiceInstance();
        BBInputSetupPnf.populatePnfToServiceInstance(pnfs, pnfId, serviceInstance);

        assertEquals(1, serviceInstance.getPnfs().size());

        Pnf pnf = serviceInstance.getPnfs().get(0);

        assertEquals(pnfId, pnf.getPnfId());
        assertEquals(pnfName, pnf.getPnfName());
        assertEquals(modelCustomizationId, pnf.getModelInfoPnf().getModelCustomizationUuid());
        assertEquals(modelInvariantId, pnf.getModelInfoPnf().getModelInvariantUuid());
        assertEquals(modelVersionId, pnf.getModelInfoPnf().getModelUuid());
        assertEquals(OrchestrationStatus.PRECREATED, pnf.getOrchestrationStatus());
    }
}
