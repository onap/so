/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai;

/**
 * Represents the source of the value to use as the AAI OAM IP address of a VNF
 */
public class OamIpAddressSource {

    private final OamIpAddressType type;
    private final String value;

    public OamIpAddressSource(final OamIpAddressType type, final String value) {
        this.type = type;
        this.value = value;
    }

    public OamIpAddressType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public enum OamIpAddressType {
        /**
         * The value passed in {@link OamIpAddress#OamIpAddress(OamIpAddressType, String)} is to be used directly as the
         * OAM IP address
         */
        LITERAL,
        /**
         * The OAM IP address is to be retrieved from the vnfConfigurableProperties returned from the VNFM using the
         * value passed in {@link OamIpAddress#OamIpAddress(OamIpAddressType, String)} as the name of a property
         */
        CONFIGURABLE_PROPERTY
    }

}
