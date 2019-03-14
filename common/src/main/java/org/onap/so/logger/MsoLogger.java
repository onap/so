/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logger;

public class MsoLogger {
    // Required MDC parameters
    public static final String REQUEST_ID = "RequestID";
    public static final String INVOCATION_ID = "InvocationID";
    public static final String INSTANCE_UUID = "InstanceUUID";
    public static final String SERVICE_NAME = "ServiceName";
    public static final String STATUSCODE = "StatusCode";
    public static final String RESPONSECODE = "ResponseCode";
    public static final String RESPONSEDESC = "ResponseDesc";
    public static final String FQDN = "ServerFQDN";
    public static final String ENTRY_TIMESTAMP = "EntryTimestamp";
    public static final String CLIENT_IPADDRESS = "EntryTimestamp";


    //HTTP Headers
    public static final String HEADER_FROM_APP_ID = "X-FromAppId";
    public static final String ONAP_PARTNER_NAME = "X-ONAP-PartnerName";
    public static final String HEADER_REQUEST_ID = "X-RequestId";
    public static final String TRANSACTION_ID = "X-TransactionID";
    public static final String ECOMP_REQUEST_ID = "X-ECOMP-RequestID";
    public static final String ONAP_REQUEST_ID = "X-ONAP-RequestID";
    public static final String CLIENT_ID = "X-ClientID";
    public static final String INVOCATION_ID_HEADER = "X-InvocationID";
    public static final String REQUESTOR_ID = "X-RequestorID";

    //Default values for not found
    public static final String UNKNOWN_PARTNER = "UnknownPartner";

    public static final String SERVICE_INSTANCE_ID = "ServiceInstanceId";
    public static final String SERVICE_NAME_IS_METHOD_NAME = "ServiceNameIsMethodName";
    public static final String SERVER_IP = "ServerIPAddress";

    public static final String REMOTE_HOST = "RemoteHost";
    public static final String ALERT_SEVERITY = "AlertSeverity";
    public static final String TIMER = "Timer";
    public static final String USER = "User";
    public static final String DUMMY_VALUE = "trace-#";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String CAT_LOG_LEVEL = "CategoryLogLevel";
    public static final String AUDI_CAT_LOG_LEVEL = "AuditCategoryLogLevel";



    public static final String PARTNER_NAME = "PartnerName";

    // Audit/Metric log specific
    public static final String BEGINTIME = "BeginTimestamp";
    public static final String STARTTIME = "StartTimeMilis";
    public static final String ENDTIME = "EndTimestamp";
    public static final String PARTNERNAME = "PartnerName";



    // Metric log specific
    public static final String METRIC_BEGIN_TIME = "MetricBeginTime";
    public static final String METRIC_START_TIME = "MetricStartTime";
    public static final String METRIC_END_TIME = "MetricEndTime";
    public static final String METRIC_TIMER = "MetricEndTime";
    public static final String TARGETENTITY = "TargetEntity";
    public static final String TARGETSERVICENAME = "TargetServiceName";
    public static final String TARGETVIRTUALENTITY = "TargetVirtualEntity";

    public static final String FATAL_LEVEL = "FATAL";
    public static final String ERROR_LEVEL = "ERROR";
    public static final String WARN_LEVEL = "WARN";
    public static final String INFO_LEVEL = "INFO";
    public static final String DEBUG_LEVEL = "DEBUG";

    public static final String ERRORCODE = "ErrorCode";
    public static final String ERRORDESC = "ErrorDesc";

    //Status Codes
    public static final String COMPLETE = "COMPLETE";
    public static final String INPROGRESS = "INPROGRESS";


    public enum Catalog {
        APIH, BPEL, RA, ASDC, GENERAL
    }


    public enum TargetEntity {
        CAMUNDA("Camunda");

        private String name;

        TargetEntity(String name) {
            this.name = name;
        }

        public String getUrl() {
            return name;
        }
    }


    public enum ResponseCode {
        Suc(0), PermissionError(100), DataError(300), DataNotFound(301), BadRequest(302), SchemaError(
            400), BusinessProcesssError(500), ServiceNotAvailable(501), InternalError(502), Conflict(
            503), DBAccessError(504), CommunicationError(505), UnknownError(900);

        private int value;

        ResponseCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }


    public enum ErrorCode {
        PermissionError(100), AvailabilityError(200), DataError(300), SchemaError(400), BusinessProcesssError(
            500), UnknownError(900);

        private int value;

        ErrorCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

}
