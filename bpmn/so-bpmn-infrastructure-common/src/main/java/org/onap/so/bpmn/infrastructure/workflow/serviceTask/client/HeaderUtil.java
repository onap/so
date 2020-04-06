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

package org.onap.so.bpmn.infrastructure.workflow.serviceTask.client;

import java.util.Base64;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.SdncUnderlayVpnOperationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderUtil {

    private static Logger logger = LoggerFactory.getLogger(HeaderUtil.class);

    public static final String USER = "admin";
    public static final String PASS = "Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U";
    public static final String DefaulAuth = getAuthorization(USER, PASS);

    public static String getAuthorization(String usr, String pwd) {

        return "Basic " + base64Encode(usr + ":" + pwd);
    }

    private static String base64Encode(String str) {
        String base64 = str;
        try {
            base64 = Base64.getEncoder().encodeToString(str.getBytes("utf-8"));
        } catch (Exception ex) {
            logger.error("Exception in base64Encode", ex);
        }
        return base64;
    }
}
