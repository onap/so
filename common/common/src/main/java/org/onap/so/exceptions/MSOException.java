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

package org.onap.so.exceptions;


public class MSOException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 4563920496855255206L;
    private Integer errorCode;

    public MSOException(String msg) {
        super(msg);
    }

    public MSOException(Throwable e) {
        super(e);
    }

    public MSOException(String msg, Throwable e) {
        super(msg, e);
    }

    public MSOException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public MSOException(String msg, int errorCode, Throwable t) {
        super(msg, t);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
