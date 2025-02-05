/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.utils;


import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.heat.model.Stack;

@Component
public class StackStatusHandler {

    private static final Logger logger = LoggerFactory.getLogger(StackStatusHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RequestsDbClient requestDBClient;

    @Async
    public void updateStackStatus(Stack stack, String requestId) {
        try {
            String stackStatus = mapper.writeValueAsString(stack);
            RequestProcessingData requestProcessingData =
                    requestDBClient.getRequestProcessingDataBySoRequestIdAndNameAndGrouping(requestId,
                            stack.getStackName(), stack.getId());
            if (requestProcessingData == null) {
                requestProcessingData = new RequestProcessingData();
                requestProcessingData.setGroupingId(stack.getId());
                requestProcessingData.setName(stack.getStackName());
                requestProcessingData.setTag("StackInformation");
                requestProcessingData.setSoRequestId(requestId);
                requestProcessingData.setValue(stackStatus);
                requestDBClient.saveRequestProcessingData(requestProcessingData);
            } else {
                requestProcessingData.setValue(stackStatus);
                requestDBClient.updateRequestProcessingData(requestProcessingData);
            }
        } catch (Exception e) {
            logger.warn("Error adding stack status to request database", e);
        }
    }
}
