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

package org.onap.so.adapters.nwrest;



import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("deleteNetworkResponse")
@XmlRootElement(name = "deleteNetworkResponse")
public class DeleteNetworkResponse extends NetworkResponseCommon {

    /**
     * 
     */
    private static final long serialVersionUID = 68336086339501537L;
    private String networkId;
    private Boolean networkDeleted;

    public DeleteNetworkResponse() {
        super();
    }

    public DeleteNetworkResponse(String networkId, Boolean networkDeleted, String messageId) {
        super(messageId);
        this.networkId = networkId;
        this.networkDeleted = networkDeleted;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Boolean getNetworkDeleted() {
        return networkDeleted;
    }

    public void setNetworkDeleted(Boolean networkDeleted) {
        this.networkDeleted = networkDeleted;
    }
}
