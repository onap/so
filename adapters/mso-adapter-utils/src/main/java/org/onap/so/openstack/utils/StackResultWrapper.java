/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.utils;

import org.apache.commons.lang3.builder.ToStringBuilder;
import com.woorea.openstack.heat.model.Stack;

public class StackResultWrapper {

    private Stack stack;
    private boolean stackTimedOutPolling;
    private boolean stackNotFound;
    private String errorMessage;
    private String errorCode;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("stack", stack).append("stackTimedOutPolling", stackTimedOutPolling)
                .append("stackNotFound", stackNotFound).append("errorMessage", errorMessage)
                .append("errorCode", errorCode).toString();
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
    }

    public boolean isStackTimedOutPolling() {
        return stackTimedOutPolling;
    }

    public void setStackTimedOutPolling(boolean stackTimedOutPolling) {
        this.stackTimedOutPolling = stackTimedOutPolling;
    }

    public boolean isStackNotFound() {
        return stackNotFound;
    }

    public void setStackNotFound(boolean stackNotFound) {
        this.stackNotFound = stackNotFound;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }


}
