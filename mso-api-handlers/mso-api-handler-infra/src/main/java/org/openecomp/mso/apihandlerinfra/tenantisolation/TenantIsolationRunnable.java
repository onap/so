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

package org.openecomp.mso.apihandlerinfra.tenantisolation;

import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.apihandlerinfra.tenantisolation.process.OperationalEnvironmentProcess;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Action;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDBHelper;

public class TenantIsolationRunnable implements Runnable {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
	private OperationalEnvironmentProcessFactory factory = null;
	private Action action;
	private String operationalEnvType;
	private CloudOrchestrationRequest cor;
	private String requestId;
	protected RequestsDBHelper requestDb;

	@Override
	public void run() {
		msoLogger.debug ("Starting threadExecution in TenantIsolationRunnable for Action " + action.name() + " and OperationalEnvType: " + operationalEnvType);
		try {
			OperationalEnvironmentProcess isolation = getFactory().getOperationalEnvironmentProcess(action, operationalEnvType, cor, requestId);
			isolation.execute();
		} catch(Exception e) {
			msoLogger.debug ("Exception during Thread initiation: ", e);
			msoLogger.error (MessageEnum.APIH_GENERAL_EXCEPTION, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.UnknownError, null, e);
			getRequestDb().updateInfraFailureCompletion(e.getMessage(), requestId, cor.getOperationalEnvironmentId());
		}
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getOperationalEnvType() {
		return operationalEnvType;
	}

	public void setOperationalEnvType(String operationalEnvType) {
		this.operationalEnvType = operationalEnvType;
	}

	public CloudOrchestrationRequest getCor() {
		return cor;
	}

	public void setCor(CloudOrchestrationRequest cor) {
		this.cor = cor;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public OperationalEnvironmentProcessFactory getFactory() {
		if(factory == null) {
			factory = new OperationalEnvironmentProcessFactory();
		}
		return factory;
	}

	public void setFactory(OperationalEnvironmentProcessFactory factory) {
		this.factory = factory;
	}
	
	protected RequestsDBHelper getRequestDb() {
		if(requestDb == null) {
			requestDb = new RequestsDBHelper();
		}
		return requestDb;
	}
	
	protected void setRequestsDBHelper(RequestsDBHelper helper) {
		this.requestDb = helper;
	}
}
