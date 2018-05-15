/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.camunda.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.openecomp.mso.apihandler.common.ResponseHandler;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * This class implements test methods of CamundaResoponseHandler.
 * 
 *
 */
public class ResponseHandlerTest {

    @Test
    public void tesParseCamundaResponse () throws JsonGenerationException, JsonMappingException, IOException {

		String content = "{\"WorkflowResponse\":{"
				+ "\"messageCode\":202"
				+ ",\"message\":\"Successfully started the process\""
				+ ",\"content\":\"<xml>xml</xml>\""
				+ ",\"processInstanceId\":\"4d3b3201a7ce\""
				+ "}}";

        HttpResponse response = createResponse (200, content, "application/json");

        ResponseHandler respHandler = new ResponseHandler (response, 1);

        int status = respHandler.getStatus ();
        assertEquals (status, HttpStatus.SC_ACCEPTED);
        assertEquals (respHandler.getResponse ().getMessage (), "Successfully started the process");

    }

    @Test
    public void tesParseBpelResponse () throws JsonGenerationException, JsonMappingException, IOException {
        String body = "<layer3activate:service-response xmlns:layer3activate=\"http://org.openecomp/mso/request/layer3serviceactivate/schema/v1\""
                      + "xmlns:reqtype=\"http://org.openecomp/mso/request/types/v1\""
                      + "xmlns:aetgt=\"http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd\""
                      + "xmlns:types=\"http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd\">"
                      + "<reqtype:request-id>req5</reqtype:request-id>"
                      + "<reqtype:request-action>Layer3ServiceActivateRequest</reqtype:request-action>"
                      + "<reqtype:source>OMX</reqtype:source>"
                      + "<reqtype:ack-final-indicator>n</reqtype:ack-final-indicator>"
                      + "</layer3activate:service-response>";

        HttpResponse response = createResponse (200, body, "text/xml");

        ResponseHandler respHandler = new ResponseHandler (response, 0);

        int status = respHandler.getStatus ();
        assertEquals (status, HttpStatus.SC_ACCEPTED);
        assertTrue (respHandler.getContent() != null);
    }

    @Test
    public void tes404ErrorResponse () throws JsonGenerationException, JsonMappingException, IOException {

    	
        HttpResponse response = createResponse (HttpStatus.SC_NOT_FOUND, "<html>error</html>", "text/html");
        ResponseHandler respHandler = new ResponseHandler (response, 1);

        int status = respHandler.getStatus ();

        assertEquals (HttpStatus.SC_NOT_IMPLEMENTED, status);

    }

    @Test
    public void tesGenricErrorResponse () throws JsonGenerationException, JsonMappingException, IOException {

		String content = "{\"WorkflowResponse\":{"
				+ "\"messageCode\":500"
				+ ",\"message\":\"Something went wrong\""
				+ ",\"content\":\"<xml>xml</xml>\""
				+ ",\"processInstanceId\":\"4d3b3201a7ce\""
				+ "}}";

        HttpResponse response = createResponse (500, content, "application/json");

        ResponseHandler respHandler = new ResponseHandler (response, 1);

        int status = respHandler.getStatus ();
        assertEquals (HttpStatus.SC_BAD_GATEWAY, status);
        assertEquals (respHandler.getResponse ().getMessage (), "Something went wrong");
        System.out.println (respHandler.getContent());

    }

    private HttpResponse createResponse (int respStatus, String respBody, String contentType) {
        HttpResponse response = new BasicHttpResponse (new BasicStatusLine (new ProtocolVersion ("HTTP", 1, 1),
                                                                            respStatus,
                                                                            ""));
        response.setStatusCode (respStatus);
        try {
            response.setEntity (new StringEntity (respBody));
            response.setHeader ("Content-Type", contentType);
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return response;
    }

}
