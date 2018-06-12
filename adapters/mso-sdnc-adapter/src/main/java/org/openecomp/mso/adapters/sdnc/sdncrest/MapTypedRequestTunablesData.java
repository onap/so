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

import org.openecomp.mso.adapters.sdnc.exception.SDNCAdapterException;
import org.openecomp.mso.adapters.sdnc.impl.Constants;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MapTypedRequestTunablesData {
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA,MapTypedRequestTunablesData.class);
	
	private static final MsoAlarmLogger alarmLogger = new MsoAlarmLogger();	
		
	@Autowired
	private Environment env;


	public TypedRequestTunables setTunables(TypedRequestTunables reqTunableOriginal) throws SDNCAdapterException {	
		TypedRequestTunables reqTunable = new TypedRequestTunables(reqTunableOriginal);		
		
		String error;
		String value = env.getProperty(reqTunable.getKey().toLowerCase(), "");

		if ("".equals(value)) {
			error= "Missing configuration for: " + reqTunable.getKey();
			msoLogger.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, reqTunable.getKey(), "SDNC", "", MsoLogger.ErrorCode.DataError, "Missing config param");
			alarmLogger.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, reqTunable.getError());		
			throw new SDNCAdapterException(error);
		}

		String[] parts = value.split("\\|");

		if (parts.length != 5) {
			error="Invalid configuration for: " + reqTunable.getKey();
			msoLogger.error(MessageEnum.RA_SDNC_INVALID_CONFIG, reqTunable.getKey(), value, "SDNC", "", MsoLogger.ErrorCode.DataError, "Invalid config");
			alarmLogger.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, reqTunable.getError());	
			throw new SDNCAdapterException(error);
		}

		reqTunable.setReqMethod(parts[0]);
		msoLogger.debug("Request Method is set to: " + reqTunable.getReqMethod());

		reqTunable.setTimeout(parts[1]);
		msoLogger.debug("Timeout is set to: " + reqTunable.getTimeout());

		String urlPropKey = Constants.REQUEST_TUNABLES + "." + parts[2];
		reqTunable.setSdncUrl(env.getProperty(urlPropKey, ""));

		if ("".equals(reqTunable.getSdncUrl())) {
			error="Missing configuration for: " + urlPropKey;
			msoLogger.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, urlPropKey, "SDNC", "", MsoLogger.ErrorCode.DataError, "Missing config param");
			alarmLogger.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, reqTunable.getError());
			throw new SDNCAdapterException(error);
		}

		msoLogger.debug("SDNC Url is set to: " + reqTunable.getSdncUrl());

		reqTunable.setHeaderName(parts[3]);
		msoLogger.debug("Header Name is set to: " + reqTunable.getHeaderName());

		reqTunable.setNamespace(parts[4]);
		msoLogger.debug("Namespace is set to: " + reqTunable.getNamespace());

		reqTunable.setMyUrl(env.getProperty(Constants.MY_URL_PROP, ""));

		if ("".equals(reqTunable.getMyUrl())) {
			error="Missing configuration for: " + Constants.MY_URL_PROP;
			msoLogger.error(MessageEnum.RA_SDNC_MISS_CONFIG_PARAM, Constants.MY_URL_PROP, "SDNC", "",
				MsoLogger.ErrorCode.DataError, "Missing config param");
			alarmLogger.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, reqTunable.getError());		
			throw new SDNCAdapterException(error);
		}

		while (reqTunable.getMyUrl().endsWith("/")) {
			reqTunable.setMyUrl(reqTunable.getMyUrl().substring(0, reqTunable.getMyUrl().length()-1));
		}

		reqTunable.setMyUrl(reqTunable.getMyUrl().concat(reqTunable.getMyUrlSuffix()));

		msoLogger.debug(reqTunable.toString());	
		return reqTunable;
	}

}
