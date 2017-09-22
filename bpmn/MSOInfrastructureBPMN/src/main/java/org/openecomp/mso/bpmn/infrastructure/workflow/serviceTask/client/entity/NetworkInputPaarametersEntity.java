package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkInputPaarametersEntity {
    public List<ParamEntity> getParamList() {
        return paramList;
    }

    public void setParamList(List<ParamEntity> paramList) {
        this.paramList = paramList;
    }

    @JsonProperty("param")
    private List<ParamEntity> paramList;
}
