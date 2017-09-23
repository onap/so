package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkRpcOutputEntity {
    public OutputEntity getOutput() {
        return output;
    }

    public void setOutput(OutputEntity output) {
        this.output = output;
    }

    @JsonProperty("output")
    private OutputEntity output;
}
