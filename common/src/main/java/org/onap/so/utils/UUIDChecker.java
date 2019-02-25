/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.utils;

import java.util.UUID;

import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class UUIDChecker {

    private static final Logger logger = LoggerFactory.getLogger(UUIDChecker.class);


    private UUIDChecker() {

    }

    public static boolean isValidUUID (String id) {
        try {
            if (null == id) {
                return false;
            }
            UUID uuid = UUID.fromString(id);
            return uuid.toString().equalsIgnoreCase(id);
        } catch (IllegalArgumentException iae) {
            logger.debug("IllegalArgumentException", iae);
            return false;
        }
    }

    public static String getUUID () {
        return UUID.randomUUID().toString();
    }

    public static String verifyOldUUID (String oldId, MsoLogger msoLogger) {
        if (!UUIDChecker.isValidUUID(oldId)) {
            String newId = UUIDChecker.getUUID();
            MsoLogger.setLogContext(newId, null);
            msoLogger.info(MessageEnum.APIH_REPLACE_REQUEST_ID, oldId, "", "");
            return newId;
        }
        MsoLogger.setLogContext(oldId, null);
        return oldId;
    }

    public static String generateUUID (MsoLogger msoLogger) {
        String newId = UUIDChecker.getUUID();
        MsoLogger.setLogContext(newId, null);
        msoLogger.info(MessageEnum.APIH_GENERATED_REQUEST_ID, newId, "", "");
        return newId;
    }

    public static String generateServiceInstanceID (MsoLogger msoLogger) {
        String newId = UUIDChecker.getUUID();
        MsoLogger.setLogContext(null, newId);
        msoLogger.info(MessageEnum.APIH_GENERATED_SERVICE_INSTANCE_ID, newId, "", "");
        return newId;
    }
}
