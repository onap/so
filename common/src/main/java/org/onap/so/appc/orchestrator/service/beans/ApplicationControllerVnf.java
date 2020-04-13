package org.onap.so.appc.orchestrator.service.beans;

import java.io.Serializable;

public class ApplicationControllerVnf implements Serializable {

    private static final long serialVersionUID = 2786675508024214637L;

    private String vnfId;
    private String vnfName;
    private String vnfHostIpAddress;
    private ApplicationControllerVm applicationControllerVm;

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getVnfName() {
        return vnfName;
    }

    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    public String getVnfHostIpAddress() {
        return vnfHostIpAddress;
    }

    public void setVnfHostIpAddress(String vnfHostIpAddress) {
        this.vnfHostIpAddress = vnfHostIpAddress;
    }

    public ApplicationControllerVm getApplicationControllerVm() {
        return applicationControllerVm;
    }

    public void setApplicationControllerVm(ApplicationControllerVm applicationControllerVm) {
        this.applicationControllerVm = applicationControllerVm;
    }



}
