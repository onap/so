/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.adapters.vfc.util;

import org.openecomp.mso.adapters.vfc.constant.HttpCode;
import org.openecomp.mso.adapters.vfc.exceptions.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateUtil {

    /**
     * Log server.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateUtil.class);

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since GSO 0.5
     */
    private ValidateUtil() {

    }

    /**
     * Assert String parameter.<br/>
     * 
     * @param paramValue parameter data
     * @param name of parameter
     * @since GSO 0.5
     */
    public static void assertStringNotNull(String paramValue, String paramName) {
        if(null != paramValue && !paramValue.isEmpty()) {
            return;
        }

        LOGGER.error(paramName + ": Parameter is null or empty.");
        throw new ApplicationException(HttpCode.BAD_REQUEST, paramName + ": Invalid parameter.");
    }

    /**
     * Assert object is null.<br/>
     * 
     * @param object data object
     * @since GSO 0.5
     */
    public static void assertObjectNotNull(Object object) {
        if(null == object) {
            LOGGER.error("Object is null.");
            throw new ApplicationException(HttpCode.BAD_REQUEST, "Object is null.");
        }

    }

    /**
     * <br>
     * 
     * @param str
     * @return
     * @since ONAP Amsterdam Release
     */
    public static boolean isStrEmpty(String str) {
        return null == str || str.isEmpty();
    }
}
