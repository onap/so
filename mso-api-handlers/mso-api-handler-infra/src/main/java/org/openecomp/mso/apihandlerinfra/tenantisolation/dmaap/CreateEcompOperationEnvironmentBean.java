package org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"operationalEnvironmentId",
"operationalEnvironmentName",
"operationalEnvironmentType",
"tenantContext",
"workloadContext"
})

public class CreateEcompOperationEnvironmentBean {

@JsonProperty("operationalEnvironmentId")
private String operationalEnvironmentId;
@JsonProperty("operationalEnvironmentName")
private String operationalEnvironmentName;
@JsonProperty("operationalEnvironmentType")
private String operationalEnvironmentType;
@JsonProperty("tenantContext")
private String tenantContext;
@JsonProperty("workloadContext")
private String workloadContext;
@JsonProperty("action")
private String action;


/**
* No args constructor for use in serialization
* 
*/
public CreateEcompOperationEnvironmentBean() {
 }

/**
* 
* @param operationalEnvironmentId
* @param operationalEnvironmentName
* @param operationalEnvironmentType
* @param tenantContext
* @param workloadContext
*/
public CreateEcompOperationEnvironmentBean(String operationalEnvironmentId, String operationalEnvironmentName, String operationalEnvironmentType, String tenantContext, String workloadContext, String action) {
super();
this.operationalEnvironmentId = operationalEnvironmentId;
this.operationalEnvironmentName = operationalEnvironmentName;
this.operationalEnvironmentType = operationalEnvironmentType;
this.tenantContext = tenantContext;
this.workloadContext = workloadContext;
this.action = action;
 }

@JsonProperty("operationalEnvironmentId")
public String getOperationalEnvironmentId() {
return operationalEnvironmentId;
 }

@JsonProperty("operationalEnvironmentId")
public void setOperationalEnvironmentId(String operationalEnvironmentId) {
this.operationalEnvironmentId = operationalEnvironmentId;
 }

public CreateEcompOperationEnvironmentBean withOperationalEnvironmentId(String operationalEnvironmentId) {
this.operationalEnvironmentId = operationalEnvironmentId;
return this;
 }


@JsonProperty("operationalEnvironmentName")
public String getoperationalEnvironmentName() {
return operationalEnvironmentName;
 }

@JsonProperty("operationalEnvironmentName")
public void setoperationalEnvironmentName(String operationalEnvironmentName) {
this.operationalEnvironmentName = operationalEnvironmentName;
 }

public CreateEcompOperationEnvironmentBean withOperationalEnvironmentName(String operationalEnvironmentName) {
this.operationalEnvironmentName = operationalEnvironmentName;
return this;
 }

@JsonProperty("operationalEnvironmentType")
public String getoperationalEnvironmentType() {
return operationalEnvironmentType;
 }

@JsonProperty("operationalEnvironmentType")
public void setoperationalEnvironmentType(String operationalEnvironmentType) {
this.operationalEnvironmentType = operationalEnvironmentType;
 }

public CreateEcompOperationEnvironmentBean withOperationalEnvironmentType(String operationalEnvironmentType) {
this.operationalEnvironmentType = operationalEnvironmentType;
return this;
 }

@JsonProperty("tenantContext")
public String gettenantContext() {
return tenantContext;
 }

@JsonProperty("tenantContext")
public void settenantContext(String tenantContext) {
this.tenantContext = tenantContext;
 }

public CreateEcompOperationEnvironmentBean withTenantContext(String tenantContext) {
this.tenantContext = tenantContext;
return this;
 }

@JsonProperty("workloadContext")
public String getworkloadContext() {
return workloadContext;
 }

@JsonProperty("workloadContext")
public void setworkloadContext(String workloadContext) {
this.workloadContext = workloadContext;
 }

public CreateEcompOperationEnvironmentBean withWorkloadContext(String workloadContext) {
this.workloadContext = workloadContext;
return this;
 }


@JsonProperty("action")
public String getaction() {
return action;
 }

@JsonProperty("action")
public void setaction(String action) {
this.action = action;
 }

public CreateEcompOperationEnvironmentBean withaction(String action) {
this.action = action;
return this;
 }



}
