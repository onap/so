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


import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import org.openecomp.mso.logger.MessageEnum;
public class RequestTunables {

	private MsoPropertiesFactory msoPropertiesFactory;
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
	public static final String MSO_PROP_SDNC_ADAPTER="MSO_PROP_SDNC_ADAPTER";

	//criteria
	private String reqId = "";
	private String msoAction = "";
	private String operation = "";
	private String action = "";

	//tunables
	private String reqMethod = "POST";
	private String sdncUrl = null;
	private String timeout = "60000";
	private String headerName = "sdnc-request-header";
	private String namespace = "";
	private String asyncInd = "N"; //future use

	private String sdncaNotificationUrl = null;

	public RequestTunables(String reqId, String msoAction, String operation, String action, MsoPropertiesFactory msoPropFactory) {
		super();
		msoPropertiesFactory = msoPropFactory;
		if (reqId != null) {
            this.reqId = reqId;
        }
		if (msoAction != null) {
            this.msoAction = msoAction;
        }
		if (operation != null) {
            this.operation = operation;
        }
		if (action != null) {
            this.action = action;
        }
	}

	public String getReqId() {
		return reqId;
	}
	public void setReqId(String reqId) {
		this.reqId = reqId;
	}
	public String getReqMethod() {
		return reqMethod;
	}
	public void setReqMethod(String reqMethod) {
		this.reqMethod = reqMethod;
	}
	public String getMsoAction() {
		return msoAction;
	}
	public void setMsoAction(String msoAction) {
		this.msoAction = msoAction;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getSdncUrl() {
		return sdncUrl;
	}
	public void setSdncUrl(String sdncUrl) {
		this.sdncUrl = sdncUrl;
	}
	public String getTimeout() {
		return timeout;
	}
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}
	public String getAsyncInd() {
		return asyncInd;
	}
	public void setAsyncInd(String asyncInd) {
		this.asyncInd = asyncInd;
	}
	public String getHeaderName() {
		return headerName;
	}
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}


	public String getSdncaNotificationUrl() {
		return sdncaNotificationUrl;
	}

	public void setSdncaNotificationUrl(String sdncaNotificationUrl) {
		this.sdncaNotificationUrl = sdncaNotificationUrl;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String toString() {
		return "RequestTunables [reqId=" + reqId + ", msoAction=" + msoAction
				+ ", operation=" + operation + ", action=" + action
				+ ", reqMethod=" + reqMethod + ", sdncUrl=" + sdncUrl
				+ ", timeout=" + timeout + ", headerName=" + headerName
				+ ", sdncaNotificationUrl=" + sdncaNotificationUrl
				+ ", namespace=" + namespace + "]";
	}

	public void setTunables()
	{
		String error = null;
		String key = null;
		if ("query".equals(action)) { //due to variable format for operation eg services/layer3-service-list/8fe4ba4f-35cf-4d9b-a04a-fd3f5d4c5cc9
			key = Constants.REQUEST_TUNABLES + "." + msoAction + ".." + action;
			msoLogger.debug("Generated key: " + key);
		}
		else if ("put".equals(action)  || "restdelete".equals(action)) { //due to variable format for operation eg services/layer3-service-list/8fe4ba4f-35cf-4d9b-a04a-fd3f5d4c5cc9
			key = Constants.REQUEST_TUNABLES + "..." + action;
			msoLogger.debug("Generated key: " + key);
		} else {
			key = Constants.REQUEST_TUNABLES + "." + msoAction + "." + operation +"."  + action;
			msoLogger.debug("Generated key: " + key);
		}

		String value;
		try {
			value = msoPropertiesFactory.getMsoJavaProperties(MSO_PROP_SDNC_ADAPTER).getProperty(key, "");
		} catch (MsoPropertiesException e) {
			msoLogger.error (MessageEnum.LOAD_PROPERTIES_FAIL, "Unknown. Mso Properties ID not found in cache: " + MSO_PROP_SDNC_ADAPTER, "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception - Mso Properties ID not found in cache", e);
			value="";
		}

		if (value != null && value.length() > 0) {

			String[] parts = value.split("\\|"); //escape pipe
			if (parts.length < 3) {
				msoLogger.warn(MessageEnum.RA_SDNC_INVALID_CONFIG, key, value, "SDNC", "", MsoLogger.ErrorCode.DataError, "Invalid config");
			}

			for (int i = 0; i < parts.length; i++) {
				if (i == 0) {
					reqMethod = parts[i];
					msoLogger.debug("Request Method is set to: " + reqMethod);
				} else if (i == 1) {
					timeout = parts[i];
					msoLogger.debug("Timeout is set to: " + timeout);
				} else if (i == 2) {
					sdncUrl = SDNCAdapterPortTypeImpl.getProperty(Constants.REQUEST_TUNABLES + "." + parts[i], "",msoPropertiesFactory);
					if (operation != null && sdncUrl != null) {
						sdncUrl = sdncUrl  + operation;
					}
					msoLogger.debug("SDNC Url is set to: " + sdncUrl);
				} else if  (i == 3) {
					headerName = parts[i];
					msoLogger.debug("HeaderName is set to: " + headerName);
				} else if  (i == 4) {
					namespace = parts[i];
					msoLogger.debug("NameSpace is set to: " + namespace);
				} else if  (i == 5) {
					asyncInd = parts[i];
					msoLogger.debug("AsyncInd is set to: " + asyncInd);
				}
			}
			if (sdncUrl == null) {
				error = "Invalid configuration, sdncUrl required for:" + key + " value:" + value;
			}
		} else {
			error = "Missing configuration for:" + key;
		}
		if (error != null) {
			msoLogger.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, key, "SDNC", "", MsoLogger.ErrorCode.DataError, "Missing config param");
			alarmLogger.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
		}
		msoLogger.debug ("RequestTunables Key:" + key + " Value:" + value + " Tunables:" + this.toString());
		return;
	}
}
