/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.so.asdc.tenantIsolation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.beans.WatchdogServiceModVerIdLookup;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogServiceModVerIdLookupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WatchdogDistribution {

    private static final Logger logger = LoggerFactory.getLogger(WatchdogDistribution.class);

    private AAIResourcesClient aaiClient;

    @Autowired
    private WatchdogDistributionStatusRepository watchdogDistributionStatusRepository;

    @Autowired
    private WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;

    @Autowired
    private WatchdogServiceModVerIdLookupRepository watchdogModVerIdLookupRepository;

    @Autowired
    private ServiceRepository serviceRepo;

    @Value("${mso.asdc.config.components.componentNames}")
    private String[] componentNames;

    public String getOverallDistributionStatus(String distributionId) throws Exception {
        logger.debug("Entered getOverallDistributionStatus method for distrubutionId: {}", distributionId);

        String status = null;
        try {
            WatchdogDistributionStatus watchdogDistributionStatus =
                    watchdogDistributionStatusRepository.findById(distributionId).orElseGet(() -> null);
            if (watchdogDistributionStatus == null) {
                watchdogDistributionStatus = new WatchdogDistributionStatus();
                watchdogDistributionStatus.setDistributionId(distributionId);
                watchdogDistributionStatusRepository.save(watchdogDistributionStatus);
            }
            String distributionStatus = watchdogDistributionStatus.getDistributionIdStatus();

            if (DistributionStatus.TIMEOUT.name().equalsIgnoreCase(distributionStatus)) {
                logger.debug("Ignoring to update WatchdogDistributionStatus as distributionId: {} status is set to: {}",
                        distributionId, distributionStatus);
                return DistributionStatus.TIMEOUT.name();
            } else {
                List<WatchdogComponentDistributionStatus> results =
                        watchdogCDStatusRepository.findByDistributionId(distributionId);
                logger.debug("Executed RequestDB getWatchdogComponentDistributionStatus for distrubutionId: {}",
                        distributionId);

                // *************************************************************************************************************************************************
                // **** Compare config values verse DB watchdog component names to see if every component has reported
                // status before returning final result back to ASDC
                // **************************************************************************************************************************************************

                List<WatchdogComponentDistributionStatus> cdStatuses =
                        watchdogCDStatusRepository.findByDistributionId(distributionId);

                boolean allComponentsComplete = true;

                for (String name : componentNames) {

                    boolean match = false;

                    for (WatchdogComponentDistributionStatus cdStatus : cdStatuses) {
                        if (name.equals(cdStatus.getComponentName())) {
                            logger.debug("Found componentName {} in the WatchDog Component DB", name);
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        logger.debug("{} has not be updated in the the WatchDog Component DB yet, so ending the loop",
                                name);
                        allComponentsComplete = false;
                        break;
                    }
                }


                if (allComponentsComplete) {
                    logger.debug("Components Size matched with the WatchdogComponentDistributionStatus results.");

                    for (WatchdogComponentDistributionStatus componentDist : results) {
                        String componentDistributionStatus = componentDist.getComponentDistributionStatus();
                        logger.debug("Component status: {} on componentName: ", componentDistributionStatus,
                                componentDist.getComponentName());
                        if (componentDistributionStatus.equalsIgnoreCase("COMPONENT_DONE_ERROR")) {
                            status = DistributionStatus.FAILURE.name();
                            break;
                        } else if (componentDistributionStatus.equalsIgnoreCase("COMPONENT_DONE_OK")) {
                            status = DistributionStatus.SUCCESS.name();
                        } else {
                            throw new Exception(
                                    "Invalid Component distribution status: " + componentDistributionStatus);
                        }
                    }

                    logger.debug("Updating overall DistributionStatus to: {} for distributionId: ", status,
                            distributionId);

                    watchdogDistributionStatusRepository.save(watchdogDistributionStatus);
                } else {
                    logger.debug("Components Size Didn't match with the WatchdogComponentDistributionStatus results.");
                    status = DistributionStatus.INCOMPLETE.name();
                    return status;
                }
            }
        } catch (Exception e) {
            logger.debug("Exception occurred on getOverallDistributionStatus : {}", e.getMessage());
            logger.error("Exception occurred", e);
            throw new Exception(e);
        }
        logger.debug("Exiting getOverallDistributionStatus method in WatchdogDistribution");
        return status;
    }

    public void executePatchAAI(String distributionId, String serviceModelInvariantUUID, String distributionStatus)
            throws Exception {
        logger.debug("Entered executePatchAAI method with distrubutionId: {} and distributionStatus: ", distributionId,
                distributionStatus);

        try {
            WatchdogServiceModVerIdLookup lookup =
                    watchdogModVerIdLookupRepository.findOneByDistributionId(distributionId);
            String serviceModelVersionId = "";

            if (lookup != null) {
                serviceModelVersionId = lookup.getServiceModelVersionId();
            }

            logger.debug("Executed RequestDB getWatchdogServiceModVerIdLookup with distributionId: {} "
                    + "and serviceModelVersionId: {}", distributionId, serviceModelVersionId);
            logger.debug("ASDC Notification ServiceModelInvariantUUID : {}", serviceModelInvariantUUID);

            if (serviceModelInvariantUUID == null || "".equals(serviceModelVersionId)) {
                String error = "No Service found with serviceModelInvariantUUID: " + serviceModelInvariantUUID;
                logger.debug(error);
                throw new Exception(error);
            }



            AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, serviceModelInvariantUUID,
                    serviceModelVersionId);
            aaiUri.depth(Depth.ZERO); // Do not return relationships if any
            logger.debug("Target A&AI Resource URI: {}", aaiUri.build().toString());

            Map<String, String> payload = new HashMap<>();
            payload.put("distribution-status", distributionStatus);
            getAaiClient().update(aaiUri, payload);

            logger.debug("A&AI UPDATE MODEL Version is success!");
        } catch (Exception e) {
            logger.debug("Exception occurred on executePatchAAI : {}", e.getMessage());
            logger.error("Exception occurred", e);
            throw new Exception(e);
        }
    }

    public void updateCatalogDBStatus(String serviceModelVersionId, String status) {
        try {
            Service foundService = serviceRepo.findOneByModelUUID(serviceModelVersionId);
            foundService.setDistrobutionStatus(status);
            serviceRepo.save(foundService);
        } catch (Exception e) {
            logger.error("Error updating CatalogDBStatus", e);
        }
    }

    public AAIResourcesClient getAaiClient() {
        if (aaiClient == null) {
            aaiClient = new AAIResourcesClient();
        }
        return aaiClient;
    }

    public void setAaiClient(AAIResourcesClient aaiClient) {
        this.aaiClient = aaiClient;
    }
}
