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

package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCRequest;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class SDNCRequestTasksTest extends SDNCRequestTasks {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    SDNCRequestTasks sndcRequestTasks = new SDNCRequestTasks();

    @Mock
    SDNCClient sdncClient;

    @Spy
    private ExceptionBuilder exceptionBuilder;

    protected DelegateExecution delegateExecution;


    @Before
    public void setup() {
        delegateExecution = new DelegateExecutionFake();
        delegateExecution.setVariable("SDNCRequest", createSDNCRequest());
    }

    @Test
    public void createCorrelationVariables_Test() {
        sndcRequestTasks.createCorrelationVariables(delegateExecution);
        assertEquals("correlationValue", delegateExecution.getVariable("correlationName_CORRELATOR"));
    }

    @Test
    public void callSDNC_Final_Test() throws MapperException, BadResponseException, IOException {
        final String sdncResponse =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/SDNCClientPut200Response.json")));
        doReturn(sdncResponse).when(sdncClient).post(createSDNCRequest().getSDNCPayload(), SDNCTopology.CONFIGURATION);
        sndcRequestTasks.callSDNC(delegateExecution);
        assertEquals(true, delegateExecution.getVariable("isSDNCCompleted"));
    }

    @Test
    public void callSDNC_Not_Final_Test() throws MapperException, BadResponseException, IOException {
        final String sdncResponse = new String(
                Files.readAllBytes(Paths.get("src/test/resources/__files/SDNCClientPut200ResponseNotFinal.json")));
        doReturn(sdncResponse).when(sdncClient).post(createSDNCRequest().getSDNCPayload(), SDNCTopology.CONFIGURATION);
        sndcRequestTasks.callSDNC(delegateExecution);
        assertEquals(false, delegateExecution.getVariable("isSDNCCompleted"));
    }

    @Test
    public void callSDNC_Error_Test() throws MapperException, BadResponseException {
        doThrow(MapperException.class).when(sdncClient).post(createSDNCRequest().getSDNCPayload(),
                SDNCTopology.CONFIGURATION);
        doReturn("processKey").when(exceptionBuilder).getProcessKey(delegateExecution);
        expectedException.expect(BpmnError.class);
        sndcRequestTasks.callSDNC(delegateExecution);
    }

    @Test
    public void convertIndicatorToBoolean_True_Test() throws MapperException, BadResponseException {
        boolean testValue = sndcRequestTasks.convertIndicatorToBoolean("Y");
        assertEquals(true, testValue);
    }

    @Test
    public void convertIndicatorToBoolean_False_Test() throws MapperException, BadResponseException {
        boolean testValue = sndcRequestTasks.convertIndicatorToBoolean("N");
        assertEquals(false, testValue);
    }

    @Test
    public void HandleTimeout_Test() throws MapperException, BadResponseException {
        doReturn("processKey").when(exceptionBuilder).getProcessKey(delegateExecution);
        expectedException.expect(BpmnError.class);
        sndcRequestTasks.handleTimeOutException(delegateExecution);
    }

    @Test
    public void processCallBack_Final_Test() throws MapperException, BadResponseException, IOException {
        final String sdncResponse =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/SDNC_Async_Request2.xml")));
        delegateExecution.setVariable("correlationName_MESSAGE", sdncResponse);
        sndcRequestTasks.processCallback(delegateExecution);
        assertEquals(true, delegateExecution.getVariable(IS_CALLBACK_COMPLETED));
    }

    @Test
    public void getXmlElementTest() throws Exception {
        final String sdncResponse =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/SDNC_Async_Request2.xml")));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(sdncResponse)));

        String finalMessageIndicator = getXmlElement(doc, "//*:ack-final-indicator");
        String responseCode = getXmlElement(doc, "/input/response-code");
        String responseMessage = getXmlElement(doc, "/input/response-message");

        assertEquals("Y", finalMessageIndicator);
        assertEquals("200", responseCode);
        assertEquals("Success", responseMessage);
    }

    public SDNCRequest createSDNCRequest() {
        SDNCRequest request = new SDNCRequest();
        request.setCorrelationName("correlationName");
        request.setCorrelationValue("correlationValue");
        request.setTopology(SDNCTopology.CONFIGURATION);
        ObjectMapper mapper = new ObjectMapper();
        try {
            GenericResourceApiServiceOperationInformation sdncReq = mapper.readValue(
                    Files.readAllBytes(Paths.get("src/test/resources/__files/SDNC_Client_Request.json")),
                    GenericResourceApiServiceOperationInformation.class);
            request.setSDNCPayload(sdncReq);
        } catch (JsonParseException e) {

        } catch (JsonMappingException e) {

        } catch (IOException e) {

        }

        return request;
    }

}
