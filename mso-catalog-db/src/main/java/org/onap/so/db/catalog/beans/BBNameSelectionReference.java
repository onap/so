/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019  Tech Mahindra
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

package org.onap.so.db.catalog.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "BBNameSelectionReference")
public class BBNameSelectionReference {
    private static final long serialVersionUID = 1L;

    @Column(name = "ACTOR")
    private String ControllerActor;

    @Column(name = "SCOPE")
    private String scope;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "BB_NAME")
    private String bb_name;

    public String getControllerActor() {
        return ControllerActor;
    }

    public void setControllerActor(String controllerActor) {
        ControllerActor = controllerActor;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBB_NAME() {
        return bb_name;
    }

    public void setBB_NAME(String bB_NAME) {
        bb_name = bB_NAME;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ControllerActor).append(bb_name).append(scope).append(action).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BBNameSelectionReference)) {
            return false;
        }
        BBNameSelectionReference castOther = (BBNameSelectionReference) other;
        return new EqualsBuilder().append(bb_name, castOther.bb_name).isEquals();
    }



}
