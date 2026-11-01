package org.onap.so.adapters.sdnc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.DocumentBuilderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.so.adapters.sdnc.RequestHeader;
import org.onap.so.adapters.sdnc.SDNCAdapterRequest;
import org.onap.so.adapters.sdnc.sdncrest.SdncRestCallbackClient;
import org.onap.so.adapters.sdncrest.SdncRestCallback;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SDNCRestClientTransportTest {
    @Test
    public void injectedRestEntryDispatchesOnTheAsyncProxy() throws Exception {
        SdncRestCallbackClient callback = mock(SdncRestCallbackClient.class);
        SDNCRestClient worker = worker(callback, "POST");
        AtomicReference<Thread> executingThread = new AtomicReference<>();
        SDNCResponse response = new SDNCResponse("operation-1");
        response.setRespCode(200);
        doAnswer(invocation -> {
            executingThread.set(Thread.currentThread());
            return response;
        }).when(worker).getSdncResp(anyString(), any());
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.setEnvironment(
                    new MockEnvironment().withProperty(Constants.MY_URL_PROP, "http://localhost/sdnc-notification"));
            context.register(AsyncConfiguration.class);
            context.registerBean(MapRequestTunables.class,
                    () -> (MapRequestTunables) ReflectionTestUtils.getField(worker, "tunablesMapper"));
            context.registerBean(SdncRestCallbackClient.class, () -> callback);
            context.registerBean(SDNCRestClient.class, () -> worker);
            context.refresh();
            SDNCRestClient injected = context.getBean(SDNCRestClient.class);
            assertTrue(org.springframework.aop.support.AopUtils.isAopProxy(injected));

            injected.executeRestRequest(request("ordinary"));

            verify(callback, timeout(10000)).send(eq("http://localhost/callback"), any());
            assertNotNull(executingThread.get());
            assertNotSame(Thread.currentThread(), executingThread.get());
        }
    }

    @TestConfiguration
    @EnableAsync
    static class AsyncConfiguration {
    }

    @Test
    public void allFourControllerRequestShapesRemainEquivalentAcrossTransports() throws Exception {
        for (String[] route : new String[][] {{"ordinary", "POST"}, {Constants.MSO_ACTION_LCM, "POST"},
                {Constants.MSO_ACTION_MDONS, "POST"}, {"ordinary", "PUT"}}) {
            SdncRestCallbackClient callback = mock(SdncRestCallbackClient.class);
            SDNCRestClient worker = worker(callback, route[1]);
            SDNCAdapterRequest request = request(route[0]);
            List<String> bodies = new ArrayList<>();
            SDNCResponse response = new SDNCResponse("operation-1");
            response.setRespCode(200);
            response.setRespMsg("OK");
            response.setSdncRespXml("<output><result>finished</result></output>");
            doAnswer(invocation -> {
                bodies.add(invocation.getArgument(0));
                return response;
            }).when(worker).getSdncResp(anyString(), any());
            doNothing().when(worker).sendRespToBpel(anyString(), any());

            worker.executeRequest(request);
            worker.executeRestRequest(request);

            assertEquals(2, bodies.size());
            assertNotNull(bodies.get(0));
            assertEquals(normalized(route[0], bodies.get(0)), normalized(route[0], bodies.get(1)));
            verify(worker).sendRespToBpel("http://localhost/callback", response);
            ArgumentCaptor<SdncRestCallback> capture = ArgumentCaptor.forClass(SdncRestCallback.class);
            verify(callback).send(eq("http://localhost/callback"), capture.capture());
            assertEquals("operation-1", capture.getValue().getRequestId());
            assertEquals("200", capture.getValue().getResponseCode());
            assertEquals("OK", capture.getValue().getResponseMessage());
            assertEquals(response.getSdncRespXml(), capture.getValue().getRequestDataXml());
        }
    }

    @Test
    public void failureAfterAcceptanceUsesOnlyTheRestErrorCallback() throws Exception {
        SdncRestCallbackClient callback = mock(SdncRestCallbackClient.class);
        SDNCRestClient worker = worker(callback, "POST");
        doThrow(new IllegalStateException("synthetic controller failure")).when(worker).getSdncResp(anyString(), any());

        worker.executeRestRequest(request("ordinary"));

        ArgumentCaptor<SdncRestCallback> capture = ArgumentCaptor.forClass(SdncRestCallback.class);
        verify(callback).send(eq("http://localhost/callback"), capture.capture());
        assertEquals("operation-1", capture.getValue().getRequestId());
        assertEquals("500", capture.getValue().getResponseCode());
        verify(worker, never()).sendRespToBpel(anyString(), any());
    }

    @Test
    public void failedRestCallbackDoesNotResubmitTheControllerRequest() throws Exception {
        SdncRestCallbackClient callback = mock(SdncRestCallbackClient.class);
        SDNCRestClient worker = worker(callback, "POST");
        SDNCResponse response = new SDNCResponse("operation-1");
        response.setRespCode(200);
        doAnswer(invocation -> response).when(worker).getSdncResp(anyString(), any());
        doThrow(new RestClientException("synthetic callback failure")).when(callback).send(anyString(), any());
        SDNCAdapterRequest request = request("ordinary");

        assertThrows(RestClientException.class, () -> worker.executeRestRequest(request));

        verify(worker).getSdncResp(anyString(), any());
        verify(callback).send(eq("http://localhost/callback"), any());
        verify(worker, never()).sendRespToBpel(anyString(), any());
    }

    private SDNCRestClient worker(SdncRestCallbackClient callback, String method) {
        SDNCRestClient worker = spy(new SDNCRestClient());
        MapRequestTunables mapper = mock(MapRequestTunables.class);
        doAnswer(invocation -> {
            RequestTunables tunables = invocation.getArgument(0);
            tunables.setReqMethod(method);
            tunables.setNamespace("urn:synthetic:sdnc");
            return tunables;
        }).when(mapper).setTunables(any());
        ReflectionTestUtils.setField(worker, "tunablesMapper", mapper);
        ReflectionTestUtils.setField(worker, "env",
                new MockEnvironment().withProperty(Constants.MY_URL_PROP, "http://localhost/sdnc-notification"));
        ReflectionTestUtils.setField(worker, "restCallbackClient", callback);
        return worker;
    }

    private SDNCAdapterRequest request(String action) throws Exception {
        RequestHeader header = new RequestHeader();
        header.setRequestId("operation-1");
        header.setMsoAction(action);
        header.setSvcAction("assign");
        header.setSvcOperation("vnf-topology-operation");
        header.setCallbackUrl("http://localhost/callback");
        SDNCAdapterRequest request = new SDNCAdapterRequest();
        request.setRequestHeader(header);
        request.setRequestData(xml("<RequestData><payload><value>test &amp; value</value></payload></RequestData>")
                .getDocumentElement());
        return request;
    }

    private Object normalized(String action, String body) throws Exception {
        if (Constants.MSO_ACTION_MDONS.equals(action)) {
            return new ObjectMapper().readTree(body);
        }
        Document document = xml(body);
        for (String name : new String[] {"sub-request-id", "timestamp"}) {
            var nodes = document.getElementsByTagName(name);
            for (int index = 0; index < nodes.getLength(); index++) {
                nodes.item(index).setTextContent("generated");
            }
        }
        return Utils.domToStr(document);
    }

    private Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(value)));
    }
}
