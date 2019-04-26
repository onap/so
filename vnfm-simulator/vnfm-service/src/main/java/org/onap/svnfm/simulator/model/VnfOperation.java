/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.svnfm.simulator.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;

/**
 *
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
@Entity
@Table(name = "VNF_OPERATION")
public class VnfOperation {
    @Id
    @Column(name = "operationId", nullable = false)
    private String id;
    private String vnfInstanceId;

    @Enumerated(EnumType.STRING)
    private InlineResponse200.OperationEnum operation;

    @Enumerated(EnumType.STRING)
    private InlineResponse200.OperationStateEnum operationState;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(final String vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public InlineResponse200.OperationEnum getOperation() {
        return operation;
    }

    public void setOperation(final InlineResponse200.OperationEnum operation) {
        this.operation = operation;
    }

    public InlineResponse200.OperationStateEnum getOperationState() {
        return operationState;
    }

    public void setOperationState(final InlineResponse200.OperationStateEnum operationState) {
        this.operationState = operationState;
    }
}
