
package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"testCaseName"})
public class Labels {

    @JsonProperty("testCaseName")
    private String testCaseName;

    @JsonProperty("testCaseName")
    public String getTestCaseName() {
        return testCaseName;
    }

    @JsonProperty("testCaseName")
    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

}
