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

package org.onap.so.asdc.client.test.rest;


import java.io.IOException;

import javax.transaction.Transactional;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.onap.so.asdc.client.ASDCController;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.onap.so.asdc.client.exceptions.ASDCParametersException;
import org.onap.so.asdc.client.test.emulators.DistributionClientEmulator;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.asdc.client.test.emulators.JsonStatusData;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
@Component
@Profile("test")
public class ASDCRestInterface {

	private static DistributionClientEmulator distributionClientEmulator;
	
	private static JsonStatusData statusData;
	
	private static final Logger logger = LoggerFactory.getLogger(ASDCRestInterface.class );
	
	@Autowired
	private ASDCController asdcController;
	
	@Autowired
	private ToscaResourceInstaller toscaInstaller;

	@POST
	@Path("/treatNotification/v1")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response invokeASDCService(NotificationDataImpl request, @HeaderParam("resource-location") String resourceLocation)
			throws ASDCControllerException, ASDCParametersException, IOException {
		distributionClientEmulator = new DistributionClientEmulator(resourceLocation);
		
		asdcController.setControllerName("asdc-controller1");
		asdcController.setDistributionClient(distributionClientEmulator);	
		asdcController.initASDC();	
		asdcController.treatNotification(request);
		asdcController.closeASDC();
		return Response.status(200).build();
	}
	
	@POST
	@Path("/statusData/v1")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response invokeASDCStatusData(String request) {
				
		try{
			distributionClientEmulator = new DistributionClientEmulator("resource-examples/");
			statusData = JsonStatusData.instantiateNotifFromJsonFile("resource-examples/");
		
			ASDCController asdcController = new ASDCController("asdc-controller1", distributionClientEmulator);
			asdcController.initASDC();
			toscaInstaller.installTheComponentStatus(statusData);
			asdcController.closeASDC();
		}catch(Exception e){
			logger.info("Error caught " + e.getMessage());
			logger.error("{} {} {} {} {} {}", MessageEnum.ASDC_GENERAL_EXCEPTION.toString(),
				"Exception caught during ASDCRestInterface", "ASDC", "invokeASDCService",
				ErrorCode.BusinessProcesssError.getValue(), "Exception in invokeASDCService", e);
		}
		logger.info("ASDC Updates are complete");
		logger.info("{} {} {} {}", MessageEnum.ASDC_ARTIFACT_DEPLOY_SUC.toString(), statusData.getDistributionID(), "ASDC",
			"ASDC Updates Are Complete");
		return null;
	}
}
