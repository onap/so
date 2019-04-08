/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.apihandler.common;


import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;

/**
 * This class implements test methods of CamundaResoponseHandler.
 * 
 *
 */
public class ResponseHandlerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void tesParseCamundaResponse() throws ApiException {
        // String body
        // ="{\"links\":[{\"method\":\"GET\",\"href\":\"http://localhost:9080/engine-rest/process-instance/2047c658-37ae-11e5-9505-7a1020524153\",\"rel\":\"self\"}],\"id\":\"2047c658-37ae-11e5-9505-7a1020524153\",\"definitionId\":\"dummy:10:73298961-37ad-11e5-9505-7a1020524153\",\"businessKey\":null,\"caseInstanceId\":null,\"ended\":true,\"suspended\":false}";

        String body = "{ \"response\": \"<xml>xml</xml>\"," + "\"messageCode\": 200,"
                + "\"message\": \"Successfully started the process\"}";

        HttpResponse response = createResponse(200, body, "application/json");

        ResponseHandler respHandler = new ResponseHandler(response, 1);

        int status = respHandler.getStatus();
        assertEquals(status, HttpStatus.SC_ACCEPTED);
        assertEquals(respHandler.getResponse().getMessage(), "Successfully started the process");

    }

    @Test
    public void tesParseCamundaResponseForCamundaTaskType() throws ApiException {
        String body = "{ \"response\": \"<xml>xml</xml>\"," + "\"messageCode\": 200,"
                + "\"message\": \"Successfully started the process\"}";

        HttpResponse response = createResponse(200, body, "application/json");

        ResponseHandler respHandler = new ResponseHandler(response, 2);

        int status = respHandler.getStatus();
        assertEquals(status, HttpStatus.SC_ACCEPTED);
        assertEquals(respHandler.getResponseBody(), body);

    }

    @Test
    public void tesParseBpelResponse() throws ApiException {
        String body = "<test:service-response xmlns:test=\"http://org.onap/so/test\">"
                + "<test:request-id>req5</test:request-id>" + "<test:request-action>test</test:request-action>"
                + "<test:source>test</test:source>" + "<test:ack-final-indicator>n</test:ack-final-indicator>"
                + "</test:service-response>";

        HttpResponse response = createResponse(200, body, "text/xml");

        ResponseHandler respHandler = new ResponseHandler(response, 0);

        int status = respHandler.getStatus();
        assertEquals(status, HttpStatus.SC_ACCEPTED);
        assertTrue(respHandler.getResponseBody() != null);
    }

    @Test
    public void tesMappingErrorResponse() throws ApiException {
        thrown.expect(ValidateException.class);
        thrown.expectMessage(startsWith("Cannot parse Camunda Response"));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_BAD_REQUEST)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));

        HttpResponse response = createResponse(HttpStatus.SC_NOT_FOUND, "<html>error</html>", "text/html");
        ResponseHandler respHandler = new ResponseHandler(response, 1);

        int status = respHandler.getStatus();

        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, status);

    }

    @Test
    public void tesGenricErrorResponse() throws ApiException {

        String body = "{ \"response\": \"<xml>xml</xml>\"," + "\"messageCode\": 500,"
                + "\"message\": \"Something went wrong\"}";

        HttpResponse response = createResponse(500, body, "application/json");
        ResponseHandler respHandler = new ResponseHandler(response, 1);
        int status = respHandler.getStatus();
        assertEquals(status, HttpStatus.SC_BAD_GATEWAY);
        assertEquals(respHandler.getResponse().getMessage(), "Something went wrong");
    }

    private HttpResponse createResponse(int respStatus, String respBody, String contentType) {
        HttpResponse response =
                new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), respStatus, ""));
        response.setStatusCode(respStatus);
        try {
            response.setEntity(new StringEntity(respBody));
            response.setHeader("Content-Type", contentType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

}
