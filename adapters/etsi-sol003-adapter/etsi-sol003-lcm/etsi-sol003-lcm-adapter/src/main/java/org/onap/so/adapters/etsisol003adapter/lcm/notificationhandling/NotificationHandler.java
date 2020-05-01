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

package org.onap.so.adapters.etsisol003adapter.lcm.notificationhandling;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.AaiHelper;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.AaiServiceProvider;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.OamIpAddressSource;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.OamIpAddressSource.OamIpAddressType;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.VnfmServiceProvider;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InlineResponse201VimConnectionInfo;
import org.onap.so.adapters.etsisol003adapter.lcm.jobmanagement.JobManager;
import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs;
import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs.ChangeTypeEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification.OperationStateEnum;
import org.slf4j.Logger;

/**
 * Performs updates to AAI based on a received notification. The updates are executed in a separate thread so as the
 * notification response to the VNFM is not delayed.
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
        final GenericVnf genericVnf = aaiServiceProvider
                .invokeQueryGenericVnf(vnfInstance.getLinks().getSelf().getHref()).getGenericVnf().get(0);

        final GenericVnf genericVnfPatch = new GenericVnf();
        genericVnfPatch.setVnfId(genericVnf.getVnfId());
        setOamIpAddress(genericVnfPatch, vnfInstance);
        genericVnfPatch.setOrchestrationStatus("Created");
        aaiServiceProvider.invokePatchGenericVnf(genericVnfPatch);

        addVservers(vnfLcmOperationOccurrenceNotification, genericVnf.getVnfId(), vnfInstance.getVimConnectionInfo());

        logger.debug("Finished handling notification for vnfm: " + vnfInstance.getId());
    }

    private void setOamIpAddress(final GenericVnf genericVnf, final InlineResponse201 vnfInstance) {
        final OamIpAddressSource oamIpAddressSource = aaiHelper.getOamIpAddressSource(vnfInstance.getId());
        if (oamIpAddressSource == null) {
            logger.warn("No source indicated for OAM IP address, no value will be set in AAI");
            return;
        }
        if (oamIpAddressSource.getType().equals(OamIpAddressType.LITERAL)) {
            genericVnf.setIpv4OamAddress(oamIpAddressSource.getValue());
        } else {
            try {
                logger.debug("ConfigurableProperties: " + vnfInstance.getVnfConfigurableProperties());
                if (vnfInstance.getVnfConfigurableProperties() == null) {
                    logger.warn("No ConfigurableProperties, cannot set OAM IP Address");
                }
                final JSONObject properties = new JSONObject((Map) vnfInstance.getVnfConfigurableProperties());
                genericVnf.setIpv4OamAddress(properties.get(oamIpAddressSource.getValue()).toString());
            } catch (final JSONException jsonException) {
                logger.error("Error getting vnfIpAddress", jsonException);
            }
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
        try {
            final GenericVnf genericVnf = aaiServiceProvider
                    .invokeQueryGenericVnf(vnfInstance.getLinks().getSelf().getHref()).getGenericVnf().get(0);
            deleteVserversFromAai(vnfLcmOperationOccurrenceNotification, genericVnf);
        } finally {
            jobManager.notificationProcessedForOperation(vnfLcmOperationOccurrenceNotification.getVnfLcmOpOccId(),
                    false);
        }
    }

    private void handleVnfTerminateCompleted() {
        GenericVnf genericVnf = null;
        boolean vServersDeletedFromAai = false;
        boolean identifierDeletedFromVnfm = false;
        boolean genericVnfUpdated = false;
        try {
            genericVnf = aaiServiceProvider.invokeQueryGenericVnf(vnfInstance.getLinks().getSelf().getHref())
                    .getGenericVnf().get(0);
            vServersDeletedFromAai = deleteVserversFromAai(vnfLcmOperationOccurrenceNotification, genericVnf);
            identifierDeletedFromVnfm = deleteVnfIdentifierOnVnfm(genericVnf);
            genericVnfUpdated = patchVnfInAai(genericVnf.getVnfId(), "Assigned", identifierDeletedFromVnfm ? "" : null);
        } finally {
            jobManager.notificationProcessedForOperation(vnfLcmOperationOccurrenceNotification.getVnfLcmOpOccId(),
                    vServersDeletedFromAai && identifierDeletedFromVnfm && genericVnfUpdated);
            jobManager.vnfDeleted(vnfLcmOperationOccurrenceNotification.getVnfLcmOpOccId());
        }
    }

    private void addVservers(final VnfLcmOperationOccurrenceNotification notification, final String vnfId,
            final List<InlineResponse201VimConnectionInfo> vnfInstancesVimConnectionInfo) {
        final Map<String, InlineResponse201VimConnectionInfo> vimConnectionIdToVimConnectionInfo = new HashMap<>();
        for (final InlineResponse201VimConnectionInfo vimConnectionInfo : vnfInstancesVimConnectionInfo) {
            vimConnectionIdToVimConnectionInfo.put(vimConnectionInfo.getId(), vimConnectionInfo);
        }

        for (final LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs vnfc : notification.getAffectedVnfcs()) {
            final InlineResponse201VimConnectionInfo vimConnectionInfo =
                    getVimConnectionInfo(vimConnectionIdToVimConnectionInfo, vnfc);
            if (ChangeTypeEnum.ADDED.equals(vnfc.getChangeType())) {
                final Vserver vserver = aaiHelper.createVserver(vnfc);
                aaiServiceProvider.invokePutVserver(getCloudOwner(vimConnectionInfo), getCloudRegion(vimConnectionInfo),
                        getTenant(vimConnectionInfo), vserver);

                aaiServiceProvider.invokePutVserverToVnfRelationship(getCloudOwner(vimConnectionInfo),
                        getCloudRegion(vimConnectionInfo), getTenant(vimConnectionInfo), vserver, vnfId);
            }
        }
    }

    private boolean deleteVserversFromAai(final VnfLcmOperationOccurrenceNotification notification,
            final GenericVnf vnf) {
        try {
            for (final LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs vnfc : notification.getAffectedVnfcs()) {
                if (ChangeTypeEnum.REMOVED.equals(vnfc.getChangeType())) {

                    final Relationship relationshipToVserver = aaiHelper.deleteRelationshipWithDataValue(vnf, "vserver",
                            "vserver.vserver-id", vnfc.getComputeResource().getResourceId());

                    aaiServiceProvider.invokeDeleteVserver(
                            aaiHelper.getRelationshipData(relationshipToVserver, "cloud-region.cloud-owner"),
                            aaiHelper.getRelationshipData(relationshipToVserver, "cloud-region.cloud-region-id"),
                            aaiHelper.getRelationshipData(relationshipToVserver, "tenant.tenant-id"),
                            vnfc.getComputeResource().getResourceId());
                }
            }
            return true;
        } catch (final Exception exception) {
            logger.error(
                    "Error encountered deleting vservers based on received notification, AAI may not be updated correctly "
                            + vnfLcmOperationOccurrenceNotification,
                    exception);
            return false;
        }
    }

    private boolean deleteVnfIdentifierOnVnfm(final GenericVnf genericVnf) {
        try {
            vnfmServiceProvider.deleteVnf(aaiHelper.getAssignedVnfm(genericVnf), genericVnf.getSelflink());
            return true;
        } catch (final Exception exception) {
            logger.error("Exception deleting the identifier " + genericVnf.getSelflink()
                    + " from the VNFM. The VNF has been terminated successfully but the identifier will remain on the VNFM.",
                    exception);
            return false;
        }
    }

    private boolean patchVnfInAai(final String vnfId, final String orchestrationStatus, final String selfLink) {
        try {
            final GenericVnf genericVnfPatch = new GenericVnf();
            genericVnfPatch.setVnfId(vnfId);
            genericVnfPatch.setOrchestrationStatus(orchestrationStatus);
            if (selfLink != null) {
                genericVnfPatch.setSelflink(selfLink);
            }
            aaiServiceProvider.invokePatchGenericVnf(genericVnfPatch);
            return true;
        } catch (final Exception exception) {
            logger.error(
                    "Error encountered setting orchestration status and/or self link based on received notification, AAI may not be updated correctly "
                            + vnfLcmOperationOccurrenceNotification,
                    exception);
            return false;
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
