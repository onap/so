package org.onap.so.db.catalog.beans;
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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

@IdClass(ControllerSelectionReferenceId.class)
@Entity
@Table(name = "controller_selection_reference")
public class ControllerSelectionReference implements Serializable {

    private static final long serialVersionUID = -608098800737567188L;

    @BusinessKey
    @Id
    @Column(name = "VNF_TYPE")
    private String vnfType;

    @BusinessKey
    @Id
    @Column(name = "CONTROLLER_NAME")
    private String controllerName;

    @BusinessKey
    @Id
    @Column(name = "ACTION_CATEGORY")
    private String actionCategory;


    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getActionCategory() {
        return actionCategory;
    }

    public void setActionCategory(String actionCategory) {
        this.actionCategory = actionCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("vnfType", vnfType).append("controllerName", controllerName)
                .append("actionCategory", actionCategory).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ControllerSelectionReference)) {
            return false;
        }
        ControllerSelectionReference castOther = (ControllerSelectionReference) other;
        return new EqualsBuilder().append(vnfType, castOther.vnfType).append(controllerName, castOther.controllerName)
                .append(actionCategory, castOther.actionCategory).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vnfType).append(controllerName).append(actionCategory).toHashCode();
    }
}
