/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
package org.onap.so.etsi.nfvo.ns.lcm.database.beans;

import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils.Utils.toIndentedString;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author mukeshsharma(mukeshsharma@est.tech)
 */
@Entity
@Table(name = "NS_LCM_OP_OCCS")
public class NsLcmOpOcc {

    @Id
    @Column(name = "ID")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATION_STATE")
    private OperationStateEnum operationState;

    @Column(name = "STATE_ENTERED_TIME")
    private LocalDateTime stateEnteredTime;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NS_INST_ID")
    private NfvoNsInst nfvoNsInst;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATION")
    private NsLcmOpType operation;

    @Column(name = "IS_AUTO_INNOVATION")
    private boolean isAutoInnovation;

    @Column(name = "OPERATION_PARAMS", columnDefinition = "LONGTEXT")
    private String operationParams;

    @Column(name = "IS_CANCEL_PENDING")
    private boolean isCancelPending;

    public NsLcmOpOcc() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public NsLcmOpOcc id(final String id) {
        this.id = id;
        return this;
    }

    public OperationStateEnum getOperationState() {
        return operationState;
    }

    public void setOperationState(final OperationStateEnum operationState) {
        this.operationState = operationState;
    }

    public NsLcmOpOcc operationState(final OperationStateEnum operationState) {
        this.operationState = operationState;
        return this;
    }

    public LocalDateTime getStateEnteredTime() {
        return stateEnteredTime;
    }

    public void setStateEnteredTime(final LocalDateTime stateEnteredTime) {
        this.stateEnteredTime = stateEnteredTime;
    }

    public NsLcmOpOcc stateEnteredTime(final LocalDateTime stateEnteredTime) {
        this.stateEnteredTime = stateEnteredTime;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public NsLcmOpOcc startTime(final LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public NfvoNsInst getNfvoNsInst() {
        return nfvoNsInst;
    }

    public void setNfvoNsInst(final NfvoNsInst nfvoNsInst) {
        this.nfvoNsInst = nfvoNsInst;
    }

    public NsLcmOpOcc nfvoNsInst(final NfvoNsInst nfvoNsInst) {
        this.nfvoNsInst = nfvoNsInst;
        return this;
    }

    public NsLcmOpType getOperation() {
        return operation;
    }

    public void setOperation(final NsLcmOpType operation) {
        this.operation = operation;
    }

    public NsLcmOpOcc operation(final NsLcmOpType operation) {
        this.operation = operation;
        return this;
    }

    public boolean getIsAutoInnovation() {
        return isAutoInnovation;
    }

    public void setIsAutoInnovation(final boolean isAutoInnovation) {
        this.isAutoInnovation = isAutoInnovation;
    }

    public NsLcmOpOcc isAutoInnovation(final boolean isAutoInnovation) {
        this.isAutoInnovation = isAutoInnovation;
        return this;
    }

    public String getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(final String operationParams) {
        this.operationParams = operationParams;
    }

    public NsLcmOpOcc operationParams(final String operationParams) {
        this.operationParams = operationParams;
        return this;
    }

    public boolean getIsCancelPending() {
        return isCancelPending;
    }

    public void setIsCancelPending(final boolean isCancelPending) {
        this.isCancelPending = isCancelPending;
    }

    public NsLcmOpOcc isCancelPending(final boolean isCancelPending) {
        this.isCancelPending = isCancelPending;
        return this;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        final NsLcmOpOcc that = (NsLcmOpOcc) object;
        return Objects.equals(id, that.id) && Objects.equals(operationState, that.operationState)
                && Objects.equals(stateEnteredTime, that.stateEnteredTime) && Objects.equals(startTime, that.startTime)
                && Objects.equals(nfvoNsInst, that.nfvoNsInst) && Objects.equals(operation, that.operation)
                && Objects.equals(isAutoInnovation, that.isAutoInnovation)
                && Objects.equals(operationParams, that.operationParams)
                && Objects.equals(isCancelPending, that.isCancelPending);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, operationState, stateEnteredTime, startTime, nfvoNsInst, operation, isAutoInnovation,
                operationParams, isCancelPending);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class NfvoNsInst {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    operationState: ").append(toIndentedString(operationState)).append("\n");
        sb.append("    stateEnteredTime: ").append(toIndentedString(stateEnteredTime)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    nfvoNsInst: ").append(nfvoNsInst != null ? toIndentedString(nfvoNsInst.getNsInstId()) : null)
                .append("\n");
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
        sb.append("    isAutoInnovation: ").append(toIndentedString(isAutoInnovation)).append("\n");
        sb.append("    operationParams: ").append(toIndentedString(operationParams)).append("\n");
        sb.append("    isCancelPending: ").append(toIndentedString(isCancelPending)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
