/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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

package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.NetworkRequestCommon;
import org.onap.so.adapters.nwrest.UpdateNetworkError;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.NetworkAdapterResources;
import org.onap.so.utils.Components;
import org.onap.logging.filter.base.ONAPComponents;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class NetworkAdapterRestV1Test {

    @Mock
    private ExceptionBuilder exceptionBuilder;
    @Mock
    private NetworkAdapterResources networkAdapterResources;
    @InjectMocks
    private NetworkAdapterRestV1 networkAdapterRestV1Tasks;

    private DelegateExecution delegateExecution;

    private static final String CREATE_NETWORK_RESPONSE = "createNetworkResponse";
    private static final String DELETE_NETWORK_RESPONSE = "deleteNetworkResponse";
    private static final String CREATE_NETWORK_ERROR = "createNetworkError";
    private static final String DELETE_NETWORK_ERROR = "deleteNetworkError";
    private static final String NET_ID_FOR_CREATE_NETWORK_RESPONSE = "netIdForCreateNetworkResponse";
    private static final String NET_ID_FOR_DELETE_NETWORK_RESPONSE = "netIdForDeleteNetworkResponse";
    private static final String CREATE_NETWORK_ERROR_MESSAGE = "createNetErrorMessage";
    private static final String DELETE_NETWORK_ERROR_MESSAGE = "deleteNetErrorMessage";

    @Before
    public void setup() {
        initMocks(this);
        delegateExecution = new DelegateExecutionFake();
    }

    @Test
    public void testUnmarshalXml() throws JAXBException {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><createNetworkResponse><messageId>ec37c121-e3ec-4697-8adf-2d7dca7044fc</messageId><networkCreated>true</networkCreated><networkFqdn>someNetworkFqdn</networkFqdn><networkId>991ec7bf-c9c4-4ac1-bb9c-4b61645bddb3</networkId><networkStackId>someStackId</networkStackId><neutronNetworkId>9c47521a-2916-4018-b2bc-71ab767497e3</neutronNetworkId><rollback><cloudId>someCloudId</cloudId><modelCustomizationUuid>b7171cdd-8b05-459b-80ef-2093150e8983</modelCustomizationUuid><msoRequest><requestId>90b32315-176e-4dab-bcf1-80eb97a1c4f4</requestId><serviceInstanceId>71e7db22-7907-4d78-8fcc-8d89d28e90be</serviceInstanceId></msoRequest><networkCreated>true</networkCreated><networkStackId>someStackId</networkStackId><networkType>SomeNetworkType</networkType><neutronNetworkId>9c47521a-2916-4018-b2bc-71ab767497e3</neutronNetworkId><tenantId>b60da4f71c1d4b35b8113d4eca6deaa1</tenantId></rollback><subnetMap><entry><key>6b381fa9-48ce-4e16-9978-d75309565bb6</key><value>bc1d5537-860b-4894-8eba-6faff41e648c</value></entry></subnetMap></createNetworkResponse>";
        CreateNetworkResponse response =
                (CreateNetworkResponse) new NetworkAdapterRestV1().unmarshalXml(xml, CreateNetworkResponse.class);
        String returnedXml = response.toXmlString();
        System.out.println(returnedXml);
    }

    @Test
    public void testUnmarshalXmlUpdate() throws JAXBException {
        UpdateNetworkResponse expectedResponse = new UpdateNetworkResponse();
        expectedResponse.setMessageId("ec100bcc-2659-4aa4-b4d8-3255715c2a51");
        expectedResponse.setNetworkId("80de31e3-cc78-4111-a9d3-5b92bf0a39eb");
        Map<String, String> subnetMap = new HashMap<>();
        subnetMap.put("95cd8437-25f1-4238-8720-cbfe7fa81476", "d8d16606-5d01-4822-b160-9a0d257303e0");
        expectedResponse.setSubnetMap(subnetMap);
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><updateNetworkResponse><messageId>ec100bcc-2659-4aa4-b4d8-3255715c2a51</messageId><networkId>80de31e3-cc78-4111-a9d3-5b92bf0a39eb</networkId><subnetMap><entry><key>95cd8437-25f1-4238-8720-cbfe7fa81476</key><value>d8d16606-5d01-4822-b160-9a0d257303e0</value></entry></subnetMap></updateNetworkResponse>";
        UpdateNetworkResponse response =
                (UpdateNetworkResponse) new NetworkAdapterRestV1().unmarshalXml(xml, UpdateNetworkResponse.class);
        assertThat(expectedResponse, sameBeanAs(response));
    }

    @Test
    public void processCallbackTest() {
        UpdateNetworkRequest updateNetworkRequest = new UpdateNetworkRequest();
        UpdateNetworkResponse updateNetworkResponse = new UpdateNetworkResponse();
        updateNetworkResponse.setMessageId("messageId");
        updateNetworkResponse.setNetworkId("networkId");
        delegateExecution.setVariable("networkAdapterRequest", updateNetworkRequest);
        delegateExecution.setVariable("NetworkAResponse_MESSAGE", updateNetworkResponse.toXmlString());

        networkAdapterRestV1Tasks.processCallback(delegateExecution);

        assertThat(updateNetworkResponse, sameBeanAs(delegateExecution.getVariable("updateNetworkResponse")));
    }

    @Test
    public void processCallbackErrorTest() {
        UpdateNetworkRequest updateNetworkRequest = new UpdateNetworkRequest();
        UpdateNetworkError updateNetworkResponse = new UpdateNetworkError();
        updateNetworkResponse.setMessageId("messageId");
        updateNetworkResponse.setMessage("test error message");
        delegateExecution.setVariable("networkAdapterRequest", updateNetworkRequest);
        delegateExecution.setVariable("NetworkAResponse_MESSAGE", updateNetworkResponse.toXmlString());

        doThrow(new BpmnError("MSOWorkflowException")).when(exceptionBuilder).buildAndThrowWorkflowException(
                any(DelegateExecution.class), anyInt(), any(String.class), any(ONAPComponents.class));

        try {
            networkAdapterRestV1Tasks.processCallback(delegateExecution);
        } catch (BpmnError be) {
            assertEquals("MSOWorkflowException", be.getErrorCode());
        }
        assertNull(delegateExecution.getVariable("updateNetworkResponse"));
        verify(exceptionBuilder, times(1)).buildAndThrowWorkflowException(any(DelegateExecution.class), eq(7000),
                eq("test error message"), eq(Components.OPENSTACK));
    }

    @Test
    public void processCallback_createNetworkResponse() {
        delegateExecution.setVariable("networkAdapterRequest", new CreateNetworkRequest());
        delegateExecution.setVariable("NetworkAResponse_MESSAGE",
                createNetworkResponse(CREATE_NETWORK_RESPONSE, NET_ID_FOR_CREATE_NETWORK_RESPONSE));
        networkAdapterRestV1Tasks.processCallback(delegateExecution);

        Object result = delegateExecution.getVariable("createNetworkResponse");
        assertTrue(result instanceof CreateNetworkResponse);
        CreateNetworkResponse createNetworkResponse = (CreateNetworkResponse) result;
        assertEquals(createNetworkResponse.getNetworkId(), NET_ID_FOR_CREATE_NETWORK_RESPONSE);
    }

    @Test
    public void processCallback_deleteNetworkResponse() {
        delegateExecution.setVariable("networkAdapterRequest", new DeleteNetworkRequest());
        delegateExecution.setVariable("NetworkAResponse_MESSAGE",
                createNetworkResponse(DELETE_NETWORK_RESPONSE, NET_ID_FOR_DELETE_NETWORK_RESPONSE));
        networkAdapterRestV1Tasks.processCallback(delegateExecution);

        Object result = delegateExecution.getVariable("deleteNetworkResponse");
        assertTrue(result instanceof DeleteNetworkResponse);
        DeleteNetworkResponse deleteNetworkResponse = (DeleteNetworkResponse) result;
        assertEquals(deleteNetworkResponse.getNetworkId(), NET_ID_FOR_DELETE_NETWORK_RESPONSE);
    }

    @Test
    public void processCallback_createNetworkError() {
        try {
            delegateExecution.setVariable("networkAdapterRequest", new CreateNetworkRequest());
            delegateExecution.setVariable("NetworkAResponse_MESSAGE",
                    createNetworkError(CREATE_NETWORK_ERROR, CREATE_NETWORK_ERROR_MESSAGE));
            networkAdapterRestV1Tasks.processCallback(delegateExecution);
        } catch (Exception e) {
            assertEquals(e.getMessage(), CREATE_NETWORK_ERROR_MESSAGE);
        }
    }

    @Test
    public void processCallback_deleteNetworkError() {
        try {
            delegateExecution.setVariable("networkAdapterRequest", new DeleteNetworkRequest());
            delegateExecution.setVariable("NetworkAResponse_MESSAGE",
                    createNetworkError(DELETE_NETWORK_ERROR, DELETE_NETWORK_ERROR_MESSAGE));
            networkAdapterRestV1Tasks.processCallback(delegateExecution);
        } catch (Exception e) {
            assertEquals(e.getMessage(), DELETE_NETWORK_ERROR_MESSAGE);
        }
    }

    @Test
    public void callNetworkAdapter_CreateNetworkRequestSuccess() throws Exception {
        // given
        String messageId = "createNetReqMessageId";
        CreateNetworkRequest createNetworkRequest = new CreateNetworkRequest();
        createNetworkRequest.setMessageId(messageId);
        delegateExecution.setVariable("networkAdapterRequest", createNetworkRequest);
        Status status = Status.OK;
        String responseEntity = "createNetworkResponse";
        Optional<Response> response = Optional.of(createResponse(status, responseEntity));
        when(networkAdapterResources.createNetworkAsync(createNetworkRequest)).thenReturn(response);
        // when
        networkAdapterRestV1Tasks.callNetworkAdapter(delegateExecution);
        // then
        verifyExecutionContent(status, responseEntity, messageId);
    }

    @Test
    public void callNetworkAdapter_DeleteNetworkRequestSuccess() throws Exception {
        // given
        String messageId = "DeleteNetReqMessageId";
        DeleteNetworkRequest deleteNetworkRequest = new DeleteNetworkRequest();
        deleteNetworkRequest.setMessageId(messageId);
        delegateExecution.setVariable("networkAdapterRequest", deleteNetworkRequest);
        Status status = Status.OK;
        String responseEntity = "deleteNetworkResponse";
        Optional<Response> response = Optional.of(createResponse(status, responseEntity));
        when(networkAdapterResources.deleteNetworkAsync(deleteNetworkRequest)).thenReturn(response);
        // when
        networkAdapterRestV1Tasks.callNetworkAdapter(delegateExecution);
        // then
        verifyExecutionContent(status, responseEntity, messageId);
    }

    @Test
    public void callNetworkAdapter_UpdateNetworkRequestSuccess() throws Exception {
        // given
        String messageId = "UpdateNetReqMessageId";
        UpdateNetworkRequest updateNetworkRequest = new UpdateNetworkRequest();
        updateNetworkRequest.setMessageId(messageId);
        delegateExecution.setVariable("networkAdapterRequest", updateNetworkRequest);
        Status status = Status.OK;
        String responseEntity = "updateNetworkResponse";
        Optional<Response> response = Optional.of(createResponse(status, responseEntity));
        when(networkAdapterResources.updateNetworkAsync(updateNetworkRequest)).thenReturn(response);
        // when
        networkAdapterRestV1Tasks.callNetworkAdapter(delegateExecution);
        // then
        verifyExecutionContent(status, responseEntity, messageId);
    }

    @Test
    public void callNetworkAdapterError_networkAdapterRequestIsNull() {
        // when
        networkAdapterRestV1Tasks.callNetworkAdapter(delegateExecution);
        // then
        verify(exceptionBuilder, times(1)).buildAndThrowWorkflowException(any(DelegateExecution.class), eq(7000),
                any(Exception.class), eq(ONAPComponents.SO));
    }

    @Test
    public void callNetworkAdapterError_noResponse() throws Exception {
        // given
        String messageId = "UpdateNetReqMessageId";
        UpdateNetworkRequest updateNetworkRequest = new UpdateNetworkRequest();
        updateNetworkRequest.setMessageId(messageId);
        delegateExecution.setVariable("networkAdapterRequest", updateNetworkRequest);
        when(networkAdapterResources.updateNetworkAsync(updateNetworkRequest)).thenReturn(Optional.empty());
        // when
        networkAdapterRestV1Tasks.callNetworkAdapter(delegateExecution);
        // then
        verify(exceptionBuilder, times(1)).buildAndThrowWorkflowException(any(DelegateExecution.class), eq(7000),
                any(Exception.class), eq(ONAPComponents.SO));
    }

    private void verifyExecutionContent(Status status, String responseEntity, String messageId) {
        assertEquals(delegateExecution.getVariable("NETWORKREST_networkAdapterStatusCode"),
                Integer.toString(status.getStatusCode()));
        assertEquals(delegateExecution.getVariable("NETWORKREST_networkAdapterResponse"), responseEntity);
        assertEquals(delegateExecution.getVariable("NetworkAResponse_CORRELATOR"), messageId);
    }

    private Response createResponse(Status status, String responseEntity) {
        OutboundMessageContext outboundMessageContext = new OutboundMessageContext();
        outboundMessageContext.setEntity(responseEntity);
        return new OutboundJaxrsResponse(status, outboundMessageContext);
    }

    private String createNetworkResponse(String networkResponseType, String networkId) {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><" + networkResponseType + "><networkId>"
                + networkId + "</networkId></" + networkResponseType + ">";
    }

    private String createNetworkError(String networkErrorType, String message) {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><" + networkErrorType + "><message>"
                + message + "</message></" + networkErrorType + ">";
    }

}
