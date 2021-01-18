package org.onap.so.beans.nsmf;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum OrchestrationStatusEnum {
    /**
     * activated
     */
    ACTIVATED("activated"),

    /**
     * deactivated
     */
    DEACTIVATED("deactivated"),

    ;

    private String value;


    OrchestrationStatusEnum(String value) {
        this.value = value;
    }

    public static OrchestrationStatusEnum getStatus(String value) {
        for (OrchestrationStatusEnum orchestrationStatus : OrchestrationStatusEnum.values()) {
            if (orchestrationStatus.value.equalsIgnoreCase(value)) {
                return orchestrationStatus;
            }
        }
        return null;
    }
}
