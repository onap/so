package org.onap.so.heatbridge.utils;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import org.junit.Test;

public class HeatBridgeUtilsTest {

    @Test(expected = IllegalStateException.class)
    public void matchServerName_canNotBeNull() {
        HeatBridgeUtils.getMatchingPserverPifName(null);
    }

    @Test
    public void matchServerName_isDedicated() {
        Optional<String> serverName = HeatBridgeUtils.getMatchingPserverPifName("dedicated-testServer");
        assertThat(serverName).isNotEmpty().hasValue("sriov-d-testServer");
    }

    @Test
    public void matchServerName_isShared() {
        Optional<String> serverName = HeatBridgeUtils.getMatchingPserverPifName("shared-testServer");
        assertThat(serverName).isNotEmpty().hasValue("sriov-s-testServer");
    }

    @Test
    public void matchServerName_unknown() {
        Optional<String> serverName = HeatBridgeUtils.getMatchingPserverPifName("differentServerName");
        assertThat(serverName).isNotEmpty().hasValue("differentServerName");
    }
}
