/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.cloudify.v3.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.CreateDeployment;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.DeleteDeployment;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.GetDeployment;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.GetDeploymentOutputs;
import org.openecomp.mso.cloudify.v3.client.DeploymentsResource.ListDeployments;
import org.openecomp.mso.cloudify.v3.model.CreateDeploymentParams;
import org.openecomp.mso.cloudify.v3.model.Deployment;
import org.openecomp.mso.cloudify.v3.model.Deployments;
import org.openecomp.mso.cloudify.v3.model.DeploymentOutputs;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class DeploymentsResourceTest {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void cloudifyDeploymentsCreate() {
		wireMockRule.stubFor(put(urlPathEqualTo("/api/v3/deployments/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		DeploymentsResource br = c.deployments();

		CreateDeploymentParams cdp = new CreateDeploymentParams();
		cdp.setBlueprintId("123");
		Map<String, Object> inputs = new HashMap<String, Object>();
		cdp.setInputs(inputs);
		CreateDeployment cd = br.create("123", cdp);
		Deployment d = cd.execute();
		assertEquals("123", d.getId());
	}

	@Test
	public void cloudifyDeploymentsList() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/deployments")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"items\": {\"id\": \"123\" } } ")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		DeploymentsResource br = c.deployments();
		ListDeployments ld = br.list();
		Deployments d = ld.execute();
		assertEquals("123", d.getItems().get(0).getId());
	}

	@Test
	public void cloudifyDeploymentsGet() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/deployments/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		DeploymentsResource br = c.deployments();
		GetDeployment gd = br.byId("123");
		Deployment d = gd.execute();
		assertEquals("123", d.getId());
	}

	@Test
	public void cloudifyDeploymentsGetOutputs() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/deployments/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"deployment_id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		DeploymentsResource br = c.deployments();
		GetDeploymentOutputs gdo = br.outputsById("123");
		DeploymentOutputs d = gdo.execute();
		assertEquals("123", d.getDeploymentId());
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", "answer");
		assertEquals("answer", d.getMapValue(map, "test", String.class));

		Integer i = d.getMapValue(map, "nil", Integer.class);
		assertNull( i );
	
		i = d.getMapValue(map, "test", Integer.class);
		assertNull( i );
	}

	@Test
	public void cloudifyDeploymentsDelete() {
		wireMockRule.stubFor(delete(urlPathEqualTo("/api/v3/deployments/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		DeploymentsResource br = c.deployments();
		DeleteDeployment cd = br.deleteByName("name");
		Deployment d = cd.execute();
		assertEquals("123", d.getId());
	}

}
