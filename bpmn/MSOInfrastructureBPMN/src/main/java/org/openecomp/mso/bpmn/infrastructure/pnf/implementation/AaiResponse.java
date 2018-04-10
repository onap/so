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

package org.openecomp.mso.bpmn.infrastructure.pnf.implementation;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public enum AaiResponse {
    NO_ENTRY(false, false),
    ENTRY_NO_IP(true, false),
    ENTRY_WITH_IP(true, true);

    private boolean containsInfoAboutPnf;
    private boolean containsInfoAboutIp;

    AaiResponse(boolean containsInfoAboutPnf, boolean containsInfoAboutIp) {
        this.containsInfoAboutPnf = containsInfoAboutPnf;
        this.containsInfoAboutIp = containsInfoAboutIp;
    }

    public boolean getContainsInfoAboutPnf() {
        return containsInfoAboutPnf;
    }

    public boolean getContainsInfoAboutIp() {
        return containsInfoAboutIp;
    }

}
