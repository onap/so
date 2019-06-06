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
