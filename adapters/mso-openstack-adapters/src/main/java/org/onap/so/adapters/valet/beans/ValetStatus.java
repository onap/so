/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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
import org.apache.commons.lang3.builder.ToStringBuilder;

/*
 * This class represents the status object as defined in the Valet Placement Operations API - part of Response objects
 */
public class ValetStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("status")
    private String status;
    @JsonProperty("message")
    private String message;


    public ValetStatus() {
        super();
    }

    public ValetStatus(String statusCode, String statusMessage) {
        super();
        this.status = statusCode;
        this.message = statusMessage;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status).append("message", message).toString();
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String statusCode) {
        this.status = statusCode;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String statusMessage) {
        this.message = statusMessage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ValetStatus)) {
            return false;
        }
        ValetStatus vs = (ValetStatus) o;
        return Objects.equals(status, vs.status) && Objects.equals(message, vs.message);
    }
}
