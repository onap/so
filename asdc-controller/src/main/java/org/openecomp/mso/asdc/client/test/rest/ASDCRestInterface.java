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

package org.openecomp.mso.asdc.client.test.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.mso.asdc.client.ASDCController;
import org.openecomp.mso.asdc.client.test.emulators.DistributionClientEmulator;
import org.openecomp.mso.asdc.client.test.emulators.JsonNotificationData;
import org.openecomp.mso.asdc.client.test.emulators.JsonStatusData;
import org.openecomp.mso.asdc.installer.heat.ToscaResourceInstaller;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * This is a TEST only rest interface.  It is not used in production, it is used to aid in testing the ASDC service on jboss without the need to be connected 
 * to the ASDC service broker.  It starts the test at the treatNotification step and simulates both the notification step as well as the artifact download step.
 * 
 * i.e. http://localhost:8080/asdc/treatNotification/v1
 * 
 * i.e. http://localhost:8080/asdc/statusData/v1
 * 
 * @author jm5423
 *
 */

@Path("/")
public class ASDCRestInterface {

	private static DistributionClientEmulator distributionClientEmulator;
	
	private static JsonNotificationData notifDataWithoutModuleInfo;
	
	private static JsonStatusData statusData;
	
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC);
    
	@GET
	@Path("/treatNotification/v1")
	@Produces(MediaType.APPLICATION_JSON)
	public Response invokeASDCService(String request) {
		
		try{
			distributionClientEmulator = new DistributionClientEmulator("resource-examples/");
			notifDataWithoutModuleInfo = JsonNotificationData.instantiateNotifFromJsonFile("resource-examples/");
		
			ASDCController asdcController = new ASDCController("asdc-controller1", distributionClientEmulator);
			LOGGER.info(MessageEnum.ASDC_INIT_ASDC_CLIENT_EXC, notifDataWithoutModuleInfo.getServiceUUID(), "ASDC", "initASDC()");
			asdcController.initASDC();
			LOGGER.info(MessageEnum.ASDC_INIT_ASDC_CLIENT_EXC, notifDataWithoutModuleInfo.getServiceUUID(), "ASDC", "treatNotification()");
			asdcController.treatNotification(notifDataWithoutModuleInfo);
			LOGGER.info(MessageEnum.ASDC_INIT_ASDC_CLIENT_EXC, notifDataWithoutModuleInfo.getServiceUUID(), "ASDC", "closeASDC()");
			asdcController.closeASDC();
		}catch(Exception e){
			System.out.println("Error caught " + e.getMessage());
			LOGGER.error(MessageEnum.ASDC_GENERAL_EXCEPTION,
					"Exception caught during ASDCRestInterface", "ASDC", "invokeASDCService", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in invokeASDCService", e);
		}
		System.out.println("ASDC Updates are complete");
		LOGGER.info(MessageEnum.ASDC_ARTIFACT_DEPLOY_SUC, notifDataWithoutModuleInfo.getServiceUUID(), "ASDC", "ASDC Updates Are Complete");
		
		return null;
	}
	
	@GET
	@Path("/statusData/v1")
	@Produces(MediaType.APPLICATION_JSON)
	public Response invokeASDCStatusData(String request) {
		
		ToscaResourceInstaller toscaInstaller = new ToscaResourceInstaller();
		
		try{
			distributionClientEmulator = new DistributionClientEmulator("resource-examples/");
			statusData = JsonStatusData.instantiateNotifFromJsonFile("resource-examples/");
		
			ASDCController asdcController = new ASDCController("asdc-controller1", distributionClientEmulator);
			//LOGGER.info(MessageEnum.ASDC_INIT_ASDC_CLIENT_EXC, notifDataWithoutModuleInfo.getServiceUUID(), "ASDC", "initASDC()");
			asdcController.initASDC();
			//LOGGER.info(MessageEnum.ASDC_INIT_ASDC_CLIENT_EXC, notifDataWithoutModuleInfo.getServiceUUID(), "ASDC", "treatNotification()");
			toscaInstaller.installTheComponentStatus(statusData);
			//asdcController.treatNotification(notifDataWithoutModuleInfo);
			//LOGGER.info(MessageEnum.ASDC_INIT_ASDC_CLIENT_EXC, notifDataWithoutModuleInfo.getServiceUUID(), "ASDC", "closeASDC()");
			asdcController.closeASDC();
		}catch(Exception e){
			System.out.println("Error caught " + e.getMessage());
			LOGGER.error(MessageEnum.ASDC_GENERAL_EXCEPTION,
					"Exception caught during ASDCRestInterface", "ASDC", "invokeASDCService", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in invokeASDCService", e);
		}
		System.out.println("ASDC Updates are complete");
		LOGGER.info(MessageEnum.ASDC_ARTIFACT_DEPLOY_SUC, statusData.getDistributionID(), "ASDC", "ASDC Updates Are Complete");
		
		return null;
	}
}
