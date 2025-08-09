/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.sdnc.impl;

import org.onap.so.logger.LoggingAnchor;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MapRequestTunables {

    private static Logger logger = LoggerFactory.getLogger(MapRequestTunables.class);
    public static final String GENERATED_KEY = "Generated key: ";

    @Autowired
    private Environment env;

    public RequestTunables setTunables(RequestTunables reqTunableOriginal) {
        RequestTunables reqTunable = new RequestTunables(reqTunableOriginal);
        String error = null;
        String key;
        String msoAction = reqTunable.getMsoAction();

        if (Constants.MSO_ACTION_LCM.equals(msoAction)) {
            key = Constants.REQUEST_TUNABLES + "." + msoAction + "." + reqTunable.getOperation();
        } else {
            if ("query".equals(reqTunable.getAction())) {
                // due to variable format for reqTunable.getOperation(), eg
                // services/layer3-service-list/8fe4ba4f-35cf-4d9b-a04a-fd3f5d4c5cc9
                key = Constants.REQUEST_TUNABLES + "." + msoAction + ".." + reqTunable.getAction();
            } else if ("put".equals(reqTunable.getAction()) || "restdelete".equals(reqTunable.getAction())) {
                // due to variable format for reqTunable.getOperation(), eg
                // services/layer3-service-list/8fe4ba4f-35cf-4d9b-a04a-fd3f5d4c5cc9
                key = Constants.REQUEST_TUNABLES + "..." + reqTunable.getAction();
            } else {
                key = Constants.REQUEST_TUNABLES + "." + msoAction + "." + reqTunable.getOperation() + "."
                        + reqTunable.getAction();
            }
        }
        logger.debug("{}{}", GENERATED_KEY, key);

        String value;
        value = env.getProperty(key, "");

        if (Constants.MSO_ACTION_LCM.equals(msoAction) && (value == null || value.length() == 0)) {
            key = Constants.REQUEST_TUNABLES + "." + msoAction + ".default";
            logger.debug("Can not find key of {}, use default: {}", reqTunable.getOperation(), key);
            value = env.getProperty(key, "");
        }

        if (value != null && value.length() > 0) {

            String[] parts = value.split("\\|"); // escape pipe
            if (parts.length < 3) {
                logger.warn(LoggingAnchor.SIX, MessageEnum.RA_SDNC_INVALID_CONFIG.toString(), key, value, "SDNC",
                        ErrorCode.DataError.getValue(), "Invalid config");
            }

            for (int i = 0; i < parts.length; i++) {
                if (i == 0) {
                    reqTunable.setReqMethod(parts[i]);
                    logger.debug("Request Method is set to: {}", reqTunable.getReqMethod());
                } else if (i == 1) {
                    reqTunable.setTimeout(parts[i]);
                    logger.debug("Timeout is set to: {}", reqTunable.getTimeout());
                } else if (i == 2) {
                    reqTunable.setSdncUrl(env.getProperty(Constants.REQUEST_TUNABLES + "." + parts[i], ""));
                    if (reqTunable.getOperation() != null && reqTunable.getSdncUrl() != null) {
                        reqTunable.setSdncUrl(reqTunable.getSdncUrl() + reqTunable.getOperation());
                    }
                    logger.debug("SDNC Url is set to: {}", reqTunable.getSdncUrl());
                } else if (i == 3) {
                    reqTunable.setHeaderName(parts[i]);
                    logger.debug("HeaderName is set to: {}", reqTunable.getHeaderName());
                } else if (i == 4) {
                    reqTunable.setNamespace(parts[i]);
                    logger.debug("NameSpace is set to: {}", reqTunable.getNamespace());
                } else if (i == 5) {
                    reqTunable.setAsyncInd(parts[i]);
                    logger.debug("AsyncInd is set to: {}", reqTunable.getAsyncInd());
                }
            }

            if (reqTunable.getSdncUrl() == null || ("").equals(reqTunable.getSdncUrl())) {
                error = "Invalid configuration, sdncUrl required for:" + key + " value:" + value;
            }
        } else {
            error = "Missing configuration for:" + key;
        }
        if (error != null) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_SDNC_MISS_CONFIG_PARAM.toString(), key, "SDNC",
                    ErrorCode.DataError.getValue(), "Missing config param");
        }
        logger.debug("RequestTunables Key:{} Value:{} Tunables:{}", key, value, this.toString());
        return reqTunable;
    }
}
