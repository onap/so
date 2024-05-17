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

package org.onap.so.bpmn.common.listener.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulatorListenerRunner;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.listener.ListenerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestsDbListenerRunner extends ListenerRunner {


    private static Logger logger = LoggerFactory.getLogger(FlowManipulatorListenerRunner.class);

    protected List<PostCompletionRequestsDbListener> postListeners;

    @PostConstruct
    protected void init() {

        postListeners =
                new ArrayList<>(Optional.ofNullable(context.getBeansOfType(PostCompletionRequestsDbListener.class))
                        .orElse(new HashMap<>()).values());

    }

    public void post(InfraActiveRequests request, BuildingBlockExecution execution) {

        List<PostCompletionRequestsDbListener> filtered =
                filterListeners(postListeners, (item -> item.shouldRunFor(execution)));

        logger.info("Running post request db listeners:\n{}",
                filtered.stream().map(item -> item.getClass().getName()).collect(Collectors.joining("\n")));
        filtered.forEach(item -> item.run(request, execution));

    }

}
