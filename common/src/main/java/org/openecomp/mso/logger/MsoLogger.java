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

package org.openecomp.mso.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.MDC;

import org.openecomp.mso.entity.MsoRequest;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResolvableErrorEnum;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple wrapper around the EELF Logger class for MSO usage. This class
 * supports all of the normal logging functions (debug, info, etc.), prepending
 * a string of format "[<requestId>|<serviceId]" to each message.
 *
 * MSO code should initialize with these IDs when available, so that individual
 * requests and/or services can be tracked throughout the various MSO component
 * logs (API Handler, BPEL, and Adapters).
 *
 *
 */
public class MsoLogger {
    // MDC parameters
    public static final String  REQUEST_ID                  = "RequestId";
    public static final String  SERVICE_INSTANCE_ID         = "ServiceInstanceId";
    public static final String  SERVICE_NAME                = "ServiceName";
    private static final String SERVICE_NAME_IS_METHOD_NAME = "ServiceNameIsMethodName";
    private static final String INSTANCE_UUID               = "InstanceUUID";
    private static final String SERVER_IP                   = "ServerIPAddress";
    private static final String FQDN                        = "ServerFQDN";
    public static final String  REMOTE_HOST                 = "RemoteHost";
    public static final String  ALERT_SEVERITY              = "AlertSeverity";
    public static final String  TIMER                       = "Timer";
    private static final String USER                        = "User";
    private static final String DUMMY_VALUE                 = "trace-#";
    public static final String  UNKNOWN                     = "UNKNOWN";
    //For getting an identity of calling application
    public static final String HEADER_FROM_APP_ID           = "X-FromAppId";
    public static final String FROM_APP_ID                  = "FromAppId";
    // Audit/Metric log specific
    private static final String BEGINTIME                   = "BeginTimestamp";
    private static final String ENDTIME                     = "EndTimestamp";
    public static final String  PARTNERNAME                 = "PartnerName";
    private static final String STATUSCODE                  = "StatusCode";
    private static final String RESPONSECODE                = "ResponseCode";
    private static final String RESPONSEDESC                = "ResponseDesc";
    // Metric log specific
    private static final String TARGETENTITY                = "TargetEntity";
    private static final String TARGETSERVICENAME           = "TargetServiceName";
    private static final String TARGETVIRTUALENTITY         = "TargetVirtualEntity";

    private static final String FATAL_LEVEL                 = "FATAL";
    private static final String ERROR_LEVEL                 = "ERROR";
    private static final String WARN_LEVEL                  = "WARN";
    private static final String INFO_LEVEL                  = "INFO";
    private static final String DEBUG_LEVEL                 = "DEBUG";

    private static final String ERRORCODE                   = "ErrorCode";
    private static final String ERRORDESC                   = "ErrorDesc";

    public enum Catalog {
        APIH, BPEL, RA, ASDC, GENERAL
    }

    public enum StatusCode {
        COMPLETE, ERROR
    }

    public enum ResponseCode {
        Suc(0), PermissionError(100), DataError(300), DataNotFound(301), BadRequest(302), SchemaError(
                400), BusinessProcesssError(500), ServiceNotAvailable(501), InternalError(
                        502), Conflict(503), DBAccessError(504), CommunicationError(505), UnknownError(900);

        private int value;

        public int getValue() {
            return this.value;
        }

        ResponseCode(int value) {
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

        ErrorCode(int value) {
            this.value = value;
        }
    }

    private EELFLogger          logger, auditLogger, metricsLogger;
    private static final String CONFIG_FILE = System.getProperty("jboss.home.dir") + "/mso-config/uuid/uuid_"
            + System.getProperty("jboss.server.name");
    private static String       instanceUUID, serverIP, serverName;
    private MessageEnum         exceptionArg, defaultException, defaultWarning, defaultAudit, defaultMetrics;

    // For internal logging of the initialization of MSO logs
    private static final Logger LOGGER      = Logger.getLogger(MsoLogger.class.getName());


    // Since four adaptors are using the instance of  MsoLogger which will be referenced everywhere
    // hence limiting the number of MsoLogger instances to five.
    private static final MsoLogger generalMsoLogger = new MsoLogger(Catalog.GENERAL);
    private static final MsoLogger apihLogger = new MsoLogger(Catalog.APIH);
    private static final MsoLogger asdcLogger = new MsoLogger(Catalog.ASDC);
    private static final MsoLogger raLogger = new MsoLogger(Catalog.RA);
    private static final MsoLogger bpelLogger = new MsoLogger(Catalog.BPEL);

