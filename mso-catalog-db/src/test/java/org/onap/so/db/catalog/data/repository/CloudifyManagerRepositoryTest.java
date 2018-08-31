package org.onap.so.db.catalog.data.repository;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.springframework.beans.factory.annotation.Autowired;

public class CloudifyManagerRepositoryTest extends BaseTest {

    @Autowired
    private CloudifyManagerRepository cloudifyManagerRepository;

    @Test
    public void findOneTest() throws Exception {
        CloudifyManager cloudifyManager = cloudifyManagerRepository.findOne("mtn13");
        Assert.assertNotNull(cloudifyManager);
        Assert.assertEquals("mtn13", cloudifyManager.getId());
    }

}