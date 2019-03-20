/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.VnfRollback;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MsoVnfCloudifyAdapterImplTest extends BaseRestTestUtils {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Autowired
	private MsoVnfCloudifyAdapterImpl instance;

	@Autowired
	private CloudConfig cloudConfig;

	@Before
	public void before() throws Exception {
		super.setUp();
		CloudifyManager cloudifyManager = new CloudifyManager();
		cloudifyManager.setId("mtn13");
		cloudifyManager.setCloudifyUrl("http://localhost:"+wireMockPort+"/v2.0");
		cloudifyManager.setUsername("m93945");
		cloudifyManager.setPassword("93937EA01B94A10A49279D4572B48369");
	}
	
	@Test 
    public void queryVnfExceptionTest() throws Exception {
		reset();
		expectedException.expect(VnfException.class);
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        instance.queryVnf("siteid", "CloudOwner", "1234", "vfname",
                msoRequest, new Holder<>(), new Holder<>(), new Holder<>(),
                new Holder<>());
    }

	@Test
	public void queryVnfTest() throws Exception {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		stubFor(get(urlPathEqualTo("/v2.0/api/v3/deployments/vfname")).willReturn(aResponse()
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		stubFor(get(urlPathEqualTo("/v2.0/api/v3/deployments/vfname/outputs")).willReturn(aResponse()
				.withBody("{ \"deployment_id\": \"123\",\"outputs\":{\"abc\":\"abc\"} }")
				.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlMatching("/v2.0/api/v3/executions?.*")).willReturn(aResponse()
				.withBody("{ \"items\": {\"id\": \"123\",\"workflow_id\":\"install\",\"status\":\"terminated\" } } ")
				.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlPathEqualTo("/v2.0/api/v3/tokens")).willReturn(aResponse()
				.withBodyFile("OpenstackResponse_Access.json")
				.withStatus(HttpStatus.SC_OK)));
		
		instance.queryVnf("mtn13", "CloudOwner", "1234", "vfname",
				msoRequest, new Holder<>(), new Holder<>(), new Holder<>(),
				new Holder<>());
	}

	@Test
	public void deleteVfModuleTest_ExceptionWhileQueryDeployment() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		instance.deleteVfModule("mtn13", "CloudOwner", "1234", "vfname", msoRequest, new Holder<>());
	}

	@Test
	public void deleteVfModuleTest_ExceptionWhileDeleteDeployment() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		stubFor(get(urlPathEqualTo("/v2.0/api/v3/deployments/vfname")).willReturn(aResponse()
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlPathEqualTo("/v2.0/api/v3/deployments/vfname/outputs")).willReturn(aResponse()
				.withBody("{ \"deployment_id\": \"123\",\"outputs\":{\"abc\":\"abc\"} }")
				.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlMatching("/v2.0/api/v3/executions?.*")).willReturn(aResponse()
				.withBody("{ \"items\": {\"id\": \"123\",\"workflow_id\":\"install\",\"status\":\"terminated\" } } ")
				.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlPathEqualTo("/v2.0/api/v3/tokens")).willReturn(aResponse()
				.withBodyFile("OpenstackResponse_Access.json")
				.withStatus(HttpStatus.SC_OK)));

		instance.deleteVfModule("mtn13", "CloudOwner", "1234", "vfname", msoRequest, new Holder<>());
	}
	
	@Test
    public void deleteVnfVnfExceptionTest() throws Exception {
		expectedException.expect(VnfException.class);
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        instance.deleteVnf("12344", "CloudOwner", "234", "vnfname", msoRequest);

    }

	@Test
	public void rollbackVnf() throws Exception {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

        VnfRollback vnfRollback = new VnfRollback();
        vnfRollback.setModelCustomizationUuid("1234");
        vnfRollback.setVfModuleStackId("2134");
        vnfRollback.setVnfId("123");
        vnfRollback.setModelCustomizationUuid("1234");

        instance.rollbackVnf(vnfRollback);
	}

	@Test
	public void rollbackVnf_Created() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		VnfRollback vnfRollback = new VnfRollback();
		vnfRollback.setModelCustomizationUuid("1234");
		vnfRollback.setVfModuleStackId("2134");
		vnfRollback.setVnfId("123");
		vnfRollback.setModelCustomizationUuid("1234");
		vnfRollback.setVnfCreated(true);

		instance.rollbackVnf(vnfRollback);
	}

	@Test
	public void createVfModuleVnfException() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		instance.createVfModule("123", "CloudOwner", "123", "vf", "v1", "", "module-005", "", "create", "3245", "234", "123", new HashMap<>(), true, true, true,  msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
	}

	@Test
	public void createVfModule_ModelCustUuidIsNull() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		instance.createVfModule("123", "CloudOwner", "123", "vf", "v1", "", "module-005", "", "create", "3245", "234", null, new 
				HashMap<>(), true, true, true,  msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
	}

	@Test
	public void createVfModule_CloudSiteIdNotFound() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		instance.createVfModule("123", "CloudOwner", "123", "vf", "v1", "", "module-005", "", "create", "3245", "234", "9b339a61-69ca-465f-86b8-1c72c582b8e8", new HashMap<>(), true, true, true,  msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
	}

	@Test
	public void createVfModule_MsoCloudifyManagerNotFound() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		instance.createVfModule("mtn13", "CloudOwner", "123", "vf", "v1", "", "module-005", "", "create", "3245", "234", "9b339a61-69ca-465f-86b8-1c72c582b8e8", new HashMap<>(), true, true, true,  msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
	}

	@Test
	public void createVfModule() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		stubFor(get(urlPathEqualTo("/v2.0/api/v3/deployments/vfname")).willReturn(aResponse()
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlPathEqualTo("/v2.0/api/v3/deployments/vfname/outputs")).willReturn(aResponse()
				.withBody("{ \"deployment_id\": \"123\",\"outputs\":{\"abc\":\"abc\"} }")
				.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlMatching("/v2.0/api/v3/executions?.*")).willReturn(aResponse()
				.withBody("{ \"items\": {\"id\": \"123\",\"workflow_id\":\"install\",\"status\":\"terminated\" } } ")
				.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlPathEqualTo("/v2.0/api/v3/tokens")).willReturn(aResponse()
				.withBodyFile("OpenstackResponse_Access.json")
				.withStatus(HttpStatus.SC_OK)));

		instance.createVfModule("mtn13", "CloudOwner", "123", "vf", "v1", "", "vfname", "", "create", "3245", "234", "9b339a61-69ca-465f-86b8-1c72c582b8e8", new HashMap<>(), true, true, true,  msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
	}

	@Test
	public void updateVfModuleVnfException() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		instance.updateVfModule("123", "CloudOwner", "1234", "fw", "v2", "vnf1", "create", "123", "12", "233", "234", new HashMap<>(), msoRequest, new Holder<>(), new Holder<>());
	}

	@Test
	public void healthCheckVNFTest() {
		instance.healthCheck();
	}

	@Test
	public void createVnfTest() {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.createVnf("mdt1", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", map,
					Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, msoRequest, new Holder<>(), new Holder<>(),
                new Holder<>());
		} catch (Exception e) {
		}
	}

	@Test
	public void updateVnfTest() {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, Object> map = new HashMap<>();
		
		map.put("key1", "value1");
		try {
			instance.updateVnf("mdt1", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1",  map, msoRequest, new Holder<>(),
                new Holder<>());
		} catch (Exception e) {

		}
	}

	@Test
	public void deleteVnfTest() {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		try {
			instance.deleteVnf("mdt1", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest);
		} catch (Exception e) {

		}
	}

}
