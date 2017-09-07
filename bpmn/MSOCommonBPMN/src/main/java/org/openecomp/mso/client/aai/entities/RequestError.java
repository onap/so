
package org.openecomp.mso.client.aai.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "serviceException"
})
public class RequestError {

    @JsonProperty("serviceException")
    private ServiceException serviceException;

    @JsonProperty("serviceException")
    public ServiceException getServiceException() {
        return serviceException;
    }

    @JsonProperty("serviceException")
    public void setServiceException(ServiceException serviceException) {
        this.serviceException = serviceException;
    }

}
