package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkRpcInputEntity {
    public InputEntity getInput() {
        return input;
    }

    public void setInput(InputEntity input) {
        this.input = input;
    }

    @JsonProperty("input")
    private InputEntity input = null;
}
