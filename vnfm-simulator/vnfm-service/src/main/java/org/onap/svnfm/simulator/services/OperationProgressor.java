package org.onap.svnfm.simulator.services;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.repository.VnfOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OperationProgressor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationProgressor.class);
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
        notificationClient = new DefaultApi(apiClient);

        final org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.ApiClient grantApiClient =
                new org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.ApiClient();
        grantApiClient.setBasePath(callBackUrl);
        grantClient = new org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.api.DefaultApi(grantApiClient);
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
            final String authHeader = "Basic " + new String(encodedAuth);
            notificationClient.lcnVnfLcmOperationOccurrenceNotificationPostWithHttpInfo(notification,
                    MediaType.APPLICATION_JSON, authHeader);
        } catch (final ApiException exception) {
            LOGGER.error("Error sending notification: " + notification, exception);
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
            final ApiResponse<InlineResponse201> response = grantClient.grantsPostWithHttpInfo(grantRequest,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, "Basic dm5mbTpwYXNzd29yZDEk");
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

}
