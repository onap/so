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

package org.onap.so.adapters.network.exceptions;


import java.io.Serializable;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;

/**
 * Jax-WS Fault Bean for Network Exceptions
 */
public class NetworkExceptionBean implements Serializable {

    private static final long serialVersionUID = 1655343530371342871L;

    private String message;
    private MsoExceptionCategory category;
    private Boolean rolledBack;

    public NetworkExceptionBean() {}

    public NetworkExceptionBean(String message) {
        this.message = message;
    }

    public NetworkExceptionBean(String message, MsoExceptionCategory category) {
        this.message = message;
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MsoExceptionCategory getCategory() {
        return category;
    }

    public void setCategory(MsoExceptionCategory category) {
        this.category = category;
    }

    public Boolean isRolledBack() {
        return rolledBack;
    }

    public void setRolledBack(Boolean rolledBack) {
        this.rolledBack = rolledBack;
    }
}
