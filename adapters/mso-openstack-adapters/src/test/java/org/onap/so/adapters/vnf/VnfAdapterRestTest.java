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

package org.onap.so.adapters.vnf;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVfModuleResponse;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleResponse;
import org.onap.so.adapters.vnfrest.QueryVfModuleResponse;
import org.onap.so.adapters.vnfrest.RollbackVfModuleRequest;
import org.onap.so.adapters.vnfrest.RollbackVfModuleResponse;
import org.onap.so.adapters.vnfrest.UpdateVfModuleRequest;
import org.onap.so.adapters.vnfrest.UpdateVfModuleResponse;
import org.onap.so.adapters.vnfrest.VfModuleExceptionResponse;
import org.onap.so.adapters.vnfrest.VfModuleRollback;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeletePublicUrlStackByNameAndID_204;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteStacks;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetPublicUrlStackByNameAndID_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksBaseStack_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksStackId_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksStackId_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksVUSP_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksVfModuleWithLocationHeader_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksVfModule_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksWithBody_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksWithBody_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacks_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostStacks_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutStacks_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;

public class VnfAdapterRestTest extends BaseRestTestUtils {


    @Autowired
    private JettisonStyleMapperProvider jettisonTypeObjectMapper;
    private static final String MESSAGE_ID = "62265093-277d-4388-9ba6-449838ade586-1517252396874";
    private static final String AAI_VNF_ID = "c93e0d34-5b63-45de-bbae-b0fe49dd3bd9";
    private static final String MSO_REQUEST_ID = "62265093-277d-4388-9ba6-449838ade586";
    private static final String MSO_SERVICE_INSTANCE_ID = "4147e06f-1b89-49c5-b21f-4faf8dc9805a";
    private static final String CLOUDSITE_ID = "mtn13";
    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String TENANT_ID = "0422ffb57ba042c0800a29dc85ca70f8";
    private static final String VNF_TYPE = "MSOTADevInfra_vSAMP10a_Service/vSAMP10a 1";
    private static final String VNF_NAME = "MSO-DEV-VNF-1802-it3-pwt3-vSAMP10a-1XXX-Replace";
    private static final String VNF_VERSION = "1.0";
    private static final String VF_MODULE_ID = "1d48aaec-b7f3-4c24-ba4a-4e798ed3223c";
    private static final String VF_MODULE_NAME = "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001";
    private static final String VF_MODULE_TYPE = "vSAMP10aDEV::PCM::module-2";
    private static final String MODEL_CUSTOMIZATION_UUID = "cb82ffd8-252a-11e7-93ae-92361f002671";
    private static final String BASE_VF_MODULE_ID = "3d7ff7b4-720b-4604-be0a-1974fc58ed96";
    // vfModuleParams specific variables
    private static final String NETWORK_NAME = "Dev-vSAMP10a-ntwk-1802-pwt3-v6-Replace-1001";
    private static final String SERVER_NAME = "Dev-vSAMP10a-addon2-1802-pwt3-v6-Replace-1001";
    private static final String IMAGE = "ubuntu_14.04_IPv6";
    private static final String EXN_DIRECT_NET_FQDN = "direct";
    private static final String EXN_HSL_NET_FQDN = "hsl";
    private static final String AVAILABILITY_ZONE_0 = "nova";
    private static final String VF_MODULE_INDEX = "0";

