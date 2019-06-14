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

package org.onap.so.bpmn.servicedecomposition.bbobjects.wrappers;

import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.wrappers.exceptions.ServiceProxyNotFoundException;

public class ServiceInstanceWrapper {

    private final ServiceInstance serviceInstance;
    private static final String SERVICE_PROXY_TRANSPORT = "TRANSPORT";

    public ServiceInstanceWrapper(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public ServiceProxy getTransportServiceProxy() throws ServiceProxyNotFoundException {
        ServiceProxy serviceProxy = null;
        for (ServiceProxy sp : serviceInstance.getServiceProxies()) {
            if (SERVICE_PROXY_TRANSPORT.equalsIgnoreCase(sp.getType())) {
                serviceProxy = sp;
                break;
            }
        }
        if (serviceProxy == null) {
            throw new ServiceProxyNotFoundException("Transport Service Proxy not found for service instance: "
                    + serviceInstance.getServiceInstanceId());
        }
        return serviceProxy;
    }


}
