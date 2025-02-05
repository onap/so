/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.cxf.interceptor;



import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

public class MDCSetup {

    protected static Logger logger = LoggerFactory.getLogger(MDCSetup.class);
    private static final String INSTANCE_UUID = UUID.randomUUID().toString();
    protected static final String serverIpAddressOverride = "SERVER_IP_ADDRESS_OVERRIDE";
    protected static final String serverFqdnOverride = "SERVER_FQDN_OVERRIDE";
    protected static final String INSTANT_PRECISION_OVERRIDE = "INSTANT_PRECISION_OVERRIDE";
    protected static final String checkHeaderLogPattern = "Checking {} header to determine the value of {}";
    protected String serverFqdn;
    protected String serverIpAddress;
    protected String[] prioritizedIdHeadersNames;
    protected String[] prioritizedPartnerHeadersNames;
    protected DateTimeFormatter iso8601Formatter;

    public MDCSetup() {
        this.prioritizedIdHeadersNames =
                new String[] {ONAPLogConstants.Headers.REQUEST_ID, Constants.HttpHeaders.HEADER_REQUEST_ID,
                        Constants.HttpHeaders.TRANSACTION_ID, Constants.HttpHeaders.ECOMP_REQUEST_ID};
        this.prioritizedPartnerHeadersNames =
                new String[] {HttpHeaders.AUTHORIZATION, ONAPLogConstants.Headers.PARTNER_NAME, HttpHeaders.USER_AGENT};
        initServerFqdnandIp();
        this.iso8601Formatter = createFormatter();
    }

