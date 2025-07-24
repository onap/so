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

package org.onap.so.client.grm;

import org.onap.so.client.grm.beans.ServiceEndPointList;
import org.onap.so.client.grm.beans.ServiceEndPointLookup;
import org.onap.so.client.grm.beans.ServiceEndPointLookupRequest;
import org.onap.so.client.grm.beans.ServiceEndPointRequest;
import org.onap.so.client.grm.beans.VersionLookup;
import org.onap.so.client.grm.exceptions.GRMClientCallFailed;

public class GRMClient {

    public String findRunningServicesAsString(String name, int majorVersion, String env) throws Exception {

        ServiceEndPointLookupRequest request = buildServiceEndPointlookupRequest(name, majorVersion, env);
        try {
            GRMRestInvoker invoker = this.getInvoker(GRMAction.FIND_RUNNING);
            return invoker.post(request, String.class);
        } catch (Exception e) {
            throw new GRMClientCallFailed("Call to GRM findRunning failed: " + e.getMessage(), e);
        }
    }

    public ServiceEndPointList findRunningServices(String name, int majorVersion, String env) throws Exception {

        ServiceEndPointLookupRequest request = buildServiceEndPointlookupRequest(name, majorVersion, env);
        try {
            GRMRestInvoker invoker = this.getInvoker(GRMAction.FIND_RUNNING);
            return invoker.post(request, ServiceEndPointList.class);
        } catch (Exception e) {
            throw new GRMClientCallFailed("Call to GRM findRunning failed: " + e.getMessage(), e);
        }
    }

    public ServiceEndPointLookupRequest buildServiceEndPointlookupRequest(String name, int majorVersion, String env) {
        VersionLookup version = new VersionLookup();
        version.setMajor(majorVersion);

        ServiceEndPointLookup endpoint = new ServiceEndPointLookup();
        endpoint.setName(name);
        endpoint.setVersion(version);

        ServiceEndPointLookupRequest request = new ServiceEndPointLookupRequest();
        request.setServiceEndPoint(endpoint);
        request.setEnv(env);
        return request;
    }

    public void addServiceEndPoint(ServiceEndPointRequest request) throws Exception {
        try {
            GRMRestInvoker invoker = this.getInvoker(GRMAction.ADD);
            invoker.post(request);
        } catch (Exception e) {
            throw new GRMClientCallFailed("Call to GRM addServiceEndPoint failed: " + e.getMessage(), e);
        }
    }

    protected GRMRestInvoker getInvoker(GRMAction action) {
        return new GRMRestInvoker(action);
    }
}
