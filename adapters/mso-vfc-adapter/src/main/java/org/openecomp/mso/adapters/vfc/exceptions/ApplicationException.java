/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.vfc.exceptions;

import javax.ws.rs.core.Response;;

public class ApplicationException extends Exception {

    /**
     * Serial number.
     */
    private static final long serialVersionUID = 1L;

    private int errorCode;

    private String errorMsg;

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @param errorCode error status
     * @param errorMsg error detail
     * @since ONAP Amsterdam Release 2017-9-6
     */
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

    /**
     * build error Response
     * <br>
     * 
     * @return
     * @since ONAP Amsterdam Release
     */
    public Response buildErrorResponse() {
        return Response.status(errorCode).entity(errorMsg).build();
    }
}
