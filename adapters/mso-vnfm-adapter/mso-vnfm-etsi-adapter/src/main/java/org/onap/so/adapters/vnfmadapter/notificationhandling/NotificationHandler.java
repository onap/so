/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.vnfmadapter.notificationhandling;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.adapters.vnfmadapter.extclients.aai.AaiHelper;
import org.onap.so.adapters.vnfmadapter.extclients.aai.AaiServiceProvider;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.VnfmServiceProvider;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification.OperationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201VimConnectionInfo;
import org.onap.so.adapters.vnfmadapter.jobmanagement.JobManager;
import org.slf4j.Logger;

/**
 * Performs updates to AAI based on a received notification. The updates are executed in a separate
 * thread so as the notification response to the VNFM is not delayed.
 */
public class NotificationHandler implements Runnable {
    private static Logger logger = getLogger(NotificationHandler.class);
    private final VnfLcmOperationOccurrenceNotification vnfLcmOperationOccurrenceNotification;
    private final AaiHelper aaiHelper;
    private final AaiServiceProvider aaiServiceProvider;
    private final VnfmServiceProvider vnfmServiceProvider;
    private final JobManager jobManager;
    private final InlineResponse201 vnfInstance;

    public NotificationHandler(final VnfLcmOperationOccurrenceNotification vnfLcmOperationOccurrenceNotification,
            final AaiHelper aaiHelper, final AaiServiceProvider aaiServiceProvider,
            final VnfmServiceProvider vnfmServiceProvider, final JobManager jobManager,
            final InlineResponse201 vnfInstance) {
        this.vnfLcmOperationOccurrenceNotification = vnfLcmOperationOccurrenceNotification;
        this.aaiHelper = aaiHelper;
        this.aaiServiceProvider = aaiServiceProvider;
        this.vnfmServiceProvider = vnfmServiceProvider;
        this.jobManager = jobManager;
        this.vnfInstance = vnfInstance;
    }

    @Override
    public void run() {
        try {
            if (vnfLcmOperationOccurrenceNotification.getOperationState().equals(OperationStateEnum.COMPLETED)) {
                switch (vnfLcmOperationOccurrenceNotification.getOperation()) {
                    case INSTANTIATE:
                        handleVnfInstantiate();
                        break;
                    case TERMINATE:
                        handleVnfTerminate();
                        break;
                    default:
                }
            }
        } catch (final Exception exception) {
            logger.error("Error encountered handling notification, AAI may not be updated correctly "
                    + vnfLcmOperationOccurrenceNotification, exception);
        }
    }

    private void handleVnfInstantiate() {
        if (vnfLcmOperationOccurrenceNotification.getOperationState().equals(OperationStateEnum.COMPLETED)) {
            handleVnfInstantiateCompleted();
        }
    }

    private void handleVnfInstantiateCompleted() {
        final GenericVnf genericVnf =
                aaiServiceProvider.invokeQueryGenericVnf(vnfInstance.getLinks().getSelf().getHref()).get(0);
        final String ipAddress = getOamIpAddress(vnfInstance);
        logger.debug("Updating " + genericVnf.getVnfId() + " with VNF OAM IP ADDRESS: " + ipAddress);
        genericVnf.setIpv4OamAddress(ipAddress);
        genericVnf.setOrchestrationStatus("Created");

        aaiServiceProvider.invokePutGenericVnf(genericVnf);

        updateVservers(vnfLcmOperationOccurrenceNotification, genericVnf.getVnfId(),
                vnfInstance.getVimConnectionInfo());

        logger.debug("Finished handling notification for vnfm: " + vnfInstance.getId());
    }

    private String getOamIpAddress(final InlineResponse201 vnfInstance) {
        try {
            logger.debug("ConfigurableProperties: " + vnfInstance.getVnfConfigurableProperties());
            if (vnfInstance.getVnfConfigurableProperties() == null) {
                logger.warn("No ConfigurableProperties, cannot set OAM IP Address");
                return null;
            }
            final JSONObject properties = new JSONObject((Map) vnfInstance.getVnfConfigurableProperties());
            return properties.get("vnfIpAddress").toString();
        } catch (final JSONException jsonException) {
            logger.error("Error getting vnfIpAddress", jsonException);
            return null;
        }
    }

