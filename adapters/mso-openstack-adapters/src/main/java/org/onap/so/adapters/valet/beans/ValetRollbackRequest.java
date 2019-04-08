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

package org.onap.so.adapters.valet.beans;

import java.io.Serializable;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * This class represents the body of a Rollback request on a Valet Placement API call
 */
public class ValetRollbackRequest implements Serializable {
    private static final long serialVersionUID = 768026109321305392L;

    @JsonProperty("stack_id")
    private String stackId;
    @JsonProperty("suppress_rollback")
    private Boolean suppressRollback = false;
    @JsonProperty("error_message")
    private String errorMessage;

    public ValetRollbackRequest() {
        super();
    }

    public String getStackId() {
        return this.stackId;
    }

    public void setStackId(String stackId) {
        this.stackId = stackId;
    }

    public Boolean getSuppressRollback() {
        return this.suppressRollback;
    }

    public void setSuppressRollback(Boolean suppressRollback) {
        this.suppressRollback = suppressRollback;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stackId, suppressRollback, errorMessage);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ValetRollbackRequest)) {
            return false;
        }
        ValetRollbackRequest vrr = (ValetRollbackRequest) o;
        return Objects.equals(stackId, vrr.stackId) && Objects.equals(suppressRollback, vrr.suppressRollback)
                && Objects.equals(errorMessage, vrr.errorMessage);
    }

}
