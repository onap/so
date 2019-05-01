/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Cloudify Intellectual Property. All rights reserved.
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

package org.onap.so.cloudify.v3.client;

import org.onap.so.cloudify.v3.client.ExecutionsResource;
import org.onap.so.cloudify.v3.model.Execution;
import org.onap.so.cloudify.v3.model.Executions;
import org.onap.so.cloudify.v3.model.Deployment;
import java.util.List;

public class Util {
    private static final int DEPWAIT_MAXTRIES = 60;
    private static final String ST_TERM = "terminated";
    private static final String ST_FAIL = "failed";
    private static final String ST_CANC = "cancelled";
    private static final String ST_PEND = "pending";
    private static final String ST_STAR = "started";
    private static final String ST_CANL = "cancelling";
    private static final String ST_FORC = "force_cancelling";
    private static final String ST_QUEU = "queued";
    private static final String ST_SCHE = "scheduled";

    public static void waitForDeploymentCreation(Cloudify client, Deployment deployment) {

        ExecutionsResource.ListExecutions le = client.executions().list();
        for (int i = 0; i < DEPWAIT_MAXTRIES; i++) {
            Executions exs = le.execute();
            List<Execution> el = exs.getItems();
            boolean found = false;
            for (int j = 0; j < el.size(); j++) {
                if (el.get(j).getDeploymentId().equals(deployment.getId())) {
                    found = true;
                    if (el.get(j).getStatus().equals(ST_TERM)) {
                        return; // nominal
                    } else if (el.get(j).getStatus().equals(ST_CANC)) {
                        throw new RuntimeException(
                                "deployment creation execution cancelled. id=" + el.get(j).getDeploymentId());
                    } else if (el.get(j).getStatus().equals(ST_FAIL)) {
                        throw new RuntimeException(
                                "deployment creation execution failed. id=" + el.get(j).getDeploymentId());
                    }
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("execution for deployment " + deployment.getId() + " not found");
            }
        }
    }

}