    @Test
    public void testCreateVfModule() throws JSONException, JsonParseException, JsonMappingException, IOException {

        CreateVfModuleRequest request = populateCreateVfModuleRequest();

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackVfModule_404(wireMockServer);

        mockOpenStackPostStacks_200(wireMockServer);

        mockOpenStackGetStackVfModule_200(wireMockServer);

        mockUpdateRequestDb(wireMockServer, "62265093-277d-4388-9ba6-449838ade586");

        headers.add("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<CreateVfModuleRequest> entity = new HttpEntity<CreateVfModuleRequest>(request, headers);

        ResponseEntity<CreateVfModuleResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                        HttpMethod.POST, entity, CreateVfModuleResponse.class);

        ResponseEntity<CreateVfModuleResponse> responseV2 =
                restTemplate.exchange(createURLWithPort("/services/rest/v2/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                        HttpMethod.POST, entity, CreateVfModuleResponse.class);

        CreateVfModuleResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
                new File("src/test/resources/__files/CreateVfModuleResponse.json"), CreateVfModuleResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(expectedResponse));

        assertEquals(Response.Status.OK.getStatusCode(), responseV2.getStatusCode().value());
        assertThat(responseV2.getBody(), sameBeanAs(expectedResponse));
    }

    @Test
    public void testCreateVfModuleAsyncCall() throws Exception {
        CreateVfModuleRequest request = populateCreateVfModuleRequest();
        request.setNotificationUrl(createURLWithPort("/mso/WorkflowMesssage"));

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_404(wireMockServer);
        mockOpenStackPostStacks_200(wireMockServer);
        mockOpenStackGetStackVfModule_200(wireMockServer);

        headers.add("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<CreateVfModuleRequest> entity = new HttpEntity<CreateVfModuleRequest>(request, headers);

        ResponseEntity<CreateVfModuleResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                        HttpMethod.POST, entity, CreateVfModuleResponse.class);

        CreateVfModuleResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
                new File("src/test/resources/__files/CreateVfModuleResponse.json"), CreateVfModuleResponse.class);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testCreateVfModuleWithEnableBridgeNull()
            throws JSONException, JsonParseException, JsonMappingException, IOException {

        CreateVfModuleRequest request = new CreateVfModuleRequest();
        request.setBackout(true);
        request.setSkipAAI(true);
        request.setFailIfExists(false);
        MsoRequest msoReq = new MsoRequest();
        boolean failIfExists = true;
        Boolean enableBridge = null;
        Map<String, Object> vfModuleParams = new HashMap<>();


        vfModuleParams.put("vf_module_id", VF_MODULE_ID);
        vfModuleParams.put("vnf_id", AAI_VNF_ID);
        vfModuleParams.put("network_name", NETWORK_NAME);
        vfModuleParams.put("vnf_name", VNF_NAME);
        vfModuleParams.put("environment_context", "");
        vfModuleParams.put("server_name", SERVER_NAME);
        vfModuleParams.put("image", IMAGE);
        vfModuleParams.put("workload_context", "");
        vfModuleParams.put("vf_module_index", VF_MODULE_INDEX);
        vfModuleParams.put("vf_module_name", VF_MODULE_NAME);
        vfModuleParams.put("availability_zone_0", AVAILABILITY_ZONE_0);
        vfModuleParams.put("exn_direct_net_fqdn", EXN_DIRECT_NET_FQDN);
        vfModuleParams.put("exn_hsl_net_fqdn", EXN_HSL_NET_FQDN);

        msoReq.setRequestId(MSO_REQUEST_ID);
        msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
        request.setMsoRequest(msoReq);
        request.setCloudSiteId(CLOUDSITE_ID);
        request.setTenantId(TENANT_ID);
        request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
        request.setVnfId(AAI_VNF_ID);
        request.setVnfVersion(VNF_VERSION);
        request.setVfModuleId(VF_MODULE_ID);
        request.setVfModuleName(VF_MODULE_NAME);
        request.setBaseVfModuleId(BASE_VF_MODULE_ID);
        request.setFailIfExists(failIfExists);
        request.setEnableBridge(enableBridge);
        request.setVfModuleParams(vfModuleParams);
        request.setMessageId(MESSAGE_ID);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackVfModule_404(wireMockServer);

        mockOpenStackPostStacks_200(wireMockServer);

        mockOpenStackGetStackVfModule_200(wireMockServer);

        mockUpdateRequestDb(wireMockServer, "62265093-277d-4388-9ba6-449838ade586");


        headers.add("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<CreateVfModuleRequest> entity = new HttpEntity<CreateVfModuleRequest>(request, headers);

        ResponseEntity<CreateVfModuleResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                        HttpMethod.POST, entity, CreateVfModuleResponse.class);

        CreateVfModuleResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
                new File("src/test/resources/__files/CreateVfModuleResponse.json"), CreateVfModuleResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(expectedResponse));
    }

