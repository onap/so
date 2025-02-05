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

package org.onap.so.adapters.vnf.exceptions;



import jakarta.xml.ws.WebFault;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoExceptionCategory;

/**
 * This class simply extends Exception (without addition additional functionality) to provide an identifier for VNF
 * related exceptions on create, delete, query.
 * 
 *
 */
@WebFault(name = "VnfException", faultBean = "org.onap.so.adapters.vnf.exceptions.VnfExceptionBean",
        targetNamespace = "http://org.onap.so/vnf")
public class VnfException extends Exception {

    private static final long serialVersionUID = 1L;

    private VnfExceptionBean faultInfo;

    public VnfException(String msg) {
        super(msg);
        faultInfo = new VnfExceptionBean(msg);
    }

    public VnfException(Throwable e) {
        super(e);
        faultInfo = new VnfExceptionBean(e.getMessage());
    }

    public VnfException(String msg, Throwable e) {
        super(msg, e);
        faultInfo = new VnfExceptionBean(msg);
    }

    public VnfException(String msg, MsoExceptionCategory category) {
        super(msg);
        faultInfo = new VnfExceptionBean(msg, category);
    }

    public VnfException(String msg, MsoExceptionCategory category, Throwable e) {
        super(msg, e);
        faultInfo = new VnfExceptionBean(msg, category);
    }

    public VnfException(MsoException e) {
        super(e);
        faultInfo = new VnfExceptionBean(e.getContextMessage(), e.getCategory());
    }

    public VnfExceptionBean getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(VnfExceptionBean faultInfo) {
        this.faultInfo = faultInfo;
    }
}
