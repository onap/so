/*-
d * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.asdc.client;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.onap.so.asdc.client.exceptions.ASDCDownloadException;
import org.onap.so.asdc.client.exceptions.ASDCParametersException;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.IVfResourceInstaller;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfModuleStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.asdc.tenantIsolation.DistributionStatus;
import org.onap.so.asdc.tenantIsolation.WatchdogDistribution;
import org.onap.so.asdc.util.ASDCNotificationLogging;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ASDCController {

    protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC,ASDCController.class);

    protected static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();

    protected boolean isAsdcClientAutoManaged = false;

    protected String controllerName;
    
    private ASDCControllerStatus controllerStatus = ASDCControllerStatus.STOPPED;
    
    protected int nbOfNotificationsOngoing = 0;

    @Autowired
    private ToscaResourceInstaller toscaInstaller;
    
    @Autowired
    private WatchdogDistributionStatusRepository wdsRepo;
    
    @Autowired
    private ASDCConfiguration asdcConfig;
    
    @Autowired
    private ASDCStatusCallBack asdcStatusCallBack;
    
    @Autowired
    private ASDCNotificationCallBack asdcNotificationCallBack;
    
    private IDistributionClient distributionClient;
    
    private static final String UUID_PARAM = "(UUID:";
    
    @Autowired
    private WatchdogDistribution wd;
   

    public int getNbOfNotificationsOngoing () {
        return nbOfNotificationsOngoing;
    }    

    public IDistributionClient getDistributionClient() {
		return distributionClient;
	}



	public void setDistributionClient(IDistributionClient distributionClient) {
		this.distributionClient = distributionClient;
	}



	protected void changeControllerStatus (ASDCControllerStatus newControllerStatus) {
        switch (newControllerStatus) {

            case BUSY:
                ++this.nbOfNotificationsOngoing;
                this.controllerStatus = newControllerStatus;
                break;

            case IDLE:
                if (this.nbOfNotificationsOngoing > 1) {
                    --this.nbOfNotificationsOngoing;
                } else {
                    this.nbOfNotificationsOngoing = 0;
                    this.controllerStatus = newControllerStatus;
                }

                break;
            default:
                this.controllerStatus = newControllerStatus;
                break;

        }
    }

    public ASDCControllerStatus getControllerStatus () {
        return this.controllerStatus;
    }
    
    public ASDCController () {
        isAsdcClientAutoManaged = true;        
    }

    public ASDCController (String controllerConfigName) {
        isAsdcClientAutoManaged = true;
        this.controllerName = controllerConfigName;
    }

    public ASDCController (String controllerConfigName, IDistributionClient asdcClient, IVfResourceInstaller resourceinstaller) {
        distributionClient = asdcClient;       
    }

    public ASDCController (String controllerConfigName,IDistributionClient asdcClient) {
        distributionClient = asdcClient;
        this.controllerName = controllerConfigName;     
    }
    public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	/**
     * This method initializes the ASDC Controller and the ASDC Client.
     *
     * @throws ASDCControllerException It throws an exception if the ASDC Client cannot be instantiated or if an init
     *         attempt is done when already initialized
     * @throws ASDCParametersException If there is an issue with the parameters provided
     * @throws IOException In case of issues when trying to load the key file
     */
    public void initASDC () throws ASDCControllerException {
        String event = "Initialize the ASDC Controller";
        MsoLogger.setServiceName ("InitASDC");
        LOGGER.debug (event);
        if (this.getControllerStatus () != ASDCControllerStatus.STOPPED) {
            String endEvent = "The controller is already initialized, call the closeASDC method first";
            throw new ASDCControllerException (endEvent);
        }

        if (asdcConfig != null) {          
            asdcConfig.setAsdcControllerName(controllerName);
        }    

        if (this.distributionClient == null) {
            distributionClient = DistributionClientFactory.createDistributionClient ();
        }
        
        long initStartTime = System.currentTimeMillis ();
        IDistributionClientResult result = this.distributionClient.init (asdcConfig,
                                                                         asdcNotificationCallBack, asdcStatusCallBack);
        if (!result.getDistributionActionResult ().equals (DistributionActionResultEnum.SUCCESS)) {
            String endEvent = "ASDC distribution client init failed with reason:"
                              + result.getDistributionMessageResult ();
            LOGGER.recordMetricEvent (initStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, "Initialization of the ASDC Controller failed with reason:" + result.getDistributionMessageResult (), "ASDC", "init", null);
            LOGGER.debug (endEvent);
            this.changeControllerStatus (ASDCControllerStatus.STOPPED);
            throw new ASDCControllerException ("Initialization of the ASDC Controller failed with reason: "
                                               + result.getDistributionMessageResult ());
        }
        LOGGER.recordMetricEvent (initStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully initialize ASDC Controller", "ASDC", "init", null);

        long clientstartStartTime = System.currentTimeMillis ();
        result = this.distributionClient.start ();
        if (!result.getDistributionActionResult ().equals (DistributionActionResultEnum.SUCCESS)) {
            String endEvent = "ASDC distribution client start failed with reason:"
                              + result.getDistributionMessageResult ();
            LOGGER.recordMetricEvent (clientstartStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, endEvent, "ASDC", "start", null);
            LOGGER.debug (endEvent);           
            this.changeControllerStatus (ASDCControllerStatus.STOPPED);
            throw new ASDCControllerException ("Startup of the ASDC Controller failed with reason: "
                                               + result.getDistributionMessageResult ());
        }
        LOGGER.recordMetricEvent (clientstartStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully start ASDC distribution client", "ASDC", "start", null);


        this.changeControllerStatus (ASDCControllerStatus.IDLE);
        LOGGER.info (MessageEnum.ASDC_INIT_ASDC_CLIENT_SUC, "ASDC", "changeControllerStatus","");
    }

    /**
     * This method closes the ASDC Controller and the ASDC Client.
     *
     * @throws ASDCControllerException It throws an exception if the ASDC Client cannot be closed because
     *         it's currently BUSY in processing notifications.
     */
    public void closeASDC () throws ASDCControllerException {

        MsoLogger.setServiceName ("CloseController");
        if (this.getControllerStatus () == ASDCControllerStatus.BUSY) {
            throw new ASDCControllerException ("Cannot close the ASDC controller as it's currently in BUSY state");
        }
        if (this.distributionClient != null) {
            this.distributionClient.stop ();
            // If auto managed we can set it to Null, ASDCController controls it.
            // In the other case the client of this class has specified it, so we can't reset it
            if (isAsdcClientAutoManaged) {
                // Next init will initialize it with a new ASDC Client
                this.distributionClient = null;
            }

        }
        this.changeControllerStatus (ASDCControllerStatus.STOPPED);
    }

    private boolean checkResourceAlreadyDeployed (VfResourceStructure resource) throws ArtifactInstallerException {

    	
   		if (toscaInstaller.isResourceAlreadyDeployed (resource)) {
    			LOGGER.info (MessageEnum.ASDC_ARTIFACT_ALREADY_EXIST,
                    resource.getResourceInstance().getResourceInstanceName(),
                    resource.getResourceInstance().getResourceUUID(),
                    resource.getResourceInstance().getResourceName(), "", "");

    			this.sendDeployNotificationsForResource(resource,DistributionStatusEnum.ALREADY_DOWNLOADED,null);
    			this.sendDeployNotificationsForResource(resource,DistributionStatusEnum.ALREADY_DEPLOYED,null);

    		return true;
    	} else {
    		return false;
    	}
    }

   

    protected IDistributionClientDownloadResult downloadTheArtifact (IArtifactInfo artifact,
                                                                   String distributionId) throws ASDCDownloadException {

        LOGGER.debug ("Trying to download the artifact : " + artifact.getArtifactURL ()
                      + UUID_PARAM
                      + artifact.getArtifactUUID ()
                      + ")");
        IDistributionClientDownloadResult downloadResult;


        try {
            downloadResult = distributionClient.download (artifact);
            if (null == downloadResult) {
            	LOGGER.info (MessageEnum.ASDC_ARTIFACT_NULL, artifact.getArtifactUUID (), "", "");
            	return downloadResult;
            }
        } catch (RuntimeException e) {
            LOGGER.debug ("Not able to download the artifact due to an exception: " + artifact.getArtifactURL ());
            this.sendASDCNotification (NotificationType.DOWNLOAD,
                                       artifact.getArtifactURL (),
                                       asdcConfig.getConsumerID (),
                                       distributionId,
                                       DistributionStatusEnum.DOWNLOAD_ERROR,
                                       e.getMessage (),
                                       System.currentTimeMillis ());

            throw new ASDCDownloadException ("Exception caught when downloading the artifact", e);
        }

        if (DistributionActionResultEnum.SUCCESS.equals(downloadResult.getDistributionActionResult ())) {

            LOGGER.info (MessageEnum.ASDC_ARTIFACT_DOWNLOAD_SUC,
                         artifact.getArtifactURL (),
                         artifact.getArtifactUUID (),
                         String.valueOf (downloadResult.getArtifactPayload ().length), "", "");

        } else {

            LOGGER.error (MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL,
                          artifact.getArtifactName (),
                          artifact.getArtifactURL (),
                          artifact.getArtifactUUID (),
                          downloadResult.getDistributionMessageResult (), "", "", MsoLogger.ErrorCode.DataError, "ASDC artifact download fail");

            this.sendASDCNotification (NotificationType.DOWNLOAD,
                                       artifact.getArtifactURL (),
                                       asdcConfig.getConsumerID (),
                                       distributionId,
                                       DistributionStatusEnum.DOWNLOAD_ERROR,
                                       downloadResult.getDistributionMessageResult (),
                                       System.currentTimeMillis ());

            throw new ASDCDownloadException ("Artifact " + artifact.getArtifactName ()
                                             + " could not be downloaded from ASDC URL "
                                             + artifact.getArtifactURL ()
                                             + UUID_PARAM
                                             + artifact.getArtifactUUID ()
                                             + ")"
                                             + System.lineSeparator ()
                                             + "Error message is "
                                             + downloadResult.getDistributionMessageResult ()
                                             + System.lineSeparator ());

        }

        this.sendASDCNotification (NotificationType.DOWNLOAD,
                                   artifact.getArtifactURL (),
                                   asdcConfig.getConsumerID (),
                                   distributionId,
                                   DistributionStatusEnum.DOWNLOAD_OK,
                                   null,
                                   System.currentTimeMillis ());
        return downloadResult;

    }

    private void writeArtifactToFile (IArtifactInfo artifact,
    		IDistributionClientDownloadResult resultArtifact) {

    	LOGGER.debug ("Trying to write artifact to file : " + artifact.getArtifactURL ()
    			+ UUID_PARAM
    			+ artifact.getArtifactUUID ()
    			+ ")");
    	
    	byte[] payloadBytes = resultArtifact.getArtifactPayload();
    	
    	try (FileOutputStream outFile = new FileOutputStream(System.getProperty("mso.config.path") + "/ASDC" + "/" + artifact.getArtifactName())) {
    		LOGGER.info(MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF, "***WRITE FILE ARTIFACT NAME", "ASDC", artifact.getArtifactName());
    		outFile.write(payloadBytes, 0, payloadBytes.length);
    		outFile.close();
    	} catch (Exception e) { 
			LOGGER.debug("Exception :",e);
            LOGGER.error(MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL,
    				artifact.getArtifactName (),
    				artifact.getArtifactURL (),
    				artifact.getArtifactUUID (),
    				resultArtifact.getDistributionMessageResult (), "", "", MsoLogger.ErrorCode.DataError, "ASDC write to file failed"); 
        } 
    	
    }


    protected void sendDeployNotificationsForResource(VfResourceStructure vfResourceStructure,DistributionStatusEnum distribStatus, String errorReason) {

    	for (IArtifactInfo artifactInfo : vfResourceStructure.getResourceInstance().getArtifacts()) {

    		if ((DistributionStatusEnum.DEPLOY_OK.equals(distribStatus) && !artifactInfo.getArtifactType().equalsIgnoreCase("OTHER") && !vfResourceStructure.isAlreadyDeployed())
    				// This could be NULL if the artifact is a VF module artifact, this won't be present in the MAP
    				&& vfResourceStructure.getArtifactsMapByUUID().get(artifactInfo.getArtifactUUID()) != null
    				&& vfResourceStructure.getArtifactsMapByUUID().get(artifactInfo.getArtifactUUID()).getDeployedInDb() == 0) {
    			this.sendASDCNotification (NotificationType.DEPLOY,
	    				artifactInfo.getArtifactURL (),
	                  asdcConfig.getConsumerID (),
	                  vfResourceStructure.getNotification().getDistributionID(),
	                  DistributionStatusEnum.DEPLOY_ERROR,
	                  "The artifact has not been used by the modules defined in the resource",
	                  System.currentTimeMillis ());
    		} else {
	    		this.sendASDCNotification (NotificationType.DEPLOY,
	    				artifactInfo.getArtifactURL (),
	                  asdcConfig.getConsumerID (),
	                  vfResourceStructure.getNotification().getDistributionID(),
	                  distribStatus,
	                  errorReason,
	                  System.currentTimeMillis ());
    		}
    	}
    }
    
    protected void sendCsarDeployNotification(INotificationData iNotif, VfResourceStructure resourceStructure, ToscaResourceStructure toscaResourceStructure, boolean deploySuccessful, String errorReason) {
    	
		IArtifactInfo csarArtifact = toscaResourceStructure.getToscaArtifact();
		
		if(deploySuccessful){
			
    		this.sendASDCNotification (NotificationType.DEPLOY,
 	    			  csarArtifact.getArtifactURL (),
	                  asdcConfig.getConsumerID (),
	                  resourceStructure.getNotification().getDistributionID(),
	                  DistributionStatusEnum.DEPLOY_OK,
	                  errorReason,
	                  System.currentTimeMillis ());
			
		} else {
			
			this.sendASDCNotification (NotificationType.DEPLOY,
    			  csarArtifact.getArtifactURL (),
                  asdcConfig.getConsumerID (),
                  resourceStructure.getNotification().getDistributionID(),
                  DistributionStatusEnum.DEPLOY_ERROR,
                  errorReason,
                  System.currentTimeMillis ());
			
		}
    }
    
    protected void deployResourceStructure (VfResourceStructure resourceStructure, ToscaResourceStructure toscaResourceStructure) throws ArtifactInstallerException {

    	LOGGER.info (MessageEnum.ASDC_START_DEPLOY_ARTIFACT, resourceStructure.getResourceInstance().getResourceInstanceName(), resourceStructure.getResourceInstance().getResourceUUID(), "ASDC");
        try {
        	String resourceType = resourceStructure.getResourceInstance().getResourceType();
        	String category = resourceStructure.getResourceInstance().getCategory();
        	if("VF".equals(resourceType) && !"Allotted Resource".equalsIgnoreCase(category)){
        		resourceStructure.createVfModuleStructures();
        	}
        	toscaInstaller.installTheResource(toscaResourceStructure, resourceStructure);        	        				

        } catch (ArtifactInstallerException e) {
        	LOGGER.info (MessageEnum.ASDC_ARTIFACT_DOWNLOAD_FAIL,
	           		resourceStructure.getResourceInstance().getResourceName(),
	          		resourceStructure.getResourceInstance().getResourceUUID(),
	                String.valueOf (resourceStructure.getVfModuleStructure().size()), "ASDC", "deployResourceStructure");
        	sendDeployNotificationsForResource(resourceStructure,DistributionStatusEnum.DEPLOY_ERROR,e.getMessage());
        	throw e;
        }

        if (resourceStructure.isDeployedSuccessfully() || toscaResourceStructure.isDeployedSuccessfully()) {
	        LOGGER.info (MessageEnum.ASDC_ARTIFACT_DEPLOY_SUC,
	           		resourceStructure.getResourceInstance().getResourceName(),
	          		resourceStructure.getResourceInstance().getResourceUUID(),
	                String.valueOf (resourceStructure.getVfModuleStructure().size()), "ASDC", "deployResourceStructure");
	        sendDeployNotificationsForResource(resourceStructure,DistributionStatusEnum.DEPLOY_OK ,null);
        }

    }
    

    private enum NotificationType {
    	DOWNLOAD, DEPLOY
    }

    protected void sendASDCNotification (NotificationType notificationType,
                                       String artifactURL,
                                       String consumerID,
                                       String distributionID,
                                       DistributionStatusEnum status,
                                       String errorReason,
                                       long timestamp) {

        String event = "Sending " + notificationType.name ()
                       + "("
                       + status.name ()
                       + ")"
                       + " notification to ASDC for artifact:"
                       + artifactURL;

        if (errorReason != null) {
        	event=event+"("+errorReason+")";
        }
        LOGGER.info (MessageEnum.ASDC_SEND_NOTIF_ASDC, notificationType.name (), status.name (), artifactURL, "ASDC", "sendASDCNotification");
        LOGGER.debug (event);

        long subStarttime = System.currentTimeMillis ();
        String action = "";
        try {
            IDistributionStatusMessage message = new DistributionStatusMessage (artifactURL,
                                                                                consumerID,
                                                                                distributionID,
                                                                                status,
                                                                                timestamp);

            switch (notificationType) {
                case DOWNLOAD:
                    if (errorReason != null) {
                        this.distributionClient.sendDownloadStatus (message, errorReason);
                    } else {
                        this.distributionClient.sendDownloadStatus (message);
                    }
                    action = "sendDownloadStatus";
                    break;
                case DEPLOY:
                    if (errorReason != null) {
                        this.distributionClient.sendDeploymentStatus (message, errorReason);
                    } else {
                        this.distributionClient.sendDeploymentStatus (message);
                    }
                    action = "sendDeploymentdStatus";
                    break;
                default:
                	break;
            }
        } catch (RuntimeException e) {           
            LOGGER.warn (MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC, "ASDC", "sendASDCNotification", MsoLogger.ErrorCode.SchemaError, "RuntimeException - sendASDCNotification", e);
        }
        LOGGER.recordMetricEvent (subStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully sent notification to ASDC", "ASDC", action, null);
    }
    
    protected void sendFinalDistributionStatus (
    		String distributionID,
    		DistributionStatusEnum status,
    		String errorReason) {


    	LOGGER.debug ("Enter sendFinalDistributionStatus with DistributionID " + distributionID + " and Status of " + status.name() + " and ErrorReason " + errorReason);

    	long subStarttime = System.currentTimeMillis ();
    	try {
    		
    		
    		IFinalDistrStatusMessage finalDistribution = new FinalDistributionStatusMessage(distributionID,status,subStarttime, asdcConfig.getConsumerID());
    		
    		if(errorReason == null){
    			this.distributionClient.sendFinalDistrStatus(finalDistribution);
    		}else{
    			this.distributionClient.sendFinalDistrStatus(finalDistribution, errorReason);
    		}
    		
 
    	} catch (RuntimeException e) {    		
    		LOGGER.debug ("Exception caught in sendFinalDistributionStatus " + e.getMessage());
    		LOGGER.warn (MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC, "ASDC", "sendASDCNotification", MsoLogger.ErrorCode.SchemaError, "RuntimeException - sendASDCNotification", e);
    	}
    	LOGGER.recordMetricEvent (subStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully sent Final notification to ASDC", "ASDC", null, null);
    }

    public void treatNotification (INotificationData iNotif) {

    	int noOfArtifacts = 0;
    	

    	for (IResourceInstance resource : iNotif.getResources ()) {
    		noOfArtifacts += resource.getArtifacts ().size ();
    	}
        LOGGER.info (MessageEnum.ASDC_RECEIVE_CALLBACK_NOTIF,
                     String.valueOf (noOfArtifacts),
                     iNotif.getServiceUUID (), "ASDC");

        try {
        	LOGGER.debug(ASDCNotificationLogging.dumpASDCNotification(iNotif));
			LOGGER.info(MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF, iNotif.getServiceUUID(), "ASDC", "treatNotification");
			this.changeControllerStatus(ASDCControllerStatus.BUSY);
			toscaInstaller.processWatchdog(iNotif.getDistributionID(),iNotif.getServiceUUID());	
			
			// Process only the Resource artifacts in MSO				
			this.processResourceNotification(iNotif);
			
			//********************************************************************************************************
			//Start Watchdog loop and wait for all components to complete before reporting final status back. 
			// **If timer expires first then we will report a Distribution Error back to ASDC
			//********************************************************************************************************
        	long initialStartTime = System.currentTimeMillis();
        	boolean componentsComplete = false;
        	String distributionStatus = null;
        	String watchdogError = null;
        	String overallStatus = null;
        	int watchDogTimeout = asdcConfig.getWatchDogTimeout() * 1000;
        	boolean isDeploySuccess = false;        	
						
        	while(!componentsComplete && (System.currentTimeMillis() - initialStartTime) < watchDogTimeout)
        	{
        		        		
    			try{        		
    				distributionStatus = wd.getOverallDistributionStatus(iNotif.getDistributionID());
    				Thread.sleep(watchDogTimeout / 10);    		
    			}catch(Exception e){
    				LOGGER.debug ("Exception in Watchdog Loop " + e.getMessage());
    				Thread.sleep(watchDogTimeout / 10);
    			}
    			
    			if(distributionStatus != null && !distributionStatus.equalsIgnoreCase(DistributionStatus.INCOMPLETE.name())){
    				
    				if(distributionStatus.equalsIgnoreCase(DistributionStatus.SUCCESS.name())){
    					isDeploySuccess = true;
    					overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.name();
    				}else{
    					overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
    				}    				
    				componentsComplete = true;
    			}
        	}
        	
        	if(!componentsComplete){
        		LOGGER.debug("Timeout of " + watchDogTimeout + " seconds was reached before all components reported status");
        		watchdogError = "Timeout occurred while waiting for all components to report status";
        		overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
        	}
        	
        	if(distributionStatus == null){        	
        		overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
        		LOGGER.debug("DistributionStatus is null for DistributionId: " + iNotif.getDistributionID());        		 	
        	}
        	
        	try {
        		wd.executePatchAAI(iNotif.getDistributionID(), iNotif.getServiceInvariantUUID(), overallStatus);
        		LOGGER.debug ("A&AI Updated succefully with Distribution Status!");
        	}
        	catch(Exception e) {
        		LOGGER.debug ("Exception in Watchdog executePatchAAI(): " + e.getMessage());
        		watchdogError = "Error calling A&AI " + e.getMessage();
        		if(e.getCause() != null) {
        			LOGGER.debug ("Exception caused by: " + e.getCause().getMessage());
        		}
        	}
     	
        	
        	if(isDeploySuccess && watchdogError == null){
        		sendFinalDistributionStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK, null);
        		WatchdogDistributionStatus wds = new WatchdogDistributionStatus(iNotif.getDistributionID());
        		wds.setDistributionIdStatus(DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.toString());
        		wdsRepo.save(wds);
        	} else {
        		sendFinalDistributionStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR, watchdogError);
        		WatchdogDistributionStatus wds = new WatchdogDistributionStatus(iNotif.getDistributionID());
        		wds.setDistributionIdStatus(DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.toString());
        		wdsRepo.save(wds);
        	}
        	
        	

        } catch (Exception e) {
            LOGGER.error (MessageEnum.ASDC_GENERAL_EXCEPTION_ARG,
                          "Unexpected exception caught during the notification processing",  "ASDC", "treatNotification", MsoLogger.ErrorCode.SchemaError, "RuntimeException in treatNotification",
                          e);
            
          	try {
        		wd.executePatchAAI(iNotif.getDistributionID(), iNotif.getServiceInvariantUUID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name());
        		LOGGER.debug ("A&AI Updated succefully with Distribution Status of " + DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name());
        	}
        	catch(Exception aaiException) {
        		LOGGER.debug ("Exception in executePatchAAI(): " + aaiException.getMessage());
        		if(aaiException.getCause() != null) {
        			LOGGER.debug ("Exception caused by: " + aaiException.getCause().getMessage());
        		}
        	}
            
             sendFinalDistributionStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR, e.getMessage());
             
        	 WatchdogDistributionStatus wds = new WatchdogDistributionStatus(iNotif.getDistributionID());
        	 wds.setDistributionIdStatus(DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.toString());
        	 wdsRepo.save(wds);
            
        } finally {
            this.changeControllerStatus (ASDCControllerStatus.IDLE);
        }
    }

    protected void processResourceNotification (INotificationData iNotif) {
    	// For each artifact, create a structure describing the VFModule in a ordered flat level
    	VfResourceStructure resourceStructure = null;
    	ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
    	boolean deploySuccessful = true;
    	String errorMessage = null;

    	try {
    		
   			this.processCsarServiceArtifacts(iNotif, toscaResourceStructure);
   			
   			// Install a service with no resources, only the service itself
   			if (iNotif.getResources() == null || iNotif.getResources().size() < 1) {
   				
   				LOGGER.debug("No resources found for Service: " + iNotif.getServiceUUID());
				
   				try{
   					resourceStructure = new VfResourceStructure(iNotif,new ResourceInstance()); 
   					
					this.deployResourceStructure(resourceStructure, toscaResourceStructure);

			 	} catch(ArtifactInstallerException e){
			 		deploySuccessful = false;
			 		errorMessage = e.getMessage();
			 	}  
   			} else { // Services with resources
   		 	
    		for (IResourceInstance resource : iNotif.getResources()){
    			
    			resourceStructure = new VfResourceStructure(iNotif,resource);
    			
  	           	String resourceType = resourceStructure.getResourceInstance().getResourceType();
            	String category = resourceStructure.getResourceInstance().getCategory();
    				       	
                LOGGER.debug("Processing Resource Type: " + resourceType + " and Model UUID: " + resourceStructure.getResourceInstance().getResourceUUID());
                	
				if("VF".equals(resourceType) && !"Allotted Resource".equalsIgnoreCase(category)){
			
	    			for (IArtifactInfo artifact : resource.getArtifacts()) {
	    				IDistributionClientDownloadResult resultArtifact = this.downloadTheArtifact(artifact,
	    						iNotif.getDistributionID());
	    				if (resultArtifact != null) {
	    					if (ASDCConfiguration.VF_MODULES_METADATA.equals(artifact.getArtifactType())) {
	    						LOGGER.debug("VF_MODULE_ARTIFACT: "+new String(resultArtifact.getArtifactPayload(),"UTF-8"));
	    						LOGGER.debug(ASDCNotificationLogging.dumpVfModuleMetaDataList(resourceStructure.decodeVfModuleArtifact(resultArtifact.getArtifactPayload())));
	    					}
	    					resourceStructure.addArtifactToStructure(distributionClient,artifact, resultArtifact);
	    				}
	    			}
				}
				
				//Deploy All resources and artifacts
				LOGGER.debug("Preparing to deploy Service: " + iNotif.getServiceUUID());
				try{
					
					this.deployResourceStructure(resourceStructure, toscaResourceStructure);

			 	} catch(ArtifactInstallerException e){
			 		deploySuccessful = false;
			 		errorMessage = e.getMessage();
			 	}  
				
    		} 	
   		}
			 this.sendCsarDeployNotification(iNotif, resourceStructure, toscaResourceStructure, deploySuccessful, errorMessage);
    		
    	} catch (ASDCDownloadException | UnsupportedEncodingException e) {
    		LOGGER.error(MessageEnum.ASDC_GENERAL_EXCEPTION_ARG,
    				"Exception caught during Installation of artifact", "ASDC", "processResourceNotification", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in processResourceNotification", e);
    	}
    }
    protected void processCsarServiceArtifacts (INotificationData iNotif, ToscaResourceStructure toscaResourceStructure) {
    	
    	List<IArtifactInfo> serviceArtifacts = iNotif.getServiceArtifacts();
    	
    		for(IArtifactInfo artifact : serviceArtifacts){
    		
    			if(artifact.getArtifactType().equals(ASDCConfiguration.TOSCA_CSAR)){
 				
    				try{
    					
    					toscaResourceStructure.setToscaArtifact(artifact);
    					
    					IDistributionClientDownloadResult resultArtifact = this.downloadTheArtifact(artifact,iNotif.getDistributionID());
    					
    					writeArtifactToFile(artifact, resultArtifact);
    					
    					toscaResourceStructure.updateResourceStructure(artifact);
    					
    					toscaResourceStructure.setServiceVersion(iNotif.getServiceVersion());
    					
    					LOGGER.debug(ASDCNotificationLogging.dumpCSARNotification(iNotif, toscaResourceStructure));
    					

    				} catch(Exception e){
    					LOGGER.error(MessageEnum.ASDC_GENERAL_EXCEPTION_ARG,
    							"Exception caught during processCsarServiceArtifacts", "ASDC", "processCsarServiceArtifacts", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in processCsarServiceArtifacts", e);
    				}
    			}
    				
    		}
    }
    
    private static final String UNKNOWN="Unknown";
    
    /**
     * @return the address of the ASDC we are connected to.
     */
    public String getAddress () {
        if (asdcConfig != null) {
            return asdcConfig.getAsdcAddress ();
        }
        return UNKNOWN;
    }

    /**
     * @return the environment name of the ASDC we are connected to.
     */
    public String getEnvironment () {
        if (asdcConfig != null) {
            return asdcConfig.getEnvironmentName ();
        }
        return UNKNOWN;
    }

}
