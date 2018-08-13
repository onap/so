package org.onap.so.db.catalog.data.repository;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.springframework.beans.factory.annotation.Autowired;

public class CloudIdentityRepositoryTest extends BaseTest {
    
    @Autowired
    private CloudIdentityRepository cloudIdentityRepository;
    
    @Test
    public void findOneTest() throws Exception {
        CloudIdentity cloudIdentity = cloudIdentityRepository.findOne("mtn13");
        Assert.assertNotNull(cloudIdentity);
        Assert.assertEquals("mtn13",cloudIdentity.getId());
    }

}