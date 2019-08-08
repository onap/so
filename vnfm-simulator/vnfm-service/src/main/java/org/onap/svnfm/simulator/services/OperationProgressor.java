package org.onap.svnfm.simulator.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.modelmapper.ModelMapper;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.ApiResponse;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsLinks;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsLinksVnfLcmOpOcc;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.ApiClient;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.ApiException;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.api.DefaultApi;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs.ChangeTypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationLinks;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification.NotificationStatusEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification.NotificationTypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification.OperationEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification.OperationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoVnfcResourceInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthenticationParamsBasic;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthenticationParamsOauth2ClientCredentials;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.repository.VnfOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public abstract class OperationProgressor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationProgressor.class);
    private static final String CERTIFICATE_TO_TRUST = "so-vnfm-adapter.crt.pem";

    private Resource keyStoreResource = new ClassPathResource("so-vnfm-simulator.p12");
    private String keyStorePassword = "7Em3&j4.19xYiMelhD5?xbQ.";

    protected final VnfOperation operation;
    protected final SvnfmService svnfmService;
    private final VnfOperationRepository vnfOperationRepository;
    private final ApplicationConfig applicationConfig;
    protected final Vnfds vnfds;
    private final SubscriptionService subscriptionService;
    private final DefaultApi notificationClient;
    private final org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.api.DefaultApi grantClient;

    public OperationProgressor(final VnfOperation operation, final SvnfmService svnfmService,
            final VnfOperationRepository vnfOperationRepository, final ApplicationConfig applicationConfig,
            final Vnfds vnfds, final SubscriptionService subscriptionService) {
        this.operation = operation;
        this.svnfmService = svnfmService;
        this.vnfOperationRepository = vnfOperationRepository;
        this.applicationConfig = applicationConfig;
        this.vnfds = vnfds;
        this.subscriptionService = subscriptionService;

        final ApiClient apiClient = new ApiClient();
        String callBackUrl = subscriptionService.getSubscriptions().iterator().next().getCallbackUri();
        callBackUrl = callBackUrl.substring(0, callBackUrl.indexOf("/lcn/"));
        apiClient.setBasePath(callBackUrl);
        apiClient.setKeyManagers(getKeyManagers());
        apiClient.setSslCaCert(getCertificateToTrust());
        notificationClient = new DefaultApi(apiClient);

        final org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.ApiClient grantApiClient =
                new org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.ApiClient();
        grantApiClient.setBasePath(callBackUrl);
        grantApiClient.setKeyManagers(getKeyManagers());
        grantApiClient.setSslCaCert(getCertificateToTrust());
        grantClient = new org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.api.DefaultApi(grantApiClient);
    }

    private InputStream getCertificateToTrust() {
        try {
            return new ClassPathResource(CERTIFICATE_TO_TRUST).getInputStream();
        } catch (final IOException exception) {
            LOGGER.error("Error reading certificate to trust, https calls to VNFM adapter will fail", exception);
            return null;
        }
    }

    private KeyManager[] getKeyManagers() {
        KeyStore keystore;
        try {
            keystore = KeyStore.getInstance("pkcs12");
            keystore.load(keyStoreResource.getInputStream(), keyStorePassword.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, keyStorePassword.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
                | UnrecoverableKeyException exception) {
            LOGGER.error("Error reading certificate, https calls using two way TLS to VNFM adapter will fail",
                    exception);
            return new KeyManager[0];
        }
    }

    @Override
    public void run() {
        try {
            final VnfLcmOperationOccurrenceNotification notificationOfStarting =
                    buildNotification(NotificationStatusEnum.START, OperationStateEnum.STARTING);
            sendNotification(notificationOfStarting);

            sleep(2000);
            setState(InlineResponse200.OperationStateEnum.PROCESSING);
            final VnfLcmOperationOccurrenceNotification notificationOfProcessing =
                    buildNotification(NotificationStatusEnum.START, OperationStateEnum.PROCESSING);
            sendNotification(notificationOfProcessing);


            final GrantRequest grantRequest = buildGrantRequest();
            final InlineResponse201 grantResponse = sendGrantRequest(grantRequest);
            final List<InlineResponse201InstantiatedVnfInfoVnfcResourceInfo> vnfcs = handleGrantResponse(grantResponse);

            svnfmService.getVnf(operation.getVnfInstanceId()).getInstantiatedVnfInfo();

            sleep(10000);
            setState(InlineResponse200.OperationStateEnum.COMPLETED);
            final VnfLcmOperationOccurrenceNotification notificationOfCompleted =
                    buildNotification(NotificationStatusEnum.RESULT, OperationStateEnum.COMPLETED);
            notificationOfCompleted.setAffectedVnfcs(getVnfcs(vnfcs));

            sendNotification(notificationOfCompleted);
        } catch (final Exception exception) {
            LOGGER.error("Error in OperationProgressor ", exception);
        }

    }

    private void sleep(final long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (final InterruptedException e) {
            operation.setOperationState(InlineResponse200.OperationStateEnum.FAILED);
            // Restore interrupted state
            Thread.currentThread().interrupt();
        }
    }

    private void setState(final InlineResponse200.OperationStateEnum state) {
        LOGGER.info("Setting state to {} for operation {}", state, operation.getId());
        operation.setOperationState(state);
        vnfOperationRepository.save(operation);
    }

    private VnfLcmOperationOccurrenceNotification buildNotification(final NotificationStatusEnum status,
            final OperationStateEnum operationState) {
        final VnfLcmOperationOccurrenceNotification notification = new VnfLcmOperationOccurrenceNotification();
        notification.setId(UUID.randomUUID().toString());
        notification.setNotificationType(NotificationTypeEnum.VNFLCMOPERATIONOCCURRENCENOTIFICATION);
        notification.setNotificationStatus(status);
        notification.setOperationState(operationState);
        notification.setOperation(OperationEnum.fromValue(operation.getOperation().toString()));
        notification.setVnfInstanceId(operation.getVnfInstanceId());
        notification.setVnfLcmOpOccId(operation.getId());

        final LcnVnfLcmOperationOccurrenceNotificationLinks links = new LcnVnfLcmOperationOccurrenceNotificationLinks();
        final LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance vnfInstanceLink =
                new LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance();
        vnfInstanceLink.setHref(getVnfLink());
        links.setVnfInstance(vnfInstanceLink);


        final LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance operationLink =
                new LcnVnfLcmOperationOccurrenceNotificationLinksVnfInstance();
        operationLink.setHref(getOperationLink());
        links.setVnfLcmOpOcc(operationLink);

        notification.setLinks(links);

        return notification;
    }

    private List<LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs> getVnfcs(
            final List<InlineResponse201InstantiatedVnfInfoVnfcResourceInfo> instantiatedVnfcs) {
        final List<LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs> vnfcs = new ArrayList<>();
        if (instantiatedVnfcs != null) {
            for (final InlineResponse201InstantiatedVnfInfoVnfcResourceInfo instantiatedVnfc : instantiatedVnfcs) {
                LOGGER.info("VNFC TO BE CONVERTED: {}", instantiatedVnfc);
                final ModelMapper mapper = new ModelMapper();
                final LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs vnfc =
                        mapper.map(instantiatedVnfc, LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs.class);
                LOGGER.info("VNFC FROM CONVERSION: {}", vnfc);
                vnfc.setChangeType(getVnfcChangeType());
                vnfcs.add(vnfc);
            }
        }
        return vnfcs;
    }

    private void sendNotification(final VnfLcmOperationOccurrenceNotification notification) {
        LOGGER.info("Sending notification: {}", notification);
        try {
            final SubscriptionsAuthenticationParamsBasic subscriptionAuthentication =
                    subscriptionService.getSubscriptions().iterator().next().getAuthentication().getParamsBasic();
            final String auth =
                    subscriptionAuthentication.getUserName() + ":" + subscriptionAuthentication.getPassword();
            final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);

            notificationClient.lcnVnfLcmOperationOccurrenceNotificationPostWithHttpInfo(notification,
                    MediaType.APPLICATION_JSON, authHeader);
        } catch (final ApiException exception) {
            LOGGER.error("Error sending notification: " + notification, exception);
            LOGGER.error("Response code: {}, body: {}, basePath: {}", exception.getCode(), exception.getResponseBody(),
                    notificationClient.getApiClient().getBasePath());

        }
    }


    public GrantRequest buildGrantRequest() {
        final GrantRequest grantRequest = new GrantRequest();
        grantRequest.setVnfInstanceId(operation.getVnfInstanceId());
        final String vnfdId = svnfmService.getVnf(operation.getVnfInstanceId()).getVnfdId();
        grantRequest.setVnfdId(vnfdId);
        grantRequest.setAddResources(getAddResources(vnfdId));
        grantRequest.setRemoveResources(getRemoveResources(vnfdId));
        grantRequest.setVnfLcmOpOccId(operation.getId());
        grantRequest
                .setOperation(org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantRequest.OperationEnum
                        .fromValue(operation.getOperation().getValue()));
        grantRequest.setIsAutomaticInvocation(false);

        final GrantsLinksVnfLcmOpOcc vnfInstanceLink = new GrantsLinksVnfLcmOpOcc();
        vnfInstanceLink.setHref(getVnfLink());
        final GrantsLinksVnfLcmOpOcc operationInstanceLink = new GrantsLinksVnfLcmOpOcc();
        operationInstanceLink.setHref(getOperationLink());
        final GrantsLinks links = new GrantsLinks();
        links.setVnfInstance(vnfInstanceLink);
        links.setVnfLcmOpOcc(operationInstanceLink);
        grantRequest.setLinks(links);
        return grantRequest;
    }

    protected abstract List<GrantsAddResources> getAddResources(final String vnfdId);

    protected abstract List<GrantsAddResources> getRemoveResources(final String vnfdId);

    protected abstract List<InlineResponse201InstantiatedVnfInfoVnfcResourceInfo> handleGrantResponse(
            InlineResponse201 grantResponse);

    protected abstract ChangeTypeEnum getVnfcChangeType();

    private InlineResponse201 sendGrantRequest(final GrantRequest grantRequest) {
        LOGGER.info("Sending grant request: {}", grantRequest);
        try {

            final SubscriptionsAuthenticationParamsOauth2ClientCredentials subscriptionAuthentication =
                    subscriptionService.getSubscriptions().iterator().next().getAuthentication()
                            .getParamsOauth2ClientCredentials();

            final String authHeader = applicationConfig.getGrantAuth().equals("oauth")
                    ? "Bearer " + getToken(notificationClient.getApiClient(), subscriptionAuthentication)
                    : null;

            final ApiResponse<InlineResponse201> response = grantClient.grantsPostWithHttpInfo(grantRequest,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, authHeader);
            LOGGER.info("Grant Response: {}", response);
            return response.getData();
        } catch (final org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.ApiException exception) {
            LOGGER.error("Error sending notification: " + grantRequest, exception);
            return null;
        }
    }

    private String getVnfLink() {
        return getLinkBaseUrl() + "/vnf_instances/" + operation.getVnfInstanceId();
    }

    private String getOperationLink() {
        return getLinkBaseUrl() + "/vnf_lcm_op_occs/" + operation.getId();
    }

    private String getLinkBaseUrl() {
        return applicationConfig.getBaseUrl() + "/vnflcm/v1";
    }

    private String getToken(final ApiClient apiClient,
            final SubscriptionsAuthenticationParamsOauth2ClientCredentials oauthClientCredentials) {
        final String basePath = apiClient.getBasePath().substring(0, apiClient.getBasePath().indexOf("/so/"));
        final String tokenUrl = basePath + "/oauth/token?grant_type=client_credentials";

        try {
            URL url = new URL(tokenUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            final String authorizationHeader = getAuthorizationHeader(oauthClientCredentials);
            connection.addRequestProperty("Authorization", authorizationHeader);

            connection.connect();

            return getResponse(connection).get("access_token").getAsString();

        } catch (IOException exception) {
            LOGGER.error("Error getting token", exception);
            return null;
        }
    }

    private String getAuthorizationHeader(
            final SubscriptionsAuthenticationParamsOauth2ClientCredentials oauthClientCredentials) {
        final String auth = oauthClientCredentials.getClientId() + ":" + oauthClientCredentials.getClientPassword();
        final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private JsonObject getResponse(HttpsURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line, data = "";
        while ((line = in.readLine()) != null) {
            data += line;
        }
        in.close();
        connection.getInputStream().close();

        JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
        return jsonObject;
    }

}
