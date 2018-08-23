package org.onap.so.db.catalog.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.catalogdb.CatalogDBApplication;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CatalogDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CatalogDbClientTest {
    public static final String MTN13 = "mtn13";
    @LocalServerPort
    private int port;
    @Autowired
    CatalogDbClient client;

    @Before
    public void setPort() {
        client.removePortFromEndpoint();
        client.setPortToEndpoint(Integer.toString(port));
    }

    @Test
    public void testGetCloudSiteHappyPath() throws Exception {
        CloudSite cloudSite = client.getCloudSite(MTN13);
        Assert.assertNotNull(cloudSite);
        Assert.assertNotNull(cloudSite.getIdentityService());
        Assert.assertEquals("MDT13", cloudSite.getClli());
        Assert.assertEquals("mtn13", cloudSite.getRegionId());
        Assert.assertEquals("MTN13", cloudSite.getIdentityServiceId());
    }

    @Test
    public void testGetCloudSiteNotFound() throws Exception {
        CloudSite cloudSite = client.getCloudSite(UUID.randomUUID().toString());
        Assert.assertNull(cloudSite);
    }

    @Test
    public void testGetCloudifyManagerHappyPath() throws Exception {
        CloudifyManager cloudifyManager = client.getCloudifyManager("mtn13");
        Assert.assertNotNull(cloudifyManager);
        Assert.assertEquals("http://localhost:28090/v2.0", cloudifyManager.getCloudifyUrl());

    }

    @Test
    public void testGetCloudifyManagerNotFound() throws Exception {
        CloudifyManager cloudifyManager = client.getCloudifyManager(UUID.randomUUID().toString());
        Assert.assertNull(cloudifyManager);
    }



    @Test
    public void testGetCloudSiteByClliAndAicVersionHappyPath() throws Exception{
        CloudSite cloudSite = client.getCloudSiteByClliAndAicVersion("MDT13","2.5");
        Assert.assertNotNull(cloudSite);
    }

    @Test
    public void testGetCloudSiteByClliAndAicVersionNotFound() throws Exception{
        CloudSite cloudSite = client.getCloudSiteByClliAndAicVersion("MDT13","232496239746328");
        Assert.assertNull(cloudSite);
    }
}
