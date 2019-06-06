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
