/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.asdc.etsi.pkg.processor;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public enum JobStatus {

    STARTED("started"),
    TIMEOUT("timeout"),
    FINISHED("finished"),
    PARTLY_FINISHED("partly_finished"),
    PROCESSING("processing"),
    ERROR("error"),
    UNKNOWN("unknown");

    private String value;

    private JobStatus(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static JobStatus getJobStatus(final String jobStatus) {
        for (final JobStatus status : JobStatus.values()) {
            if (status.getValue().equalsIgnoreCase(jobStatus)) {
                return status;
            }
        }
        return JobStatus.UNKNOWN;
    }

}
