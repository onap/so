package org.onap.so.logging.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.MdcConstants;
import org.slf4j.MDC;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MDCSetupTest {

    @Mock
    private org.onap.logging.filter.base.MDCSetup mdcSet;

    @Mock
    private ExternalTask externalTask;

    private String requestId = "9bb86b8d-a02f-4a0b-81a9-2eb963850009";
    private String serviceName = "testServiceName";

    @Spy
    @InjectMocks
    AuditMDCSetup setup = new AuditMDCSetup();

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void setupMDCTest() {
        doReturn(requestId).when(externalTask).getVariable("mso-request-id");
        doReturn(serviceName).when(externalTask).getTopicName();

        setup.setupMDC(externalTask);

        assertNotNull(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
        assertEquals(requestId, MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
        assertEquals(serviceName, MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME));
        assertEquals("SO.OPENSTACK_ADAPTER", MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
        assertEquals(ONAPLogConstants.ResponseStatus.INPROGRESS.toString(),
                MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
    }

    @Test
    public void setElapsedTimeTest() {
        MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP, "2019-06-18T02:09:06.024Z");

        setup.setElapsedTime();

        assertNotNull(MDC.get(ONAPLogConstants.MDCs.ELAPSED_TIME));
    }

    @Test
    public void setResponseCodeTest() {
        setup.setResponseCode(ONAPLogConstants.ResponseStatus.INPROGRESS.toString());

        assertEquals(ONAPLogConstants.ResponseStatus.INPROGRESS.toString(),
                MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
        assertEquals(ONAPLogConstants.ResponseStatus.INPROGRESS.toString(),
                MDC.get(MdcConstants.OPENSTACK_STATUS_CODE));
    }

    @Test
    public void clearClientMDCsTest() {
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        MDC.put(MdcConstants.OPENSTACK_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP, "2019-06-18T02:09:06.024Z");
        MDC.put(ONAPLogConstants.MDCs.ELAPSED_TIME, "318");
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, "SO.OPENSTACK_ADAPTER");

        setup.clearClientMDCs();

        assertNull(MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
        assertNull(MDC.get(MdcConstants.OPENSTACK_STATUS_CODE));
        assertNull(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
        assertNull(MDC.get(ONAPLogConstants.MDCs.ELAPSED_TIME));
        assertNull(MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));

    }

}
