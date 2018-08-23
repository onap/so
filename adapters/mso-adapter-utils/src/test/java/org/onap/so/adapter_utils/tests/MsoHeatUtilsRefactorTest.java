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

package org.onap.so.adapter_utils.tests;


import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.ServerType;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;

/**PoConfigTest
 * This class implements test methods of the MsoHeatUtils
 *
 *
 */
public class MsoHeatUtilsRefactorTest extends BaseTest {

	@Autowired
	private  MsoHeatUtils msoHeatUtils;

	@Before
	public void init() throws IOException {
		CloudIdentity identity = new CloudIdentity();

		identity.setId("MTN13");
		identity.setMsoId("m93945");
		identity.setMsoPass("93937EA01B94A10A49279D4572B48369");
		identity.setAdminTenant("admin");
		identity.setMemberRole("admin");
		identity.setTenantMetadata(true);
		identity.setIdentityUrl("http://localhost:28090/v2.0");
		identity.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD);

		CloudSite cloudSite = new CloudSite();
		cloudSite.setId("MTN13");
		cloudSite.setCloudVersion("3.0");
		cloudSite.setClli("MDT13");
		cloudSite.setRegionId("MTN13");
		identity.setIdentityServerType(ServerType.KEYSTONE);
		cloudSite.setIdentityService(identity);


		stubFor(get(urlPathEqualTo("/cloudSite/DEFAULT")).willReturn(aResponse()
				.withBody(getBody(mapper.writeValueAsString(cloudSite),wireMockPort, ""))
				.withHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_OK)));
		stubFor(get(urlPathEqualTo("/cloudIdentity/MTN13")).willReturn(aResponse()
				.withBody(getBody(mapper.writeValueAsString(identity),wireMockPort, ""))
				.withHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_OK)));
	}
	
	@Test
	public final void testGetKeystoneUrl() throws MsoCloudSiteNotFound {
		String keyUrl = msoHeatUtils.getCloudSiteKeystoneUrl("DAN");
		assertEquals("http://localhost:28090/v2.0", keyUrl);
	}
}
