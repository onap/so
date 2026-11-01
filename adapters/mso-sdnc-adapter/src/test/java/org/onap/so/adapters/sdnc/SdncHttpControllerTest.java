package org.onap.so.adapters.sdnc;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.so.adapters.sdnc.impl.SDNCRestClient;
import org.onap.so.adapters.sdncrest.SdncRestRequest;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.w3c.dom.Node;

public class SdncHttpControllerTest {
    private static final String PATH = "/adapters/rest/v2/sdnc/requests";
    private final SDNCRestClient worker = mock(SDNCRestClient.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private SdncRestRequest request;

    @Before
    public void prepareIngress() {
        mvc = MockMvcBuilders.standaloneSetup(new SdncHttpController(worker)).build();
        request = new SdncRestRequest();
        request.setRequestId("operation-1");
        request.setServiceInstanceId("service-1");
        request.setMsoAction("assign");
        request.setSvcAction("assign");
        request.setSvcOperation("vnf-topology-operation");
        request.setCallbackUrl("http://localhost:8081/mso/sdnc/callbacks/v1");
        request.setRequestDataXml("<RequestData><value>Gr\u00fc\u00dfe &amp; SO</value></RequestData>");
    }

    @Test
    public void acceptsOnlyAfterDispatchWithTheOriginalHeaderAndRequestData() throws Exception {
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.requestId").value("operation-1"));

        ArgumentCaptor<SDNCAdapterRequest> capture = ArgumentCaptor.forClass(SDNCAdapterRequest.class);
        verify(worker).executeRestRequest(capture.capture());
        verify(worker, never()).executeRequest(any());
        RequestHeader header = capture.getValue().getRequestHeader();
        assertEquals(request.getRequestId(), header.getRequestId());
        assertEquals(request.getServiceInstanceId(), header.getSvcInstanceId());
        assertEquals(request.getMsoAction(), header.getMsoAction());
        assertEquals(request.getSvcAction(), header.getSvcAction());
        assertEquals(request.getSvcOperation(), header.getSvcOperation());
        assertEquals(request.getCallbackUrl(), header.getCallbackUrl());
        Node data = (Node) capture.getValue().getRequestData();
        assertEquals("RequestData", data.getOwnerDocument().getDocumentElement().getLocalName());
        assertEquals("Gr\u00fc\u00dfe & SO", data.getTextContent());
    }

    @Test
    public void rejectsMalformedXmlSoapEnvelopesAndDtdsWithoutDispatch() throws Exception {
        for (String xml : new String[] {"<broken>",
                "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body/></Envelope>",
                "<!DOCTYPE RequestData [<!ENTITY secret SYSTEM 'file:///not-readable'>]>"
                        + "<RequestData>&secret;</RequestData>"}) {
            request.setRequestDataXml(xml);
            mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                    .andExpect(status().isBadRequest());
        }
        verify(worker, never()).executeRestRequest(any());
    }

    @Test
    public void rejectsMissingRequestIdAndInvalidCallbackWithoutDispatch() throws Exception {
        request.setRequestId("");
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest());
        request.setRequestId("operation-1");
        request.setCallbackUrl("file:///callback");
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest());
        verify(worker, never()).executeRestRequest(any());
    }

    @Test
    public void rejectedExecutorIsNotAcknowledged() throws Exception {
        doThrow(new TaskRejectedException("synthetic executor rejection")).when(worker).executeRestRequest(any());

        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isServiceUnavailable());
    }
}
