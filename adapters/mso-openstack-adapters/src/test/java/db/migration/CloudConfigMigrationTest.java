/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package db.migration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CloudConfigMigrationTest extends BaseRestTestUtils {

    @Qualifier("dataSource")
    @Autowired
    DataSource dataSource;

    R__CloudConfigMigration cloudConfigMigration;

    @Before
    public void setup() {
        cloudConfigMigration = new R__CloudConfigMigration();
    }

    @Test
    public void testMigrate() throws Exception {
        System.setProperty("spring.profiles.active", "test");
        cloudConfigMigration.migrate(dataSource.getConnection());
        assertMigratedIdentityServiceData();
        assertMigratedCloudSiteData();
        assertMigratedCloudManagerData();
    }

    @Test
    public void testMigrateNoData() throws Exception {
        System.setProperty("spring.profiles.active", "nomigrate");
        int identityCount = getDataCount("identity_services");
        int cloudSiteCount = getDataCount("cloud_sites");
        int cloudManagerCount = getDataCount("cloudify_managers");

        cloudConfigMigration.migrate(dataSource.getConnection());

        Assert.assertEquals(identityCount, getDataCount("identity_services"));
        Assert.assertEquals(cloudSiteCount, getDataCount("cloud_sites"));
        Assert.assertEquals(cloudManagerCount, getDataCount("cloudify_managers"));
    }


    private int getDataCount(String tableName) throws Exception {
        try (Connection con = dataSource.getConnection(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("select count(1) from " + tableName)) {
            while (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private void assertMigratedIdentityServiceData() throws Exception {
        try (Connection con = dataSource.getConnection(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("select * from identity_services where id='MTKEYSTONE'")) {
            boolean dataAvailable = false;
            while (rs.next()) {
                dataAvailable = true;
                Assert.assertEquals("MTKEYSTONE", rs.getString("id"));
                Assert.assertEquals("http://localhost:5000/v2.0", rs.getString("identity_url"));
                Assert.assertEquals("john", rs.getString("mso_id"));
                Assert.assertEquals("313DECE408AF7759D442D7B06DD9A6AA", rs.getString("mso_pass"));
                Assert.assertEquals("admin", rs.getString("admin_tenant"));
                Assert.assertEquals("_member_", rs.getString("member_role"));
                Assert.assertEquals("KEYSTONE", rs.getString("identity_server_type"));
                Assert.assertEquals("USERNAME_PASSWORD", rs.getString("identity_authentication_type"));
            }
            Assert.assertTrue("Expected data in identity_services table post migration but didnt find any!!!", dataAvailable);
        }
    }

    private void assertMigratedCloudSiteData() throws Exception {
        try (Connection con = dataSource.getConnection(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("select * from cloud_sites where id='regionOne'")) {
            boolean dataAvailable = false;
            while (rs.next()) {
                dataAvailable = true;
                Assert.assertEquals("regionOne", rs.getString("id"));
                Assert.assertEquals("regionOne", rs.getString("region_id"));
                Assert.assertEquals("MT2", rs.getString("clli"));
                Assert.assertEquals("2.5", rs.getString("cloud_version"));
                Assert.assertEquals("MTKEYSTONE", rs.getString("identity_service_id"));
            }
            Assert.assertTrue("Expected data in identity_services table post migration but didnt find any!!!", dataAvailable);
        }
    }

    private void assertMigratedCloudManagerData() throws Exception {
        try (Connection con = dataSource.getConnection(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("select * from cloudify_managers where id='manager'")) {
            boolean dataAvailable = false;
            while (rs.next()) {
                dataAvailable = true;
                Assert.assertEquals("http://localhost:8080", rs.getString("cloudify_url"));
                Assert.assertEquals("user", rs.getString("username"));
                Assert.assertEquals("password", rs.getString("password"));
                Assert.assertEquals("2.0", rs.getString("version"));
            }
            Assert.assertTrue("Expected data in identity_services table post migration but didnt find any!!!", dataAvailable);
        }
    }
}
