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

package org.onap.so.apihandlerinfra;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import jakarta.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.Tenant;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIDeserializeTest extends BaseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MsoRequest msoReq;

    @Value("${wiremock.server.port}")
    private String wiremockPort;

    @Before
    public void beforeClass() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/MsoRequestTest" + JsonInput;
        return new String(Files.readAllBytes(Paths.get(JsonInput)));
    }

    @Test
    public void doNotFailOnUnknownPropertiesTest() throws JsonParseException, JsonMappingException, IOException {
        wireMockServer.stubFor(get(("/aai/" + AAIVersion.LATEST
                + "/cloud-infrastructure/cloud-regions/cloud-region/cloudOwner/mdt1/tenants/tenant/88a6ca3ee0394ade9403f075db23167e"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile("aai/UnknownProperty.json")
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        ServiceInstancesRequest sir = mapper.readValue(inputStream("/AAI.json"), ServiceInstancesRequest.class);
        String tenantId = "88a6ca3ee0394ade9403f075db23167e";
        String tenantNameFromAAI = "testTenantName";
        String cloudOwner = "cloudOwner";
        sir.getRequestDetails().getCloudConfiguration().setCloudOwner(cloudOwner);
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setTenantName(tenantNameFromAAI);
        String tenantName = msoReq.getTenantNameFromAAI(sir);
        assertEquals(tenantNameFromAAI, tenantName);
    }

}

