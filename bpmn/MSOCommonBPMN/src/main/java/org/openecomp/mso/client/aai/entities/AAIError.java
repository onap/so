
package org.openecomp.mso.client.aai.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "requestError"
})
public class AAIError {

    @JsonProperty("requestError")
    private RequestError requestError;

    @JsonProperty("requestError")
    public RequestError getRequestError() {
        return requestError;
    }

    @JsonProperty("requestError")
    public void setRequestError(RequestError requestError) {
        this.requestError = requestError;
    }

}
