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
 * <br>
 * <p>
 * </p>
 * identification of adapter exception
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-9-6
 */
public class DriverExceptionID {

    public static final String INVALID_RESPONSE_FROM_INSTANTIATE_OPERATION =
            "Invalid response from instantiate operation";

    public static final String INVALID_RESPONSEE_FROM_CREATE_OPERATION = "Invalid response from create operation";

    public static final String FAIL_TO_INSTANTIATE_NS = "Fail to instantiate ns";

    public static final String FAIL_TO_CREATE_NS = "Fail to create ns";

    public static final String INVALID_RESPONSE_FROM_TERMINATE_OPERATION = "Invalid response from terminate operation";

    public static final String FAIL_TO_DELETE_NS = "Fail to delete ns";

    public static final String FAIL_TO_TERMINATE_NS = "Fail to terminate ns";

    public static final String JOB_STATUS_ERROR = "Job status error";

    public static final String FAIL_TO_QUERY_JOB_STATUS = "Fail to query job status";

    public static final String FAIL_TO_SCALE_NS = "Fail to scale network service";

    public static final String INVALID_RESPONSE_FROM_SCALE_OPERATION = "Invalid response from scale operation";


    private DriverExceptionID() {

    }

}
