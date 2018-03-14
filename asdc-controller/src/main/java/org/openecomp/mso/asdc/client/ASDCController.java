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

package org.openecomp.mso.asdc.client;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.IDistributionStatusMessage;
import org.openecomp.sdc.api.consumer.IFinalDistrStatusMessage;
import org.openecomp.sdc.api.consumer.INotificationCallback;
import org.openecomp.sdc.api.consumer.IStatusCallback;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.notification.IStatusData;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.impl.DistributionClientFactory;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import org.openecomp.sdc.utils.DistributionStatusEnum;
import org.openecomp.mso.asdc.client.exceptions.ASDCControllerException;
import org.openecomp.mso.asdc.client.exceptions.ASDCDownloadException;
import org.openecomp.mso.asdc.client.exceptions.ASDCParametersException;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.asdc.installer.IVfResourceInstaller;
import org.openecomp.mso.asdc.installer.ToscaResourceStructure;
import org.openecomp.mso.asdc.installer.VfResourceStructure;
import org.openecomp.mso.asdc.installer.heat.ToscaResourceInstaller;
import org.openecomp.mso.asdc.installer.heat.VfResourceInstaller;
import org.openecomp.mso.asdc.tenantIsolation.DistributionStatus;
import org.openecomp.mso.asdc.tenantIsolation.WatchdogDistribution;
import org.openecomp.mso.asdc.util.ASDCNotificationLogging;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.WatchdogDistributionStatusDb;
import org.openecomp.mso.utils.UUIDChecker;

public class ASDCController {

    protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC);

    protected static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();

    protected boolean isAsdcClientAutoManaged = false;

    protected String controllerName;

    protected ToscaResourceInstaller toscaInstaller;
    
    private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
    
   
   private final class ResourceInstance implements IResourceInstance {
    	       
        @Override
        public String getResourceInstanceName(){
        	return new String();
        }
        
        @Override
        public String getResourceName(){
        	return new String();
        }
        
        @Override
        public String getResourceVersion(){
        	return new String();
        }
        
        @Override
        public String getResourceType(){
        	return new String();
        }
        
        @Override
        public String getResourceUUID(){
        	return new String();
        }
        
        // Method descriptor #10 ()Ljava/util/List;
        @Override
        public java.util.List getArtifacts(){
        	return new ArrayList();
        }
        
        @Override
        public String getResourceInvariantUUID(){
        	return new String();
        }
        
        @Override
        public String getResourceCustomizationUUID(){
        	return new String();
        }
        
        @Override
        public String getCategory(){
        	return new String();
        }
        
        @Override
        public String getSubcategory(){
        	return new String();
        }
    	
    }
    
   private final class ASDCStatusCallBack implements IStatusCallback {
	       	
        @Override
        public void activateCallback (IStatusData iStatus) {
         	
            long startTime = System.currentTimeMillis ();
            UUIDChecker.generateUUID (LOGGER);
            MsoLogger.setServiceName ("ASDCStatusCallBack");
            MsoLogger.setLogContext (iStatus.getDistributionID (), iStatus.getComponentName());
            String event = "Receive a callback componentStatus in ASDC, for componentName: " + iStatus.getComponentName() + " and status of " + iStatus.getStatus() + " distributionID of " + iStatus.getDistributionID() + 
            		       " consumerID of " + iStatus.getConsumerID() + " errorReason of " + iStatus.getErrorReason();
            
            try{
            	
            	if (iStatus.getStatus() == DistributionStatusEnum.COMPONENT_DONE_OK || 
            		iStatus.getStatus() == DistributionStatusEnum.COMPONENT_DONE_ERROR) {
            	            		
            		LOGGER.debug(event);
            
            		toscaInstaller.installTheComponentStatus(iStatus);
            	
            	}
            
            }catch(ArtifactInstallerException e){
             	System.out.println("Error in ASDCStatusCallback " + e.getMessage());
             	LOGGER.debug("Error in ASDCStatusCallback " + e.getMessage());
            }
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Completed the Status Call Back"); 
        }
    	
    }
    

    /**
     * Inner class for Notification callback
     *
     *
     */
    private final class ASDCNotificationCallBack implements INotificationCallback {

        private ASDCController asdcController;

        ASDCNotificationCallBack (ASDCController controller) {
            asdcController = controller;
        }

        /**
         * This method can be called multiple times at the same moment.
         * The controller must be thread safe !
         */
        @Override
        public void activateCallback (INotificationData iNotif) {
            long startTime = System.currentTimeMillis ();
            UUIDChecker.generateUUID (LOGGER);
            MsoLogger.setServiceName ("NotificationHandler");
            MsoLogger.setLogContext (iNotif.getDistributionID (), iNotif.getServiceUUID ());
            String event = "Receive a callback notification in ASDC, nb of resources: " + iNotif.getResources ().size ();
            LOGGER.debug(event);
            asdcController.treatNotification (iNotif);
            LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Completed the treatment of the notification");
        }
    }
    

    // ***** Controller STATUS code

    protected int nbOfNotificationsOngoing = 0;

    public int getNbOfNotificationsOngoing () {
        return nbOfNotificationsOngoing;
    }

    private ASDCControllerStatus controllerStatus = ASDCControllerStatus.STOPPED;

    protected synchronized final void changeControllerStatus (ASDCControllerStatus newControllerStatus) {
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

    public synchronized final ASDCControllerStatus getControllerStatus () {
        return this.controllerStatus;
    }

    // ***** END of Controller STATUS code
    protected ASDCConfiguration asdcConfig;
    private IDistributionClient distributionClient;
    private IVfResourceInstaller resourceInstaller;

    public ASDCController (String controllerConfigName) {
        isAsdcClientAutoManaged = true;
        this.controllerName = controllerConfigName;
        this.resourceInstaller = new VfResourceInstaller();
        toscaInstaller = new ToscaResourceInstaller();
    }

    public ASDCController (String controllerConfigName, IDistributionClient asdcClient, IVfResourceInstaller resourceinstaller) {

        distributionClient = asdcClient;
        this.resourceInstaller = resourceinstaller;
        this.controllerName = controllerConfigName;
    }

    public ASDCController (String controllerConfigName,IDistributionClient asdcClient) {
        distributionClient = asdcClient;
        this.controllerName = controllerConfigName;
        this.resourceInstaller = new VfResourceInstaller();
        toscaInstaller = new ToscaResourceInstaller();
    }

    /**
     * This method refresh the ASDC Controller config and restart the client.
     *
     * @return true if config has been reloaded, false otherwise
     * @throws ASDCControllerException If case of issue with the init or close called during the config reload
     * @throws ASDCParametersException If there is an issue with the parameters
     * @throws IOException In case of the key file could not be loaded properly
     */
    public boolean updateConfigIfNeeded () throws ASDCParametersException, ASDCControllerException, IOException {
        LOGGER.debug ("Checking whether ASDC config must be reloaded");

        try {
            if (this.asdcConfig != null && this.asdcConfig.hasASDCConfigChanged ()) {
                LOGGER.debug ("ASDC Config must be reloaded");
                this.closeASDC ();
                this.asdcConfig.refreshASDCConfig ();
                this.initASDC ();
                return true;
            } else {
                LOGGER.debug ("ASDC Config must NOT be reloaded");
                return false;
            }
        } catch (ASDCParametersException ep) {
            // Try to close it at least to make it consistent with the file specified
            // We cannot let it run with a different config file, even if it's bad.
            // This call could potentially throw a ASDCController exception if the controller is currently BUSY.
            this.closeASDC ();

            throw ep;
        }
    }

    /**
     * This method initializes the ASDC Controller and the ASDC Client.
     *
     * @throws ASDCControllerException It throws an exception if the ASDC Client cannot be instantiated or if an init
     *         attempt is done when already initialized
     * @throws ASDCParametersException If there is an issue with the parameters provided
     * @throws IOException In case of issues when trying to load the key file
     */
    public void initASDC () throws ASDCControllerException, ASDCParametersException, IOException {
        String event = "Initialize the ASDC Controller";
        MsoLogger.setServiceName ("InitASDC");
        LOGGER.debug (event);
        if (this.getControllerStatus () != ASDCControllerStatus.STOPPED) {
            String endEvent = "The controller is already initialized, call the closeASDC method first";
            throw new ASDCControllerException (endEvent);
        }

        if (asdcConfig == null) {
            asdcConfig = new ASDCConfiguration (this.controllerName);

        }
        // attempt to refresh during init as MsoProperties is may be pointing to an old file
        // Be careful this is static in MsoProperties
        asdcConfig.refreshASDCConfig ();

        if (this.distributionClient == null) {
            distributionClient = DistributionClientFactory.createDistributionClient ();
        }
        long initStartTime = System.currentTimeMillis ();
        IDistributionClientResult result = this.distributionClient.init (asdcConfig,
                                                                         new ASDCNotificationCallBack (this), new ASDCStatusCallBack());
        if (!result.getDistributionActionResult ().equals (DistributionActionResultEnum.SUCCESS)) {
            String endEvent = "ASDC distribution client init failed with reason:"
                              + result.getDistributionMessageResult ();
            LOGGER.recordMetricEvent (initStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, "Initialization of the ASDC Controller failed with reason:" + result.getDistributionMessageResult (), "ASDC", "init", null);
            LOGGER.debug (endEvent);
            asdcConfig = null;

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
            asdcConfig = null;
            this.changeControllerStatus (ASDCControllerStatus.STOPPED);
            throw new ASDCControllerException ("Startup of the ASDC Controller failed with reason: "
                                               + result.getDistributionMessageResult ());
        }
        LOGGER.recordMetricEvent (clientstartStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully start ASDC distribution client", "ASDC", "start", null);


        this.changeControllerStatus (ASDCControllerStatus.IDLE);
        LOGGER.info (MessageEnum.ASDC_INIT_ASDC_CLIENT_SUC, "ASDC", "changeControllerStatus");
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

    private final static String UUID_PARAM = "(UUID:";

    private IDistributionClientDownloadResult downloadTheArtifact (IArtifactInfo artifact,
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
    		IDistributionClientDownloadResult resultArtifact) throws ASDCDownloadException {

    	LOGGER.debug ("Trying to download the artifact : " + artifact.getArtifactURL ()
    			+ UUID_PARAM
    			+ artifact.getArtifactUUID ()
    			+ ")");
    	
    	File spoolFile = new File(System.getProperty("mso.config.path") + "/ASDC" + "/" + artifact.getArtifactName()); 
    	  	
    	
    	byte[] payloadBytes = resultArtifact.getArtifactPayload();
    	
    	try {
    		LOGGER.info(MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF, "***WRITE FILE ARTIFACT NAME", "ASDC", artifact.getArtifactName());

    		FileOutputStream outFile = new FileOutputStream(System.getProperty("mso.config.path") + "/ASDC" + "/" + artifact.getArtifactName());
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


    private void sendDeployNotificationsForResource(VfResourceStructure vfResourceStructure,DistributionStatusEnum distribStatus, String errorReason) {

    	for (IArtifactInfo artifactInfo : vfResourceStructure.getResourceInstance().getArtifacts()) {

    		if ((DistributionStatusEnum.DEPLOY_OK.equals(distribStatus) && !artifactInfo.getArtifactType().equalsIgnoreCase("OTHER"))
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
    
    private void sendCsarDeployNotification(INotificationData iNotif, VfResourceStructure resourceStructure, ToscaResourceStructure toscaResourceStructure, boolean deploySuccessful, String errorReason) {
    	
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
    
    private void deployResourceStructure (VfResourceStructure resourceStructure, ToscaResourceStructure toscaResourceStructure) throws ArtifactInstallerException {

    	LOGGER.info (MessageEnum.ASDC_START_DEPLOY_ARTIFACT, resourceStructure.getResourceInstance().getResourceInstanceName(), resourceStructure.getResourceInstance().getResourceUUID(), "ASDC", "deployResourceStructure");
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

    private void sendASDCNotification (NotificationType notificationType,
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
            // TODO: May be a list containing the unsent notification should be
            // kept
            LOGGER.warn (MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC, "ASDC", "sendASDCNotification", MsoLogger.ErrorCode.SchemaError, "RuntimeException - sendASDCNotification", e);
        }
        LOGGER.recordMetricEvent (subStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully sent notification to ASDC", "ASDC", action, null);
    }
    
    private void sendFinalDistributionStatus (
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
    		// TODO: May be a list containing the unsent notification should be
    		// kept
    		LOGGER.debug ("Exception caught in sendFinalDistributionStatus " + e.getMessage());
    		LOGGER.warn (MessageEnum.ASDC_SEND_NOTIF_ASDC_EXEC, "ASDC", "sendASDCNotification", MsoLogger.ErrorCode.SchemaError, "RuntimeException - sendASDCNotification", e);
    	}
    	LOGGER.recordMetricEvent (subStarttime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully sent Final notification to ASDC", "ASDC", null, null);
    }

    public void treatNotification (INotificationData iNotif) {

    	int noOfArtifacts = 0;
    	WatchdogDistribution wd = new WatchdogDistribution();

    	for (IResourceInstance resource : iNotif.getResources ()) {
    		noOfArtifacts += resource.getArtifacts ().size ();
    	}
        LOGGER.info (MessageEnum.ASDC_RECEIVE_CALLBACK_NOTIF,
                     String.valueOf (noOfArtifacts),
                     iNotif.getServiceUUID (), "ASDC", "treatNotification");

        try {
        	LOGGER.debug(ASDCNotificationLogging.dumpASDCNotification(iNotif));
			LOGGER.info(MessageEnum.ASDC_RECEIVE_SERVICE_NOTIF, iNotif.getServiceUUID(), "ASDC", "treatNotification");
			this.changeControllerStatus(ASDCControllerStatus.BUSY);
			
			
						
			// Process only the Resource artifacts in MSO
			for (IResourceInstance resource : iNotif.getResources()) {
				
				// We process only VNF(VF), Network(VL) and PNF resources on MSO Side
				//if ("VF".equals(resource.getResourceType()) || "VL".equals(resource.getResourceType()) || "PNF".equals(resource.getResourceType())){
				this.processResourceNotification(iNotif,resource);
				//}

			}
			
			//Handle services without any resources	
			if (iNotif.getResources() == null || iNotif.getResources().size() < 1){
				
				this.processResourceNotification(iNotif, new ResourceInstance());
			}
			
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
        	WatchdogDistributionStatusDb wdDistributionStatus = WatchdogDistributionStatusDb.getInstance();
        	
						
        	while(componentsComplete == false && (System.currentTimeMillis() - initialStartTime) < watchDogTimeout)
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
        	
        	if(componentsComplete == false){
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
        		wdDistributionStatus.updateWatchdogDistributionIdStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.name());        
        	} else {
        		sendFinalDistributionStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR, watchdogError);
        		wdDistributionStatus.updateWatchdogDistributionIdStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name());        
        	}
        	
        	

        } catch (Exception e) {
            LOGGER.error (MessageEnum.ASDC_GENERAL_EXCEPTION_ARG,
                          "Unexpected exception caught during the notification processing", "ASDC", "treatNotification", MsoLogger.ErrorCode.SchemaError, "RuntimeException in treatNotification",
                          e);
            
             sendFinalDistributionStatus(iNotif.getDistributionID(), DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR, e.getMessage());
            
        } finally {
            this.changeControllerStatus (ASDCControllerStatus.IDLE);
        }
    }


    private void processResourceNotification (INotificationData iNotif,IResourceInstance resource) {
		// For each artifact, create a structure describing the VFModule in a ordered flat level
    	VfResourceStructure resourceStructure = new VfResourceStructure(iNotif,resource);
    	ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
    	boolean deploySuccessful = true;
    	String errorMessage = null;

		try {

			if (!this.checkResourceAlreadyDeployed(resourceStructure)) {
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
				this.processCsarServiceArtifacts(iNotif, toscaResourceStructure);
				
				try{
				
					this.deployResourceStructure(resourceStructure, toscaResourceStructure);
					
				} catch(ArtifactInstallerException e){
					deploySuccessful = false;
					errorMessage = e.getMessage();
				}
												
					this.sendCsarDeployNotification(iNotif, resourceStructure, toscaResourceStructure, deploySuccessful, errorMessage);
					


		}
		} catch (ArtifactInstallerException | ASDCDownloadException | UnsupportedEncodingException e) {
			LOGGER.error(MessageEnum.ASDC_GENERAL_EXCEPTION_ARG,
					"Exception caught during Installation of artifact", "ASDC", "processResourceNotification", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in processResourceNotification", e);
		}
    }

    private void processCsarServiceArtifacts (INotificationData iNotif, ToscaResourceStructure toscaResourceStructure) {
    	
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
    					System.out.println("Whats the error " + e.getMessage());
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
