/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.beans.WatchdogServiceModVerIdLookup;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogServiceModVerIdLookupRepository;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WatchdogDistribution {

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC,WatchdogDistribution.class);

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
		LOGGER.debug("Entered getOverallDistributionStatus method for distrubutionId: " + distributionId);
		
		String status = null;
		try {
			WatchdogDistributionStatus watchdogDistributionStatus = watchdogDistributionStatusRepository.findById(distributionId)
			        .orElseGet( () -> null);
			if(watchdogDistributionStatus == null){
				watchdogDistributionStatus = new WatchdogDistributionStatus();
				watchdogDistributionStatus.setDistributionId(distributionId);
				watchdogDistributionStatusRepository.save(watchdogDistributionStatus);
			}
			String distributionStatus = watchdogDistributionStatus.getDistributionIdStatus();
			
			if(DistributionStatus.TIMEOUT.name().equalsIgnoreCase(distributionStatus)) {
				LOGGER.debug("Ignoring to update WatchdogDistributionStatus as distributionId: " + distributionId + " status is set to: " + distributionStatus);
				return DistributionStatus.TIMEOUT.name();
			} else {
				List<WatchdogComponentDistributionStatus> results = watchdogCDStatusRepository.findByDistributionId(distributionId);
				LOGGER.debug("Executed RequestDB getWatchdogComponentDistributionStatus for distrubutionId: " + distributionId);
		
				//*************************************************************************************************************************************************
				//**** Compare config values verse DB watchdog component names to see if every component has reported status before returning final result back to ASDC
				//**************************************************************************************************************************************************
				
				List<WatchdogComponentDistributionStatus> cdStatuses = watchdogCDStatusRepository.findByDistributionId(distributionId);
				
				boolean allComponentsComplete = true;
		      
		       for(String name : componentNames ) {		                 
		                
		                boolean match = false;
		                
						for(WatchdogComponentDistributionStatus cdStatus: cdStatuses){							
							if(name.equals(cdStatus.getComponentName())){
								LOGGER.debug("Found componentName " + name + " in the WatchDog Component DB");
								match = true;
								break;
							}
						}						
						if(!match){
							LOGGER.debug(name + " has not be updated in the the WatchDog Component DB yet, so ending the loop");
							allComponentsComplete = false;
							break;
						}
		            }
		         
				
				if(allComponentsComplete) {				
					LOGGER.debug("Components Size matched with the WatchdogComponentDistributionStatus results.");
					
					 for(WatchdogComponentDistributionStatus componentDist : results) {
						 String componentDistributionStatus = componentDist.getComponentDistributionStatus();
						 LOGGER.debug("Component status: " + componentDistributionStatus + " on componentName: " + componentDist.getComponentName());
						 if(componentDistributionStatus.equalsIgnoreCase("COMPONENT_DONE_ERROR")) {
							 status = DistributionStatus.FAILURE.name();
							 break;
						 } else if(componentDistributionStatus.equalsIgnoreCase("COMPONENT_DONE_OK")) {
							 status = DistributionStatus.SUCCESS.name();
						 } else {
							 throw new Exception("Invalid Component distribution status: " + componentDistributionStatus);
						 }
					 }
					 
					 LOGGER.debug("Updating overall DistributionStatus to: " + status + " for distributionId: " + distributionId);
					 
					 watchdogDistributionStatus.setDistributionIdStatus(status);
					 watchdogDistributionStatusRepository.save(watchdogDistributionStatus);
				} else {
					LOGGER.debug("Components Size Didn't match with the WatchdogComponentDistributionStatus results.");
					status = DistributionStatus.INCOMPLETE.name();
					return status;
				}
			}
		}catch (Exception e) {
			LOGGER.debug("Exception occurred on getOverallDistributionStatus : " + e.getMessage());
			LOGGER.error(e);
			throw new Exception(e);
		}		
		LOGGER.debug("Exiting getOverallDistributionStatus method in WatchdogDistribution");
		return status;
	}
	
	public void executePatchAAI(String distributionId, String serviceModelInvariantUUID, String distributionStatus) throws Exception {
		LOGGER.debug("Entered executePatchAAI method with distrubutionId: " + distributionId + " and distributionStatus: " + distributionStatus);
		
		try {
			WatchdogServiceModVerIdLookup lookup = watchdogModVerIdLookupRepository.findOneByDistributionId(distributionId);
			String serviceModelVersionId = "";
			
			if(lookup != null) {
				serviceModelVersionId = lookup.getServiceModelVersionId();
			}
			
			LOGGER.debug("Executed RequestDB getWatchdogServiceModVerIdLookup with distributionId: " + distributionId + " and serviceModelVersionId: " + serviceModelVersionId);
			LOGGER.debug("ASDC Notification ServiceModelInvariantUUID : " + serviceModelInvariantUUID);
			
			if(serviceModelInvariantUUID == null || "".equals(serviceModelVersionId)) {
				String error = "No Service found with serviceModelInvariantUUID: " + serviceModelInvariantUUID;
				LOGGER.debug(error);
				throw new Exception(error);
			}
			
			AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, serviceModelInvariantUUID, serviceModelVersionId);
			aaiUri.depth(Depth.ZERO); //Do not return relationships if any
			LOGGER.debug("Target A&AI Resource URI: " + aaiUri.build().toString());
			
			Map<String, String> payload = new HashMap<>();
			payload.put("distribution-status", distributionStatus);
			getAaiClient().update(aaiUri, payload);
			
			LOGGER.debug("A&AI UPDATE MODEL Version is success!");
		} catch (Exception e) {
			LOGGER.debug("Exception occurred on executePatchAAI : " + e.getMessage());
			LOGGER.error(e);
			throw new Exception(e);
		}
	}

	public AAIResourcesClient getAaiClient() {
		if(aaiClient == null) {
			aaiClient = new AAIResourcesClient();
		}
		return aaiClient;
	}

	public void setAaiClient(AAIResourcesClient aaiClient) {
		this.aaiClient = aaiClient;
	}
}
