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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onap.so.adapters.vnfmadapter.Constants.ETSI_SUBSCRIPTION_NOTIFICATION_CONTROLLER_BASE_URL;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vnfmadapter.Constants;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.model.ProblemDetails;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.notification.model.NOTIFICATIONLINKSERIALIZER;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.notification.model.PkgChangeNotification;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.notification.model.PkgOnboardingNotification;
import org.onap.so.adapters.vnfmadapter.extclients.etsicatalog.notification.model.PkgmLinks;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.PkgmSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsAuthentication;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsAuthenticationParamsBasic;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.SubscriptionsAuthenticationParamsOauth2ClientCredentials;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.notification.model.VnfPackageChangeNotification;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.notification.model.VnfPackageOnboardingNotification;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VnfmAdapterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_CLASS)
public class EtsiSubscriptionNotificationControllerTest {

    @LocalServerPort
    private int port;

    private static final URI CALLBACK_URI = URI.create("http://test_callback_uri/notification");
    private static final String TOKEN_ENDPOINT = "http://test_token_endpoint_uri/";
    private static final String TOKEN = "dXNlcm5hbWU6cGFzc3dvcmQ=......";
    private static final String JSON_TOKEN = "{\"access_token\":\"" + TOKEN + "\"}";
    private static final String LOCALHOST_URL = "http://localhost:";
    private static final String NOTIFICATION_BASE_URL =
            ETSI_SUBSCRIPTION_NOTIFICATION_CONTROLLER_BASE_URL + "/notification";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String EXPECTED_BASIC_AUTHORIZATION = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=";
    private static final String EXPECTED_OAUTH_AUTHORIZATION = "Bearer " + TOKEN;
    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    private static final OffsetDateTime TIMESTAMP =
            OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 1, 1, 1, 1), ZoneOffset.ofHours(1));
    private static final String VNFPKG_ID = UUID.randomUUID().toString();
    private static final String VNFD_ID = UUID.randomUUID().toString();
    private static final String EXPECTED_VNF_PACKAGE_HREF =
            "https://so-vnfm-adapter.onap:30406/so/vnfm-adapter/v1/vnfpkgm/v1/vnf_packages/" + VNFPKG_ID;
    private static final String EXPECTED_SUBSCRIPTION_HREF =
            "https://so-vnfm-adapter.onap:30406/so/vnfm-adapter/v1/vnfpkgm/v1/subscriptions/" + SUBSCRIPTION_ID;

    private BasicHttpHeadersProvider basicHttpHeadersProvider;
    private final Gson gson = new GsonBuilder().create();;

    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate restTemplate;
    private MockRestServiceServer mockRestServer;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CacheManager cacheServiceProvider;
    private Cache cache;

    @Before
    public void setUp() {
        mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();
        basicHttpHeadersProvider = new BasicHttpHeadersProvider();
        cache = cacheServiceProvider.getCache(Constants.PACKAGE_MANAGEMENT_SUBSCRIPTION_CACHE);
        cache.clear();
    }

    @After
    public void tearDown() {
        cache.clear();
    }

    @Test
    public void testSubscriptionNotificationEndPoint_ReturnsNoContent() {
        final ResponseEntity<?> response = sendHttpGet(NOTIFICATION_BASE_URL);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testOnboardingNotificationSentOnToVnfmCallbackUri_SubscriptionRequestInCache_Success() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgOnboardingNotification notification = buildPkgOnboardingNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.id").value(NOTIFICATION_ID))
                .andExpect(jsonPath("$.notificationType")
                        .value(VnfPackageOnboardingNotification.NotificationTypeEnum.VNFPACKAGEONBOARDINGNOTIFICATION
                                .toString()))
                .andExpect(jsonPath("$.subscriptionId").value(SUBSCRIPTION_ID))
                .andExpect(jsonPath("$.timeStamp").value(TIMESTAMP.toString()))
                .andExpect(jsonPath("$.vnfPkgId").value(VNFPKG_ID.toString()))
                .andExpect(jsonPath("$.vnfdId").value(VNFD_ID.toString()))
                .andExpect(jsonPath("$._links")
                        .value(buildPkgmLinks(EXPECTED_VNF_PACKAGE_HREF, EXPECTED_SUBSCRIPTION_HREF)))
                .andExpect(header("Authorization", EXPECTED_BASIC_AUTHORIZATION)).andRespond(withSuccess());

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testOnboardingNotificationNotSentOnToVnfmCallbackUri_SubscriptionRequestNotInCache_Fail() {
        final PkgOnboardingNotification notification = buildPkgOnboardingNotification();
        final String notificationString = gson.toJson(notification);
        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "No subscription found with subscriptionId " + SUBSCRIPTION_ID
                + ". Unable to forward notification to subscriber.";

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testOnboardingNotificationSentOnToVnfmCallbackUri_BadRequestResponseFromCallbackUri_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgOnboardingNotification notification = buildPkgOnboardingNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.BAD_REQUEST + ".\n" + "No result found for given url: " + CALLBACK_URI;

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testOnboardingNotificationSentOnToVnfmCallbackUri_301MovedPermanentlyResponseFromCallbackUri_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgOnboardingNotification notification = buildPkgOnboardingNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.MOVED_PERMANENTLY));

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed.";

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testOnboardingNotificationSentOnToVnfmCallbackUri_NotFoundResponseFromCallbackUri_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgOnboardingNotification notification = buildPkgOnboardingNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.NOT_FOUND + ".\n" + "No result found for given url: " + CALLBACK_URI;

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testOnboardingNotificationSentOnToVnfmCallbackUri_InternalServerErrorResponseFromCallbackUri_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgOnboardingNotification notification = buildPkgOnboardingNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.INTERNAL_SERVER_ERROR.value() + ".\n" + "Unable to invoke HTTP POST using URL: "
                + CALLBACK_URI;

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testChangeNotificationSentOnToVnfmCallbackUri_SubscriptionRequestInCache_Success() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.id").value(NOTIFICATION_ID))
                .andExpect(jsonPath("$.notificationType").value(
                        VnfPackageChangeNotification.NotificationTypeEnum.VNFPACKAGECHANGENOTIFICATION.getValue()))
                .andExpect(jsonPath("$.subscriptionId").value(SUBSCRIPTION_ID))
                .andExpect(jsonPath("$.timeStamp").value(TIMESTAMP.toString()))
                .andExpect(jsonPath("$.vnfPkgId").value(VNFPKG_ID.toString()))
                .andExpect(jsonPath("$.vnfdId").value(VNFD_ID.toString()))
                .andExpect(
                        jsonPath("$.changeType").value(PkgChangeNotification.ChangeTypeEnum.OP_STATE_CHANGE.toString()))
                .andExpect(jsonPath("$.operationalState")
                        .value(PkgChangeNotification.OperationalStateEnum.ENABLED.toString()))
                .andExpect(jsonPath("$._links")
                        .value(buildPkgmLinks(EXPECTED_VNF_PACKAGE_HREF, EXPECTED_SUBSCRIPTION_HREF)))
                .andExpect(header("Authorization", EXPECTED_BASIC_AUTHORIZATION)).andRespond(withSuccess());

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testChangeNotificationNotSentOnToVnfmCallbackUri_SubscriptionRequestNotInCache_Fail() {
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);
        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "No subscription found with subscriptionId " + SUBSCRIPTION_ID
                + ". Unable to forward notification to subscriber.";

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testChangeNotificationSentOnToVnfmCallbackUri_BadRequestResponseFromCallbackUri_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.BAD_REQUEST + ".\n" + "No result found for given url: " + CALLBACK_URI;

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testChangeNotificationSentOnToVnfmCallbackUri_NotFoundResponseFromCallbackUri_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.NOT_FOUND + ".\n" + "No result found for given url: " + CALLBACK_URI;

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testChangeNotificationSentOnToVnfmCallbackUri_InternalServerErrorResponseFromCallbackUri_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.INTERNAL_SERVER_ERROR.value() + ".\n" + "Unable to invoke HTTP POST using URL: "
                + CALLBACK_URI;

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testNotificationSentOnToVnfm_BasicAuthUserPasswordAuthorized_Success() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgOnboardingNotification notification = buildPkgOnboardingNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.id").value(NOTIFICATION_ID))
                .andExpect(jsonPath("$.notificationType")
                        .value(VnfPackageOnboardingNotification.NotificationTypeEnum.VNFPACKAGEONBOARDINGNOTIFICATION
                                .toString()))
                .andExpect(jsonPath("$.subscriptionId").value(SUBSCRIPTION_ID))
                .andExpect(jsonPath("$.timeStamp").value(TIMESTAMP.toString()))
                .andExpect(jsonPath("$.vnfPkgId").value(VNFPKG_ID.toString()))
                .andExpect(jsonPath("$.vnfdId").value(VNFD_ID.toString()))
                .andExpect(jsonPath("$._links")
                        .value(buildPkgmLinks(EXPECTED_VNF_PACKAGE_HREF, EXPECTED_SUBSCRIPTION_HREF)))
                .andExpect(header("Authorization", EXPECTED_BASIC_AUTHORIZATION)).andRespond(withSuccess());

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testNotificationSentOnToVnfm_BasicAuthUserPasswordNotAuthorized_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.BASIC);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", EXPECTED_BASIC_AUTHORIZATION))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Sending of notification to VNFM failed with response: "
                + HttpStatus.UNAUTHORIZED.value() + ".\n" + "Unable to invoke HTTP POST using URL: " + CALLBACK_URI;

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testNotificationSentOnToVnfm_OAuthAuthorized_Success() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.OAUTH2_CLIENT_CREDENTIALS);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(TOKEN_ENDPOINT)).andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", EXPECTED_BASIC_AUTHORIZATION))
                .andRespond(withSuccess(JSON_TOKEN, MediaType.APPLICATION_JSON));

        mockRestServer.expect(requestTo(CALLBACK_URI)).andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", EXPECTED_OAUTH_AUTHORIZATION))
                .andExpect(jsonPath("$.id").value(NOTIFICATION_ID))
                .andExpect(jsonPath("$.notificationType").value(
                        VnfPackageChangeNotification.NotificationTypeEnum.VNFPACKAGECHANGENOTIFICATION.toString()))
                .andExpect(jsonPath("$.subscriptionId").value(SUBSCRIPTION_ID))
                .andExpect(jsonPath("$.timeStamp").value(TIMESTAMP.toString()))
                .andExpect(jsonPath("$.vnfPkgId").value(VNFPKG_ID.toString()))
                .andExpect(jsonPath("$.vnfdId").value(VNFD_ID.toString()))
                .andExpect(
                        jsonPath("$.changeType").value(PkgChangeNotification.ChangeTypeEnum.OP_STATE_CHANGE.toString()))
                .andExpect(jsonPath("$.operationalState")
                        .value(PkgChangeNotification.OperationalStateEnum.ENABLED.toString()))
                .andExpect(jsonPath("$._links")
                        .value(buildPkgmLinks(EXPECTED_VNF_PACKAGE_HREF, EXPECTED_SUBSCRIPTION_HREF)))
                .andRespond(withSuccess());

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testNotificationSentOnToVnfm_OAuthTokenNotReceived_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.OAUTH2_CLIENT_CREDENTIALS);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);

        mockRestServer.expect(requestTo(TOKEN_ENDPOINT)).andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", EXPECTED_BASIC_AUTHORIZATION)).andRespond(withSuccess());

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Unable to retrieve OAuth Token from VNFM for notification.";

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    @Test
    public void testNotificationSentOnToVnfm_TLSCertNotYetSupported_Fail() {
        final PkgmSubscriptionRequest subscriptionRequest =
                buildPkgmSubscriptionRequest(SubscriptionsAuthentication.AuthTypeEnum.TLS_CERT);
        cache.put(SUBSCRIPTION_ID, subscriptionRequest);
        final PkgChangeNotification notification = buildPkgChangeNotification();
        final String notificationString = gson.toJson(notification);

        final ResponseEntity<?> response = sendHttpPost(notificationString);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ProblemDetails);

        final ProblemDetails problemDetails = (ProblemDetails) response.getBody();
        final String errorMessage = "An error occurred.  Authentication type "
                + subscriptionRequest.getAuthentication().getAuthType().toString() + " not currently supported.";

        assertEquals(errorMessage, problemDetails.getDetail());
    }

    private PkgOnboardingNotification buildPkgOnboardingNotification() {
        final PkgOnboardingNotification notification = new PkgOnboardingNotification();
        notification.setId(NOTIFICATION_ID);
        notification
                .setNotificationType(PkgOnboardingNotification.NotificationTypeEnum.VNFPACKAGEONBOARDINGNOTIFICATION);
        notification.setSubscriptionId(SUBSCRIPTION_ID);
        notification.setTimeStamp(TIMESTAMP);
        notification.setVnfPkgId(VNFPKG_ID);
        notification.setVnfdId(VNFD_ID);
        notification.setLinks(buildPkgmLinks());
        return notification;
    }

    private PkgChangeNotification buildPkgChangeNotification() {
        final PkgChangeNotification notification = new PkgChangeNotification();
        notification.setId(NOTIFICATION_ID);
        notification.setNotificationType(PkgChangeNotification.NotificationTypeEnum.VNFPACKAGECHANGENOTIFICATION);
        notification.setSubscriptionId(SUBSCRIPTION_ID);
        notification.setTimeStamp(TIMESTAMP);
        notification.setVnfPkgId(VNFPKG_ID);
        notification.setVnfdId(VNFD_ID);
        notification.setChangeType(PkgChangeNotification.ChangeTypeEnum.OP_STATE_CHANGE);
        notification.setOperationalState(PkgChangeNotification.OperationalStateEnum.ENABLED);
        notification.setLinks(buildPkgmLinks());
        return notification;
    }

    private PkgmLinks buildPkgmLinks() {
        return buildPkgmLinks("vnf_package_href", "subscription_href");
    }

    private PkgmLinks buildPkgmLinks(final String vnfPkgHref, final String subscriptionHref) {
        return new PkgmLinks().vnfPackage(new NOTIFICATIONLINKSERIALIZER().href(vnfPkgHref))
                .subscription(new NOTIFICATIONLINKSERIALIZER().href(subscriptionHref));
    }

    private PkgmSubscriptionRequest buildPkgmSubscriptionRequest(
            final SubscriptionsAuthentication.AuthTypeEnum authTypeEnum) {
        final PkgmSubscriptionRequest subscriptionRequest = new PkgmSubscriptionRequest();
        subscriptionRequest.setCallbackUri(CALLBACK_URI.toString());
        subscriptionRequest.setAuthentication(buildSubscriptionsAuthentication(authTypeEnum));
        return subscriptionRequest;
    }

    // TODO update for auth types other than basicAuth
    private SubscriptionsAuthentication buildSubscriptionsAuthentication(
            final SubscriptionsAuthentication.AuthTypeEnum authTypeEnum) {
        final SubscriptionsAuthentication subscriptionsAuthentication = new SubscriptionsAuthentication();
        final List<SubscriptionsAuthentication.AuthTypeEnum> authTypes = new ArrayList<>();
        authTypes.add(authTypeEnum);
        subscriptionsAuthentication.setAuthType(authTypes);
        if (authTypeEnum == SubscriptionsAuthentication.AuthTypeEnum.TLS_CERT) {
            // TODO: remove basic params and code for TLS
            final SubscriptionsAuthenticationParamsBasic basicParams =
                    new SubscriptionsAuthenticationParamsBasic().userName(USERNAME).password(PASSWORD);
            subscriptionsAuthentication.setParamsBasic(basicParams);
        } else if (authTypeEnum == SubscriptionsAuthentication.AuthTypeEnum.OAUTH2_CLIENT_CREDENTIALS) {
            final SubscriptionsAuthenticationParamsOauth2ClientCredentials oathParams =
                    new SubscriptionsAuthenticationParamsOauth2ClientCredentials().clientId(USERNAME)
                            .clientPassword(PASSWORD).tokenEndpoint(TOKEN_ENDPOINT);
            subscriptionsAuthentication.setParamsOauth2ClientCredentials(oathParams);
        } else {
            final SubscriptionsAuthenticationParamsBasic basicParams =
                    new SubscriptionsAuthenticationParamsBasic().userName(USERNAME).password(PASSWORD);
            subscriptionsAuthentication.setParamsBasic(basicParams);
        }

        return subscriptionsAuthentication;
    }

    private <T> ResponseEntity<ProblemDetails> sendHttpPost(final T notification) {
        final String testURL = LOCALHOST_URL + port + NOTIFICATION_BASE_URL;
        final HttpEntity<?> request = new HttpEntity<>(notification, basicHttpHeadersProvider.getHttpHeaders());
        return testRestTemplate.withBasicAuth("test", "test").exchange(testURL, HttpMethod.POST, request,
                ProblemDetails.class);
    }

    private ResponseEntity<Void> sendHttpGet(final String url) {
        final String testURL = LOCALHOST_URL + port + url;
        final HttpEntity<?> request = new HttpEntity<>(basicHttpHeadersProvider.getHttpHeaders());
        return testRestTemplate.withBasicAuth("test", "test").exchange(testURL, HttpMethod.GET, request, Void.class);
    }

}
