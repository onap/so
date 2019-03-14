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


import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.onap.so.entity.MsoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


/**
 * This class supports all of the normal logging functions (debug, info, etc.), prepending
 * a string of format "[<requestId>|<serviceId]" to each message.
 *
 * SO code should initialize with these IDs when available, so that individual
 * requests and/or services can be tracked throughout the various MSO component
 * logs (API Handler, BPEL, and Adapters).
 *
 *
 */

public class MsoLogger {
    // Required MDC parameters
    public static final String REQUEST_ID                  = "RequestID";
    public static final String INVOCATION_ID               = "InvocationID";
    public static final String INSTANCE_UUID               = "InstanceUUID";
    public static final String SERVICE_NAME                = "ServiceName";
    public static final String STATUSCODE                  = "StatusCode";
    public static final String RESPONSECODE                = "ResponseCode";
    public static final String RESPONSEDESC                = "ResponseDesc";
    public static final String FQDN                        = "ServerFQDN";
    public static final String ENTRY_TIMESTAMP             = "EntryTimestamp";
    public static final String CLIENT_IPADDRESS            = "EntryTimestamp";
    
    
    //HTTP Headers
    public static final String HEADER_FROM_APP_ID          = "X-FromAppId";
    public static final String ONAP_PARTNER_NAME           = "X-ONAP-PartnerName";
    public static final String HEADER_REQUEST_ID           = "X-RequestId";
    public static final String TRANSACTION_ID              = "X-TransactionID";
    public static final String ECOMP_REQUEST_ID            = "X-ECOMP-RequestID";
    public static final String ONAP_REQUEST_ID            = "X-ONAP-RequestID";
    public static final String CLIENT_ID                   = "X-ClientID";
    public static final String INVOCATION_ID_HEADER        = "X-InvocationID";
    public static final String REQUESTOR_ID                = "X-RequestorID";
    
    //Default values for not found
    public static final String UNKNOWN_PARTNER                 = "UnknownPartner";
    
    public static final String SERVICE_INSTANCE_ID         = "ServiceInstanceId";    
    public static final String SERVICE_NAME_IS_METHOD_NAME = "ServiceNameIsMethodName";
    public static final String SERVER_IP                   = "ServerIPAddress";   
    
    public static final String REMOTE_HOST                 = "RemoteHost";
    public static final String ALERT_SEVERITY              = "AlertSeverity";
    public static final String TIMER                       = "Timer";
    public static final String USER                        = "User";
    public static final String DUMMY_VALUE                 = "trace-#";
    public static final String UNKNOWN                     = "UNKNOWN";
    public static final String CAT_LOG_LEVEL 			   = "CategoryLogLevel";
    public static final String AUDI_CAT_LOG_LEVEL 		   = "AuditCategoryLogLevel";
    


    public static final String PARTNER_NAME                 = "PartnerName";

    // Audit/Metric log specific
    public static final String BEGINTIME                   = "BeginTimestamp";
    public static final String STARTTIME                   = "StartTimeMilis";
    public static final String ENDTIME                     = "EndTimestamp";
    public static final String PARTNERNAME                 = "PartnerName";

    
    
    // Metric log specific
    public static final String METRIC_BEGIN_TIME           = "MetricBeginTime";
    public static final String METRIC_START_TIME           = "MetricStartTime";
    public static final String METRIC_END_TIME                = "MetricEndTime";
    public static final String METRIC_TIMER                = "MetricEndTime";
    public static final String TARGETENTITY                = "TargetEntity";
    public static final String TARGETSERVICENAME           = "TargetServiceName";
    public static final String TARGETVIRTUALENTITY         = "TargetVirtualEntity";

    public static final String FATAL_LEVEL                 = "FATAL";
    public static final String ERROR_LEVEL                 = "ERROR";
    public static final String WARN_LEVEL                  = "WARN";
    public static final String INFO_LEVEL                  = "INFO";
    public static final String DEBUG_LEVEL                 = "DEBUG";

