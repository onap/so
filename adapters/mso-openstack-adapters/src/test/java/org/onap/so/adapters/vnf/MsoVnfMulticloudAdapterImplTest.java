/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.adapters.openstack.MsoOpenstackAdaptersApplication;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class MsoVnfMulticloudAdapterImplTest extends BaseRestTestUtils{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private MsoVnfPluginAdapterImpl instance;

    @Autowired
    private CloudConfig cloudConfig;

    @Before
    public void before() throws Exception {
        super.orchestrator = "multicloud";
        super.cloudEndpoint = "/api/multicloud/v1/cloud_owner/cloud_region_id/infra_workload";
        super.setUp();
    }

    @Test
    public void createVfModule() throws Exception {
        
        Map<String, Object> stackInputs = new HashMap<>();
        stackInputs.put("oof_directives", "{}");
        stackInputs.put("sdnc_directives", "{}");
        stackInputs.put("generic_vnf_id", "genVNFID");
        stackInputs.put("vf_module_id", "vfMODULEID");

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        stubFor(get(urlPathEqualTo("/api/multicloud/v1/cloud_owner/cloud_region_id/infra_workload/vfname")).willReturn(aResponse()
                //.withHeader()
                .withStatus(HttpStatus.SC_NOT_FOUND)));

        stubFor(get(urlPathEqualTo("/api/multicloud/v1/cloud_owner/cloud_region_id/infra_workload/workload-id")).willReturn(aResponse()
                //.withHeader()
                .withBodyFile("MulticloudResponse_Stack.json")
                .withStatus(HttpStatus.SC_OK)));

        stubFor(get(urlPathEqualTo("/api/multicloud/v1/cloud_owner/cloud_region_id/infra_workload/vfname/outputs")).willReturn(aResponse()
                .withStatus(HttpStatus.SC_NOT_FOUND)));

        stubFor(post(urlPathEqualTo("/api/multicloud/v1/cloud_owner/cloud_region_id/infra_workload")).willReturn(aResponse()
                .withBodyFile("MulticloudResponse_Stack_Create.json")
                .withStatus(HttpStatus.SC_CREATED)));

        instance.createVfModule("MTN13", "123", "vf", "v1", "genericVnfId", "vfname", "vfModuleId", "create", null, "234", "9b339a61-69ca-465f-86b8-1c72c582b8e8", stackInputs, true, true, true,  msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
    }

    @Test
    public void deleteVfModule() throws Exception {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        stubFor(get(urlPathEqualTo("/api/multicloud/v1/cloud_owner/cloud_region_id/infra_workload/workload-id")).willReturn(aResponse()
                .withBodyFile("MulticloudResponse_Stack.json")
                .withStatus(HttpStatus.SC_OK)));

        stubFor(delete(urlPathEqualTo("/api/multicloud/v1/cloud_owner/cloud_region_id/infra_workload/workload-id")).willReturn(aResponse()
                .withStatus(HttpStatus.SC_NO_CONTENT)));

        instance.deleteVfModule("MTN13", "123", "workload-id", msoRequest, new Holder<>());
    }

    @Test
    public void queryVfModule() throws Exception {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        stubFor(get(urlPathEqualTo("/api/multicloud/v1/cloud_owner/cloud_region_id/infra_workload/workload-id")).willReturn(aResponse()
                .withBodyFile("MulticloudResponse_Stack.json")
                .withStatus(HttpStatus.SC_OK)));

        instance.queryVnf("MTN13", "123", "workload-id", msoRequest, new Holder<>(), new Holder<>(), new Holder<>(), new Holder<>());
    }

    // TODO Error Tests
}
