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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.cloudify.v3.client.BlueprintsResource.DeleteBlueprint;
import org.openecomp.mso.cloudify.v3.client.BlueprintsResource.GetBlueprint;
import org.openecomp.mso.cloudify.v3.client.BlueprintsResource.ListBlueprints;
import org.openecomp.mso.cloudify.v3.client.BlueprintsResource.UploadBlueprint;
import org.openecomp.mso.cloudify.v3.model.Blueprint;
import org.openecomp.mso.cloudify.v3.model.Blueprints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class BlueprintsResourceTest {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void cloudifyClientBlueprintFromStream() {
		wireMockRule.stubFor(put(urlPathEqualTo("/api/v3/blueprints/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"id\": \"123\"}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		BlueprintsResource br = c.blueprints();
		InputStream is = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
		UploadBlueprint ub = br.uploadFromStream("123", "blueprint.json", is);
		Blueprint b = ub.execute();
		assertEquals("123", b.getId());
	}

	@Test
	public void cloudifyClientBlueprintFromUrl() {
		wireMockRule.stubFor(put(urlPathEqualTo("/api/v3/blueprints/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"id\": \"123\"}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		BlueprintsResource br = c.blueprints();
		UploadBlueprint ub = br.uploadFromUrl("123", "blueprint.json", "http://localhost:"+port+"/blueprint");
		Blueprint b = ub.execute();
		assertEquals("123", b.getId());
	}

	@Test
	public void cloudifyClientBlueprintDelete() {
		wireMockRule.stubFor(delete(urlPathEqualTo("/api/v3/blueprints/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"id\": \"123\"}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		BlueprintsResource br = c.blueprints();
		DeleteBlueprint db = br.deleteById("123");
		Blueprint b = db.execute();
		assertEquals("123", b.getId());
	}

	@Test
	public void cloudifyClientBlueprintList() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/blueprints")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"items\": [{\"id\": \"123\"}]}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		BlueprintsResource br = c.blueprints();
		ListBlueprints lb = br.list();
		Blueprints b = lb.execute();
		assertEquals("123", b.getItems().get(0).getId());
	}

	@Test
	public void cloudifyClientBlueprintGetById() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/blueprints/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"id\": \"123\"}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		BlueprintsResource br = c.blueprints();
		GetBlueprint gb = br.getById("123");
		Blueprint b = gb.execute();
		assertEquals("123", b.getId());
	}

	@Test
	public void cloudifyClientBlueprintGetMetadataById() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/blueprints/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"id\": \"123\"}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		BlueprintsResource br = c.blueprints();
		GetBlueprint gb = br.getMetadataById("123");
		Blueprint b = gb.execute();
		assertEquals("123", b.getId());
	}


}