    public static final String ERRORCODE                   = "ErrorCode";
    public static final String ERRORDESC                   = "ErrorDesc";
    
    //Status Codes
    public static final String COMPLETE                    = "COMPLETE";    
    public static final String INPROGRESS                    = "INPROGRESS";
    
    public enum Catalog {
        APIH, BPEL, RA, ASDC, GENERAL
    }

    public enum StatusCode {
        COMPLETE, ERROR
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
                400), BusinessProcesssError(500), ServiceNotAvailable(501), InternalError(
                        502), Conflict(503), DBAccessError(504), CommunicationError(505), UnknownError(900);

        private int value;

        public int getValue() {
            return this.value;
        }

        private ResponseCode(int value) {
            this.value = value;
        }
    }

    public enum ErrorCode {
        PermissionError(100), AvailabilityError(200), DataError(300), SchemaError(400), BusinessProcesssError(
                500), UnknownError(900);

        private int value;

        public int getValue() {
            return this.value;
        }

        private ErrorCode(int value) {
            this.value = value;
        }
    }


    private Logger logger;
    private Logger metricsLogger;
    private Logger auditLogger;
    private static String       instanceUUID, serverIP, serverName;
    private MessageEnum         exceptionArg, defaultException, defaultWarning, defaultAudit, defaultMetrics;

    private MsoLogger() {
        this(MsoLogger.Catalog.GENERAL);
    }
    
    private MsoLogger(MsoLogger.Catalog cat) {
        this(cat, MethodHandles.lookup().lookupClass());
    }
    
    private MsoLogger(MsoLogger.Catalog cat, Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.auditLogger = LoggerFactory.getLogger("AUDIT");
        this.metricsLogger = LoggerFactory.getLogger("METRIC");       
        setDefaultLogCatalog(cat);
    }
    
    public static MsoLogger getMsoLogger(MsoLogger.Catalog cat, Class<?> clazz) {
        return new MsoLogger(cat,clazz);
    }
    
    

    /**
     * Record the Metrics event with no argument
     * 
     * @param startTime
     *            Transaction starting time in millieseconds
     * @param statusCode
     *            StatusCode of the transaction, either COMPLETE or ERROR
     * @param responseCode
     *            The response code returned by the sub-components
     * @param responseDesc
     *            Human redable description of the response code
     * @param targetEntity
     *            The component which is invoked for this sub-operation
     * @param targetServiceName
     *            API invoked on the TargetEntity
     * @param targetVEntity
     *            Target VNF or VM acted opon by the component, if available
     */

    public void recordMetricEvent(Long startTime, StatusCode statusCode, ResponseCode responseCode, String responseDesc,
            String targetEntity, String targetServiceName, String targetVEntity) {
        prepareMetricMsg(startTime, statusCode, responseCode.getValue(), responseDesc, targetEntity, targetServiceName,
                targetVEntity);
        metricsLogger.info("");
        MDC.remove(TIMER);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }


    /**
     * Record the Audit event
     *
     * @param startTime
     *            Transaction starting time in millieseconds
     * @param statusCode
     *            StatusCode of the transaction, either COMPLETE or ERROR
     * @param responseCode
     *            The application specific response code
     * @param responseDesc
     *            Human redable description of the application response code
     */

    public void recordAuditEvent(Long startTime, StatusCode statusCode, ResponseCode responseCode,
            String responseDesc) {
    	if (StringUtils.isEmpty(MDC.get(MsoLogger.PARTNERNAME))) {
    		MDC.put(MsoLogger.PARTNERNAME, "UNKNOWN");
    	}
        prepareAuditMsg(startTime, statusCode, responseCode.getValue(), responseDesc);
        auditLogger.info("");
        MDC.remove(TIMER);
    }

    // Debug methods
    /**
     * Record the Debug event
     *
     * @param msg
     *            The log message to put
     */
    public void debug(String msg) {
        prepareMsg(DEBUG_LEVEL);
        logger.debug(msg);
    }

    /**
     * Record the Debug event
     *
     * @param msg
     *            The log message to put
     * @param t
     *            The exception to put
     */
    public void debug(String msg, Throwable t) {
        prepareMsg(DEBUG_LEVEL);
        logger.debug(msg, t);
    }
    
    public void info(String msg) {
        prepareMsg(DEBUG_LEVEL);
        logger.info(msg);
    }
    
    
    /**
     * Log error message with the details of the exception that caused the error.
     * @param msg
     * @param throwable
     */
    public void error(String msg) {
        prepareMsg(ERROR_LEVEL);
        logger.error(msg);
    }
    
    /**
     * Log error message with the details of the exception that caused the error.
     * @param msg
     * @param throwable
     */
    public void error(String msg, Throwable throwable) {
        prepareMsg(ERROR_LEVEL);
        logger.error(msg, throwable);
    }

    // Info methods
    /**
     * Record the Info event
     *
     * @param msg
     *            The log message to put
     */
    public void info(String msg, String targetEntity, String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Info event with 1 argument
     *
     * @param msg
     *            The log message to put
     * @param arg0
     *            The argument used in the log message
     */
    public void info(MessageEnum msg, String arg0, String targetEntity, String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg.toString(), normalize(arg0));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Info event with 2 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1
     *            The arguments used in the log message
     */
    public void info(String msg, String arg0, String arg1, String targetEntity,
            String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg, normalize(arg0), normalize(arg1));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Info event with 3 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2
     *            The arguments used in the log message
     */
    public void info(MessageEnum msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg.toString(), normalize(arg0), normalize(arg1), normalize(arg2));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Info event with 4 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3
     *            The arguments used in the log message
     */
    public void info(String msg, String arg0, String arg1, String arg2, String arg3,
            String targetEntity, String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Info event with 5 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3,arg4
     *            The arguments used in the log message
     */
    public void info(String msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String targetEntity, String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Info event with 6 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3,arg4,arg5
     *            The arguments used in the log message
     */
    public void info(String msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String arg5, String targetEntity, String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4),
                normalize(arg5));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    // Warning methods
    
    
    /**
     * Record the Warning event
     *
     * @param msg
     *            The log message to put
     */
    public void warnSimple( String targetServiceName, String errorDesc) {     
        logger.warn("Service Name: {} Error: {}" , targetServiceName, errorDesc);            
    }
    /**
     * Record the Warning event
     *
     * @param msg
     *            The log message to put
     */
    public void warn(MessageEnum msg, String targetEntity, String targetServiceName, ErrorCode errorCode,
            String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);

        logger.warn(msg.toString());
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warning event
     *
     * @param msg
     *            The log message to put
     * @param t
     *            The exception info
     */
    public void warn(MessageEnum msg, String targetEntity, String targetServiceName, ErrorCode errorCode,
            String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn("Warning: "+msg, t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 1 argument
     *
     * @param msg
     *            The log message to put
     * @param arg
     *            The argument used in the log message
     */
    public void warn(MessageEnum msg, String arg, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg.toString(), arg);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 1 argument
     *
     * @param msg
     *            The log message to put
     * @param arg
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void warn(MessageEnum msg, String arg, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg.toString(), arg);
        logger.debug("Exception raised", t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 2 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1
     *            The arguments used in the log message
     */
    public void warn(MessageEnum msg, String arg0, String arg1, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg.toString(), normalize(arg0), normalize(arg1));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 2 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void warn(String msg, String arg0, String arg1, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1));
        logger.warn(msg, t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 3 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2
     *            The arguments used in the log message
     */
    public void warn(String msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 3 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void warn(String msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2));
        logger.warn(msg, t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 4 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3
     *            The arguments used in the log message
     */
    public void warn(String msg, String arg0, String arg1, String arg2, String arg3,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 4 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,
     *            arg3 The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void warn(String msg, String arg0, String arg1, String arg2, String arg3,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3));
        logger.warn(msg, t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 5 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3,arg4
     *            The arguments used in the log message
     */
    public void warn(String msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Warn event with 5 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3,arg4
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void warn(String msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4));
        logger.warn(msg, t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }
    
    

    // Error methods
    /**
     * Record the Error event
     *
     * @param generalException
     *            The log message to put
     */
    public void error(MessageEnum generalException, String targetEntity, String targetServiceName, ErrorCode errorCode,
            String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(generalException.toString() + ": " +errorDesc);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }
    
    
    /**
     * Record the Error event
     *
     * @param msg
     *            The log message to put
     * @param t
     *            The exception info
     */
    public void trace(String traceMessage) {     
        logger.trace(traceMessage);      
    }
    
    
    /**
     * Record the Error event
     *
     * @param msg
     *            The log message to put
     * @param t
     *            The exception info
     */
    public void error( Throwable t) {     
        logger.error(t.getMessage(), t);
    }
    

    /**
     * Record the Error event
     *
     * @param msg
     *            The log message to put
     * @param t
     *            The exception info
     */
    public void error(MessageEnum msg, String targetEntity, String targetServiceName, ErrorCode errorCode,
            String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), t);        
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }
    
    /**
     * Record the Error event with 1 argument
     *
     * @param msg
     *            The log message to put
     * @param arg0
     *            The arguments used in the log message
     */
    public void error(MessageEnum msg, String arg0, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 1 argument
     *
     * @param msg
     *            The log message to put
     * @param arg0
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void error(MessageEnum msg, String arg0, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), t);       
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 2 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1
     *            The arguments used in the log message
     */
    public void error(MessageEnum msg, String arg0, String arg1, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), normalize(arg1));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 2 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void error(MessageEnum msg, String arg0, String arg1, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), normalize(arg1), t);
        logger.debug("Exception raised", t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 3 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2
     *            The arguments used in the log message
     */
    public void error(MessageEnum msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), normalize(arg1), normalize(arg2));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 3 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void error(MessageEnum msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), normalize(arg1), normalize(arg2), t);        
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 4 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3
     *            The arguments used in the log message
     */
    public void error(MessageEnum msg, String arg0, String arg1, String arg2, String arg3,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 4 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void error(MessageEnum msg, String arg0, String arg1, String arg2, String arg3,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), t);
      
        logger.debug("Exception raised", t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 5 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3,arg4
     *            The arguments used in the log message
     */
    public void error(MessageEnum msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4));
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    /**
     * Record the Error event with 5 arguments
     *
     * @param msg
     *            The log message to put
     * @param arg0,arg1,arg2,arg3,arg4
     *            The arguments used in the log message
     * @param t
     *            The exception info
     */
    public void error(MessageEnum msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg.toString(), normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4), t);      
        logger.debug("Exception raised", t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }
    
	public void error(String errorMessage, String errorSource, String targetEntity, String targetServiceName,
			ErrorCode errorCode, String errorText) {
		 prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorText);
	        logger.error(errorMessage);     	       
	        MDC.remove(TARGETENTITY);
	        MDC.remove(TARGETSERVICENAME);
		
	}

    private void prepareMsg(String loggingLevel) {
        prepareMsg(loggingLevel, null, null);
    }

    private void prepareMsg(String loggingLevel, String serviceNamep, String timer) {
        String reqId = MDC.get(REQUEST_ID);
        String svcId = MDC.get(SERVICE_INSTANCE_ID);

        // Based on the discussion with Adrian,
        // if these 2 parameters is not available, using dummy value "trace-#"
        if (reqId == null || reqId.isEmpty()) {
            MDC.put(REQUEST_ID, DUMMY_VALUE);
        }

        if (timer != null) {
            MDC.put(TIMER, timer);
        } 

        writeIfNotNullorEmpty(SERVICE_NAME,getFinalServiceName(serviceNamep));
        writeIfNotNullorEmpty(ALERT_SEVERITY,getSeverityLevel(loggingLevel));
        writeIfNotNullorEmpty(INSTANCE_UUID,instanceUUID);
        writeIfNotNullorEmpty(SERVER_IP,serverIP);
        writeIfNotNullorEmpty(FQDN,serverName);
 
    }

	private void writeIfNotNullorEmpty(String Key, String value) {
		if (MDC.get(Key) == null|| MDC.get(Key).isEmpty()) {
        	MDC.put(Key, value);
        }
	}

    private void prepareAuditMsg(long startTime, StatusCode statusCode, int responseCode, String responseDesc) {
        long endTime = System.currentTimeMillis();
        prepareMsg(INFO_LEVEL, null, String.valueOf(endTime - startTime));
        prepareAuditMetricMsg(startTime, endTime, statusCode, responseCode, responseDesc);
    }

    private void prepareAuditMetricMsg(long startTime, long endTime, StatusCode statusCode, int responseCode,
            String responseDesc) {
        Date startDate = new Date(startTime);
        Date endDate = new Date(endTime);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        MDC.put(BEGINTIME, String.valueOf(formatter.format(startDate)));
        MDC.put(ENDTIME, String.valueOf(formatter.format(endDate)));
        MDC.put(STATUSCODE, statusCode.name());
        MDC.put(RESPONSECODE, String.valueOf(responseCode));
        MDC.put(RESPONSEDESC, responseDesc);
    }

    private void prepareErrorMsg(String loggingLevel, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc) {
        MDC.put(ALERT_SEVERITY, getSeverityLevel(loggingLevel));
        if(errorCode != null) {
            MDC.put(ERRORCODE, Integer.toString(errorCode.getValue()));
        }
        MDC.put(ERRORDESC, errorDesc);
        MDC.put(TARGETENTITY, targetEntity);
        MDC.put(TARGETSERVICENAME, targetServiceName);
    }

    private void prepareMetricMsg(long startTime, StatusCode statusCode, int responseCode, String responseDesc,
            String targetEntity, String targetServiceName, String targetVEntity) {
        long endTime = System.currentTimeMillis();
        prepareMsg(INFO_LEVEL, null, String.valueOf(endTime - startTime));
        prepareAuditMetricMsg(startTime, endTime, statusCode, responseCode, responseDesc);

        // Populate Metric log specific parameter
        MDC.put(TARGETENTITY, targetEntity);
        MDC.put(TARGETSERVICENAME, targetServiceName);

        if (null != targetVEntity) {
            MDC.put(TARGETVIRTUALENTITY, targetVEntity);
        }
    }

    private String getSeverityLevel(String loggingLevel) {
        String severity;
        // According to the Nagios alerting: 0=OK; 1=WARNING; 2=UNKOWN;
        // 3=CRITICAL
        switch (loggingLevel) {
            case ERROR_LEVEL:
                severity = "2";
                break;
            case FATAL_LEVEL:
                severity = "3";
                break;
            case WARN_LEVEL:
                severity = "1";
                break;
            default:
                severity = "0";
                break;
        }
        return severity;
    }

    private String getFinalServiceName(String serviceNamep) {
        // This step to set the serviceName should be put after the className is
        // get,
        // since the default serviceName is obtained during the method to get
        // the className.
        //
        // There's 3 ways to set the serviceName. The first method has the most
        // priority to set the value.
        // a) If the serviceName is set within the log method, this value will
        // be used first
        // b) If serviceName is not set within the log method, the value defined
        // in the MDC will be used
        // c) If nothing is set specifically, then MsoLogger will assign a
        // default(MSO.<method_name>) value to it
        String serName = MDC.get(MsoLogger.SERVICE_NAME);

        // Check if service name was already set as the method name by a
        // previous call to this method.
        String isMethodNameStr = MDC.get(MsoLogger.SERVICE_NAME_IS_METHOD_NAME);
        boolean isMethodName = isMethodNameStr != null && isMethodNameStr.equals(Boolean.TRUE.toString());
        if (serviceNamep != null) {
            return serviceNamep;
        } else if (serName != null && !isMethodName) {
            return serName;
        }

        MDC.put(MsoLogger.SERVICE_NAME_IS_METHOD_NAME, Boolean.TRUE.toString());
        int limit;
        StackTraceElement[] classArr = new Exception().getStackTrace();
        if (classArr.length >= 6) {
            limit = 7;
        } else {
            limit = classArr.length;
        }
        for (int i = 1; i < limit; i++) {
            if (!classArr[i].getClassName().equals(this.getClass().getName())) {
                return classArr[i].getMethodName();
            }
        }
        return classArr[0].getMethodName();
    }

    /**
     * Set the requestId and serviceInstanceId
     *
     * @param reqId
     *            The requestId
     * @param svcId
     *            The serviceInstanceId
     */
    public static void setLogContext(String reqId, String svcId) {
        if (null != reqId) {
            MDC.put(REQUEST_ID, reqId);
        }

        if (null != svcId) {
            MDC.put(SERVICE_INSTANCE_ID, svcId);
        }
    }

    private String normalize(String input) {
        if (input == null) {
            return null;
        }
        String result = input.replace('|', '!');
        result = result.replace("\n", " - ");
        return result;
    }

    private void setDefaultLogCatalog(MsoLogger.Catalog cat) {
        if ("APIH".equals(cat.toString())) {
            exceptionArg = MessageEnum.APIH_GENERAL_EXCEPTION_ARG;
            defaultException = MessageEnum.APIH_GENERAL_EXCEPTION;
            defaultWarning = MessageEnum.APIH_GENERAL_WARNING;
            defaultAudit = MessageEnum.APIH_AUDIT_EXEC;
            defaultMetrics = MessageEnum.APIH_GENERAL_METRICS;
        } else if ("RA".equals(cat.toString())) {
            exceptionArg = MessageEnum.RA_GENERAL_EXCEPTION_ARG;
            defaultException = MessageEnum.RA_GENERAL_EXCEPTION;
            defaultWarning = MessageEnum.RA_GENERAL_WARNING;
            defaultAudit = MessageEnum.RA_AUDIT_EXEC;
            defaultMetrics = MessageEnum.RA_GENERAL_METRICS;
        } else if ("BPEL".equals(cat.toString())) {
            exceptionArg = MessageEnum.BPMN_GENERAL_EXCEPTION_ARG;
            defaultException = MessageEnum.BPMN_GENERAL_EXCEPTION;
            defaultWarning = MessageEnum.BPMN_GENERAL_WARNING;
            defaultAudit = MessageEnum.BPMN_AUDIT_EXEC;
            defaultMetrics = MessageEnum.BPMN_GENERAL_METRICS;
        } else if ("ASDC".equals(cat.toString())) {
            exceptionArg = MessageEnum.ASDC_GENERAL_EXCEPTION_ARG;
            defaultException = MessageEnum.ASDC_GENERAL_EXCEPTION;
            defaultWarning = MessageEnum.ASDC_GENERAL_WARNING;
            defaultAudit = MessageEnum.ASDC_AUDIT_EXEC;
            defaultMetrics = MessageEnum.ASDC_GENERAL_METRICS;
        } else {
            exceptionArg = MessageEnum.GENERAL_EXCEPTION_ARG;
            defaultException = MessageEnum.GENERAL_EXCEPTION;
            defaultWarning = MessageEnum.GENERAL_WARNING;
            defaultAudit = MessageEnum.AUDIT_EXEC;
            defaultMetrics = MessageEnum.GENERAL_METRICS;
        }
    }
}
