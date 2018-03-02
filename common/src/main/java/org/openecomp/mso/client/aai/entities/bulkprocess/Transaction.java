package org.openecomp.mso.client.aai.entities.bulkprocess;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"patch",
"patch",
"delete"
})
public class Transaction {

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonProperty("put")
private List<OperationBody> put = new ArrayList<>();

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonProperty("patch")
private List<OperationBody> patch = new ArrayList<>();

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonProperty("delete")
private List<OperationBody> delete = new ArrayList<>();

@JsonProperty("put")
public List<OperationBody> getPut() {
return put;
}

@JsonProperty("put")
public void setPut(List<OperationBody> put) {
this.put = put;
}

public Transaction withPut(List<OperationBody> put) {
this.put = put;
return this;
}

@JsonProperty("patch")
public List<OperationBody> getPatch() {
return patch;
}

@JsonProperty("patch")
public void setPatch(List<OperationBody> patch) {
this.patch = patch;
}

public Transaction withPatch(List<OperationBody> patch) {
this.patch = patch;
return this;
}

@JsonProperty("delete")
public List<OperationBody> getDelete() {
return delete;
}

@JsonProperty("delete")
public void setDelete(List<OperationBody> delete) {
this.delete = delete;
}

public Transaction withDelete(List<OperationBody> delete) {
this.delete = delete;
return this;
}

}
