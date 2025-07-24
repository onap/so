package org.onap.so.db.connections;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DbDnsIpAddressTest {

    @Test
    public void test() {
        final String expectedIpAddress = "10.0.75.1";

        DbDnsIpAddress dbDnsIpAddress = new DbDnsIpAddress();
        dbDnsIpAddress.setIpAddress(expectedIpAddress);

        assertEquals(expectedIpAddress, dbDnsIpAddress.getIpAddress());

    }

}
