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

package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.onap.so.adapters.etsi.sol003.adapter.common.CommonConstants.ETSI_SUBSCRIPTION_NOTIFICATION_BASE_URL;
import static org.onap.so.adapters.etsi.sol003.adapter.common.CommonConstants.PACKAGE_MANAGEMENT_BASE_URL;
import static org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.EtsiCatalogServiceProviderConfiguration.ETSI_CATALOG_REST_TEMPLATE_BEAN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.PackageManagementConstants;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.BasicAuth;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.LinkSelf;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.NsdmSubscription;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.PkgmNotificationsFilter;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.PkgmSubscription;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.ProblemDetails;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.SubscriptionAuthentication;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.Version;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.VnfProducts;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.VnfProductsProviders;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.InlineResponse2002;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.InlineResponse201;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.PkgmSubscriptionRequest;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.SubscriptionsAuthentication;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.SubscriptionsFilter1;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.SubscriptionsFilterVnfProductsFromProviders;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.SubscriptionsLinks;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.model.VnfPackagesLinksSelf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
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
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

/**
 * @author Ronan Kenny (ronan.kenny@est.tech)
 * @author Gareth Roper (gareth.roper@est.tech)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Sol003PackageManagementSubscriptionControllerTest {

    private final Gson gson = new Gson();
    private final URI msbEndpoint = URI.create("http://msb-iag.onap:80/api/vnfpkgm/v1/subscriptions");
    private static final String _NOTIFICATION_CALLBACK_URI =
            "https://so-vnfm-adapter.onap:30406" + ETSI_SUBSCRIPTION_NOTIFICATION_BASE_URL;
    private static final String LOCALHOST_URL = "http://localhost:";

    @Autowired
    @Qualifier(ETSI_CATALOG_REST_TEMPLATE_BEAN)
    private RestTemplate restTemplate;
    private MockRestServiceServer mockRestServiceServer;
    @Autowired
    private CacheManager cacheServiceProvider;
    @Autowired
    private Sol003PackageManagementSubscriptionController sol003PackageManagementSubscriptionController;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private static final String ID = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
        final Cache cache =
                cacheServiceProvider.getCache(PackageManagementConstants.PACKAGE_MANAGEMENT_SUBSCRIPTION_CACHE);
        cache.clear();
    }

    @After
    public void after() {
        mockRestServiceServer.reset();
    }

    @Test
    public void testSuccessPostSubscription() throws GeneralSecurityException, URISyntaxException {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = postSubscriptionForTest();
        final ResponseEntity<InlineResponse201> response =
                (ResponseEntity<InlineResponse201>) sol003PackageManagementSubscriptionController
                        .postSubscriptionRequest(pkgmSubscriptionRequest);

        final HttpHeaders headers = buildHttpHeaders(Objects.requireNonNull(response.getBody()).getCallbackUri());

        final SubscriptionsLinks subscriptionsLinks = new SubscriptionsLinks();
        final VnfPackagesLinksSelf vnfPackagesLinksSelf = new VnfPackagesLinksSelf();
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

        mockRestServiceServer.expect(requestTo(msbEndpoint + "/" + ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(new NsdmSubscription().id(ID)), MediaType.APPLICATION_JSON));

        final ResponseEntity<InlineResponse201> response =
                (ResponseEntity<InlineResponse201>) sol003PackageManagementSubscriptionController
                        .postSubscriptionRequest(pkgmSubscriptionRequest);
        final String subscriptionId = response.getBody().getId();



        final ResponseEntity<InlineResponse201> responseEntity =
                (ResponseEntity<InlineResponse201>) sol003PackageManagementSubscriptionController
                        .getSubscription(subscriptionId);

        final HttpHeaders headers = buildHttpHeaders(response.getBody().getCallbackUri());

        assertEquals(response.getBody().getFilter(), pkgmSubscriptionRequest.getFilter());
        assertEquals(response.getHeaders(), headers);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pkgmSubscriptionRequest.getFilter(), response.getBody().getFilter());
        // Ensure CallBackUri is set to new URI
        assertNotEquals(pkgmSubscriptionRequest.getCallbackUri(), response.getBody().getCallbackUri());
    }

    @Test
    public void testFailGetSubscriptionWithInvalidSubscriptionId() {
        final String invalidId = "invalidSubscriptionId";
        mockRestServiceServer.expect(requestTo(msbEndpoint + "/" + invalidId)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        final ResponseEntity<?> response = sol003PackageManagementSubscriptionController.getSubscription(invalidId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);
    }

    @Test
    public void testSuccessGetSubscriptions() throws GeneralSecurityException {
        final PkgmSubscription pkgmSubscription = buildPkgmSubscription();
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = buildPkgmSubscriptionRequest();

        mockRestServiceServer.expect(requestTo(msbEndpoint)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(gson.toJson(pkgmSubscription), MediaType.APPLICATION_JSON));
        mockRestServiceServer.expect(requestTo(msbEndpoint + "/" + ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(new NsdmSubscription().id(ID)), MediaType.APPLICATION_JSON));

        sol003PackageManagementSubscriptionController.postSubscriptionRequest(pkgmSubscriptionRequest);
        final ResponseEntity<List<InlineResponse201>> response =
                sol003PackageManagementSubscriptionController.getSubscriptions();

        final List<InlineResponse201> subscriptionsList = response.getBody();

        assertEquals(Objects.requireNonNull(response.getBody()).get(0).getFilter(),
                pkgmSubscriptionRequest.getFilter());
        assertNotNull(subscriptionsList != null);
        assertNotEquals('0', subscriptionsList.size());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testSuccessDeleteSubscriptionWithSubscriptionId() throws GeneralSecurityException {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = buildPkgmSubscriptionRequest();
        final PkgmSubscription pkgmSubscription = buildPkgmSubscription();
        final String subscriptionId = pkgmSubscription.getId();

        mockRestServiceServer.expect(requestTo(msbEndpoint)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(gson.toJson(pkgmSubscription), MediaType.APPLICATION_JSON));

        mockRestServiceServer.expect(requestTo(msbEndpoint + "/" + subscriptionId)).andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
        mockRestServiceServer.expect(requestTo(msbEndpoint + "/" + subscriptionId)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(gson.toJson(new NsdmSubscription().id(subscriptionId)),
                        MediaType.APPLICATION_JSON));

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
    public void testDeleteSubscription_SubscripitonNotFoundInEtsiCatalogManager_SubscriptionDeletedFromLocalCache()
            throws GeneralSecurityException {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = buildPkgmSubscriptionRequest();
        final PkgmSubscription pkgmSubscription = buildPkgmSubscription();

        mockRestServiceServer.expect(requestTo(msbEndpoint)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(gson.toJson(pkgmSubscription), MediaType.APPLICATION_JSON));

        mockRestServiceServer.expect(requestTo(msbEndpoint + "/" + ID)).andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        final ResponseEntity<InlineResponse2002> responsePost =
                (ResponseEntity<InlineResponse2002>) sol003PackageManagementSubscriptionController
                        .postSubscriptionRequest(pkgmSubscriptionRequest);

        final Cache cache =
                cacheServiceProvider.getCache(PackageManagementConstants.PACKAGE_MANAGEMENT_SUBSCRIPTION_CACHE);
        assertNotNull(cache.get(ID));

        final ResponseEntity responseDelete = sol003PackageManagementSubscriptionController.deleteSubscription(ID);

        assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
        assertNull(cache.get(ID));

    }

    @Test
    public void testFailDeleteSubscriptionWithInvalidSubscriptionId() throws URISyntaxException, InterruptedException {
        final ResponseEntity<Void> responseDelete = (ResponseEntity<Void>) sol003PackageManagementSubscriptionController
                .deleteSubscription("invalidSubscriptionId");
        assertEquals(HttpStatus.NOT_FOUND, responseDelete.getStatusCode());
    }


    @Test
    public void testSuccessPostSubscriptionWithValidNotificationTypes() throws Exception {

        final String file = getAbsolutePath("src/test/resources/requests/SubscriptionRequest.json");
        final String json = new String(Files.readAllBytes(Paths.get(file)));
        final PkgmSubscriptionRequest request = gson.fromJson(json, PkgmSubscriptionRequest.class);

        mockRestServiceServer.expect(requestTo(msbEndpoint)).andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(gson.toJson(getEtsiCatalogPkgmSubscriptionRequest())))
                .andRespond(withSuccess(gson.toJson(buildPkgmSubscription()), MediaType.APPLICATION_JSON));


        final ResponseEntity<InlineResponse201> responseEntity =
                testRestTemplate.postForEntity(LOCALHOST_URL + port + PACKAGE_MANAGEMENT_BASE_URL + "/subscriptions",
                        request, InlineResponse201.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        final InlineResponse201 actual = responseEntity.getBody();
        assertEquals(ID, actual.getId());


    }

    private org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.PkgmSubscriptionRequest getEtsiCatalogPkgmSubscriptionRequest() {
        return new org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.etsicatalog.model.PkgmSubscriptionRequest()
                .filter(new PkgmNotificationsFilter()
                        .addNotificationTypesItem(
                                PkgmNotificationsFilter.NotificationTypesEnum.VNFPACKAGEONBOARDINGNOTIFICATION)
                        .addVnfdIdItem("VNFDID").addVnfPkgIdItem("VNFPKGID")
                        .addOperationalStateItem(PkgmNotificationsFilter.OperationalStateEnum.ENABLED)
                        .addVnfProductsFromProvidersItem(new VnfProductsProviders().vnfProvider("EST")
                                .addVnfProductsItem(new VnfProducts().vnfProductName("VnfProducts")
                                        .addVersionsItem(new Version().vnfSoftwareVersion("vnfSoftwareVersion")
                                                .addVnfdVersionsItem("version1")))))
                .callbackUri(_NOTIFICATION_CALLBACK_URI).authentication(
                        new SubscriptionAuthentication().addAuthTypeItem(SubscriptionAuthentication.AuthTypeEnum.BASIC)
                                .paramsBasic(new BasicAuth().userName("vnfm").password("password1$")));
    }

    private PkgmSubscriptionRequest buildPkgmSubscriptionRequest() {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = new PkgmSubscriptionRequest();
        final SubscriptionsFilter1 sub = buildSubscriptionsFilter();
        final SubscriptionsAuthentication auth = new SubscriptionsAuthentication();
        pkgmSubscriptionRequest.setFilter(sub);
        pkgmSubscriptionRequest.setCallbackUri(msbEndpoint.toString());
        pkgmSubscriptionRequest.setAuthentication(auth);
        return pkgmSubscriptionRequest;
    }

    private SubscriptionsFilter1 buildSubscriptionsFilter() {
        final SubscriptionsFilter1 sub = new SubscriptionsFilter1();
        final List<String> vnfdIdList = new ArrayList<>();
        final List<String> vnfPkgIdList = new ArrayList<>();
        final List<SubscriptionsFilter1.NotificationTypesEnum> notificationTypes = new ArrayList<>();
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
        final PkgmSubscription pkgmSubscription = new PkgmSubscription();
        final PkgmNotificationsFilter pkgmNotificationsFilter = new PkgmNotificationsFilter();
        final LinkSelf linkSelf = new LinkSelf();

        pkgmSubscription.setId(ID);
        pkgmSubscription.setCallbackUri(msbEndpoint + "/" + pkgmSubscription.getId().toString());
        pkgmSubscription.setFilter(pkgmNotificationsFilter);
        pkgmSubscription.setLinks(linkSelf);
        return pkgmSubscription;
    }

    private PkgmSubscriptionRequest postSubscriptionForTest() {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = buildPkgmSubscriptionRequest();
        final PkgmSubscription pkgmSubscription = buildPkgmSubscription();

        mockRestServiceServer.expect(requestTo(msbEndpoint)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(gson.toJson(pkgmSubscription), MediaType.APPLICATION_JSON));
        return pkgmSubscriptionRequest;
    }

    private HttpHeaders buildHttpHeaders(final String uri) throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        final URI myUri = new URI(uri);
        headers.setLocation(myUri);
        return headers;
    }

    private String getAbsolutePath(final String path) {
        final File file = new File(path);
        return file.getAbsolutePath();
    }

}
