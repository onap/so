package org.onap.so.openstack.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.ServerType;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;

@RunWith(MockitoJUnitRunner.class)
public class MsoTenantUtilsFactoryTest {

    @Mock
    private CloudConfig cloudConfig;
    @Mock
    private MsoKeystoneUtils msoKeystoneUtils;
    @Mock
    private MsoKeystoneV3Utils msoKeystoneV3Utils;
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

    @Test
    public void getTenantUtils_shouldReturnNull_forInvalidServerType() throws MsoCloudSiteNotFound {
        // GIVEN
        String cloudSiteId = "CloudSiteId";
        CloudSite cloudSite = mock(CloudSite.class, RETURNS_DEEP_STUBS);
        given(cloudConfig.getCloudSite(cloudSiteId)).willReturn(Optional.of(cloudSite));

        // WHEN
        MsoTenantUtils tenantUtils = msoTenantUtilsFactory.getTenantUtils(cloudSiteId);

        // THEN
        assertThat(tenantUtils).isNull();
    }

    @Test
    public void getTenantUtils_shouldReturnKeystoneUtils_forKeystoneServerType() throws MsoCloudSiteNotFound {
        assertCorrectUtilsInstanceForServerType(ServerType.KEYSTONE, msoKeystoneUtils);
    }

    @Test
    public void getTenantUtils_shouldReturnKeystoneV3Utils_forKeystoneV3ServerType() throws MsoCloudSiteNotFound {
        assertCorrectUtilsInstanceForServerType(ServerType.KEYSTONE_V3, msoKeystoneV3Utils);
    }

    private <T extends MsoTenantUtils> void assertCorrectUtilsInstanceForServerType(ServerType serverType,
        T expectedInstance)
        throws MsoCloudSiteNotFound {
        // GIVEN
        String cloudSiteId = "CloudSiteId";
        CloudSite cloudSite = mock(CloudSite.class, RETURNS_DEEP_STUBS);
        given(cloudSite.getIdentityService().getIdentityServerType()).willReturn(serverType);
        given(cloudConfig.getCloudSite(cloudSiteId)).willReturn(Optional.of(cloudSite));

        // WHEN
        MsoTenantUtils tenantUtils = msoTenantUtilsFactory.getTenantUtils(cloudSiteId);

        // THEN
        assertThat(tenantUtils).isEqualTo(expectedInstance);
    }
}
