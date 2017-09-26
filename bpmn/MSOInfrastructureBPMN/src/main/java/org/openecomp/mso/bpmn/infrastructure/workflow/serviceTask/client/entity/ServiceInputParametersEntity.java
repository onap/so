package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class ServiceInputParametersEntity {
    public List<ParamEntity> getParamList() {
        return paramList;
    }

    public void setParamList(List<ParamEntity> paramList) {
        this.paramList = paramList;
    }

    @JsonProperty("param")
    private List<ParamEntity> paramList;
}
