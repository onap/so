package org.onap.so.bpmn.servicedecomposition.tasks;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Pnfs;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class PnfInitializerTest {

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

        Pnf pnf = new Pnf();

        new PnfInitializer(pnf).populatePnf(pnfs, pnfId);

        assertEquals(pnf.getPnfId(), pnfId);
        assertEquals(pnf.getPnfName(), pnfName);
        assertEquals(pnf.getModelCustomizationId(), modelCustomizationId);
        assertEquals(pnf.getModelInvariantId(), modelInvariantId);
        assertEquals(pnf.getModelVersionId(), modelVersionId);
        assertEquals(pnf.getOrchestrationStatus(), OrchestrationStatus.PRECREATED);
    }
}