    private void handleVnfTerminate() {
        switch (vnfLcmOperationOccurrenceNotification.getOperationState()) {
            case COMPLETED:
                handleVnfTerminateCompleted();
                break;
            case FAILED:
            case ROLLING_BACK:
                handleVnfTerminateFailed();
                break;
            default:
        }
    }

    private void handleVnfTerminateFailed() {
        final GenericVnf genericVnf =
                aaiServiceProvider.invokeQueryGenericVnf(vnfInstance.getLinks().getSelf().getHref()).get(0);
        updateVservers(vnfLcmOperationOccurrenceNotification, genericVnf.getVnfId(),
                vnfInstance.getVimConnectionInfo());
        jobManager.notificationProcessedForOperation(vnfLcmOperationOccurrenceNotification.getId(), false);
    }

    private void handleVnfTerminateCompleted() {
        final GenericVnf genericVnf =
                aaiServiceProvider.invokeQueryGenericVnf(vnfInstance.getLinks().getSelf().getHref()).get(0);
        updateVservers(vnfLcmOperationOccurrenceNotification, genericVnf.getVnfId(),
                vnfInstance.getVimConnectionInfo());

        boolean deleteSuccessful = false;
        try {
            vnfmServiceProvider.deleteVnf(genericVnf.getSelflink());
            deleteSuccessful = true;
        } finally {
            jobManager.notificationProcessedForOperation(vnfLcmOperationOccurrenceNotification.getId(),
                    deleteSuccessful);
            genericVnf.setOrchestrationStatus("Assigned");
            aaiServiceProvider.invokePutGenericVnf(genericVnf);
        }
    }

    private void updateVservers(final VnfLcmOperationOccurrenceNotification notification, final String vnfId,
            final List<InlineResponse201VimConnectionInfo> vnfInstancesVimConnectionInfo) {
        final Map<String, InlineResponse201VimConnectionInfo> vimConnectionIdToVimConnectionInfo = new HashMap<>();
        for (final InlineResponse201VimConnectionInfo vimConnectionInfo : vnfInstancesVimConnectionInfo) {
            vimConnectionIdToVimConnectionInfo.put(vimConnectionInfo.getId(), vimConnectionInfo);
        }

        for (final LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs vnfc : notification.getAffectedVnfcs()) {
            final InlineResponse201VimConnectionInfo vimConnectionInfo =
                    getVimConnectionInfo(vimConnectionIdToVimConnectionInfo, vnfc);
            switch (vnfc.getChangeType()) {
                case ADDED:
                    final Vserver vserver = aaiHelper.createVserver(vnfc);
                    aaiHelper.addRelationshipFromVserverVnfToGenericVnf(vserver, vnfId);

                    aaiServiceProvider.invokePutVserver(getCloudOwner(vimConnectionInfo),
                            getCloudRegion(vimConnectionInfo), getTenant(vimConnectionInfo), vserver);
                    break;
                case REMOVED:
                    aaiServiceProvider.invokeDeleteVserver(getCloudOwner(vimConnectionInfo),
                            getCloudRegion(vimConnectionInfo), getTenant(vimConnectionInfo),
                            vnfc.getComputeResource().getResourceId());
                    break;
                case MODIFIED:
                case TEMPORARY:
                default:
            }
        }
    }

    private InlineResponse201VimConnectionInfo getVimConnectionInfo(
            final Map<String, InlineResponse201VimConnectionInfo> vimConnectionIdToVimConnectionInfo,
            final LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs vnfc) {
        final String vimConnectionId = vnfc.getComputeResource().getVimConnectionId();
        return vimConnectionIdToVimConnectionInfo.get(vimConnectionId);
    }

    private String getCloudOwner(final InlineResponse201VimConnectionInfo vimConnectionInfo) {
        final String vimId = vimConnectionInfo.getVimId();
        return vimId.substring(0, vimId.indexOf("_"));
    }

    private String getCloudRegion(final InlineResponse201VimConnectionInfo vimConnectionInfo) {
        final String vimId = vimConnectionInfo.getVimId();
        return vimId.substring(vimId.indexOf("_") + 1);
    }

    private String getTenant(final InlineResponse201VimConnectionInfo vimConnectionInfo) {
        final JSONObject vimConnectionJsonObject = new JSONObject(vimConnectionInfo);
        return vimConnectionJsonObject.getJSONObject("accessInfo").get("projectId").toString();
    }

}
