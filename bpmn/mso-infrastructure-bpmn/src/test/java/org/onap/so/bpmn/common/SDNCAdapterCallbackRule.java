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

package org.onap.so.bpmn.common;

import jakarta.xml.ws.Endpoint;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.onap.so.bpmn.common.workflow.service.SDNCAdapterCallbackServiceImpl;

/**
 * A JUnit rule that starts the SDNC Adapter Callback Service before every test, and tears it down after every test.
 * Example:
 * 
 * <pre>
 * &#64;Rule
 * public final SDNCAdapterCallbackRule sdncAdapterCallbackRule = new SDNCAdapterCallbackRule(processEngineRule);
 * </pre>
 */
public class SDNCAdapterCallbackRule implements TestRule {
    public static final String DEFAULT_ENDPOINT_URL = "http://localhost:28080/mso/SDNCAdapterCallbackService";

    private final ProcessEngineServices processEngineServices;
    private final String endpointUrl;

    public SDNCAdapterCallbackRule(ProcessEngineServices processEngineServices) {
        this(processEngineServices, DEFAULT_ENDPOINT_URL);
    }

    public SDNCAdapterCallbackRule(ProcessEngineServices processEngineServices, String endpointUrl) {
        this.processEngineServices = processEngineServices;
        this.endpointUrl = endpointUrl;
    }

    @Override
    public Statement apply(final Statement baseStmt, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Endpoint endpoint = null;

                try {
                    SDNCAdapterCallbackServiceImpl sdncCallbackService = new SDNCAdapterCallbackServiceImpl();
                    sdncCallbackService.setProcessEngineServices4junit(processEngineServices);

                    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
                    System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

                    System.out.println("Publishing Endpoint - " + endpointUrl);
                    endpoint = Endpoint.publish(endpointUrl, sdncCallbackService);

                    baseStmt.evaluate();
                } finally {
                    if (endpoint != null) {
                        System.out.println("Stopping Endpoint - " + endpointUrl);
                        endpoint.stop();
                    }
                }
            }
        };
    }
}
