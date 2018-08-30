package org.onap.so.db.catalog.data.repository;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.CloudSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class CloudSiteRepositoryTest extends BaseTest {
    
    @Autowired
    private CloudSiteRepository cloudSiteRepository;
    
    @Test
    public void findByClliAndAicVersionTest() throws Exception {
        CloudSite cloudSite = cloudSiteRepository.findByClliAndCloudVersion("MDT13","2.5");
        Assert.assertNotNull(cloudSite);
        Assert.assertEquals("mtn13",cloudSite.getId());
    }

    @Test
    public void findOneTest() throws Exception {
        CloudSite cloudSite = cloudSiteRepository.findOne("mtn13");
        Assert.assertNotNull(cloudSite);
        Assert.assertEquals("mtn13",cloudSite.getId());
    }

    @Test
    public void findAllTest() throws Exception {
        List<CloudSite> cloudSiteList = cloudSiteRepository.findAll();
        Assert.assertFalse(CollectionUtils.isEmpty(cloudSiteList));
    }

}