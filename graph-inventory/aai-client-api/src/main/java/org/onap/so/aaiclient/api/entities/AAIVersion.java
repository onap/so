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

package org.onap.so.aaiclient.api.entities;

public enum AAIVersion {
    V10("V10"),
    V11("V11"),
    V12("V12"),
    V13("v13"),
    V14("v14"),
    V15("v15"),
    V16("v16"),
    V17("v17"),
    V18("v18"),
    V19("v19"),
    V20("v20"),
    V21("v21"),
    V22("v22"),
    V23("v23"),
    V24("v24");

    public static final AAIVersion LATEST = AAIVersion.values()[AAIVersion.values().length - 1];
    private final String value;

    private AAIVersion(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
