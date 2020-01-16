package org.onap.so.bpmn.servicedecomposition.entities;

import org.onap.so.db.catalog.beans.Service;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("serviceModel")
public class ServiceModel {

    @JsonProperty("currentService")
    private Service currentService;
    @JsonProperty("newService")
    private Service newService;


    public Service getCurrentService() {
        return currentService;
    }

    public void setCurrentService(Service currentService) {
        this.currentService = currentService;
    }

    public Service getNewService() {
        return newService;
    }

    public void setNewService(Service newService) {
        this.newService = newService;
    }

}
