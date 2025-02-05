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

package org.onap.so.apihandlerinfra.infra.rest.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.javatuples.Pair;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.listener.ListenerRunner;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestValidatorListenerRunner extends ListenerRunner {

    private static Logger logger = LoggerFactory.getLogger(RequestValidatorListenerRunner.class);

    protected List<RequestValidator> requestValidators;

    @PostConstruct
    protected void init() {
        requestValidators = new ArrayList<>(
                Optional.ofNullable(context.getBeansOfType(RequestValidator.class)).orElse(new HashMap<>()).values());
    }

    public boolean runValidations(String requestURI, Map<String, String> instanceIdMap, ServiceInstancesRequest request,
            Map<String, String> queryParams, Actions action) throws ApiException {
        logger.info("Running local validations");
        List<Pair<String, Optional<String>>> results =
                runValidations(requestValidators, instanceIdMap, request, queryParams, requestURI, action);
        if (!results.isEmpty()) {
            throw new ValidateException("Failed Validations:\n"
                    + results.stream().map(item -> String.format("%s: %s", item.getValue0(), item.getValue1().get()))
                            .collect(Collectors.joining("\n")),
                    400);
        }

        return true;
    }

    protected List<Pair<String, Optional<String>>> runValidations(List<? extends RequestValidator> validators,
            Map<String, String> instanceIdMap, ServiceInstancesRequest request, Map<String, String> queryParams,
            String requestURI, Actions action) {

        List<? extends RequestValidator> filtered =
                filterListeners(validators, (item -> item.shouldRunFor(requestURI, request, action)));

        List<Pair<String, Optional<String>>> results = new ArrayList<>();
        filtered.forEach(item -> results.add(
                new Pair<>(item.getClass().getName(), item.validate(instanceIdMap, request, queryParams, action))));

        return results.stream().filter(item -> item.getValue1().isPresent()).collect(Collectors.toList());
    }
}
