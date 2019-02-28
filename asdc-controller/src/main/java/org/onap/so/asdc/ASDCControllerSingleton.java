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

package org.onap.so.asdc;

import javax.annotation.PreDestroy;

import org.onap.so.asdc.client.ASDCController;
import org.onap.so.asdc.client.exceptions.ASDCControllerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;


@Component
@Profile("!test")
public class ASDCControllerSingleton {
   
   
    @Autowired
    private ASDCController asdcController;
    private static Logger logger = LoggerFactory.getLogger(ASDCControllerSingleton.class);
  


    @Scheduled (fixedRate = 50000)
	public void periodicControllerTask() {
			try {
				int randomNumber = new SecureRandom().nextInt(Integer.MAX_VALUE);
				asdcController.setControllerName("mso-controller" + randomNumber);
				asdcController.initASDC();
			} catch (ASDCControllerException e) {
				logger.error("Exception occurred", e);
			}
	}
   
   @PreDestroy
   private void terminate () {
		 try {
			 asdcController.closeASDC();
		 } catch (ASDCControllerException e) {
			 logger.error("Exception occurred", e);
		 }
	 }

}
