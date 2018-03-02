package org.openecomp.mso.client.policy.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"dictionaryJson",
"dictionaryData",
"responseCode",
"responseMessage"
})
public class  AllowedTreatments{

@JsonProperty("dictionaryJson")
private DictionaryJson dictionaryJson;
@JsonProperty("dictionaryData")
private Object dictionaryData;
@JsonProperty("responseCode")
private Integer responseCode;
@JsonProperty("responseMessage")
private String responseMessage;

@JsonProperty("dictionaryJson")
public DictionaryJson getDictionaryJson() {
return dictionaryJson;
 }

@JsonProperty("dictionaryJson")
public void setDictionaryJson(DictionaryJson dictionaryJson) {
this.dictionaryJson = dictionaryJson;
 }

public AllowedTreatments withDictionaryJson(DictionaryJson dictionaryJson) {
this.dictionaryJson = dictionaryJson;
return this;
 }

@JsonProperty("dictionaryData")
public Object getDictionaryData() {
return dictionaryData;
 }

@JsonProperty("dictionaryData")
public void setDictionaryData(Object dictionaryData) {
this.dictionaryData = dictionaryData;
 }

public AllowedTreatments withDictionaryData(Object dictionaryData) {
this.dictionaryData = dictionaryData;
return this;
 }

@JsonProperty("responseCode")
public Integer getResponseCode() {
return responseCode;
 }

@JsonProperty("responseCode")
public void setResponseCode(Integer responseCode) {
this.responseCode = responseCode;
 }

public AllowedTreatments withResponseCode(Integer responseCode) {
this.responseCode = responseCode;
return this;
 }

@JsonProperty("responseMessage")
public String getResponseMessage() {
return responseMessage;
 }

@JsonProperty("responseMessage")
public void setResponseMessage(String responseMessage) {
this.responseMessage = responseMessage;
 }

public AllowedTreatments withResponseMessage(String responseMessage) {
this.responseMessage = responseMessage;
return this;
 }

}