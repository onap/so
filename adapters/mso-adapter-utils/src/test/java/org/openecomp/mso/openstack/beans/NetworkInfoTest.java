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

package org.openecomp.mso.openstack.beans;

import static org.assertj.core.api.Assertions.assertThat;

import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Segment;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class NetworkInfoTest {

    private static final String NETWORK_STATUS_ACTIVE = "ACTIVE";
    private static final String NETWORK_STATUS_ID = "networkIdTest";
    private static final String NETWORK_STATUS_NAME = "networkNameTest";
    private static final String SUBNET_NAME = "subnetTest";
    private static final String PROVIDER = "providerTest";
    private static final String PROVIDER_NETWORK_TYPE_VLAN = "vlan";
    private static final String PROVIDER_NETWORK_TYPE_OTHER = "providerTypeTest";
    private static final Integer PROVIDER_SEGMENTATION_ID = 777;
    private static final String PROVIDER_FOR_SEGMENT = "providerSegmentTest";
    private static final Integer PROVIDER_SEGMENTATION_ID_FOR_SEGMENT = 123;

    @Test
    public void networkStatusUnknownWhenIsNullInNetwork() {
        NetworkInfo networkInfo = new NetworkInfo(prepareNetwork(null));
        assertThat(networkInfo.getStatus()).isEqualTo(NetworkStatus.UNKNOWN);
        checkCommonPartWhenProviderIsNotPresent(networkInfo);
    }

    @Test
    public void networkStatusUnknownWhenNotFoundInNetworkStatusMap() {
        NetworkInfo networkInfo = new NetworkInfo(prepareNetwork("notExistingNetworkStatus"));
        assertThat(networkInfo.getStatus()).isEqualTo(NetworkStatus.UNKNOWN);
        checkCommonPartWhenProviderIsNotPresent(networkInfo);
    }

    @Test
    public void setNetworkStatusWhenNetworkStatusFoundInNetworkStatusMap() {
        NetworkInfo networkInfo = new NetworkInfo(prepareNetwork(NETWORK_STATUS_ACTIVE));
        assertThat(networkInfo.getStatus()).isEqualTo(NetworkStatus.ACTIVE);
        checkCommonPartWhenProviderIsNotPresent(networkInfo);
    }

    @Test
    public void setVLANProviderFromTheNetwork() {
        NetworkInfo networkInfo = new NetworkInfo(prepareNetworkWithProvider(NETWORK_STATUS_ACTIVE, PROVIDER,
                PROVIDER_NETWORK_TYPE_VLAN));
        assertThat(networkInfo.getStatus()).isEqualTo(NetworkStatus.ACTIVE);
        assertThat(networkInfo.getProvider()).isEqualTo(PROVIDER);
        assertThat(networkInfo.getVlans()).hasSize(1).contains(PROVIDER_SEGMENTATION_ID);
        checkCommonPart(networkInfo);
    }

    @Test
    public void setOtherProviderFromTheNetwork() {
        NetworkInfo networkInfo = new NetworkInfo(prepareNetworkWithProvider(NETWORK_STATUS_ACTIVE, PROVIDER,
                PROVIDER_NETWORK_TYPE_OTHER));
        assertThat(networkInfo.getStatus()).isEqualTo(NetworkStatus.ACTIVE);
        assertThat(networkInfo.getProvider()).isEqualTo(PROVIDER);
        assertThat(networkInfo.getVlans()).isEmpty();
        checkCommonPart(networkInfo);
    }

    @Test
    public void setVLANProviderFromTheNetworkSegments() {
        NetworkInfo networkInfo = new NetworkInfo(prepareNetworkWithSegments(NETWORK_STATUS_ACTIVE,
                prepareSegment(PROVIDER_NETWORK_TYPE_VLAN)));
        assertThat(networkInfo.getStatus()).isEqualTo(NetworkStatus.ACTIVE);
        assertThat(networkInfo.getProvider()).isEqualTo(PROVIDER_FOR_SEGMENT);
        assertThat(networkInfo.getVlans()).hasSize(1).contains(PROVIDER_SEGMENTATION_ID_FOR_SEGMENT);
        checkCommonPart(networkInfo);
    }

    @Test
    public void setOtherProviderFromTheNetworkSegments() {
        NetworkInfo networkInfo = new NetworkInfo(prepareNetworkWithSegments(NETWORK_STATUS_ACTIVE,
                prepareSegment(PROVIDER_NETWORK_TYPE_OTHER)));
        assertThat(networkInfo.getStatus()).isEqualTo(NetworkStatus.ACTIVE);
        assertThat(networkInfo.getProvider()).isEqualTo(PROVIDER_FOR_SEGMENT);
        assertThat(networkInfo.getVlans()).isEmpty();
        checkCommonPart(networkInfo);
    }

    @Test
    public void setNetworkStatusNotFoundWhenNetworkIsNull() {
        NetworkInfo networkInfo = new NetworkInfo(null);
        assertThat(networkInfo.getStatus()).isEqualTo(NetworkStatus.NOTFOUND);
    }

    private void checkCommonPartWhenProviderIsNotPresent(NetworkInfo networkInfo) {
        assertThat(networkInfo.getProvider()).isEmpty();
        assertThat(networkInfo.getVlans()).isEmpty();
        checkCommonPart(networkInfo);
    }

    private void checkCommonPart(NetworkInfo networkInfo) {
        assertThat(networkInfo.getId()).isEqualTo(NETWORK_STATUS_ID);
        assertThat(networkInfo.getName()).isEqualTo(NETWORK_STATUS_NAME);
        assertThat(networkInfo.getSubnets()).hasSize(1).contains(SUBNET_NAME);
    }

    private Network prepareNetwork(String networkStatus) {
        Network network = new Network();
        network.setId(NETWORK_STATUS_ID);
        network.setName(NETWORK_STATUS_NAME);
        network.setStatus(networkStatus);
        List<String> subnets = new ArrayList<>();
        subnets.add(SUBNET_NAME);
        network.setSubnets(subnets);
        return network;
    }

    private Network prepareNetworkWithProvider(String networkStatus, String providerPhysicalNetwork, String providerNetworkType) {
        Network network = prepareNetwork(networkStatus);
        network.setProviderPhysicalNetwork(providerPhysicalNetwork);
        network.setProviderNetworkType(providerNetworkType);
        network.setProviderSegmentationId(PROVIDER_SEGMENTATION_ID);
        return network;
    }

    private Network prepareNetworkWithSegments(String networkStatus, Segment segment) {
        Network network = prepareNetwork(networkStatus);
        List<Segment> segments = new ArrayList<>();
        segments.add(segment);
        network.setSegments(segments);
        return network;
    }

    private Segment prepareSegment(String providerNetworkType) {
        Segment segment = new Segment();
        segment.setProviderPhysicalNetwork(PROVIDER_FOR_SEGMENT);
        segment.setProviderNetworkType(providerNetworkType);
        segment.setProviderSegmentationId(PROVIDER_SEGMENTATION_ID_FOR_SEGMENT);
        return segment;
    }

}
