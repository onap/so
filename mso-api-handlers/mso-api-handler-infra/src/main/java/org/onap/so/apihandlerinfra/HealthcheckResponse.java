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
package org.onap.so.apihandlerinfra;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class HealthcheckResponse {
    private String apih;
    private String bpmn;
    private String sdncAdapter;
    private String asdcController;
    private String catalogdbAdapter;
    private String requestdbAdapter;
    private String openstackAdapter;
    private String requestdbAdapterAttsvc;
    private String message = "";

    public String getApih() {
        return apih;
    }

    public void setApih(String apih) {
        this.apih = apih;
    }

    public String getBpmn() {
        return bpmn;
    }

    public void setBpmn(String bpmn) {
        this.bpmn = bpmn;
    }

    public String getSdncAdapter() {
        return sdncAdapter;
    }

    public void setSdncAdapter(String sdncAdapter) {
        this.sdncAdapter = sdncAdapter;
    }

    public String getAsdcController() {
        return asdcController;
    }

    public void setAsdcController(String asdcController) {
        this.asdcController = asdcController;
    }

    public String getCatalogdbAdapter() {
        return catalogdbAdapter;
    }

    public void setCatalogdbAdapter(String catalogdbAdapter) {
        this.catalogdbAdapter = catalogdbAdapter;
    }

    public String getRequestdbAdapter() {
        return requestdbAdapter;
    }

    public void setRequestdbAdapter(String requestdbAdapter) {
        this.requestdbAdapter = requestdbAdapter;
    }

    public String getOpenstackAdapter() {
        return openstackAdapter;
    }

    public void setOpenstackAdapter(String openstackAdapter) {
        this.openstackAdapter = openstackAdapter;
    }

    public String getRequestdbAdapterAttsvc() {
        return requestdbAdapterAttsvc;
    }

    public void setRequestdbAdapterAttsvc(String requestdbAdapterAttsvc) {
        this.requestdbAdapterAttsvc = requestdbAdapterAttsvc;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("apih", this.apih).append("pbmn", this.bpmn)
                .append("sdncAdapter", this.sdncAdapter).append("asdcController", this.asdcController)
                .append("catalogdbAdapter", this.catalogdbAdapter).append("requestdbAdapter", this.requestdbAdapter)
                .append("openstackAdapter", this.openstackAdapter)
                .append("requestdbAdapterAttsvc", this.requestdbAdapterAttsvc).append("message", this.message)
                .toString();
    }
}
