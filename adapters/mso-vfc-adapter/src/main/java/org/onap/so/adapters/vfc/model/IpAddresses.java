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

import java.util.List;

public class IpAddresses {
    private String type;
    private List<String> fixedAddresses;
    private int numDynamicAddresses;
    private AddressRange addressRange;
    private String subnetId;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFixedAddresses() {
        return fixedAddresses;
    }

    public void setFixedAddresses(List<String> fixedAddresses) {
        this.fixedAddresses = fixedAddresses;
    }

    public int getNumDynamicAddresses() {
        return numDynamicAddresses;
    }

    public void setNumDynamicAddresses(int numDynamicAddresses) {
        this.numDynamicAddresses = numDynamicAddresses;
    }

    public AddressRange getAddressRange() {
        return addressRange;
    }

    public void setAddressRange(AddressRange addressRange) {
        this.addressRange = addressRange;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }
}
