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

package org.onap.so.adapters.requestsdb.exceptions;



import jakarta.xml.ws.WebFault;
import org.onap.so.exceptions.MSOException;
import org.onap.so.logging.filter.base.ErrorCode;

/**
 * This class simply extends Exception (without addition additional functionality) to provide an identifier for
 * RequestsDB related exceptions on create, delete, query.
 * 
 *
 */
@WebFault(name = "MsoRequestsDbException",
        faultBean = "org.onap.so.adapters.requestsdb.exceptions.MsoRequestsDbExceptionBean",
        targetNamespace = "http://org.onap.so/requestsdb")
public class MsoRequestsDbException extends MSOException {

    private static final long serialVersionUID = 1L;

    private MsoRequestsDbExceptionBean faultInfo;


    public MsoRequestsDbException(String msg) {
        super(msg);
        faultInfo = new MsoRequestsDbExceptionBean(msg);
    }

    public MsoRequestsDbException(Throwable e) {
        super(e);
        faultInfo = new MsoRequestsDbExceptionBean(e.getMessage());
    }

    public MsoRequestsDbException(String msg, Throwable e) {
        super(msg, e);
        faultInfo = new MsoRequestsDbExceptionBean(msg);
    }

    public MsoRequestsDbException(String msg, ErrorCode errorCode) {
        super(msg, errorCode.getValue());
    }

    public MsoRequestsDbException(String msg, ErrorCode errorCode, Throwable t) {
        super(msg, errorCode.getValue(), t);
    }

    public MsoRequestsDbExceptionBean getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(MsoRequestsDbExceptionBean faultInfo) {
        this.faultInfo = faultInfo;
    }
}
