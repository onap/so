/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.nssmf.enums;

import org.onap.so.adapters.nssmf.exceptions.ApplicationException;


public enum JobStatus {
    STARTED, PROCESSING, FINISHED, ERROR;

    public static JobStatus fromString(String s) throws ApplicationException {
        if (s == null)
            return null;
        if (("started").equalsIgnoreCase(s))
            return STARTED;
        if (("processing").equalsIgnoreCase(s))
            return PROCESSING;
        if (("finished").equalsIgnoreCase(s))
            return FINISHED;
        if (("error").equalsIgnoreCase(s))
            return ERROR;
        throw new ApplicationException(500, "Invalid value for Job " + "Status: " + s);
    }
}
