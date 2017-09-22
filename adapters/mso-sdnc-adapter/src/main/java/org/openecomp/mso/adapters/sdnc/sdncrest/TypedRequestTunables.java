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
package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.openecomp.mso.adapters.sdnc.impl.Constants;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

/**
 * Typed Request Tunables.  Each entry is identified by a TYPE in the property name.
 * Different types can have different keys.
 * <p>
 * General format:
 * <pre>
 * org.openecomp.mso.adapters.sdnc.TYPE.KEY1[.KEY2...]=METHOD|TIMEOUT|URL|HEADER|NAMESPACE
 * </pre>
 * Currently supported type(s): service
 * <pre>
 * org.openecomp.mso.adapters.sdnc.service.SERVICE.OPERATION=METHOD|TIMEOUT|URL|HEADER|NAMESPACE
 * </pre>
 */
public class TypedRequestTunables {

	private static final String MSO_PROPERTIES_ID = "MSO_PROP_SDNC_ADAPTER";

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static final MsoAlarmLogger ALARMLOGGER = new MsoAlarmLogger();

	private final MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

	private final String reqId;
	private final String myUrlSuffix;
	private String key = null;
	private String error = null;

	// tunables (all are required)
	private String reqMethod = null;
	private String timeout = null;
	private String sdncUrl = null;
	private String headerName = null;
	private String namespace = null;
	private String myUrl = null;

	public TypedRequestTunables(String reqId, String myUrlSuffix) {
		this.reqId = reqId;
		this.myUrlSuffix = myUrlSuffix;
	}

	/**
	 * Sets the key for a service request:
	 * <pre>
	 * org.openecomp.mso.adapters.sdnc.service.SERVICE.OPERATION
	 * </pre>
	 * @param service the sdncService
	 * @param operation the sdncOperation
	 */
	public void setServiceKey(String service, String operation) {
		key = Constants.REQUEST_TUNABLES + ".service." + service + "." + operation;
		LOGGER.debug("Generated " + getClass().getSimpleName() + " key: " + key);
	}

	/**
	 * Gets the SDNC request ID.
	 */
	public String getReqId() {
		return reqId;
	}

	/**
	 * Gets the generated key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the most recent error, or null if there was no error.
	 */
	public String getError() {
		return error;
	}

	public String getReqMethod() {
		return reqMethod;
	}

	public String getTimeout() {
		return timeout;
	}

	public String getSdncUrl() {
		return sdncUrl;
	}

	public String getHeaderName() {
		return headerName;
	}

	public String getNamespace() {
		return namespace;
	}

	/**
	 * Gets the SDNC adapter notification URL, trimmed of trailing '/' characters.
	 */
	public String getMyUrl() {
		return myUrl;
	}

	/**
	 * Returns true if successful.  If there is an error, it is logged and alarmed.
	 * The error description may be retrieved by calling getError().
	 */
	public boolean setTunables() {
		error = null;
		MsoJavaProperties properties;

		try {
			properties = msoPropertiesFactory.getMsoJavaProperties(MSO_PROPERTIES_ID);
		} catch (MsoPropertiesException e) {
			error = "Mso Properties ID not found in cache: " + MSO_PROPERTIES_ID;
			LOGGER.error(MessageEnum.LOAD_PROPERTIES_FAIL, "Unknown. " +  error, "SDNC", "",
				MsoLogger.ErrorCode.DataError, "Exception - Mso Properties ID not found in cache", e);
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
			return false;
		}

		String value = properties.getProperty(key, "");

		if ("".equals(value)) {
			error = "Missing configuration for: " + key;
			LOGGER.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, key, "SDNC", "", MsoLogger.ErrorCode.DataError, "Missing config param");
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
			return false;
		}

		String[] parts = value.split("\\|");

		if (parts.length != 5) {
			error = "Invalid configuration for: " + key;
			LOGGER.error(MessageEnum.RA_SDNC_INVALID_CONFIG, key, value, "SDNC", "", MsoLogger.ErrorCode.DataError, "Invalid config");
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
			return false;
		}

		reqMethod = parts[0];
		LOGGER.debug("Request Method is set to: " + reqMethod);

		timeout = parts[1];
		LOGGER.debug("Timeout is set to: " + timeout);

		String urlPropKey = Constants.REQUEST_TUNABLES + "." + parts[2];
		sdncUrl = properties.getProperty(urlPropKey, "");

		if ("".equals(sdncUrl)) {
			error = "Missing configuration for: " + urlPropKey;
			LOGGER.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, urlPropKey, "SDNC", "", MsoLogger.ErrorCode.DataError, "Missing config param");
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
			return false;
		}

		LOGGER.debug("SDNC Url is set to: " + sdncUrl);

		headerName = parts[3];
		LOGGER.debug("Header Name is set to: " + headerName);

		namespace = parts[4];
		LOGGER.debug("Namespace is set to: " + namespace);

		myUrl = properties.getProperty(Constants.MY_URL_PROP, "");

		if ("".equals(myUrl)) {
			error = "Missing configuration for: " + Constants.MY_URL_PROP;
			LOGGER.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, Constants.MY_URL_PROP, "SDNC", "",
				MsoLogger.ErrorCode.DataError, "Missing config param");
			ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, error);
			return false;
		}

		while (myUrl.endsWith("/")) {
			myUrl = myUrl.substring(0, myUrl.length()-1);
		}

		myUrl += myUrlSuffix;

		LOGGER.debug(toString());
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
			+ "reqId=" + reqId
			+ (key == null ? "" : ", key=" + key)
			+ (reqMethod == null ? "" : ", reqMethod=" + reqMethod)
			+ (sdncUrl == null ? "" : ", sdncUrl=" + sdncUrl)
			+ (timeout == null ? "" : ", timeout=" + timeout)
			+ (headerName == null ? "" : ", headerName=" + headerName)
			+ (namespace == null ? "" : ", namespace=" + namespace)
			+ (myUrl == null ? "" : ", myUrl=" + myUrl)
			+ "]";
	}
}
