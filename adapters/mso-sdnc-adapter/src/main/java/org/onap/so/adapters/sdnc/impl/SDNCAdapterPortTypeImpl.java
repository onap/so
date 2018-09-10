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

package org.onap.so.adapters.sdnc.impl;

import javax.annotation.PostConstruct;
import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;

import org.onap.so.adapters.sdnc.SDNCAdapterPortType;
import org.onap.so.adapters.sdnc.SDNCAdapterRequest;
import org.onap.so.adapters.sdnc.SDNCAdapterResponse;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//BPEL SDNCAdapter SOAP Web Service implementation
@WebService(serviceName = "SDNCAdapterService", endpointInterface = "org.onap.so.adapters.sdnc.SDNCAdapterPortType", targetNamespace = "http://org.onap/workflow/sdnc/adapter/wsdl/v1")
@Component
public class SDNCAdapterPortTypeImpl implements SDNCAdapterPortType {



	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA,SDNCAdapterPortTypeImpl.class);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
	
	@Autowired
	private SDNCRestClient sdncClient;
	
	@PostConstruct
	public void init () {
		msoLogger.info(MessageEnum.RA_INIT_SDNC_ADAPTER, "SDNC", "SDNCAdapterPortType", "");
	}

	/**
	 * Health Check web method.  Does nothing but return to show the adapter is deployed.
	 */
	@Override
	public void healthCheck ()
	{
		msoLogger.debug("Health check call in SDNC Adapter");
	}


	@Override
	public SDNCAdapterResponse sdncAdapter(SDNCAdapterRequest bpelRequest) {
		String bpelReqId = bpelRequest.getRequestHeader().getRequestId();
		String callbackUrl = bpelRequest.getRequestHeader().getCallbackUrl();
		try {
			sdncClient.executeRequest(bpelRequest);
		}
		catch (Exception e){
			String respMsg = "Error sending request to SDNC. Failed to start SDNC Client thread " + e.getMessage();
			msoLogger.error(MessageEnum.RA_SEND_REQUEST_SDNC_ERR, "SDNC", "", MsoLogger.ErrorCode.DataError, respMsg, e);
			alarmLogger.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, respMsg);
			SDNCResponse sdncResp = new SDNCResponse(bpelReqId);
			sdncResp.setRespCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			sdncResp.setRespMsg(respMsg);
			sdncClient.sendRespToBpel(callbackUrl, sdncResp);
		}
		return (new SDNCAdapterResponse());
	}
}
