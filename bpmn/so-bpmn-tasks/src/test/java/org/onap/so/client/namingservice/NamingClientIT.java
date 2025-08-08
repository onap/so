/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.namingservice;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.namingservice.model.Deleteelement;
import org.onap.namingservice.model.Element;
import org.onap.namingservice.model.NameGenDeleteRequest;
import org.onap.namingservice.model.NameGenRequest;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.beans.factory.annotation.Autowired;

public class NamingClientIT extends BaseIntegrationTest {
    @Autowired
    NamingClient client;
    @Autowired
    NamingRequestObjectBuilder requestBuilder;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void assignNameGenRequest() throws BadResponseException, IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/web/service/v1/genNetworkElementName"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("NamingClient/AssignResponse.json").withStatus(HttpStatus.SC_ACCEPTED)));

        NameGenRequest request = assignSetup();
        String response = client.postNameGenRequest(request);
        assertTrue(response.equals("$vnf-name"));
    }

    @Test
    public void assignNameGenRequestError() throws BadResponseException, IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/web/service/v1/genNetworkElementName")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBodyFile("NamingClient/ErrorResponse.json")
                .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        thrown.expect(BadResponseException.class);
        thrown.expectMessage("Error from Naming Service: External Key is required and must be unique");
        NameGenRequest request = assignSetup();
        client.postNameGenRequest(request);
    }

    @Test
    public void unassignNameGenRequest() throws BadResponseException, IOException {
        wireMockServer.stubFor(delete(urlPathEqualTo("/web/service/v1/genNetworkElementName"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("NamingClient/UnassignResponse.json").withStatus(HttpStatus.SC_ACCEPTED)));

        String response = client.deleteNameGenRequest(unassignSetup());
        assertTrue(response.equals(""));
    }

    @Test
    public void unassignNameGenRequestError() throws BadResponseException, IOException {
        wireMockServer.stubFor(delete(urlPathEqualTo("/web/service/v1/genNetworkElementName")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBodyFile("NamingClient/ErrorResponse.json")
                .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        thrown.expect(BadResponseException.class);
        thrown.expectMessage("Error from Naming Service: External Key is required and must be unique");
        client.deleteNameGenRequest(unassignSetup());
    }

    public NameGenRequest assignSetup() {
        NameGenRequest request = new NameGenRequest();
        List<Element> elements = new ArrayList<>();
        Element testElement = new Element();
        testElement = requestBuilder.elementMapper("SomeUniqueValue", "SDNC_Policy.Config_MS_1806SRIOV_VNATJson.4.xml",
                "VNF", "nfNamingCode", "vnf_name");
        elements.add(testElement);
        request = requestBuilder.nameGenRequestMapper(elements);
        return request;
    }

    public NameGenDeleteRequest unassignSetup() {
        NameGenDeleteRequest request = new NameGenDeleteRequest();
        List<Deleteelement> deleteElements = new ArrayList<>();
        Deleteelement testElement = new Deleteelement();
        testElement = requestBuilder.deleteElementMapper("instanceGroupId");
        deleteElements.add(testElement);
        request = requestBuilder.nameGenDeleteRequestMapper(deleteElements);
        return request;
    }
}
