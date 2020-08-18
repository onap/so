/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.cnf.exceptions;

import static org.onap.so.adapters.cnf.util.CNfAdapterUtil.marshal;
import org.onap.so.adapters.cnf.model.ErrorResponse;
import org.springframework.http.ResponseEntity;

public class ApplicationException extends Exception {

    private static final long serialVersionUID = 1L;

    private int errorCode;

    private String errorMsg;

    public ApplicationException(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public ResponseEntity buildErrorResponse() {
        String message;
        try {
            ErrorResponse err = new ErrorResponse(errorCode, errorMsg);
            message = marshal(err);
        } catch (ApplicationException e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
        return ResponseEntity.status(errorCode).body(message);
    }
}
