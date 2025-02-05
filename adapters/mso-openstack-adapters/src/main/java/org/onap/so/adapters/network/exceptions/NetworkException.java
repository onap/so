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

package org.onap.so.adapters.network.exceptions;



import jakarta.xml.ws.WebFault;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;

/**
 * This class simply extends Exception (without addition additional functionality) to provide an identifier for Network
 * related exceptions on create, delete, query.
 * 
 *
 */
@WebFault(name = "NetworkException", faultBean = "org.onap.so.adapters.network.exceptions.NetworkExceptionBean",
        targetNamespace = "http://org.onap.so/network")
public class NetworkException extends Exception {

    private static final long serialVersionUID = 1L;

    private NetworkExceptionBean faultInfo;

    public NetworkException(String msg) {
        super(msg);
        faultInfo = new NetworkExceptionBean(msg);
    }

    public NetworkException(Throwable e) {
        super(e);
        faultInfo = new NetworkExceptionBean(e.getMessage());
    }

    public NetworkException(String msg, Throwable e) {
        super(msg, e);
        faultInfo = new NetworkExceptionBean(msg);
    }

    public NetworkException(String msg, MsoExceptionCategory category) {
        super(msg);
        faultInfo = new NetworkExceptionBean(msg, category);
    }

    public NetworkException(String msg, MsoExceptionCategory category, Throwable e) {
        super(msg, e);
        faultInfo = new NetworkExceptionBean(msg, category);
    }

    public NetworkException(MsoException e) {
        super(e);
        faultInfo = new NetworkExceptionBean(e.getContextMessage(), e.getCategory());
    }

    public NetworkExceptionBean getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(NetworkExceptionBean faultInfo) {
        this.faultInfo = faultInfo;
    }
}
