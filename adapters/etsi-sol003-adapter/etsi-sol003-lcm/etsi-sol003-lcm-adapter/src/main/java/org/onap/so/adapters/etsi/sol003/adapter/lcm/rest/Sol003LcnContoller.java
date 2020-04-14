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

package org.onap.so.adapters.etsi.sol003.adapter.lcm.rest;

import static org.onap.so.adapters.etsi.sol003.adapter.common.CommonConstants.BASE_URL;
import static org.onap.so.adapters.etsi.sol003.adapter.lcm.LifeCycleManagementConstants.OPERATION_NOTIFICATION_ENDPOINT;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.core.MediaType;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.aai.AaiHelper;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.aai.AaiServiceProvider;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.vnfm.VnfmServiceProvider;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.jobmanagement.JobManager;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.VnfIdentifierCreationNotification;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.VnfIdentifierDeletionNotification;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification.OperationEnum;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification.OperationStateEnum;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.notificationhandling.NotificationHandler;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.vnfm.model.InlineResponse201;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling notifications from the VNFM (Virtual Network Function Manager).
 */
@Controller
@RequestMapping(value = BASE_URL, produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
        consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class Sol003LcnContoller {
    private static Logger logger = getLogger(Sol003LcnContoller.class);
    private static final String LOG_LCN_RECEIVED = "LCN received from VNFM: ";
    private final AaiServiceProvider aaiServiceProvider;
    private final AaiHelper aaiHelper;
    private final VnfmServiceProvider vnfmServiceProvider;
    private final JobManager jobManager;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    Sol003LcnContoller(final AaiServiceProvider aaiServiceProvider, final AaiHelper aaiHelper,
            final VnfmServiceProvider vnfmServiceProvider, final JobManager jobManager) {
        this.aaiServiceProvider = aaiServiceProvider;
        this.aaiHelper = aaiHelper;
        this.vnfmServiceProvider = vnfmServiceProvider;
        this.jobManager = jobManager;
    }

    @PostMapping(value = "/lcn/VnfIdentifierCreationNotification")
    public ResponseEntity<Void> lcnVnfIdentifierCreationNotificationPost(
            @RequestBody final VnfIdentifierCreationNotification vnfIdentifierCreationNotification) {
        logger.info(LOG_LCN_RECEIVED + vnfIdentifierCreationNotification);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/lcn/VnfIdentifierDeletionNotification")
    public ResponseEntity<Void> lcnVnfIdentifierDeletionNotificationPost(
            @RequestBody final VnfIdentifierDeletionNotification vnfIdentifierDeletionNotification) {
        logger.info(LOG_LCN_RECEIVED + vnfIdentifierDeletionNotification);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = OPERATION_NOTIFICATION_ENDPOINT)
    public ResponseEntity<Void> lcnVnfLcmOperationOccurrenceNotificationPost(
            @RequestBody final VnfLcmOperationOccurrenceNotification vnfLcmOperationOccurrenceNotification) {
        logger.info(LOG_LCN_RECEIVED + vnfLcmOperationOccurrenceNotification);

        if (isANotificationOfInterest(vnfLcmOperationOccurrenceNotification)) {
            final InlineResponse201 vnfInstance = getVnfInstance(vnfLcmOperationOccurrenceNotification);
            final NotificationHandler handler = new NotificationHandler(vnfLcmOperationOccurrenceNotification,
                    aaiHelper, aaiServiceProvider, vnfmServiceProvider, jobManager, vnfInstance);
            executor.execute(handler);
        }

        logger.info("Sending notification response");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private boolean isANotificationOfInterest(final VnfLcmOperationOccurrenceNotification notification) {
        return isInstanitiateCompleted(notification) || isTerminateTerminalState(notification);
    }

    private boolean isInstanitiateCompleted(final VnfLcmOperationOccurrenceNotification notification) {
        return notification.getOperation().equals(OperationEnum.INSTANTIATE)
                && notification.getOperationState().equals(OperationStateEnum.COMPLETED);
    }

    private boolean isTerminateTerminalState(final VnfLcmOperationOccurrenceNotification notification) {
        return notification.getOperation().equals(OperationEnum.TERMINATE)
                && (notification.getOperationState().equals(OperationStateEnum.COMPLETED)
                        || notification.getOperationState().equals(OperationStateEnum.FAILED)
                        || notification.getOperationState().equals(OperationStateEnum.ROLLED_BACK));
    }

    private InlineResponse201 getVnfInstance(
            final VnfLcmOperationOccurrenceNotification vnfLcmOperationOccurrenceNotification) {
        GenericVnf vnfInAai = aaiServiceProvider
                .invokeQueryGenericVnf(vnfLcmOperationOccurrenceNotification.getLinks().getVnfInstance().getHref())
                .getGenericVnf().get(0);
        EsrVnfm vnfm = aaiHelper.getAssignedVnfm(vnfInAai);
        return vnfmServiceProvider
                .getVnf(vnfm, vnfLcmOperationOccurrenceNotification.getLinks().getVnfInstance().getHref()).get();
    }

}
