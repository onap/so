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

package org.onap.so.apihandlerinfra.exceptions;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.onap.so.apihandlerinfra.logging.AlarmLoggerInfo;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, ApiExceptionMapper.class);
    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
    @Override
    public Response toResponse(ApiException exception) {

        return Response.status(exception.getHttpResponseCode()).entity(buildErrorString(exception)).build();
    }

    protected String buildErrorString(ApiException exception) {
        String errorText = exception.getMessage();
        String messageId = exception.getMessageID();
        List<String> variables = exception.getVariables();
        ErrorLoggerInfo errorLoggerInfo = exception.getErrorLoggerInfo();
        AlarmLoggerInfo alarmLoggerInfo = exception.getAlarmLoggerInfo();


        if (errorText.length() > 1999) {
            errorText = errorText.substring(0, 1999);
        }

        writeErrorLog(exception, errorText, errorLoggerInfo, alarmLoggerInfo);

        return buildServiceErrorResponse(errorText,messageId,variables);

    }

    protected String buildServiceErrorResponse(String errorText, String messageId, List<String> variables){
        RequestError re = new RequestError();
        ServiceException se = new ServiceException();
        se.setMessageId(messageId);
        se.setText(errorText);
        if (variables != null) {
            for (String variable : variables) {
                se.getVariables().add(variable);
            }
        }
        re.setServiceException(se);
        String requestErrorStr;

        ObjectMapper mapper = createObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        try {
            requestErrorStr = mapper.writeValueAsString(re);
        } catch (JsonProcessingException e) {
            String errorMsg = "Exception in buildServiceErrorResponse writing exceptionType to string " + e.getMessage();
            logger.error(MessageEnum.GENERAL_EXCEPTION, "BuildServiceErrorResponse", "", "", MsoLogger.ErrorCode.DataError, errorMsg, e);
            return errorMsg;
        }

        return requestErrorStr;
    }

    protected void writeErrorLog(Exception e, String errorText, ErrorLoggerInfo errorLogInfo, AlarmLoggerInfo alarmLogInfo) {
        if( e!= null)
            logger.error(e);
        if(errorLogInfo != null)
            logger.error(errorLogInfo.getLoggerMessageType().toString(), errorLogInfo.getErrorSource(), errorLogInfo.getTargetEntity(), errorLogInfo.getTargetServiceName(), errorLogInfo.getErrorCode(), errorText);
        if(alarmLogInfo != null){
            alarmLogger.sendAlarm(alarmLogInfo.getAlarm(),alarmLogInfo.getState(),alarmLogInfo.getDetail());
        }
    }

    public ObjectMapper createObjectMapper(){
        return new ObjectMapper();
    }
}
