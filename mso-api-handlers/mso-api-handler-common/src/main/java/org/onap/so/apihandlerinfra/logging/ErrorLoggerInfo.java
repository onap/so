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

package org.onap.so.apihandlerinfra.logging;

import java.io.Serializable;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logging.filter.base.ErrorCode;



public class ErrorLoggerInfo implements Serializable {
    /**
    * 
    */
    private static final long serialVersionUID = -2917784544098682110L;
    private MessageEnum loggerMessageType;
    private String errorSource;
    private String targetEntity;
    private String targetServiceName;
    private ErrorCode errorCode;

    private ErrorLoggerInfo(MessageEnum loggerMessageType, String errorSource, String targetEntity,
            String targetServiceName, ErrorCode errorCode) {
        this.loggerMessageType = loggerMessageType;
        this.errorSource = errorSource;
        this.targetEntity = targetEntity;
        this.targetServiceName = targetServiceName;
        this.errorCode = errorCode;
    }

    public MessageEnum getLoggerMessageType() {
        return loggerMessageType;
    }

    public String getErrorSource() {
        return errorSource;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public static class Builder {
        private MessageEnum loggerMessageType;
        private String errorSource = "";
        private String targetEntity = "";
        private String targetServiceName = "";
        private ErrorCode errorCode;

        public Builder(MessageEnum loggerMessageType, ErrorCode errorCode) {
            this.loggerMessageType = loggerMessageType;
            this.errorCode = errorCode;
        }

        public Builder loggerMessageType(MessageEnum loggerMessageType) {
            this.loggerMessageType = loggerMessageType;
            return this;
        }

        public Builder errorSource(String errorSource) {
            this.errorSource = errorSource;
            return this;
        }

        public Builder targetEntity(String targetEntity) {
            this.targetEntity = targetEntity;
            return this;
        }

        public Builder targetServiceName(String targetServiceName) {
            this.targetServiceName = targetServiceName;
            return this;
        }

        public Builder errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorLoggerInfo build() {
            return new ErrorLoggerInfo(loggerMessageType, errorSource, targetEntity, targetServiceName, errorCode);
        }

    }
}
