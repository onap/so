package org.openecomp.mso.client.aai.entities.bulkprocess;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"uri",
"body"
})
public class OperationBody {

@JsonProperty("uri")
private String uri;
@JsonProperty("body")
private Object body;

@JsonProperty("uri")
public String getUri() {
return uri;
}

@JsonProperty("uri")
public void setUri(String uri) {
this.uri = uri;
}

public OperationBody withUri(String uri) {
this.uri = uri;
return this;
}

@JsonProperty("body")
public Object getBody() {
return body;
}

@JsonProperty("body")
public void setBody(Object body) {
this.body = body;
}

public OperationBody withBody(Object body) {
this.body = body;
return this;
}

}
