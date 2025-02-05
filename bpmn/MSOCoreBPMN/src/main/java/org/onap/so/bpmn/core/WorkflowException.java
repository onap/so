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

package org.onap.so.bpmn.core;

import java.io.Serializable;
import org.onap.so.logging.filter.base.ONAPComponentsList;

/**
 * An object that represents a workflow exception.
 */
public class WorkflowException implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String processKey;
    private final int errorCode;
    private final String errorMessage;
    private final String workStep;
    private ONAPComponentsList extSystemErrorSource;

    /**
     * Constructor
     * 
     * @param processKey the process key for the process that generated the exception
     * @param errorCode the numeric error code (normally 1xxx or greater)
     * @param errorMessage a short error message
     */
    public WorkflowException(String processKey, int errorCode, String errorMessage) {
        this.processKey = processKey;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        workStep = "*";
    }

    public WorkflowException(String processKey, int errorCode, String errorMessage,
            ONAPComponentsList extSystemErrorSource) {
        this.processKey = processKey;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        workStep = "*";
        this.extSystemErrorSource = extSystemErrorSource;
    }

    public WorkflowException(String processKey, int errorCode, String errorMessage, String workStep) {
        this.processKey = processKey;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.workStep = workStep;
    }

    public WorkflowException(String processKey, int errorCode, String errorMessage, String workStep,
            ONAPComponentsList extSystemErrorSource) {
        this.processKey = processKey;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.workStep = workStep;
        this.extSystemErrorSource = extSystemErrorSource;
    }

    /**
     * Returns the process key.
     */
    public String getProcessKey() {
        return processKey;
    }

    /**
     * Returns the error code.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the error message.
     */
    public String getWorkStep() {
        return workStep;
    }

    public ONAPComponentsList getExtSystemErrorSource() {
        return extSystemErrorSource;
    }

    /**
     * Returns a string representation of this object.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[processKey=" + getProcessKey() + ",errorCode=" + getErrorCode()
                + ",errorMessage=" + getErrorMessage() + ",workStep=" + getWorkStep() + ",extSystemErrorSource="
                + extSystemErrorSource + "]";
    }
}
