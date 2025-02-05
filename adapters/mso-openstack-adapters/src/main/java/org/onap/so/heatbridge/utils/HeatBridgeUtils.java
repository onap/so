/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.heatbridge.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.annotation.Nonnull;
import org.apache.commons.collections.CollectionUtils;
import org.onap.aai.domain.yang.SriovVf;
import org.onap.aai.domain.yang.Vserver;

public final class HeatBridgeUtils {

    /**
     * IaaS naming convention for compute/p-interface to openstack/physical-network name mapping
     */
    private static final String OS_SIDE_SHARED_SRIOV_PREFIX = "shared-";
    private static final String OS_SIDE_DEDICATED_SRIOV_PREFIX = "dedicated-";
    private static final String COMPUTE_SIDE_SHARED_SRIOV_PREFIX = "sriov-s-";
    private static final String COMPUTE_SIDE_DEDICATED_SRIOV_PREFIX = "sriov-d-";

    private HeatBridgeUtils() {
        throw new IllegalStateException("Trying to instantiate a utility class.");
    }

    public static Optional<String> getMatchingPserverPifName(@Nonnull final String physicalNetworkName) {
        Preconditions.checkState(!Strings.isNullOrEmpty(physicalNetworkName),
                "Physical network name is null or empty!");
        if (physicalNetworkName.contains(OS_SIDE_DEDICATED_SRIOV_PREFIX)) {
            return Optional.of(
                    physicalNetworkName.replace(OS_SIDE_DEDICATED_SRIOV_PREFIX, COMPUTE_SIDE_DEDICATED_SRIOV_PREFIX));
        } else if (physicalNetworkName.contains(OS_SIDE_SHARED_SRIOV_PREFIX)) {
            return Optional
                    .of(physicalNetworkName.replace(OS_SIDE_SHARED_SRIOV_PREFIX, COMPUTE_SIDE_SHARED_SRIOV_PREFIX));
        }
        return Optional.of(physicalNetworkName);
    }

    public static List<String> extractPciIdsFromVServer(Vserver vserver) {
        if (vserver.getLInterfaces() == null) {
            return Collections.emptyList();
        }
        return vserver.getLInterfaces().getLInterface().stream()
                .filter(lInterface -> lInterface.getSriovVfs() != null
                        && CollectionUtils.isNotEmpty(lInterface.getSriovVfs().getSriovVf()))
                .flatMap(lInterface -> lInterface.getSriovVfs().getSriovVf().stream()).map(SriovVf::getPciId)
                .collect(Collectors.toList());
    }

}
