package org.onap.so.openstack.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;

@RunWith(MockitoJUnitRunner.class)
public class MsoTenantUtilsFactoryTest {

    @Mock
    private CloudConfig cloudConfig;
    @InjectMocks
    private MsoTenantUtilsFactory msoTenantUtilsFactory;

    @Test
    public void getTenantUtils_shouldThrowException_whenNoCloudSiteFoundForGivenId() {
        // GIVEN
        String cloudSiteId = "CloudSiteId";
        given(cloudConfig.getCloudSite(cloudSiteId)).willReturn(Optional.empty());

        // WHEN
        MsoCloudSiteNotFound msoCloudSiteNotFound = catchThrowableOfType(
            () -> msoTenantUtilsFactory.getTenantUtils(cloudSiteId), MsoCloudSiteNotFound.class);

        // THEN
        assertThat(msoCloudSiteNotFound.getMessage()).contains(cloudSiteId);
    }

}