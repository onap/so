package org.openecomp.mso.client.ruby.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"msoRequest"
})
public class Event {

@JsonProperty("msoRequest")
private MsoRequest msoRequest;

/**
* No args constructor for use in serialization
* 
*/
public Event() {
 }

/**
* 
* @param msoRequest
*/
public Event(MsoRequest msoRequest) {
super();
this.msoRequest = msoRequest;
 }

@JsonProperty("msoRequest")
public MsoRequest getMsoRequest() {
return msoRequest;
 }

@JsonProperty("msoRequest")
public void setMsoRequest(MsoRequest msoRequest) {
this.msoRequest = msoRequest;
 }

public Event withMsoRequest(MsoRequest msoRequest) {
this.msoRequest = msoRequest;
return this;
 }

}