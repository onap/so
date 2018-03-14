/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.client.ruby.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"requestClientName",
"requestId",
"requestTime",
"sourceName",
"reason",
"action",
"workflowId",
"notification"
})
public class MsoRequest {

@JsonProperty("requestClientName")
private String requestClientName;
@JsonProperty("requestId")
private String requestId;
@JsonProperty("requestTime")
private String requestTime;
@JsonProperty("sourceName")
private String sourceName;
@JsonProperty("reason")
private String reason;
@JsonProperty("action")
private String action;
@JsonProperty("workflowId")
private String workflowId;
@JsonProperty("notification")
private String notification;

/**
* No args constructor for use in serialization
* 
*/
public MsoRequest() {
 }

/**
* 
* @param requestClientName
* @param requestTime
* @param reason
* @param requestId
* @param workflowId
* @param sourceName
* @param action
* @param notification
*/
public MsoRequest(String requestClientName, String requestId, String requestTime, String sourceName, String reason, String action, String workflowId, String notification) {
super();
this.requestClientName = requestClientName;
this.requestId = requestId;
this.requestTime = requestTime;
this.sourceName = sourceName;
this.reason = reason;
this.action = action;
this.workflowId = workflowId;
this.notification = notification;
 }

@JsonProperty("requestClientName")
public String getRequestClientName() {
return requestClientName;
 }

@JsonProperty("requestClientName")
public void setRequestClientName(String requestClientName) {
this.requestClientName = requestClientName;
 }

public MsoRequest withRequestClientName(String requestClientName) {
this.requestClientName = requestClientName;
return this;
 }

@JsonProperty("requestId")
public String getRequestId() {
return requestId;
 }

@JsonProperty("requestId")
public void setRequestId(String requestId) {
this.requestId = requestId;
 }

public MsoRequest withRequestId(String requestId) {
this.requestId = requestId;
return this;
 }

@JsonProperty("requestTime")
public String getRequestTime() {
return requestTime;
 }

@JsonProperty("requestTime")
public void setRequestTime(String requestTime) {
this.requestTime = requestTime;
 }

public MsoRequest withRequestTime(String requestTime) {
this.requestTime = requestTime;
return this;
 }

@JsonProperty("sourceName")
public String getSourceName() {
return sourceName;
 }

@JsonProperty("sourceName")
public void setSourceName(String sourceName) {
this.sourceName = sourceName;
 }

public MsoRequest withSourceName(String sourceName) {
this.sourceName = sourceName;
return this;
 }

@JsonProperty("reason")
public String getReason() {
return reason;
 }

@JsonProperty("reason")
public void setReason(String reason) {
this.reason = reason;
 }

public MsoRequest withReason(String reason) {
this.reason = reason;
return this;
 }

@JsonProperty("action")
public String getAction() {
return action;
 }

@JsonProperty("action")
public void setAction(String action) {
this.action = action;
 }

public MsoRequest withAction(String action) {
this.action = action;
return this;
 }

@JsonProperty("workflowId")
public String getWorkflowId() {
return workflowId;
 }

@JsonProperty("workflowId")
public void setWorkflowId(String workflowId) {
this.workflowId = workflowId;
 }

public MsoRequest withWorkflowId(String workflowId) {
this.workflowId = workflowId;
return this;
 }

@JsonProperty("notification")
public String getNotification() {
return notification;
 }

@JsonProperty("notification")
public void setNotification(String notification) {
this.notification = notification;
 }

public MsoRequest withNotification(String notification) {
this.notification = notification;
return this;
 }

}