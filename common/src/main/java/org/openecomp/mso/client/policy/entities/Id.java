package org.openecomp.mso.client.policy.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"integral",
"valueType"
})
public class Id {

@JsonProperty("integral")
private Boolean integral;
@JsonProperty("valueType")
private String valueType;

@JsonProperty("integral")
public Boolean getIntegral() {
return integral;
 }

@JsonProperty("integral")
public void setIntegral(Boolean integral) {
this.integral = integral;
 }

public Id withIntegral(Boolean integral) {
this.integral = integral;
return this;
 }

@JsonProperty("valueType")
public String getValueType() {
return valueType;
 }

@JsonProperty("valueType")
public void setValueType(String valueType) {
this.valueType = valueType;
 }

public Id withValueType(String valueType) {
this.valueType = valueType;
return this;
 }

}