    @Test
    public void testCreateVfModuleFail() throws IOException {

        CreateVfModuleRequest request = new CreateVfModuleRequest();
        request.setBackout(true);
        request.setSkipAAI(true);
        request.setFailIfExists(false);
        MsoRequest msoReq = new MsoRequest();
        boolean failIfExists = true;
        boolean enableBridge = false;
        Map<String, Object> vfModuleParams = new HashMap<>();

        vfModuleParams.put("vf_module_id", VF_MODULE_ID);
        vfModuleParams.put("vnf_id", AAI_VNF_ID);
        vfModuleParams.put("network_name", NETWORK_NAME);
        vfModuleParams.put("vnf_name", VNF_NAME);
        vfModuleParams.put("environment_context", "");
        vfModuleParams.put("server_name", SERVER_NAME);
        vfModuleParams.put("image", IMAGE);
        vfModuleParams.put("workload_context", "");
        vfModuleParams.put("vf_module_index", VF_MODULE_INDEX);
        vfModuleParams.put("vf_module_name", VF_MODULE_NAME);
        vfModuleParams.put("availability_zone_0", AVAILABILITY_ZONE_0);
        vfModuleParams.put("exn_direct_net_fqdn", EXN_DIRECT_NET_FQDN);
        vfModuleParams.put("exn_hsl_net_fqdn", EXN_HSL_NET_FQDN);

        msoReq.setRequestId(MSO_REQUEST_ID);
        msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
        request.setMsoRequest(msoReq);
        request.setCloudSiteId(CLOUDSITE_ID);
        request.setTenantId(TENANT_ID);
        request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
        request.setVnfId(AAI_VNF_ID);
        request.setVnfType(VNF_TYPE);
        request.setVnfVersion(VNF_VERSION);
        request.setVfModuleId(VF_MODULE_ID);
        request.setVfModuleName(VF_MODULE_NAME);
        request.setVfModuleType(VF_MODULE_TYPE);
        request.setBaseVfModuleStackId(BASE_VF_MODULE_ID);
        request.setFailIfExists(failIfExists);
        request.setEnableBridge(enableBridge);
        request.setVfModuleParams(vfModuleParams);
        request.setMessageId(MESSAGE_ID);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackVfModule_404(wireMockServer);

        mockOpenStackGetStacks_404(wireMockServer);

        mockOpenStackPostStacks_200(wireMockServer);

        mockOpenStackGetStackVfModule_200(wireMockServer);

        headers.add("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<CreateVfModuleRequest> entity = new HttpEntity<CreateVfModuleRequest>(request, headers);

        ResponseEntity<VfModuleExceptionResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                        HttpMethod.POST, entity, VfModuleExceptionResponse.class);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());

        mockOpenStackGetStacksWithBody_200(wireMockServer, "DELETE_IN_PROGRESS");

