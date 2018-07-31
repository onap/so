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

import org.onap.so.apihandlerinfra.logging.AlarmLoggerInfo;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;

public abstract class ApiException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = 683162058616691134L;
	private int httpResponseCode;
    private String messageID;
    private ErrorLoggerInfo errorLoggerInfo;
    private AlarmLoggerInfo alarmLoggerInfo;
    private List<String> variables;    

    public ApiException(Builder builder){
        super(builder.message, builder.cause);

        this.httpResponseCode = builder.httpResponseCode;
        this.messageID = builder.messageID;
        this.variables = builder.variables;
        this.errorLoggerInfo = builder.errorLoggerInfo;
        this.alarmLoggerInfo = builder.alarmLoggerInfo;
        this.variables = builder.variables;        
    }

    public ApiException(String message, Throwable cause) {
    	 super(message, cause);
	}

	public String getMessageID() {
        return messageID;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public ErrorLoggerInfo getErrorLoggerInfo() {
        return errorLoggerInfo;
    }

    public AlarmLoggerInfo getAlarmLoggerInfo() {
        return alarmLoggerInfo;
    }

    public List<String> getVariables() {
        return variables;
    }

    public static class Builder<T extends Builder<T>> {
        private String message;
        private Throwable cause = null;
        private int httpResponseCode;
        private String messageID;
        private ErrorLoggerInfo errorLoggerInfo = null;
        private AlarmLoggerInfo alarmLoggerInfo = null;
        private List<String> variables = null;
        
        public Builder(String message, int httpResponseCode, String messageID) {
            this.message = message;
            this.httpResponseCode = httpResponseCode;
            this.messageID = messageID;
        }

        public T message(String message) {
            this.message = message;
            return (T) this;
        }

        public T cause(Throwable cause) {
            this.cause = cause;
            return (T) this;
        }

        public T httpResponseCode(int httpResponseCode) {
            this.httpResponseCode = httpResponseCode;
            return (T) this;
        }

        public T messageID(String messageID) {
            this.messageID = messageID;
            return (T) this;
        }

        public T errorInfo(ErrorLoggerInfo errorLoggerInfo){
            this.errorLoggerInfo = errorLoggerInfo;
            return (T) this;
        }

        public T alarmInfo(AlarmLoggerInfo alarmLoggerInfo){
            this.alarmLoggerInfo = alarmLoggerInfo;
            return (T) this;
        }

        public T variables(List<String> variables) {
            this.variables = variables;
            return (T) this;
        }
    }
}
