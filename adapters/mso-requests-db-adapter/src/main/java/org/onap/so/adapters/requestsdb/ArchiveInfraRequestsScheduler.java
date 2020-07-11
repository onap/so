/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.requestsdb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.db.request.beans.ArchivedInfraRequests;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.ArchivedInfraRequestsRepository;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.ScheduledTasksMDCSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import net.javacrumbs.shedlock.core.SchedulerLock;

@Component
public class ArchiveInfraRequestsScheduler {

    private static Logger logger = LoggerFactory.getLogger(ArchiveInfraRequestsScheduler.class);

    @Autowired
    private InfraActiveRequestsRepository infraActiveRepo;
    @Autowired
    private ArchivedInfraRequestsRepository archivedInfraRepo;
    @Autowired
    private ScheduledTasksMDCSetup scheduledMDCSetup;


    @Value("${mso.infra-requests.archived.period}")
    private int archivedPeriod;

    /**
     * Runs the scheduler nightly [Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @SchedulerLock(name = "archiveInfraRequestsScheduler")
    public void infraRequestsScheduledTask() {
        scheduledMDCSetup.mdcSetup(ONAPComponents.REQUEST_DB, "infraRequestsScheduledTask");
        logger.debug("Start of archiveInfraRequestsScheduler");

        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -archivedPeriod);
        Date archivingDate = calendar.getTime();

        logger.debug("Date before 6 months: " + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE)
                + "-" + calendar.get(Calendar.YEAR));

        List<InfraActiveRequests> requestsByEndTime = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(0, 100);
        do {
            requestsByEndTime = infraActiveRepo.findByEndTimeLessThan(archivingDate, pageRequest);
            logger.debug("{} requests to be archived based on End Time", requestsByEndTime.size());
            archiveInfraRequests(requestsByEndTime);
        } while (!requestsByEndTime.isEmpty());

        List<InfraActiveRequests> requestsByStartTime = new ArrayList<>();
        do {
            requestsByStartTime = infraActiveRepo.findByStartTimeLessThanAndEndTime(archivingDate, null, pageRequest);
            logger.debug("{} requests to be archived based on Start Time", requestsByEndTime.size());
            archiveInfraRequests(requestsByStartTime);
        } while (!requestsByStartTime.isEmpty());

        logger.debug("End of archiveInfraRequestsScheduler");
        scheduledMDCSetup.exitAndClearMDC();
    }

    protected void archiveInfraRequests(List<InfraActiveRequests> requests) {
        List<ArchivedInfraRequests> newArchivedReqs = new ArrayList<>();
        List<InfraActiveRequests> oldInfraReqs = new ArrayList<>();

        for (InfraActiveRequests iar : requests) {
            ArchivedInfraRequests archivedInfra = new ArchivedInfraRequests();
            try {
                archivedInfra.setAaiServiceId(iar.getAaiServiceId());
                archivedInfra.setAction(iar.getAction());
                archivedInfra.setAicCloudRegion(iar.getAicCloudRegion());
                archivedInfra.setAicNodeClli(iar.getAicNodeClli());
                archivedInfra.setCallBackUrl(iar.getCallBackUrl());
                archivedInfra.setClientRequestId(iar.getClientRequestId());
                archivedInfra.setConfigurationId(iar.getConfigurationId());
                archivedInfra.setConfigurationName(iar.getConfigurationName());
                archivedInfra.setCorrelator(iar.getCorrelator());
                archivedInfra.setEndTime(iar.getEndTime());
                archivedInfra.setLastModifiedBy(iar.getLastModifiedBy());
                archivedInfra.setNetworkId(iar.getNetworkId());
                archivedInfra.setNetworkName(iar.getNetworkName());
                archivedInfra.setNetworkType(iar.getNetworkType());
                archivedInfra.setOperationalEnvId(iar.getOperationalEnvId());
                archivedInfra.setOperationalEnvName(iar.getOperationalEnvName());
                archivedInfra.setRequestUrl(iar.getRequestUrl());
                archivedInfra.setProgress(iar.getProgress());
                archivedInfra.setProvStatus(iar.getProvStatus());
                archivedInfra.setRequestAction(iar.getRequestAction());
                archivedInfra.setRequestBody(iar.getRequestBody());
                archivedInfra.setRequestId(iar.getRequestId());
                archivedInfra.setRequestorId(iar.getRequestorId());
                archivedInfra.setRequestScope(iar.getRequestScope());
                archivedInfra.setRequestStatus(iar.getRequestStatus());
                archivedInfra.setRequestType(iar.getRequestType());
                archivedInfra.setResponseBody(iar.getResponseBody());
                archivedInfra.setServiceInstanceId(iar.getServiceInstanceId());
                archivedInfra.setServiceInstanceName(iar.getServiceInstanceName());
                archivedInfra.setServiceType(iar.getServiceType());
                archivedInfra.setSource(iar.getSource());
                archivedInfra.setStartTime(iar.getStartTime());
                archivedInfra.setStatusMessage(iar.getStatusMessage());
                archivedInfra.setTenantId(iar.getTenantId());
                archivedInfra.setVfModuleId(iar.getVfModuleId());
                archivedInfra.setVfModuleModelName(iar.getVfModuleModelName());
                archivedInfra.setVfModuleName(iar.getVfModuleName());
                archivedInfra.setVnfId(iar.getVnfId());
                archivedInfra.setVnfName(iar.getVnfName());
                archivedInfra.setVnfOutputs(iar.getVnfOutputs());
                archivedInfra.setVnfParams(iar.getVnfParams());
                archivedInfra.setVnfType(iar.getVnfType());
                archivedInfra.setVolumeGroupId(iar.getVolumeGroupId());
                archivedInfra.setVolumeGroupName(iar.getVolumeGroupName());

                newArchivedReqs.add(archivedInfra);
                oldInfraReqs.add(iar);
            } catch (Exception e) {
                scheduledMDCSetup.errorMDCSetup(ErrorCode.UnknownError, e.getMessage());
                MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.ERROR.toString());
                logger.error(LoggingAnchor.TWO, MessageEnum.RA_GENERAL_EXCEPTION.toString(),
                        ErrorCode.UnknownError.getValue(), e);
            }
        }

        logger.info("Creating archivedInfraRequest records: {}", newArchivedReqs.size());
        archivedInfraRepo.saveAll(newArchivedReqs);

        logger.info("Deleting InfraActiveRequest records: {}", oldInfraReqs.size());
        infraActiveRepo.deleteAll(oldInfraReqs);
    }
}
