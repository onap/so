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

package org.onap.so.adapters.valet;

import org.apache.commons.lang3.builder.ToStringBuilder;

/*
 * The purpose of this class is to encapsulate the possible responses from Valet in to one generic class that the vnf
 * adapter can more easily utilize. This will ensure we get an object back. Any status code other than 200 will be
 * treated as a failure. We may still get a 200 back - but the ValetStatus.status is "failed" - which will also be
 * treated as a failure. The T class is expected to be one of the Valet*Response pojos.
 */
public class GenericValetResponse<T> {
    private int statusCode;
    private String errorMessage;
    private T returnObject;


    public GenericValetResponse(int statusCode, String errorMessage, T obj) {
        super();
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.returnObject = obj;
    }

    public GenericValetResponse() {
        this(-1, "not set", null);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("statusCode", statusCode).append("errorMessage", errorMessage)
                .append("returnObject", returnObject).toString();
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setReturnObject(T obj) {
        this.returnObject = obj;
    }

    public T getReturnObject() {
        return this.returnObject;
    }

}

