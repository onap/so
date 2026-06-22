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

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/bbNameSelectionReference")
@Table(name = "bbname_selection_reference")
public class BBNameSelectionReference implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @BusinessKey
    @Column(name = "CONTROLLER_ACTOR")
    private String controllerActor;

    @Column(name = "SCOPE")
    private String scope;

    @Column(name = "ACTION")
    private String action;

    @BusinessKey
    @Column(name = "BB_NAME")
    private String bbName;

    public String getControllerActor() {
        return controllerActor;
    }

    public void setControllerActor(String controllerActor) {
        this.controllerActor = controllerActor;
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

    public Integer getID() {
        return ID;
    }

    public String getBbName() {
        return bbName;
    }

    public void setBbName(String bbName) {
        this.bbName = bbName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("ID", ID).append("controllerActor", controllerActor)
                .append("scope", scope).append("action", action).append("bbName", bbName).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BBNameSelectionReference)) {
            return false;
        }
        BBNameSelectionReference castOther = (BBNameSelectionReference) other;
        return new EqualsBuilder().append(controllerActor, castOther.controllerActor).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerActor).append(bbName).toHashCode();
    }
}
