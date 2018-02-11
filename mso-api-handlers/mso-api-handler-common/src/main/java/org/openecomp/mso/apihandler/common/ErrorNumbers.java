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

package org.openecomp.mso.apihandler.common;
 
public final class ErrorNumbers {

    private ErrorNumbers() {
    }

    public static final String REQUEST_FAILED_SCHEMA_VALIDATION = "1000";
    public static final String RECIPE_DOES_NOT_EXIST = "1010";
    public static final String VFMODULE_TYPE_DOES_NOT_EXIST = "1011";
    public static final String SERVICE_PARAMETERS_FAILED_SCHEMA_VALIDATION = "1020";
    public static final String LOCKED_SAME_SERVICE_TYPE_AND_REQUEST_ACTION = "1030";
    public static final String LOCKED_CANCEL_ON_REQUEST_IN_PROGRESS = "1031";
    public static final String LOCKED_REQUEST_IN_PROGRESS_TIMEOUT = "1032";
    public static final String LOCKED_NOT_ALLOWED_FOR_REQUEST_IN_PROGRESS = "1033";
    public static final String WITHDRAW_AFTER_COMPLETE = "1034";
    public static final String COMPLETE_AFTER_WITHDRAW = "1035"; // NOT USED
    public static final String REQUEST_PREVIOUSLY_COMPLETED = "1036";
    public static final String CANNOT_RETRY_FINAL_ACTIVATION = "1037";
    public static final String PREVIOUSLY_COMPLETED_WITH_DIFF_ORDER_VERSION = "1038";
    public static final String LOCKED_DIFFERENT_REQUEST_ACTION = "1040";
    public static final String LOCKED_DIFF_REQUEST_ACTION_SAME_SERVICE_INSTANCE_ID = "1041";
    public static final String REQUEST_TIMED_OUT = "1050";

    public static final String DISCONNECT_ALREADY_COMPLETE = "1100";
    public static final String CANCEL_ALREADY_COMPLETE = "1110";
    
    public static final String LOCKED_CREATE_ON_THE_SAME_VNF_NAME_IN_PROGRESS = "1400";
    public static final String LOCKED_SAME_ACTION_AND_VNF_ID = "1410";

    public static final String ERROR_FROM_BPEL = "2000";
    public static final String NO_COMMUNICATION_TO_BPEL = "2010";
    public static final String NO_RESPONSE_FROM_BPEL = "2020";

    public static final String COULD_NOT_WRITE_TO_REQUESTS_DB = "4000";
    public static final String NO_COMMUNICATION_TO_REQUESTS_DB = "4010";
    public static final String NO_COMMUNICATION_TO_CATALOG_DB = "4020";
    public static final String ERROR_FROM_CATALOG_DB = "4030";

    public static final String ERROR_FROM_SDNC = "5300";
    public static final String NO_COMMUNICATION_TO_SDNC = "5310";
    public static final String NO_RESPONSE_FROM_SDNC = "5320";

    public static final String NO_COMMUNICATION_TO_SDNC_ADAPTER = "7000";
    public static final String NO_RESPONSE_FROM_SDNC_ADAPTER = "7010";
    public static final String ERROR_FROM_SDNC_ADAPTER = "7020";
    
    public static final String SVC_GENERAL_SERVICE_ERROR = "SVC0001";
    public static final String SVC_BAD_PARAMETER = "SVC0002";
    public static final String SVC_NO_SERVER_RESOURCES = "SVC1000";
    public static final String SVC_DETAILED_SERVICE_ERROR = "SVC2000";

}
