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

package org.openecomp.mso.client.sdnc.sync;

import javax.annotation.PostConstruct;
import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;

import org.openecomp.mso.client.sdnc.beans.SDNCRequest;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

//BPEL SDNCAdapter SOAP Web Service implementation
@WebService(serviceName = "SDNCAdapterService", endpointInterface = "org.openecomp.mso.client.sdnc.sync.SDNCAdapterPortType", targetNamespace = "http://org.openecomp/workflow/sdnc/ad")
public class SDNCAdapterPortTypeImpl implements SDNCAdapterPortType {

	private MsoPropertiesFactory msoPropertiesFactory=new MsoPropertiesFactory();

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
	private static final String LOG_SERVICE_NAME = "MSO-BPMN:MSO-SDNCAdapter.";
	private static final String LOG_REPLY_NAME = "MSO-SDNCAdapter:MSO-BPMN.";
	public static final String MSO_PROP_SDNC_ADAPTER="MSO_PROP_SDNC_ADAPTER";

	@PostConstruct
	public void init () {
		msoLogger.info(MessageEnum.RA_INIT_SDNC_ADAPTER, "SDNC", "SDNCAdapterPortType");
	}

	/**
	 * Health Check web method.  Does nothing but return to show the adapter is deployed.
	 */
	@Override
	public void healthCheck ()
	{
		msoLogger.debug("Health check call in SDNC Adapter");
	}

	public static String getProperty(String key, String defaultValue, MsoPropertiesFactory msoPropertiesFactoryp) {
		String value;
		try {
			value = msoPropertiesFactoryp.getMsoJavaProperties(MSO_PROP_SDNC_ADAPTER).getProperty(key, defaultValue);
		} catch (MsoPropertiesException e) {
			msoLogger.error (MessageEnum.NO_PROPERTIES, "Unknown. Mso Properties ID not found in cache: " + MSO_PROP_SDNC_ADAPTER, "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception - Mso Properties ID not found in cache", e);
			return null;
		}
		msoLogger.debug("Config read for " + MSO_PROP_SDNC_ADAPTER + " - key:" + key + " value:" + value);
		return value;
	}

	@Override
	public SDNCAdapterResponse sdncAdapter(SDNCRequest bpelRequest) {
		String bpelReqId = bpelRequest.getRequestId();
		String callbackUrl = bpelRequest.getCallbackUrl();
		long startTime = System.currentTimeMillis ();
		MsoLogger.setLogContext(SDNCRequestIdUtil.getSDNCOriginalRequestId (bpelReqId), bpelRequest.getSvcInstanceId());
		MsoLogger.setServiceName (bpelRequest.getSvcAction().toString());
		msoLogger.info(MessageEnum.RA_RECEIVE_BPEL_REQUEST, bpelReqId, callbackUrl, "SDNC", "");

		SDNCSyncRpcClient sdncClient = new SDNCSyncRpcClient(bpelRequest,msoPropertiesFactory);
		long subStartTime = System.currentTimeMillis ();
		try {
			Thread sdncClientThread = new Thread(sdncClient);
			sdncClientThread.start();
		}
		catch (Exception e){
			String respMsg = "Error sending request to SDNC. Failed to start SDNC Client thread " + e.getMessage();
			msoLogger.error(MessageEnum.RA_SEND_REQUEST_SDNC_ERR, "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception sending request to SDNC. Failed to start SDNC Client thread", e);
			alarmLogger.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, respMsg);
			SDNCResponse sdncResp = new SDNCResponse(bpelReqId);
			sdncResp.setRespCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			sdncResp.setRespMsg(respMsg);
		}

		msoLogger.debug("Sending synchronous response to BPEL");
		SDNCAdapterResponse wsResp = new SDNCAdapterResponse();
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
		return (wsResp);
	}

	@Override
	public org.openecomp.mso.client.sdnc.sync.SDNCAdapterResponse sdncAdapter(
			org.openecomp.mso.client.sdnc.sync.SDNCAdapterRequest sdncAdapterRequest) {
		// TODO Auto-generated method stub
		return null;
	}
}
