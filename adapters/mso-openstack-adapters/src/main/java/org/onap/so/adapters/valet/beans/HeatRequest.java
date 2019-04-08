/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.valet.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * This class represents the heat request as sent to OpenStack as defined in the Valet Placement Operations API
 */
public class HeatRequest implements Serializable {
    private static final long serialVersionUID = 768026109321305392L;
    @JsonProperty("stack_name")
    private String stackName;
    @JsonProperty("disable_rollback")
    private Boolean disableRollback;
    @JsonProperty("timeout_mins")
    private Integer timeoutMins;
    @JsonProperty("template")
    private String template;
    @JsonProperty("environment")
    private String environment;
    @JsonProperty("files")
    private Map<String, Object> files = new HashMap<>();
    @JsonProperty("parameters")
    private Map<String, Object> parameters = new HashMap<>();

    public HeatRequest(String stackName, boolean disableRollback, int timeoutMins, String template, String environment,
            Map<String, Object> files, Map<String, Object> parameters) {
        super();
        this.stackName = stackName;
        this.disableRollback = disableRollback;
        this.timeoutMins = timeoutMins;
        this.template = template;
        this.environment = environment;
        this.files = files;
        this.parameters = parameters;
    }

    public String getStackName() {
        return this.stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public Boolean getDisableRollback() {
        return this.disableRollback;
    }

    public void setDisableRollback(Boolean disableRollback) {
        this.disableRollback = disableRollback;
    }

    public Integer getTimeoutMins() {
        return this.timeoutMins;
    }

    public void setTimeoutMins(Integer timeoutMins) {
        this.timeoutMins = timeoutMins;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Map<String, Object> getFiles() {
        return this.files;
    }

    public void setFiles(Map<String, Object> files) {
        this.files = files;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stackName, disableRollback, timeoutMins, template, environment, files, parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HeatRequest)) {
            return false;
        }
        HeatRequest hr = (HeatRequest) o;
        return Objects.equals(stackName, hr.stackName) && Objects.equals(disableRollback, hr.disableRollback)
                && Objects.equals(timeoutMins, hr.timeoutMins) && Objects.equals(template, hr.template)
                && Objects.equals(environment, hr.environment) && Objects.equals(files, hr.files)
                && Objects.equals(parameters, hr.parameters);
    }
}
