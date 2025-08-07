/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.catalogdb.catalogrest;

import static org.junit.Assert.assertEquals;
import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.ServerType;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

public class CloudConfigTest extends CatalogDbAdapterBaseTest {

    protected TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

    protected HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    private int port;

    @Test
    @Transactional
    public void createCloudSiteRest_TEST() {
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);

        CloudSite cloudSite = new CloudSite();
        cloudSite.setId("MTN7");
        cloudSite.setClli("TESTCLLI");
        cloudSite.setRegionId("regionId");
        cloudSite.setCloudVersion("VERSION");
        cloudSite.setPlatform("PLATFORM");

        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setId("RANDOMID-test");
        cloudIdentity.setIdentityUrl("URL");
        cloudIdentity.setMsoId("MSO_ID");
        cloudIdentity.setMsoPass("MSO_PASS");
        cloudIdentity.setAdminTenant("ADMIN_TENANT");
        cloudIdentity.setMemberRole("ROLE");
        cloudIdentity.setIdentityServerType(ServerType.KEYSTONE);
        cloudIdentity.setIdentityAuthenticationType(AuthenticationType.RACKSPACE_APIKEY);
        cloudSite.setIdentityService(cloudIdentity);
        String uri = "/cloudSite";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:" + port + uri);
        HttpEntity<CloudSite> request = new HttpEntity<CloudSite>(cloudSite, headers);
        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.POST, request, String.class);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatusCode().value());

        builder = UriComponentsBuilder.fromHttpUrl("http://localhost:" + port + uri + "/" + cloudSite.getId());
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<CloudSite> actualCloudSite =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, CloudSite.class);
        builder = UriComponentsBuilder.fromHttpUrl("http://localhost:" + port + uri);
        ResponseEntity<String> cloudSiteString =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
        System.out.println(cloudSiteString.getBody());
        assertEquals(Response.Status.OK.getStatusCode(), actualCloudSite.getStatusCode().value());
        assertThat(actualCloudSite.getBody(), sameBeanAs(cloudSite).ignoring("created").ignoring("updated")
                .ignoring("identityService.created").ignoring("identityService.updated"));

    }



}
