package org.openecomp.mso.client.sdno.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"key",
"value"
})
public class AAIParamList {

@JsonProperty("key")
private String key;
@JsonProperty("value")
private String value;

/**
* No args constructor for use in serialization
* 
*/
public AAIParamList() {
 }

/**
* 
* @param value
* @param key
*/
public AAIParamList(String key, String value) {
super();
this.key = key;
this.value = value;
 }

@JsonProperty("key")
public String getKey() {
return key;
 }

@JsonProperty("key")
public void setKey(String key) {
this.key = key;
 }

public AAIParamList withKey(String key) {
this.key = key;
return this;
 }

@JsonProperty("value")
public String getValue() {
return value;
 }

@JsonProperty("value")
public void setValue(String value) {
this.value = value;
 }

public AAIParamList withValue(String value) {
this.value = value;
return this;
 }

}