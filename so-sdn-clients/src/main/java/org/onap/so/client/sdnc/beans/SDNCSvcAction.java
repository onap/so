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

package org.onap.so.client.sdnc.beans;

import org.onap.sdnc.northbound.client.model.GenericResourceApiSvcActionEnumeration;

public enum SDNCSvcAction {
    ACTIVATE("activate", GenericResourceApiSvcActionEnumeration.ACTIVATE),
    DELETE("delete", GenericResourceApiSvcActionEnumeration.DELETE),
    ASSIGN("assign", GenericResourceApiSvcActionEnumeration.ASSIGN),
    ROLLBACK("rollback", GenericResourceApiSvcActionEnumeration.ROLLBACK),
    UNASSIGN("unassign", GenericResourceApiSvcActionEnumeration.UNASSIGN),
    DEACTIVATE("deactivate", GenericResourceApiSvcActionEnumeration.DEACTIVATE),
    CHANGE_DELETE("changedelete", GenericResourceApiSvcActionEnumeration.CHANGEDELETE),
    CHANGE_ASSIGN("changeassign", GenericResourceApiSvcActionEnumeration.CHANGEASSIGN),
    CREATE("create", GenericResourceApiSvcActionEnumeration.CREATE),
    ENABLE("enable", GenericResourceApiSvcActionEnumeration.ENABLE),
    DISABLE("disable", GenericResourceApiSvcActionEnumeration.DISABLE);

    private final String name;

    private GenericResourceApiSvcActionEnumeration sdncApiAction;

    private SDNCSvcAction(String name, GenericResourceApiSvcActionEnumeration sdncApiAction) {
        this.name = name;
        this.sdncApiAction = sdncApiAction;
    }

    public GenericResourceApiSvcActionEnumeration getSdncApiAction() {
        return this.sdncApiAction;
    }

    @Override
    public String toString() {
        return name;
    }
}
