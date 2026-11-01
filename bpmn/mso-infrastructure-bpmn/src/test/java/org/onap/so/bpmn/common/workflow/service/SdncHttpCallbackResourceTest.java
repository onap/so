package org.onap.so.bpmn.common.workflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.so.adapters.sdncrest.SdncRestCallback;
import org.onap.so.bpmn.common.adapter.sdnc.CallbackHeader;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;

public class SdncHttpCallbackResourceTest {
    private CallbackHandlerService handler;
    private SdncHttpCallbackResource resource;
    private SdncRestCallback request;

    @Before
    public void prepareCallback() {
        handler = spy(new CallbackHandlerService(mock(RuntimeService.class)));
        doReturn(true).when(handler).correlate(anyString(), anyString(), anyString(), any(), anyString());
        resource = new SdncHttpCallbackResource(handler);
        request = new SdncRestCallback();
        request.setRequestId("operation-1");
        request.setResponseCode("200");
        request.setResponseMessage("OK & ready");
        request.setRequestDataXml("<output><value>Gr\u00fc\u00dfe &amp; SO</value></output>");
    }

    @Test
    public void injectsExactlyTheLegacySerializedVariableBeforeAcknowledgement() {
        assertEquals(204, resource.deliver(request).getStatus());

        ArgumentCaptor<Map<String, Object>> variables = ArgumentCaptor.forClass(Map.class);
        verify(handler).correlate(org.mockito.ArgumentMatchers.eq("sdncAdapterCallbackRequest"),
                org.mockito.ArgumentMatchers.eq("SDNCA_requestId"), org.mockito.ArgumentMatchers.eq("operation-1"),
                variables.capture(), anyString());
        assertEquals("operation-1", variables.getValue().get("SDNCA_requestId"));
        assertEquals(legacy(request).toString(), variables.getValue().get("sdncAdapterCallbackRequest"));
        assertEquals(2, variables.getValue().size());
        verify(handler, never()).handleCallback(anyString(), any(), anyString(), anyString(), anyString(), anyString(),
                anyString());
    }

    @Test
    public void preservesNullAndEmptyOptionalDataWithoutInventingXml() {
        for (String xml : new String[] {null, ""}) {
            request.setRequestDataXml(xml);
            assertEquals(204, resource.deliver(request).getStatus());
        }
        ArgumentCaptor<Map<String, Object>> variables = ArgumentCaptor.forClass(Map.class);
        verify(handler, times(2)).correlate(anyString(), anyString(), anyString(), variables.capture(), anyString());
        request.setRequestDataXml(null);
        assertEquals(legacy(request).toString(), variables.getAllValues().get(0).get("sdncAdapterCallbackRequest"));
        request.setRequestDataXml("");
        assertEquals(legacy(request).toString(), variables.getAllValues().get(1).get("sdncAdapterCallbackRequest"));
    }

    @Test
    public void missingOrDuplicateWaiterDoesNotReceiveSuccessfulAcknowledgement() {
        doReturn(true, false).when(handler).correlate(anyString(), anyString(), anyString(), any(), anyString());

        assertEquals(204, resource.deliver(request).getStatus());
        var rejected = resource.deliver(request);
        assertEquals(500, rejected.getStatus());
        assertEquals(java.util.Collections.singletonMap("error", "SDNC callback correlation failed"),
                rejected.getEntity());
    }

    @Test
    public void resumedWorkflowFailureIsNotAcknowledged() {
        doThrow(new IllegalStateException("synthetic process failure")).when(handler).correlate(anyString(),
                anyString(), anyString(), any(), anyString());

        assertEquals(500, resource.deliver(request).getStatus());
    }

    @Test
    public void rejectsMissingIdentifiersAndUnsafeXmlBeforeCorrelation() {
        assertEquals(400, resource.deliver(null).getStatus());
        request.setRequestId("");
        assertEquals(400, resource.deliver(request).getStatus());
        request.setRequestId("operation-1");
        for (String xml : new String[] {"<broken>",
                "<!DOCTYPE output [<!ENTITY secret SYSTEM 'file:///not-readable'>]><output>&secret;</output>",
                "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body/></Envelope>"}) {
            request.setRequestDataXml(xml);
            assertEquals(400, resource.deliver(request).getStatus());
        }
        verify(handler, never()).correlate(anyString(), anyString(), anyString(), any(), anyString());
    }

    private SDNCAdapterCallbackRequest legacy(SdncRestCallback source) {
        CallbackHeader header = new CallbackHeader();
        header.setRequestId(source.getRequestId());
        header.setResponseCode(source.getResponseCode());
        header.setResponseMessage(source.getResponseMessage());
        SDNCAdapterCallbackRequest legacy = new SDNCAdapterCallbackRequest();
        legacy.setCallbackHeader(header);
        legacy.setRequestData(source.getRequestDataXml());
        return legacy;
    }
}
