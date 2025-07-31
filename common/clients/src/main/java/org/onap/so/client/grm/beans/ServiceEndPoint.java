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

package org.onap.so.client.grm.beans;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "serviceEndPoint")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"name", "version", "hostAddress", "listenPort", "latitude", "longitude", "registrationTime",
        "expirationTime", "contextPath", "routeOffer", "statusInfo", "eventStatusInfo", "validatorStatusInfo",
        "operationalInfo", "protocol", "properties", "disableType"})
public class ServiceEndPoint implements Serializable {

    private static final long serialVersionUID = -1594441352549128491L;

    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private Version version;
    @JsonProperty("hostAddress")
    private String hostAddress;
    @JsonProperty("listenPort")
    private String listenPort;
    @JsonProperty("latitude")
    private String latitude;
    @JsonProperty("longitude")
    private String longitude;
    @JsonProperty("registrationTime")
    private String registrationTime;
    @JsonProperty("expirationTime")
    private String expirationTime;
    @JsonProperty("contextPath")
    private String contextPath;
    @JsonProperty("routeOffer")
    private String routeOffer;
    @JsonProperty("statusInfo")
    private Status statusInfo;
    @JsonProperty("eventStatusInfo")
    private Status eventStatusInfo;
    @JsonProperty("validatorStatusInfo")
    private Status validatorStatusInfo;
    @JsonProperty("operationalInfo")
    private OperationalInfo operationalInfo;
    @JsonProperty("protocol")
    private String protocol;
    @JsonProperty("properties")
    private List<Property> properties = null;
    @JsonProperty("disableType")
    private List<Object> disableType = null;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("version")
    public Version getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(Version version) {
        this.version = version;
    }

    @JsonProperty("hostAddress")
    public String getHostAddress() {
        return hostAddress;
    }

    @JsonProperty("hostAddress")
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    @JsonProperty("listenPort")
    public String getListenPort() {
        return listenPort;
    }

    @JsonProperty("listenPort")
    public void setListenPort(String listenPort) {
        this.listenPort = listenPort;
    }

    @JsonProperty("latitude")
    public String getLatitude() {
        return latitude;
    }

    @JsonProperty("latitude")
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @JsonProperty("longitude")
    public String getLongitude() {
        return longitude;
    }

    @JsonProperty("longitude")
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @JsonProperty("registrationTime")
    public String getRegistrationTime() {
        return registrationTime;
    }

    @JsonProperty("registrationTime")
    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    @JsonProperty("expirationTime")
    public String getExpirationTime() {
        return expirationTime;
    }

    @JsonProperty("expirationTime")
    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    @JsonProperty("contextPath")
    public String getContextPath() {
        return contextPath;
    }

    @JsonProperty("contextPath")
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @JsonProperty("routeOffer")
    public String getRouteOffer() {
        return routeOffer;
    }

    @JsonProperty("routeOffer")
    public void setRouteOffer(String routeOffer) {
        this.routeOffer = routeOffer;
    }

    @JsonProperty("statusInfo")
    public Status getStatusInfo() {
        return statusInfo;
    }

    @JsonProperty("statusInfo")
    public void setStatusInfo(Status statusInfo) {
        this.statusInfo = statusInfo;
    }

    @JsonProperty("eventStatusInfo")
    public Status getEventStatusInfo() {
        return eventStatusInfo;
    }

    @JsonProperty("eventStatusInfo")
    public void setEventStatusInfo(Status eventStatusInfo) {
        this.eventStatusInfo = eventStatusInfo;
    }

    @JsonProperty("validatorStatusInfo")
    public Status getValidatorStatusInfo() {
        return validatorStatusInfo;
    }

    @JsonProperty("validatorStatusInfo")
    public void setValidatorStatusInfo(Status validatorStatusInfo) {
        this.validatorStatusInfo = validatorStatusInfo;
    }

    @JsonProperty("operationalInfo")
    public OperationalInfo getOperationalInfo() {
        return operationalInfo;
    }

    @JsonProperty("operationalInfo")
    public void setOperationalInfo(OperationalInfo operationalInfo) {
        this.operationalInfo = operationalInfo;
    }

    @JsonProperty("protocol")
    public String getProtocol() {
        return protocol;
    }

    @JsonProperty("protocol")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @JsonProperty("properties")
    public List<Property> getProperties() {
        return properties;
    }

    @JsonProperty("properties")
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    @JsonProperty("disableType")
    public List<Object> getDisableType() {
        return disableType;
    }

    @JsonProperty("disableType")
    public void setDisableType(List<Object> disableType) {
        this.disableType = disableType;
    }

}
