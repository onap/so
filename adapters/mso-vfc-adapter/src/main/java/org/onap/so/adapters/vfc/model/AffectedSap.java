/***
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

public class AffectedSap {
    private String sapInstanceId;
    private String sapdId;
    private String sapName;

    private enum changeType {
        ADD, REMOVE, MODIFY
    }
    private enum changeResult {
        COMPLETED, ROLLED_BACK, FAILED
    }

    public String getSapInstanceId() {
        return sapInstanceId;
    }

    public void setSapInstanceId(String sapInstanceId) {
        this.sapInstanceId = sapInstanceId;
    }

    public String getSapdId() {
        return sapdId;
    }

    public void setSapdId(String sapdId) {
        this.sapdId = sapdId;
    }

    public String getSapName() {
        return sapName;
    }

    public void setSapName(String sapName) {
        this.sapName = sapName;
    }
}
