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

import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.wrappers.exceptions.ServiceProxyNotFoundException;

public class ServiceInstanceWrapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getTransportServiceProxyTest() throws ServiceProxyNotFoundException {
        ServiceInstance si = buildServiceInstance();
        si.getServiceProxies().add(buildServiceProxy());
        ServiceInstanceWrapper sw = new ServiceInstanceWrapper(si);
        ServiceProxy sp = sw.getTransportServiceProxy();
        assertEquals("sp-001", sp.getId());
        assertEquals("transport", sp.getType());
    }

    @Test
    public void getTransportServiceProxyExceptionTest() throws ServiceProxyNotFoundException {
        expectedException.expect(ServiceProxyNotFoundException.class);
        ServiceInstanceWrapper sw = new ServiceInstanceWrapper(buildServiceInstance());
        sw.getTransportServiceProxy();
    }

    private ServiceInstance buildServiceInstance() {
        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("si-001");
        si.setServiceInstanceName("Test SI");
        return si;
    }

    private ServiceProxy buildServiceProxy() {
        ServiceProxy sp = new ServiceProxy();
        sp.setId("sp-001");
        sp.setType("transport");
        return sp;
    }

}
