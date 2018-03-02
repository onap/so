
package org.openecomp.mso.client.ruby.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"event"
})
public class Ruby {

@JsonProperty("event")
private Event event;

/**
* No args constructor for use in serialization
* 
*/
public Ruby() {
 }

/**
* 
* @param event
*/
public Ruby(Event event) {
super();
this.event = event;
 }

@JsonProperty("event")
public Event getEvent() {
return event;
 }

@JsonProperty("event")
public void setEvent(Event event) {
this.event = event;
 }

public Ruby withEvent(Event event) {
this.event = event;
return this;
 }

}