    static {
        if (instanceUUID == null || ("").equals(instanceUUID)) {
            instanceUUID = getInstanceUUID();
        }

        if (serverIP == null || serverName == null || ("").equals(serverIP) || ("").equals(serverName)) {
            try {
                InetAddress server = InetAddress.getLocalHost();
                serverIP = server.getHostAddress();
                serverName = server.getHostName();
            } catch (UnknownHostException e) {
                LOGGER.log(Level.SEVERE, "Could not get local hostname", e);
                serverIP = "";
                serverName = "";
            }
        }
    }

    // Singleton instances of the EELFLogger of all types are referenced by MsoLogger
    private MsoLogger(Catalog cat) {
        this.logger = EELFManager.getInstance().getErrorLogger();
        this.auditLogger = EELFManager.getInstance().getAuditLogger();
        this.metricsLogger = EELFManager.getInstance().getMetricsLogger();
        this.setDefaultLogCatalog(cat);
    }



    /**
     * Get the MsoLogger based on the catalog
     * This method is fixed now to resolve the total number of objects that are getting created
     * everytime this function gets called. Its supposed to have fixed number of instance per java process.
     *
     * @param cat
     *            Catalog of the logger
     * @return the MsoLogger
     */
    public static synchronized MsoLogger getMsoLogger(MsoLogger.Catalog cat) {
        switch (cat) {
            case GENERAL:
                return generalMsoLogger;
            case APIH:
                return apihLogger;
            case RA:
                return raLogger;
            case BPEL:
                return bpelLogger;
            case ASDC:
                return asdcLogger;
            default:
                return generalMsoLogger;
        }
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

    // Info methods
    /**
     * Record the Info event
     *
     * @param msg
     *            The log message to put
     */
    public void info(EELFResolvableErrorEnum msg, String targetEntity, String targetServiceName) {
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
    public void info(EELFResolvableErrorEnum msg, String arg0, String targetEntity, String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg, normalize(arg0));
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
    public void info(EELFResolvableErrorEnum msg, String arg0, String arg1, String targetEntity,
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
    public void info(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName) {
        prepareErrorMsg(INFO_LEVEL, targetEntity, targetServiceName, null, "");

        logger.info(msg, normalize(arg0), normalize(arg1), normalize(arg2));
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
    public void info(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3,
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
    public void info(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3, String arg4,
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
    public void info(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3, String arg4,
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
    public void warn(EELFResolvableErrorEnum msg, String targetEntity, String targetServiceName, ErrorCode errorCode,
            String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);

        logger.warn(msg);
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
    public void warn(EELFResolvableErrorEnum msg, String targetEntity, String targetServiceName, ErrorCode errorCode,
            String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg);
        logger.warn("Exception raised: " + getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
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
    public void warn(EELFResolvableErrorEnum msg, String arg, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, arg);
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
    public void warn(EELFResolvableErrorEnum msg, String arg, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, arg);
        logger.warn("Exception raised: " + getNormalizedStackTrace(t));
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
    public void warn(EELFResolvableErrorEnum msg, String arg0, String arg1, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1));
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
    public void warn(EELFResolvableErrorEnum msg, String arg0, String arg1, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1));
        logger.warn("Exception raised: " + getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
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
    public void warn(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String targetEntity,
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
    public void warn(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2));
        logger.warn("Exception raised: " + getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
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
    public void warn(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3,
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
    public void warn(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3));
        logger.warn("Exception raised: " + getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
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
    public void warn(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3, String arg4,
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
    public void warn(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(WARN_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.warn(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4));
        logger.warn("Exception raised: " + getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    // Error methods
    /**
     * Record the Error event
     *
     * @param msg
     *            The log message to put
     */
    public void error(EELFResolvableErrorEnum msg, String targetEntity, String targetServiceName, ErrorCode errorCode,
            String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg);
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
    public void error(EELFResolvableErrorEnum msg, String targetEntity, String targetServiceName, ErrorCode errorCode,
            String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg);
        logger.error(exceptionArg, getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0));
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String targetEntity, String targetServiceName,
            ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0));
        logger.error(exceptionArg, getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String arg1, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0), normalize(arg1));
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String arg1, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0), normalize(arg1));
        logger.error(exceptionArg, getNormalizedStackTrace(t));
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0), normalize(arg1), normalize(arg2));
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String targetEntity,
            String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0), normalize(arg1), normalize(arg2));
        logger.error(exceptionArg, getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3));
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3));
        logger.error(exceptionArg, getNormalizedStackTrace(t));
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4));
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
    public void error(EELFResolvableErrorEnum msg, String arg0, String arg1, String arg2, String arg3, String arg4,
            String targetEntity, String targetServiceName, ErrorCode errorCode, String errorDesc, Throwable t) {
        prepareErrorMsg(ERROR_LEVEL, targetEntity, targetServiceName, errorCode, errorDesc);
        logger.error(msg, normalize(arg0), normalize(arg1), normalize(arg2), normalize(arg3), normalize(arg4));
        logger.error(exceptionArg, getNormalizedStackTrace(t));
        logger.debug("Exception raised", t);
        MDC.remove(TARGETENTITY);
        MDC.remove(TARGETSERVICENAME);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
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

        if (svcId == null || svcId.isEmpty()) {
            MDC.put(SERVICE_INSTANCE_ID, DUMMY_VALUE);
        }

        if (timer != null) {
            MDC.put(TIMER, timer);
        } else {
            MDC.remove(TIMER);
        }

        MDC.put(SERVICE_NAME, getFinalServiceName(serviceNamep));
        MDC.put(ALERT_SEVERITY, getSeverityLevel(loggingLevel));
        MDC.put(INSTANCE_UUID, instanceUUID);
        MDC.put(SERVER_IP, serverIP);
        MDC.put(FQDN, serverName);
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
        MDC.put(ERRORCODE, String.valueOf(errorCode));
        MDC.put(ERRORDESC, errorDesc);
        MDC.put(TARGETENTITY, targetEntity);
        MDC.put(TARGETSERVICENAME, targetServiceName);
        MDC.put(SERVICE_NAME, getFinalServiceName(getServiceName()));
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
            String className = classArr[i].getClassName();
            if (!className.equals(this.getClass().getName())) {
                return classArr[i].getMethodName();
            }
        }
        return classArr[0].getMethodName();
    }

    // Based on the discussion with Adrian, instanceUUID is used to identifiy
    // the mso instance,
    // it is generated during mso instance initialization period
    // The same mso instnace will use the same instanceUUID value, even after
    // restart
    private static String getInstanceUUID() {
        // Avoid creation during build and tests
        if (System.getProperty("jboss.server.name") == null) {
            return "Test UUID as JBoss not found";
        }
        File configFile = new File(CONFIG_FILE);
        String uuid = "";
        BufferedReader in = null;
        BufferedWriter bw = null;
        try {
            // Verify whether instanceUUID file exist,
            // If yes, read the content; if not, generate the instanceUUID and
            // write to the file
            if (configFile.exists()) {
                // read the content of the file
                in = new BufferedReader(new FileReader(CONFIG_FILE));
                if ((uuid = in.readLine()) == null) {
                    // the file is empty, regenerate the file
                    uuid = UUID.randomUUID().toString();
                    FileWriter fw = new FileWriter(configFile.getAbsoluteFile());
                    bw = new BufferedWriter(fw);
                    bw.write(uuid);
                    bw.close();
                }
                in.close();
            } else {
                // file doesn't exist yet -> create the file and generate the
                // instanceUUID
                uuid = UUID.randomUUID().toString();
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                FileWriter fw = new FileWriter(configFile.getAbsoluteFile());
                bw = new BufferedWriter(fw);
                bw.write(uuid);
                bw.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error trying to read UUID file", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error trying to close UUID file", ex);
            }
        }
        return uuid;
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

    /**
     * Set the remoteIp and the basic HTTP Authentication user
     * 
     * @param remoteIpp
     *            The remote ip address
     * @param userp
     *            The basic http authencitation user
     */
    public static void setLoggerParameters(String remoteIpp, String userp) {
        if (null != remoteIpp) {
            MDC.put(REMOTE_HOST, remoteIpp);
        }
        if (null != userp) {
            MDC.put(USER, userp);
        }
    }

    /**
     * Set the serviceName
     * 
     * @param serviceNamep
     *            The service name
     */
    public static void setServiceName(String serviceNamep) {
        if (null != serviceNamep) {
            MDC.put(SERVICE_NAME, serviceNamep);
            MDC.remove(SERVICE_NAME_IS_METHOD_NAME);
        }
    }

    /**
     * Get the serviceName
     * 
     * @return The service name
     */
    public static String getServiceName() {
        return MDC.get(SERVICE_NAME);
    }

    /**
     * Reset the serviceName
     */
    public static void resetServiceName() {
        MDC.remove(SERVICE_NAME);
    }

    /**
     * Set the requestId and serviceInstanceId based on the mso request
     * 
     * @param msoRequest
     *            The mso request
     */
    public static void setLogContext(MsoRequest msoRequest) {
        if (msoRequest != null) {
            MDC.put(REQUEST_ID, msoRequest.getRequestId());
            MDC.put(SERVICE_INSTANCE_ID, msoRequest.getServiceInstanceId());
        } else {
            MDC.put(REQUEST_ID, DUMMY_VALUE);
            MDC.put(SERVICE_INSTANCE_ID, DUMMY_VALUE);
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

    private String getNormalizedStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString().replace('|', '!').replace("\n", " - ");
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
