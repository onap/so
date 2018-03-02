package org.openecomp.mso.client.aai.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"operational-environment-id",
"operational-environment-name",
"operational-environment-type",
"operational-environment-status",
"tenant-context",
"workload-context",
"resource-version"
})
public class AAIOperationalEnvironment {

@JsonProperty("operational-environment-id")
private String operationalEnvironmentId;
@JsonProperty("operational-environment-name")
private String operationalEnvironmentName;
@JsonProperty("operational-environment-type")
private String operationalEnvironmentType;
@JsonProperty("operational-environment-status")
private String operationalEnvironmentStatus;
@JsonProperty("tenant-context")
private String tenantContext;
@JsonProperty("workload-context")
private String workloadContext;
@JsonProperty("resource-version")
private String resourceVersion;

@JsonProperty("operational-environment-id")
public String getOperationalEnvironmentId() {
return operationalEnvironmentId;
 }

@JsonProperty("operational-environment-id")
public void setOperationalEnvironmentId(String operationalEnvironmentId) {
this.operationalEnvironmentId = operationalEnvironmentId;
 }

public AAIOperationalEnvironment withOperationalEnvironmentId(String operationalEnvironmentId) {
this.operationalEnvironmentId = operationalEnvironmentId;
return this;
 }

@JsonProperty("operational-environment-name")
public String getOperationalEnvironmentName() {
return operationalEnvironmentName;
 }

@JsonProperty("operational-environment-name")
public void setOperationalEnvironmentName(String operationalEnvironmentName) {
this.operationalEnvironmentName = operationalEnvironmentName;
 }

public AAIOperationalEnvironment withOperationalEnvironmentName(String operationalEnvironmentName) {
this.operationalEnvironmentName = operationalEnvironmentName;
return this;
 }

@JsonProperty("operational-environment-type")
public String getOperationalEnvironmentType() {
return operationalEnvironmentType;
 }

@JsonProperty("operational-environment-type")
public void setOperationalEnvironmentType(String operationalEnvironmentType) {
this.operationalEnvironmentType = operationalEnvironmentType;
 }

public AAIOperationalEnvironment withOperationalEnvironmentType(String operationalEnvironmentType) {
this.operationalEnvironmentType = operationalEnvironmentType;
return this;
 }

@JsonProperty("operational-environment-status")
public String getOperationalEnvironmentStatus() {
return operationalEnvironmentStatus;
 }

@JsonProperty("operational-environment-status")
public void setOperationalEnvironmentStatus(String operationalEnvironmentStatus) {
this.operationalEnvironmentStatus = operationalEnvironmentStatus;
 }

public AAIOperationalEnvironment withOperationalEnvironmentStatus(String operationalEnvironmentStatus) {
this.operationalEnvironmentStatus = operationalEnvironmentStatus;
return this;
 }

@JsonProperty("tenant-context")
public String getTenantContext() {
return tenantContext;
 }

@JsonProperty("tenant-context")
public void setTenantContext(String tenantContext) {
this.tenantContext = tenantContext;
 }

public AAIOperationalEnvironment withTenantContext(String tenantContext) {
this.tenantContext = tenantContext;
return this;
 }

@JsonProperty("workload-context")
public String getWorkloadContext() {
return workloadContext;
 }

@JsonProperty("workload-context")
public void setWorkloadContext(String workloadContext) {
this.workloadContext = workloadContext;
 }

public AAIOperationalEnvironment withWorkloadContext(String workloadContext) {
this.workloadContext = workloadContext;
return this;
 }

@JsonProperty("resource-version")
public String getResourceVersion() {
return resourceVersion;
 }

@JsonProperty("resource-version")
public void setResourceVersion(String resourceVersion) {
this.resourceVersion = resourceVersion;
 }

public AAIOperationalEnvironment withResourceVersion(String resourceVersion) {
this.resourceVersion = resourceVersion;
return this;
 }

}
