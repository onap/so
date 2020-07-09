package org.onap.so.bpmn.infrastructure.workflow.tasks;

public class ReplaceInstanceRelatedInformation {

    private String oldVolumeGroupName;

    public String getOldVolumeGroupName() {
        return oldVolumeGroupName;
    }

    public ReplaceInstanceRelatedInformation setOldVolumeGroupName(String oldVolumeGroupName) {
        this.oldVolumeGroupName = oldVolumeGroupName;
        return this;
    }

}
