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

package org.onap.so.adapters.vfc.constant;

/**
 * Constant Class.<br/>
 * <p>
 * Define constant for http operation.
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam 2016/8/4
 */
public class HttpCode {

    /**
     * Fail to request.
     */
    public static final int BAD_REQUEST = 400;

    /**
     * Inner error
     */
    public static final int INTERNAL_SERVER_ERROR = 500;

    /**
     * Not accept request.
     */
    public static final int NOT_ACCEPTABLE = 406;

    /**
     * Not found service.
     */
    public static final int NOT_FOUND = 404;

    /**
     * Accept request.
     */
    public static final int RESPOND_ACCEPTED = 202;

    /**
     * Http response is ok.
     */
    public static final int RESPOND_OK = 200;

    public static final int CREATED_OK = 201;

    /**
     * Conflict
     */
    public static final int RESPOND_CONFLICT = 409;

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since ONAP Amsterdam Release 2017-9-6
     */
    private HttpCode() {

    }

    /**
     * Whether request is successful.<br/>
     * 
     * @param httpCode response code
     * @return true or false
     * @since ONAP Amsterdam Release 2017-9-6
     */
    public static boolean isSucess(int httpCode) {
        return httpCode / 100 == 2;
    }
}
