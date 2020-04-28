/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import org.onap.so.client.aai.AAICommonObjectMapperProvider;
import org.onap.so.client.aai.AAIQueryClient;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.cds.CDSProcessingClient;
import org.onap.so.client.cds.CDSProcessingListener;
import org.onap.so.client.policy.PolicyClientImpl;
import org.springframework.stereotype.Component;

/*
 * This object is intended to be a helper for acquiring classes that cannot be acquired via Spring injection.
 *
 * It brings two benefits:
 *
 * 1) Enforces acquisition of a new copy of these classes every time to help with picking up properties files changes,
 * etc 2) The classes are exposed in such a way that mocks of them can still be injected when testing the Spring objects
 * that use them
 */

@Component
public class InjectionHelper {
    public AAIResourcesClient getAaiClient() {
        return new AAIResourcesClient();
    }

    public AAIQueryClient getAaiQueryClient() {
        return new AAIQueryClient();
    }

    public AAICommonObjectMapperProvider getAaiCommonObjectMapperProvider() {
        return new AAICommonObjectMapperProvider();
    }

    public AAIResultWrapper getAaiResultWrapper(String json) {
        return new AAIResultWrapper(json);
    }

    public PolicyClientImpl getPolicyClient() {
        return new PolicyClientImpl();
    }

    public CDSProcessingClient getCdsClient(CDSProcessingListener listener) {
        return new CDSProcessingClient(listener);
    }
}
