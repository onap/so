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

package org.onap.so.apihandlerinfra.tenantisolation;

import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolation.process.ActivateVnfOperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolation.process.ActivateVnfStatusOperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolation.process.CreateEcompOperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolation.process.CreateVnfOperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolation.process.DeactivateVnfOperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Action;
import org.onap.so.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.onap.so.db.request.data.repository.OperationalEnvDistributionStatusRepository;
import org.onap.so.db.request.data.repository.OperationalEnvServiceModelStatusRepository;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.requestsdb.RequestsDBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TenantIsolationRunnable {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH, TenantIsolationRunnable.class);
	
	@Autowired 
	private RequestsDBHelper requestDb; 
	@Autowired 
	private CreateEcompOperationalEnvironment createEcompOpEnv;
	@Autowired 
	private CreateVnfOperationalEnvironment createVnfOpEnv;
	@Autowired 
	private ActivateVnfOperationalEnvironment activateVnfOpEnv;
	@Autowired 
	private DeactivateVnfOperationalEnvironment deactivateVnfOpEnv;
	@Autowired 
	private ActivateVnfStatusOperationalEnvironment activateVnfStatusOpEnv;
	@Autowired
	private OperationalEnvDistributionStatusRepository distributionStatusRepository;
	@Autowired
	private OperationalEnvServiceModelStatusRepository modelStatusRepository;
	
	@Async
	public void run(Action action, String operationalEnvType, CloudOrchestrationRequest cor, String requestId) throws ApiException {
		msoLogger.debug ("Starting threadExecution in TenantIsolationRunnable for Action " + action.name() + " and OperationalEnvType: " + operationalEnvType);
		try {
			
			if(Action.create.equals(action)) {
				if(OperationalEnvironment.ECOMP.name().equalsIgnoreCase(operationalEnvType)) {
					createEcompOpEnv.execute(requestId, cor);
				} else if(OperationalEnvironment.VNF.name().equalsIgnoreCase(operationalEnvType)) {
					createVnfOpEnv.execute(requestId, cor);
				} else {
                    ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.DataError).build();
                    ValidateException validateException = new ValidateException.Builder("Invalid OperationalEnvironment Type specified for Create Action",
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo).build();

                    throw validateException;
				}
			} else if(Action.activate.equals(action)) {
				activateVnfOpEnv.execute(requestId, cor, distributionStatusRepository, modelStatusRepository);
			} else if(Action.deactivate.equals(action)) {
				deactivateVnfOpEnv.execute(requestId, cor);
			} else if(Action.distributionStatus.equals(action)) {
				activateVnfStatusOpEnv.execute(requestId, cor, distributionStatusRepository, modelStatusRepository);
			} else {
                ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.DataError).build();
                ValidateException validateException = new ValidateException.Builder("Invalid Action specified: " + action,
                        HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(errorLoggerInfo).build();
                throw validateException;
			}
		}catch(ApiException e) {
            requestDb.updateInfraFailureCompletion(e.getMessage(), requestId, cor.getOperationalEnvironmentId());
            throw e;
        }
	}
}

