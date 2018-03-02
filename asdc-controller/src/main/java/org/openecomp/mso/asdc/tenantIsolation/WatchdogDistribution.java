package org.openecomp.mso.asdc.tenantIsolation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.entities.uri.Depth;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJsonProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.WatchdogComponentDistributionStatus;
import org.openecomp.mso.requestsdb.WatchdogComponentDistributionStatusDb;
import org.openecomp.mso.requestsdb.WatchdogDistributionStatusDb;
import org.openecomp.mso.requestsdb.WatchdogServiceModVerIdLookupDb;

import com.fasterxml.jackson.databind.JsonNode;

public class WatchdogDistribution {

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC);
	private static final String MSO_PROP_ASDC = "MSO_PROP_ASDC";
	private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
	private WatchdogDistributionStatusDb watchdogDistDb;
	private WatchdogComponentDistributionStatusDb watchdogCompDistDb;
	private WatchdogServiceModVerIdLookupDb watchdogSerlookupDb;
	private CatalogDatabase catalogDb;
	private AAIResourcesClient aaiClient;
	//protected ASDCConfiguration asdcConfig;
	   
	public String getOverallDistributionStatus(String distributionId) throws MsoPropertiesException, Exception {
		LOGGER.debug("Entered getOverallDistributionStatus method for distrubutionId: " + distributionId);
		
		String status = null;
		try { 
			String distributionStatus = getWatchdogDistDb().getWatchdogDistributionIdStatus(distributionId);
			
			if(DistributionStatus.TIMEOUT.name().equalsIgnoreCase(distributionStatus)) {
				LOGGER.debug("Ignoring to update WatchdogDistributionStatus as distributionId: " + distributionId + " status is set to: " + distributionStatus);
				return DistributionStatus.TIMEOUT.name();
			} else {
				List<WatchdogComponentDistributionStatus> results = getWatchdogCompDistDb().getWatchdogComponentDistributionStatus(distributionId);
				LOGGER.debug("Executed RequestDB getWatchdogComponentDistributionStatus for distrubutionId: " + distributionId);
		
				MsoJsonProperties properties = msoPropertiesFactory.getMsoJsonProperties(MSO_PROP_ASDC);
				
				//*************************************************************************************************************************************************
				//**** Compare config values verse DB watchdog component names to see if every component has reported status before returning final result back to ASDC
				//**************************************************************************************************************************************************
				
				//List<String> configNames = asdcConfig.getComponentNames();
				
				List<String> dbNames = watchdogCompDistDb.getWatchdogComponentNames(distributionId);
				
				boolean allComponentsComplete = true;
							
				JsonNode masterConfigNode = properties.getJsonRootNode().get("componentNames");
				
		        if (masterConfigNode != null) { 
		            
		            Iterator<JsonNode> config = masterConfigNode.elements();
		      
		            while( config.hasNext() ) {
		                String name = (String)config.next().asText();	                
		                
		                boolean match = false;
		                
						for(String dbName: dbNames){
							
							if(name.equals(dbName)){
								LOGGER.debug("Found componentName " + name + " in the WatchDog Component DB");
								match = true;
								break;
							}
						}
						
						if(match==false){
							LOGGER.debug(name + " has not be updated in the the WatchDog Component DB yet, so ending the loop");
							allComponentsComplete = false;
							break;
						}

		            }

		        } 
				
				if(allComponentsComplete) {
				//if(node.asInt() == results.size()) {
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
					 getWatchdogDistDb().updateWatchdogDistributionIdStatus(distributionId, status);
				} else {
					LOGGER.debug("Components Size Didn't match with the WatchdogComponentDistributionStatus results.");
					status = DistributionStatus.INCOMPLETE.name();
					return status;
				}
			}
		} catch (MsoPropertiesException e) {
			String error = "Error occurred when trying to load MSOJson Properties.";
			LOGGER.debug(error);
			throw new MsoPropertiesException(e.getMessage());
		} catch (Exception e) {
			LOGGER.debug("Exception occurred on getOverallDistributionStatus : " + e.getMessage());
			throw new Exception(e);
		}
		
		LOGGER.debug("Exciting getOverallDistributionStatus method in WatchdogDistribution");
		return status;
	}
	
	public void executePatchAAI(String distributionId, String serviceModelInvariantUUID, String distributionStatus) throws Exception {
		LOGGER.debug("Entered executePatchAAI method with distrubutionId: " + distributionId + " and distributionStatus: " + distributionStatus);
		
		try { 
			String serviceModelVersionId = getWatchdogSerlookupDb().getWatchdogServiceModVerId(distributionId);
			LOGGER.debug("Executed RequestDB getWatchdogServiceModVerIdLookup with distributionId: " + distributionId + " and serviceModelVersionId: " + serviceModelVersionId);
			
			LOGGER.debug("ASDC Notification ServiceModelInvariantUUID : " + serviceModelInvariantUUID);
			
			if(serviceModelInvariantUUID == null) {
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
			throw new Exception(e);
		}
	}
	
	public WatchdogDistributionStatusDb getWatchdogDistDb() {
		if(watchdogDistDb == null) {
			watchdogDistDb =  WatchdogDistributionStatusDb.getInstance();
		}
		return watchdogDistDb;
	}

	public void setWatchdogDistDb(WatchdogDistributionStatusDb watchdogDistDb) {
		this.watchdogDistDb = watchdogDistDb;
	}

	public WatchdogComponentDistributionStatusDb getWatchdogCompDistDb() {
		if(watchdogCompDistDb == null) {
			watchdogCompDistDb =  WatchdogComponentDistributionStatusDb.getInstance();
		}
		return watchdogCompDistDb;
	}

	public void setWatchdogCompDistDb(WatchdogComponentDistributionStatusDb watchdogCompDistDb) {
		this.watchdogCompDistDb = watchdogCompDistDb;
	}

	public WatchdogServiceModVerIdLookupDb getWatchdogSerlookupDb() {
		if(watchdogSerlookupDb == null) {
			watchdogSerlookupDb =  WatchdogServiceModVerIdLookupDb.getInstance();
		}
		return watchdogSerlookupDb;
	}

	public void setWatchdogSerlookupDb(WatchdogServiceModVerIdLookupDb watchdogSerlookupDb) {
		this.watchdogSerlookupDb = watchdogSerlookupDb;
	}

	public CatalogDatabase getCatalogDb() {
		if(catalogDb == null) {
			catalogDb = CatalogDatabase.getInstance();
		}
		return catalogDb;
	}

	public void setCatalogDb(CatalogDatabase catalogDb) {
		this.catalogDb = catalogDb;
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