    protected String getCurrentTimeStamp() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(iso8601Formatter);
    }

    protected DateTimeFormatter createFormatter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            Integer instantPrecision = Integer.valueOf(System.getProperty(INSTANT_PRECISION_OVERRIDE, "3"));
            builder.appendInstant(instantPrecision);
        } catch (NumberFormatException nfe) {
            logger.warn("instant precision could not be read and thus won't be set, the default will be used instead."
                    + nfe.getMessage());
        }
        return builder.toFormatter();
    }

    public void setInstanceID() {
        MDC.put(ONAPLogConstants.MDCs.INSTANCE_UUID, INSTANCE_UUID);
    }

    protected void initServerFqdnandIp() {
        serverFqdn = getProperty(serverFqdnOverride);
        serverIpAddress = getProperty(serverIpAddressOverride);

        if (serverIpAddress.equals(Constants.DefaultValues.UNKNOWN)
                || serverFqdn.equals(Constants.DefaultValues.UNKNOWN)) {
            try {
                InetAddress addr = InetAddress.getLocalHost();
                if (serverFqdn.equals(Constants.DefaultValues.UNKNOWN)) {
                    serverFqdn = addr.getCanonicalHostName();
                }
                if (serverIpAddress.equals(Constants.DefaultValues.UNKNOWN)) {
                    serverIpAddress = addr.getHostAddress();
                }
            } catch (UnknownHostException e) {
                logger.trace("Cannot Resolve Host Name." + e.getMessage());
            }
        }
    }

    public void setServerFQDN() {
        MDC.put(ONAPLogConstants.MDCs.SERVER_FQDN, serverFqdn);
        MDC.put(ONAPLogConstants.MDCs.SERVER_IP_ADDRESS, serverIpAddress);
    }

    public void setClientIPAddress(HttpServletRequest httpServletRequest) {
        String clientIpAddress = "";
        if (httpServletRequest != null) {
            // This logic is to avoid setting the client ip address to that of the load
            // balancer in front of the application
            String getForwadedFor = httpServletRequest.getHeader("X-Forwarded-For");
            if (getForwadedFor != null) {
                clientIpAddress = getForwadedFor;
            } else {
                clientIpAddress = httpServletRequest.getRemoteAddr();
            }
        }
        MDC.put(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS, clientIpAddress);
    }

    public void setEntryTimeStamp() {
        MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP, getCurrentTimeStamp());
    }

    public String getRequestId(SimpleMap headers) {
        String requestId = null;
        for (String headerName : this.prioritizedIdHeadersNames) {
            logger.trace(checkHeaderLogPattern, headerName, ONAPLogConstants.Headers.REQUEST_ID);
            requestId = headers.get(headerName);
            if (requestId != null && !requestId.isEmpty()) {
                return requestId;
            }
        }
        logger.trace("No valid requestId headers. Generating requestId: {}", requestId);
        return UUID.randomUUID().toString();
    }

    public void setInvocationId(SimpleMap headers) {
        String invocationId = headers.get(ONAPLogConstants.Headers.INVOCATION_ID);
        if (invocationId == null || invocationId.isEmpty())
            invocationId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.SERVER_INVOCATION_ID, invocationId);
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

    public void setMDCPartnerName(SimpleMap headers) {
        logger.info("In setMDCPartnerName method headers : {}", headers);
        String partnerName = getMDCPartnerName(headers);
        logger.info("In setMDCPartnerName method partnerName : {}", partnerName);
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
    }

    protected String getMDCPartnerName(SimpleMap headers) {
        logger.info("In getMDCPartnerName method headers : {}", headers);
        String partnerName = null;
        for (String headerName : prioritizedPartnerHeadersNames) {
            logger.info("In getMDCPartnerName headerName : {}", headerName);
            logger.trace(checkHeaderLogPattern, headerName, ONAPLogConstants.MDCs.PARTNER_NAME);
            if (headerName.equals(HttpHeaders.AUTHORIZATION)) {
                logger.info("In getMDCPartnerName before partnerName : {}", partnerName);
                partnerName = getBasicAuthUserName(headers);
                logger.info("In getMDCPartnerName after partnerName : {}", partnerName);
            } else {
                partnerName = headers.get(headerName);
            }
            if (partnerName != null && !partnerName.isEmpty()) {
                return partnerName;
            }

        }
        logger.trace("{} value could not be determined, defaulting partnerName to {}.",
                ONAPLogConstants.MDCs.PARTNER_NAME, Constants.DefaultValues.UNKNOWN);
        return Constants.DefaultValues.UNKNOWN;
    }

    public void setLogTimestamp() {
        logger.info("In setLogTimestamp method");
        MDC.put(ONAPLogConstants.MDCs.LOG_TIMESTAMP, getCurrentTimeStamp());
    }

    public void setElapsedTime() {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            ZonedDateTime entryTimestamp =
                    ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP), timeFormatter);
            ZonedDateTime endTimestamp =
                    ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.LOG_TIMESTAMP), timeFormatter);

            MDC.put(ONAPLogConstants.MDCs.ELAPSED_TIME,
                    Long.toString(ChronoUnit.MILLIS.between(entryTimestamp, endTimestamp)));
        } catch (Exception e) {
            logger.trace("Unable to calculate elapsed time due to error: {}", e.getMessage());
        }
    }

    public void setElapsedTimeInvokeTimestamp() {
        logger.info("In setElapsedTimeInvokeTimestamp method");
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            ZonedDateTime entryTimestamp =
                    ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP), timeFormatter);
            ZonedDateTime endTimestamp =
                    ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.LOG_TIMESTAMP), timeFormatter);

            MDC.put(ONAPLogConstants.MDCs.ELAPSED_TIME,
                    Long.toString(ChronoUnit.MILLIS.between(entryTimestamp, endTimestamp)));
        } catch (Exception e) {
            logger.trace("Unable to calculate elapsed time due to error: {}", e.getMessage());
        }
    }

    public void setResponseStatusCode(int code) {
        logger.info("In setResponseStatusCode method code : {}", code);
        String statusCode;
        if (Response.Status.Family.familyOf(code).equals(Response.Status.Family.SUCCESSFUL)) {
            statusCode = ONAPLogConstants.ResponseStatus.COMPLETE.toString();
            logger.info("In setResponseStatusCode if block statusCode : {}", statusCode);
        } else {
            statusCode = ONAPLogConstants.ResponseStatus.ERROR.toString();
            logger.info("In setResponseStatusCode else block statusCode : {}", statusCode);
            setErrorCode(code);
            setErrorDescription(code);
        }
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
    }

    public void setTargetEntity(ONAPComponentsList targetEntity) {
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity.toString());
    }

    public void clearClientMDCs() {
        MDC.remove(ONAPLogConstants.MDCs.CLIENT_INVOCATION_ID);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_CODE);
        MDC.remove(ONAPLogConstants.MDCs.TARGET_ENTITY);
        MDC.remove(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME);
        MDC.remove(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP);
        MDC.remove(ONAPLogConstants.MDCs.ERROR_CODE);
        MDC.remove(ONAPLogConstants.MDCs.ERROR_DESC);
    }

    public void setResponseDescription(int statusCode) {
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION, extractDescription(statusCode));
    }

    private String extractDescription(int statusCode) {
        Response.Status responseStatus = Response.Status.fromStatusCode(statusCode);
        if (responseStatus != null) {
            return responseStatus.toString();
        }
        CustomResponseStatus customResponseStatus = CustomResponseStatus.fromStatusCode(statusCode);
        if (customResponseStatus != null) {
            return customResponseStatus.toString();
        }
        return String.format("Unknown description for response code %d.", statusCode);
    }

    public void setErrorCode(int statusCode) {
        MDC.put(ONAPLogConstants.MDCs.ERROR_CODE, String.valueOf(statusCode));
    }

    public void setErrorDescription(int statusCode) {
        MDC.put(ONAPLogConstants.MDCs.ERROR_DESC, extractDescription(statusCode));
    }

    public String getProperty(String property) {
        String propertyValue = System.getProperty(property);
        if (propertyValue == null || propertyValue.isEmpty()) {
            propertyValue = System.getenv(property);
            if (propertyValue == null || propertyValue.isEmpty()) {
                propertyValue = Constants.DefaultValues.UNKNOWN;
            }
        }
        return propertyValue;
    }

    protected String getBasicAuthUserName(SimpleMap headers) {
        logger.info("In getBasicAuthUserName method headers : {}", headers);
        String encodedAuthorizationValue = headers.get(HttpHeaders.AUTHORIZATION);
        logger.info("In getBasicAuthUserName method encodedAuthorizationValue : {}", encodedAuthorizationValue);
        if (encodedAuthorizationValue != null && encodedAuthorizationValue.startsWith("Basic")) {
            logger.info("In getBasicAuthUserName method if condition");
            try {
                // This will strip the word Basic and single space
                encodedAuthorizationValue = encodedAuthorizationValue.substring(6);
                logger.info("In getBasicAuthUserName method if condition encodedAuthorizationValue: {}",
                        encodedAuthorizationValue);
                byte[] decodedBytes = Base64.getDecoder().decode(encodedAuthorizationValue);
                String decodedString = new String(decodedBytes);
                int idx = decodedString.indexOf(':');
                return decodedString.substring(0, idx);
            } catch (IllegalArgumentException e) {
                logger.error("could not decode basic auth value " + encodedAuthorizationValue, e);
            }
        }
        return null;
    }
}

