package org.onap.so.appc.orchestrator.service.beans;

import java.io.Serializable;

public class ApplicationControllerVm implements Serializable {

    private static final long serialVersionUID = 2786675508024214638L;

    private String vserverId;
    private String vmId;

    public String getVserverId() {
        return vserverId;
    }

    public void setVserverId(String vserverId) {
        this.vserverId = vserverId;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }


}
