/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vnfmadapter.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.onap.so.adapters.vnfmadapter.Constants.PACKAGE_MANAGEMENT_BASE_URL;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import com.google.gson.Gson;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vnfmadapter.Constants;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.LinkSelf;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmNotificationsFilter;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.PkgmSubscription;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.InlineResponse2002;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.PkgmSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsAuthentication;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsFilter;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsFilter.NotificationTypesEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsFilterVnfProductsFromProviders;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsLinks;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.VnfPackagesLinksSelf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VnfmAdapterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Sol003PackageManagementSubscriptionControllerTest {

    private final Gson gson = new Gson();
    private final URI msbEndpoint = URI.create("http://msb-iag.onap:80/api/vnfpkgm/v1/subscriptions");
    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate testRestTemplate;
    private MockRestServiceServer mockRestServer;
    @Autowired
    private CacheManager cacheServiceProvider;
    @Autowired
    private Sol003PackageManagementSubscriptionController sol003PackageManagementSubscriptionController;

    @Before
    public void setUp() {
        mockRestServer = MockRestServiceServer.bindTo(testRestTemplate).build();
        final Cache cache = cacheServiceProvider.getCache(Constants.PACKAGE_MANAGEMENT_SUBSCRIPTION_CACHE);
        cache.clear();
    }

    @Test
    public void testSuccessPostSubscription() throws GeneralSecurityException, URISyntaxException {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = postSubscriptionForTest();
        final ResponseEntity<InlineResponse2002> response =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .postSubscriptionRequest(pkgmSubscriptionRequest);

        final HttpHeaders headers = buildHttpHeaders(Objects.requireNonNull(response.getBody()).getCallbackUri());

        SubscriptionsLinks subscriptionsLinks = new SubscriptionsLinks();
        VnfPackagesLinksSelf vnfPackagesLinksSelf = new VnfPackagesLinksSelf();
        vnfPackagesLinksSelf.setHref("https://so-vnfm-adapter.onap:30406" + PACKAGE_MANAGEMENT_BASE_URL
                + "/subscriptions/" + response.getBody().getId());
        subscriptionsLinks.setSelf(vnfPackagesLinksSelf);

        assertEquals(pkgmSubscriptionRequest.getFilter(), response.getBody().getFilter());
        assertEquals(subscriptionsLinks, response.getBody().getLinks());
        assertEquals(response.getBody().getFilter(), pkgmSubscriptionRequest.getFilter());
        assert (response.getHeaders().equals(headers));
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertNotNull(response.getBody().getCallbackUri());
    }

    @Test
    public void testFailPostSubscriptionAlreadyExists() throws GeneralSecurityException {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = postSubscriptionForTest();

        final ResponseEntity<InlineResponse2002> response =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .postSubscriptionRequest(pkgmSubscriptionRequest);

        // Create duplicate entry
        final PkgmSubscriptionRequest pkgmSubscriptionRequest2 = buildPkgmSubscriptionRequest();

        final ResponseEntity<InlineResponse2002> response2002 =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .postSubscriptionRequest(pkgmSubscriptionRequest2);

        assertEquals(HttpStatus.SEE_OTHER, response2002.getStatusCode());
    }

    @Test
    public void testSuccessGetSubscriptionWithSubscriptionId() throws GeneralSecurityException, URISyntaxException {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = postSubscriptionForTest();

        final ResponseEntity<InlineResponse2002> response =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .postSubscriptionRequest(pkgmSubscriptionRequest);
        final String subscriptionId = response.getBody().getId();

        final ResponseEntity<InlineResponse2002> response2002 =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .getSubscription(subscriptionId);

        final HttpHeaders headers = buildHttpHeaders(response.getBody().getCallbackUri());

        assertEquals(response.getBody().getFilter(), pkgmSubscriptionRequest.getFilter());
        assertEquals(response.getHeaders(), headers);
        assertEquals(HttpStatus.OK, response2002.getStatusCode());
        assertEquals(pkgmSubscriptionRequest.getFilter(), response.getBody().getFilter());
        // Ensure CallBackUri is set to new URI
        assertNotEquals(pkgmSubscriptionRequest.getCallbackUri(), response.getBody().getCallbackUri());
    }

    @Test
    public void testFailGetSubscriptionWithInvalidSubscriptionId() {
        final ResponseEntity<InlineResponse2002> response =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .getSubscription("invalidSubscriptionId");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testSuccessGetSubscriptions() throws GeneralSecurityException {
        final PkgmSubscription pkgmSubscription = buildPkgmSubscription();
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = buildPkgmSubscriptionRequest();

        mockRestServer.expect(requestTo(msbEndpoint)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(gson.toJson(pkgmSubscription), MediaType.APPLICATION_JSON));

        sol003PackageManagementSubscriptionController.postSubscriptionRequest(pkgmSubscriptionRequest);
        ResponseEntity<List<InlineResponse2002>> response =
                sol003PackageManagementSubscriptionController.getSubscriptions();

        List<InlineResponse2002> subscriptionsList = response.getBody();

        assertEquals(Objects.requireNonNull(response.getBody()).get(0).getFilter(),
                pkgmSubscriptionRequest.getFilter());
        assert (subscriptionsList != null);
        assertNotEquals('0', subscriptionsList.size());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testSuccessDeleteSubscriptionWithSubscriptionId() throws GeneralSecurityException {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = buildPkgmSubscriptionRequest();
        final PkgmSubscription pkgmSubscription = buildPkgmSubscription();
        final String subscriptionId = pkgmSubscription.getId();

        mockRestServer.expect(requestTo(msbEndpoint)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(gson.toJson(pkgmSubscription), MediaType.APPLICATION_JSON));
        mockRestServer.expect(requestTo(msbEndpoint + "/" + subscriptionId)).andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        final ResponseEntity<InlineResponse2002> responsePost =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .postSubscriptionRequest(pkgmSubscriptionRequest);

        final ResponseEntity responseDelete =
                sol003PackageManagementSubscriptionController.deleteSubscription(subscriptionId);

        // Attempt to retrieve the subscription after delete
        final ResponseEntity<InlineResponse2002> responseGetSubscription =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .getSubscription(subscriptionId);

        assertEquals(HttpStatus.NOT_FOUND, responseGetSubscription.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
    }

    @Test
    public void testFailDeleteSubscriptionWithInvalidSubscriptionId() throws URISyntaxException, InterruptedException {
        final ResponseEntity<Void> responseDelete = (ResponseEntity<Void>) sol003PackageManagementSubscriptionController
                .deleteSubscription("invalidSubscriptionId");
        assertEquals(HttpStatus.NOT_FOUND, responseDelete.getStatusCode());
    }

    private PkgmSubscriptionRequest buildPkgmSubscriptionRequest() {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = new PkgmSubscriptionRequest();
        final SubscriptionsFilter sub = buildSubscriptionsFilter();
        final SubscriptionsAuthentication auth = new SubscriptionsAuthentication();
        pkgmSubscriptionRequest.setFilter(sub);
        pkgmSubscriptionRequest.setCallbackUri(msbEndpoint.toString());
        pkgmSubscriptionRequest.setAuthentication(auth);
        return pkgmSubscriptionRequest;
    }

    private SubscriptionsFilter buildSubscriptionsFilter() {
        final SubscriptionsFilter sub = new SubscriptionsFilter();
        final List<String> vnfdIdList = new ArrayList();
        final List<String> vnfPkgIdList = new ArrayList();
        final List<NotificationTypesEnum> notificationTypes = new ArrayList<>();
        final SubscriptionsFilterVnfProductsFromProviders subscriptionsFilterVnfProductsFromProviders =
                new SubscriptionsFilterVnfProductsFromProviders();
        final List<SubscriptionsFilterVnfProductsFromProviders> vnfProductsFromProviders = new ArrayList<>();

        vnfProductsFromProviders.add(subscriptionsFilterVnfProductsFromProviders);
        sub.setVnfdId(vnfdIdList);
        sub.setNotificationTypes(notificationTypes);
        sub.setVnfPkgId(vnfPkgIdList);
        sub.setVnfProductsFromProviders(vnfProductsFromProviders);
        return sub;
    }

    private PkgmSubscription buildPkgmSubscription() {
        PkgmSubscription pkgmSubscription = new PkgmSubscription();
        PkgmNotificationsFilter pkgmNotificationsFilter = new PkgmNotificationsFilter();
        LinkSelf linkSelf = new LinkSelf();
        String id = UUID.randomUUID().toString();
        pkgmSubscription.setId(id);
        pkgmSubscription.setCallbackUri(msbEndpoint + "/" + pkgmSubscription.getId().toString());
        pkgmSubscription.setFilter(pkgmNotificationsFilter);
        pkgmSubscription.setLinks(linkSelf);
        return pkgmSubscription;
    }

    private PkgmSubscriptionRequest postSubscriptionForTest() {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = buildPkgmSubscriptionRequest();
        final PkgmSubscription pkgmSubscription = buildPkgmSubscription();

        mockRestServer.expect(requestTo(msbEndpoint)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(gson.toJson(pkgmSubscription), MediaType.APPLICATION_JSON));
        return pkgmSubscriptionRequest;
    }

    private HttpHeaders buildHttpHeaders(String uri) throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        URI myUri = new URI(uri);
        headers.setLocation(myUri);
        return headers;
    }

}
