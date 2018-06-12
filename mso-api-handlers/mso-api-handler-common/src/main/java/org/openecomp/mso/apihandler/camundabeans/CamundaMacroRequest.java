package org.openecomp.mso.apihandler.camundabeans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.openecomp.mso.apihandler.common.CommonConstants;

/**
 * POJO which encapsulates the fields required to create a JSON request to invoke generic macro BPEL.
 */
@JsonPropertyOrder({CommonConstants.G_REQUEST_ID, CommonConstants.G_ACTION})
@JsonRootName(CommonConstants.CAMUNDA_ROOT_INPUT)
public class CamundaMacroRequest {

    @JsonProperty(CommonConstants.G_REQUEST_ID)
    private CamundaInput requestId;

    @JsonProperty(CommonConstants.G_ACTION)
    private CamundaInput action;

    @JsonProperty(CommonConstants.G_SERVICEINSTANCEID)
    private CamundaInput serviceInstanceId;


    /**
     * Sets new requestId.
     *
     * @param requestId New value of requestId.
     */
    public void setRequestId(CamundaInput requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets action.
     *
     * @return Value of action.
     */
    public CamundaInput getAction() {
        return action;
    }

    /**
     * Sets new action.
     *
     * @param action New value of action.
     */
    public void setAction(CamundaInput action) {
        this.action = action;
    }

    /**
     * Gets requestId.
     *
     * @return Value of requestId.
     */
    public CamundaInput getRequestId() {
        return requestId;
    }

    /**
     * Sets new serviceInstanceId.
     *
     * @param serviceInstanceId New value of serviceInstanceId.
     */
    public void setServiceInstanceId(CamundaInput serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    /**
     * Gets serviceInstanceId.
     *
     * @return Value of serviceInstanceId.
     */
    public CamundaInput getServiceInstanceId() {
        return serviceInstanceId;
    }

    @Override
    public String toString() {
        return "CamundaMacroRequest{" +
                "requestId=" + requestId +
                ", action=" + action +
                ", serviceInstanceId=" + serviceInstanceId +
                '}';
    }
}
