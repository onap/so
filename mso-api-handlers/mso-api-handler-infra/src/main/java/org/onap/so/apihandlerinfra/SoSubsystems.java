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

public enum SoSubsystems implements Subsystem {
    APIH("API Handler"),
    ASDC("ASDC Controller"),
    BPMN("BPMN Infra"),
    CATALOGDB("CatalogDb Adapter"),
    OPENSTACK("Openstack Adapter"),
    REQUESTDB("RequestDB Adapter"),
    SDNC("SDNC Adapter"),
    SOAPPCORCHESTRATOR("SO APPC Orchestrator");

    private String subsystem;

    private SoSubsystems(String subsystem) {
        this.subsystem = subsystem;
    }

    @Override
    public String toString() {
        return subsystem;
    }

}