        response = restTemplate.exchange(createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                HttpMethod.POST, entity, VfModuleExceptionResponse.class);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());

        mockOpenStackGetStacksWithBody_200(wireMockServer, "DELETE_FAILED");

        response = restTemplate.exchange(createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                HttpMethod.POST, entity, VfModuleExceptionResponse.class);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());

        mockOpenStackGetStacksWithBody_200(wireMockServer, "UPDATE_COMPLETE");

        response = restTemplate.exchange(createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                HttpMethod.POST, entity, VfModuleExceptionResponse.class);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());

        mockOpenStackGetStacksWithBody_404(wireMockServer);

        response = restTemplate.exchange(createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"),
                HttpMethod.POST, entity, VfModuleExceptionResponse.class);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());

    }

    @Test
    public void testDeleteVfModule() throws IOException {

        DeleteVfModuleRequest request = new DeleteVfModuleRequest();
        MsoRequest msoRequest = new MsoRequest();
        String vfModuleStackId = "stackId";

        msoRequest.setRequestId(MSO_REQUEST_ID);
        msoRequest.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
        request.setCloudSiteId(CLOUDSITE_ID);
        request.setTenantId(TENANT_ID);
        request.setVfModuleId(VF_MODULE_ID);
        request.setVfModuleStackId(vfModuleStackId);
        request.setVnfId(AAI_VNF_ID);
        request.setMsoRequest(msoRequest);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackPostStacks_200(wireMockServer);

        mockOpenStackGetStacksStackId_404(wireMockServer);

        mockOpenStackGetPublicUrlStackByNameAndID_200(wireMockServer, wireMockPort);

        mockOpenStackDeletePublicUrlStackByNameAndID_204(wireMockServer);

        mockUpdateRequestDb(wireMockServer, "62265093-277d-4388-9ba6-449838ade586");

        headers.add("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<DeleteVfModuleRequest> entity = new HttpEntity<DeleteVfModuleRequest>(request, headers);

        ResponseEntity<DeleteVfModuleResponse> response = restTemplate.exchange(
                createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID),
                HttpMethod.DELETE, entity, DeleteVfModuleResponse.class);

        ResponseEntity<DeleteVfModuleResponse> responseV2 = restTemplate.exchange(
                createURLWithPort("/services/rest/v2/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID),
                HttpMethod.DELETE, entity, DeleteVfModuleResponse.class);


        DeleteVfModuleResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
                new File("src/test/resources/__files/DeleteVfModuleResponse.json"), DeleteVfModuleResponse.class);


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(expectedResponse));

        assertEquals(Response.Status.OK.getStatusCode(), responseV2.getStatusCode().value());
        assertThat(responseV2.getBody(), sameBeanAs(expectedResponse));
    }

    @Test
    public void testUpdateVfModule() throws IOException {

        UpdateVfModuleRequest request = new UpdateVfModuleRequest();
        MsoRequest msoRequest = new MsoRequest();
        String vfModuleStackId = "vfModuleStackId";
        Boolean failIfExists = false;
        Boolean backout = false;
        msoRequest.setRequestId(MSO_REQUEST_ID);
        msoRequest.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);

        Map<String, Object> vfModuleParams = new HashMap<>();

        vfModuleParams.put("exn_direct_net_fqdn", EXN_DIRECT_NET_FQDN);
        vfModuleParams.put("exn_hsl_net_fqdn", EXN_HSL_NET_FQDN);

        Map<String, String> vfModuleOutputs = new HashMap<String, String>();

        vfModuleOutputs.put("output name", "output value");

        request.setBackout(backout);
        request.setCloudSiteId(CLOUDSITE_ID);
        request.setFailIfExists(failIfExists);
        request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
        request.setMsoRequest(msoRequest);
        request.setTenantId(TENANT_ID);
        request.setVfModuleId(VF_MODULE_ID);
        request.setVfModuleName(VF_MODULE_NAME);
        request.setVfModuleStackId(vfModuleStackId);
        request.setBackout(backout);
        request.setVfModuleParams(vfModuleParams);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStacksVfModuleWithLocationHeader_200(wireMockServer, wireMockPort);

        mockOpenStackGetStacksVfModule_200(wireMockServer, wireMockPort);

        mockOpenStackGetStacksBaseStack_200(wireMockServer, wireMockPort);

        mockOpenStackPutStacks_200(wireMockServer);

        UpdateVfModuleResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
                new File("src/test/resources/__files/UpdateVfModuleResponse.json"), UpdateVfModuleResponse.class);
        expectedResponse.setVfModuleOutputs(vfModuleOutputs);

        headers.add("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<UpdateVfModuleRequest> entity = new HttpEntity<UpdateVfModuleRequest>(request, headers);

        ResponseEntity<UpdateVfModuleResponse> response = restTemplate.exchange(
                createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_TYPE),
                HttpMethod.PUT, entity, UpdateVfModuleResponse.class);

        ResponseEntity<UpdateVfModuleResponse> responseV2 = restTemplate.exchange(
                createURLWithPort("/services/rest/v2/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_TYPE),
                HttpMethod.PUT, entity, UpdateVfModuleResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(expectedResponse));

        assertEquals(Response.Status.OK.getStatusCode(), responseV2.getStatusCode().value());
        assertThat(responseV2.getBody(), sameBeanAs(expectedResponse));

    }

    @Test
    public void testRollbackVfModule() throws IOException {


        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId(MSO_REQUEST_ID);
        msoRequest.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);

        VfModuleRollback vfModuleRollback = new VfModuleRollback(AAI_VNF_ID, VF_MODULE_ID, "StackId", false, TENANT_ID,
                CLOUD_OWNER, CLOUDSITE_ID, msoRequest, "messageId");

        RollbackVfModuleRequest request = new RollbackVfModuleRequest();
        request.setVfModuleRollback(vfModuleRollback);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStacksStackId_200(wireMockServer, wireMockPort);

        mockOpenStackDeleteStacks(wireMockServer);

        mockOpenStackGetStacksVUSP_404(wireMockServer);

        headers.add("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<RollbackVfModuleRequest> entity = new HttpEntity<RollbackVfModuleRequest>(request, headers);

        ResponseEntity<RollbackVfModuleResponse> response = restTemplate.exchange(
                createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID + "/rollback"),
                HttpMethod.DELETE, entity, RollbackVfModuleResponse.class);

        RollbackVfModuleResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
                new File("src/test/resources/__files/RollbackVfModuleResponse.json"), RollbackVfModuleResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(expectedResponse));

    }

    @Ignore
    @Test
    public void testQueryVfModule() throws IOException {

        String testUrl = createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID);
        String testUri = UriBuilder.fromPath("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID)
                .host("localhost").port(wireMockPort).scheme("http")
                // .queryParam("cloudSiteId", CLOUDSITE_ID).queryParam("tenantId", TENANT_ID)
                .build().toString();
        System.out.println(testUri);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);


        headers.add("Accept", MediaType.APPLICATION_JSON);
        // HttpEntity entity = new HttpEntity(null, headers);
        ResponseEntity<QueryVfModuleResponse> response =
                restTemplate.getForEntity(testUri, QueryVfModuleResponse.class);
        // System.out.println(response);

        QueryVfModuleResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
                new File("src/test/resources/__files/QueryVfModuleResponse.json"), QueryVfModuleResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(expectedResponse));
    }

    private CreateVfModuleRequest populateCreateVfModuleRequest() {
        CreateVfModuleRequest request = new CreateVfModuleRequest();
        request.setBackout(true);
        request.setSkipAAI(true);
        request.setFailIfExists(false);
        MsoRequest msoReq = new MsoRequest();
        boolean failIfExists = true;
        boolean enableBridge = false;
        Map<String, Object> vfModuleParams = new HashMap<>();

        vfModuleParams.put("vf_module_id", VF_MODULE_ID);
        vfModuleParams.put("vnf_id", AAI_VNF_ID);
        vfModuleParams.put("network_name", NETWORK_NAME);
        vfModuleParams.put("vnf_name", VNF_NAME);
        vfModuleParams.put("environment_context", "");
        vfModuleParams.put("server_name", SERVER_NAME);
        vfModuleParams.put("image", IMAGE);
        vfModuleParams.put("workload_context", "");
        vfModuleParams.put("vf_module_index", VF_MODULE_INDEX);
        vfModuleParams.put("vf_module_name", VF_MODULE_NAME);
        vfModuleParams.put("availability_zone_0", AVAILABILITY_ZONE_0);
        vfModuleParams.put("exn_direct_net_fqdn", EXN_DIRECT_NET_FQDN);
        vfModuleParams.put("exn_hsl_net_fqdn", EXN_HSL_NET_FQDN);

        msoReq.setRequestId(MSO_REQUEST_ID);
        msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
        request.setMsoRequest(msoReq);
        request.setCloudSiteId(CLOUDSITE_ID);
        request.setTenantId(TENANT_ID);
        request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
        request.setVnfId(AAI_VNF_ID);
        request.setVnfType(VNF_TYPE);
        request.setVnfVersion(VNF_VERSION);
        request.setVfModuleId(VF_MODULE_ID);
        request.setVfModuleName(VF_MODULE_NAME);
        request.setVfModuleType(VF_MODULE_TYPE);
        request.setBaseVfModuleId(BASE_VF_MODULE_ID);
        request.setFailIfExists(failIfExists);
        request.setEnableBridge(enableBridge);
        request.setVfModuleParams(vfModuleParams);
        request.setMessageId(MESSAGE_ID);

        return request;
    }

    public static void mockUpdateRequestDb(WireMockServer wireMockServer, String requestId) throws IOException {
        wireMockServer.stubFor(patch(urlPathEqualTo("/infraActiveRequests/" + requestId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
    }
}
