/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

package org.onap.so.client.oof;


import org.json.JSONObject;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


@Component
public class OofValidator {

    private static final Logger logger = LoggerFactory.getLogger(OofValidator.class);

    /**
     * Validates the synchronous homing response from oof
     *
     * @throws BadResponseException
     */
    public void validateDemandsResponse(LinkedHashMap<?, ?> response) throws BadResponseException {
        logger.debug("Validating oofs synchronous response");
        if (!response.isEmpty()) {
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("requestStatus")) {
                String status = jsonResponse.getString("requestStatus");
                if (status.equals("accepted")) {
                    logger.debug("oofs synchronous response indicates accepted");
                } else {
                    String message = jsonResponse.getString("statusMessage");
                    if (isNotBlank(message)) {
                        logger.debug("oofs response indicates failed: " + message);
                    } else {
                        logger.debug("oofs response indicates failed: no status message provided");
                        message = "error message not provided";
                    }
                    throw new BadResponseException("oofs synchronous response indicates failed: " + message);
                }
            } else {
                logger.debug("oofs synchronous response does not contain: request status");
                throw new BadResponseException("oofs synchronous response does not contain: request status");
            }
        } else {
            logger.debug("oofs synchronous response is empty");
            throw new BadResponseException("oofs synchronous response i is empty");
        }
    }

    /**
     * Validates the asynchronous/callback response from oof which contains the homing and licensing solutions
     *
     * @throws BadResponseException
     */
    public void validateSolution(String response) throws BadResponseException {
        logger.debug("Validating oofs asynchronous callback response");
        if (isNotBlank(response)) {
            JSONObject jsonResponse = new JSONObject(response);
            if (!jsonResponse.has("serviceException")) {
                logger.debug("oofs asynchronous response is valid");
            } else {
                String message = jsonResponse.getJSONObject("serviceException").getString("text");
                if (isNotBlank(message)) {
                    logger.debug("oofs response contains a service exception: " + message);
                } else {
                    logger.debug("oofs response contains a service exception: no service exception text provided");
                    message = "error message not provided";
                }
                throw new BadResponseException("oofs asynchronous response contains a service exception: " + message);
            }
        } else {
            logger.debug("oofs asynchronous response is empty");
            throw new BadResponseException("oofs asynchronous response is empty");
        }
    }

}
