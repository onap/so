/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.beans.nsmf.oof;

import lombok.Getter;
import org.onap.so.beans.nsmf.NetworkType;

@Getter
public enum SubnetType {
    AN("AN", NetworkType.ACCESS),

    AN_NF("AN_NF", NetworkType.ACCESS),

    CN("CN", NetworkType.CORE),

    TN_FH("TN_FH", NetworkType.TRANSPORT),

    TN_MH("TN_MH", NetworkType.TRANSPORT),

    TN_BH("TN_BH", NetworkType.TRANSPORT),;

    private NetworkType networkType;

    private String subnetType;

    SubnetType(String subnetType, NetworkType networkType) {
        this.subnetType = subnetType;
        this.networkType = networkType;
    }

    public static NetworkType getNetworkType(String subnetType) {
        for (SubnetType type : SubnetType.values()) {
            if (type.subnetType.equalsIgnoreCase(subnetType)) {
                return type.networkType;
            }
        }
        return null;
    }
}
