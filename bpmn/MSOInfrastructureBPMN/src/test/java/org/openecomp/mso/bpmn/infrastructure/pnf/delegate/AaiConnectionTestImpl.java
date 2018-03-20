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

package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.AaiConnection;

public class AaiConnectionTestImpl implements AaiConnection {

    public static final String CORRECT_ID = "correctId";
    public static final String INCORRECT_ID = "incorrectId";
    public static final String CORRECT_ID_NO_IP = "correctIdNoIp";
    public static final String IP = "1.2.3.4";

    @Override
    public boolean containsEntryFor(String correlationId) {
        return correlationId.equals(CORRECT_ID) || correlationId.equals(CORRECT_ID_NO_IP);
    }

    @Override
    public String getIpFor(String correlationId) {
        if (correlationId.equals(CORRECT_ID)) {
            return IP;
        } else {
            return null;
        }
    }
}
