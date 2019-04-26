package org.onap.svnfm.simulator.services;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.modelmapper.ModelMapper;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.ApiResponse;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsAddResources.TypeEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsLinks;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.GrantsLinksVnfLcmOpOcc;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201AddResources;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.model.InlineResponse201VimConnections;
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
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoResourceHandle;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201InstantiatedVnfInfoVnfcResourceInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201VimConnectionInfo;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.SubscriptionsAuthenticationParamsBasic;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.model.Vnfds.Vnfc;
import org.onap.svnfm.simulator.model.Vnfds.Vnfd;
import org.onap.svnfm.simulator.repository.VnfOperationRepository;
import org.onap.svnfm.simulator.repository.VnfmCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationProgressor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationProgressor.class);
    private final VnfOperation operation;
    private final VnfmCacheRepository vnfRepository;
    private final VnfOperationRepository vnfOperationRepository;
    private final ApplicationConfig applicationConfig;
    private final Vnfds vnfds;
    private final SubscriptionService subscriptionService;
    private final DefaultApi notificationClient;
    private final org.onap.so.adapters.vnfmadapter.extclients.vnfm.grant.api.DefaultApi grantClient;

    public OperationProgressor(final VnfOperation operation, final VnfmCacheRepository vnfRepository,
            final VnfOperationRepository vnfOperationRepository, final ApplicationConfig applicationConfig,
            final Vnfds vnfds, final SubscriptionService subscriptionService) {
        this.operation = operation;
        this.vnfRepository = vnfRepository;
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
            final InlineResponse201InstantiatedVnfInfo instantiatedVnfInfo = createInstantiatedVnfInfo(grantResponse);
            vnfRepository.updateVnf(instantiatedVnfInfo, operation.getVnfInstanceId(),
                    getVimConnections(grantResponse));

            sleep(10000);
            setState(InlineResponse200.OperationStateEnum.COMPLETED);
            final VnfLcmOperationOccurrenceNotification notificationOfCompleted =
                    buildNotification(NotificationStatusEnum.RESULT, OperationStateEnum.COMPLETED);
            notificationOfCompleted.setAffectedVnfcs(getVnfcs(instantiatedVnfInfo.getVnfcResourceInfo()));

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
        for (final InlineResponse201InstantiatedVnfInfoVnfcResourceInfo instantiatedVnfc : instantiatedVnfcs) {
            LOGGER.info("VNFC TO BE CONVERTED: {}", instantiatedVnfc);
            final ModelMapper mapper = new ModelMapper();
            final LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs vnfc =
                    mapper.map(instantiatedVnfc, LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs.class);
            LOGGER.info("VNFC FROM CONVERSION: {}", vnfc);
            vnfc.setChangeType(ChangeTypeEnum.ADDED);
            vnfcs.add(vnfc);
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
        final String vnfdId = vnfRepository.getVnf(operation.getVnfInstanceId()).getVnfdId();
        grantRequest.setVnfdId(vnfdId);
        grantRequest.setAddResources(getAddResources(vnfdId));
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

    private List<GrantsAddResources> getAddResources(final String vnfdId) {
        final List<GrantsAddResources> resources = new ArrayList<>();

        for (final Vnfd vnfd : vnfds.getVnfdList()) {
            if (vnfd.getVnfdId().equals(vnfdId)) {
                for (final Vnfc vnfc : vnfd.getVnfcList()) {
                    final GrantsAddResources addResource = new GrantsAddResources();
                    vnfc.setGrantResourceId(UUID.randomUUID().toString());
                    addResource.setId(vnfc.getGrantResourceId());
                    addResource.setType(TypeEnum.fromValue(vnfc.getType()));
                    addResource.setResourceTemplateId(vnfc.getResourceTemplateId());
                    addResource.setVduId(vnfc.getVduId());
                    resources.add(addResource);
                }
            }
        }
        return resources;
    }

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

    private InlineResponse201InstantiatedVnfInfo createInstantiatedVnfInfo(final InlineResponse201 grantResponse) {
        final InlineResponse201InstantiatedVnfInfo instantiatedVnfInfo = new InlineResponse201InstantiatedVnfInfo();

        final Map<String, String> mapOfGrantResourceIdToVimConnectionId = new HashMap<>();
        for (final InlineResponse201AddResources addResource : grantResponse.getAddResources()) {
            mapOfGrantResourceIdToVimConnectionId.put(addResource.getResourceDefinitionId(),
                    addResource.getVimConnectionId());
        }

        for (final Vnfd vnfd : vnfds.getVnfdList()) {
            if (vnfd.getVnfdId().equals(vnfRepository.getVnf(operation.getVnfInstanceId()).getVnfdId())) {
                for (final Vnfc vnfc : vnfd.getVnfcList()) {
                    final InlineResponse201InstantiatedVnfInfoVnfcResourceInfo vnfcResourceInfoItem =
                            new InlineResponse201InstantiatedVnfInfoVnfcResourceInfo();
                    vnfcResourceInfoItem.setId(vnfc.getVnfcId());
                    vnfcResourceInfoItem.setVduId(vnfc.getVduId());
                    final InlineResponse201InstantiatedVnfInfoResourceHandle computeResource =
                            new InlineResponse201InstantiatedVnfInfoResourceHandle();
                    computeResource.setResourceId(UUID.randomUUID().toString());
                    computeResource
                            .setVimConnectionId(mapOfGrantResourceIdToVimConnectionId.get(vnfc.getGrantResourceId()));
                    computeResource.setVimLevelResourceType("OS::Nova::Server");
                    vnfcResourceInfoItem.setComputeResource(computeResource);
                    instantiatedVnfInfo.addVnfcResourceInfoItem(vnfcResourceInfoItem);
                }
            }
        }

        return instantiatedVnfInfo;
    }

    private List<InlineResponse201VimConnectionInfo> getVimConnections(final InlineResponse201 grantResponse) {
        final List<InlineResponse201VimConnectionInfo> vimConnectionInfo = new ArrayList<>();
        for (final InlineResponse201VimConnections vimConnection : grantResponse.getVimConnections()) {
            final ModelMapper modelMapper = new ModelMapper();
            vimConnectionInfo.add(modelMapper.map(vimConnection, InlineResponse201VimConnectionInfo.class));
        }
        return vimConnectionInfo;
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
