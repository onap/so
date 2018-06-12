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

package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import org.apache.http.HttpStatus;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.exceptions.ApiException;
import org.openecomp.mso.apihandlerinfra.exceptions.ValidateException;
import org.openecomp.mso.apihandlerinfra.logging.ErrorLoggerInfo;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap.DmaapOperationalEnvClient;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientObjectBuilder;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateEcompOperationalEnvironment {
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH, CreateEcompOperationalEnvironment.class);
    
	@Autowired 
	private AAIClientObjectBuilder aaiClientObjectBuilder;
	@Autowired 
	private AAIClientHelper aaiHelper;
	@Autowired 
	private RequestsDBHelper requestDb;
	@Autowired 
	private DmaapOperationalEnvClient dmaapClient;

	public void execute(String requestId, CloudOrchestrationRequest request) throws ApiException{

		//Create ECOMP Managing Environment object in A&AI
			aaiHelper.createOperationalEnvironment(aaiClientObjectBuilder.buildAAIOperationalEnvironment("ACTIVE", request));

			// Call client to publish to DMaap
        try {
        	msoLogger.debug("1" + request.getOperationalEnvironmentId());
        	msoLogger.debug("2" + request.getRequestDetails().getRequestInfo().getInstanceName());
        	msoLogger.debug("3" + request.getRequestDetails().getRequestParameters().getOperationalEnvironmentType().toString());
        	msoLogger.debug("4" + request.getRequestDetails().getRequestParameters().getTenantContext());
        	msoLogger.debug("5" + request.getRequestDetails().getRequestParameters().getWorkloadContext());


            dmaapClient.dmaapPublishOperationalEnvRequest(request.getOperationalEnvironmentId(),
                    request.getRequestDetails().getRequestInfo().getInstanceName(),
                    request.getRequestDetails().getRequestParameters().getOperationalEnvironmentType().toString(),
                    request.getRequestDetails().getRequestParameters().getTenantContext(),
                    request.getRequestDetails().getRequestParameters().getWorkloadContext(),
                    "Create");
        }catch(Exception e){
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.DataError).build();
            ValidateException validateException = new ValidateException.Builder("Could not publish DMaap", HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                    .errorInfo(errorLoggerInfo).build();
            requestDb.updateInfraFailureCompletion(e.getMessage(), requestId, request.getOperationalEnvironmentId());
            throw validateException;
        }
			//Update request database
			requestDb.updateInfraSuccessCompletion("SUCCESSFULLY Created ECOMP OperationalEnvironment.", requestId, request.getOperationalEnvironmentId());

	}
}
