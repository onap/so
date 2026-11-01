package org.onap.so.adapters.requestsdb.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.requestsdb.RequestDbOperationsService;
import org.onap.so.db.request.data.repository.SiteStatusRepository;

@RunWith(MockitoJUnitRunner.class)
public class RequestDbOperationsServiceTest {

    @Mock
    private SiteStatusRepository siteRepo;

    @InjectMocks
    private RequestDbOperationsService operations;

    @Test
    public void absentSiteRemainsEnabled() {
        when(siteRepo.findOneBySiteName("site-a")).thenReturn(null);

        assertThat(operations.getSiteStatus("site-a")).isTrue();
    }
}
