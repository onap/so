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

package org.onap.so.asdc.client;

import org.onap.sdc.api.consumer.IStatusCallback;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ASDCStatusCallBack implements IStatusCallback {

	@Autowired
	private ToscaResourceInstaller toscaInstaller;

	protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC,ASDCStatusCallBack.class);
	
	@Autowired
	private WatchdogDistributionStatusRepository watchdogDistributionStatusRepository;

	@Override
	public void activateCallback (IStatusData iStatus) {
		
		long startTime = System.currentTimeMillis ();
		UUIDChecker.generateUUID (LOGGER);
		MsoLogger.setServiceName ("ASDCStatusCallBack");
		MsoLogger.setLogContext (iStatus.getDistributionID (), iStatus.getComponentName());
		String event = "Receive a callback componentStatus in ASDC, for componentName: " + iStatus.getComponentName() + " and status of " + iStatus.getStatus() + " distributionID of " + iStatus.getDistributionID();

		try{

		  if(iStatus.getStatus() != null){	
			if(iStatus.getStatus().equals(DistributionStatusEnum.COMPONENT_DONE_OK) || iStatus.getStatus().equals(DistributionStatusEnum.COMPONENT_DONE_ERROR)) {
				WatchdogDistributionStatus watchdogDistributionStatus = watchdogDistributionStatusRepository.findOne(iStatus.getDistributionID ());
				if(watchdogDistributionStatus==null){
					watchdogDistributionStatus = new WatchdogDistributionStatus();
					watchdogDistributionStatus.setDistributionId(iStatus.getDistributionID ());
					watchdogDistributionStatusRepository.save(watchdogDistributionStatus);
				}
				LOGGER.debug(event); 
				toscaInstaller.installTheComponentStatus(iStatus);
				
			}
		  }
		}catch(ArtifactInstallerException e){
			LOGGER.error("Error in ASDCStatusCallback " + e.getMessage(),e);
			LOGGER.debug("Error in ASDCStatusCallback " + e.getMessage());
		}         
		LOGGER.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Completed the treatment of the notification");
	} 	
}