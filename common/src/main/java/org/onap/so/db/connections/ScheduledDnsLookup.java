package org.onap.so.db.connections;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logging.filter.base.ScheduledLogging;
import org.onap.so.logging.filter.base.ScheduledTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.zaxxer.hikari.HikariPoolMXBean;

@Component
@Profile("!test")
public class ScheduledDnsLookup {

    private static final String JMX_HIKARI_DB_POOL_LOOKUP = "com.zaxxer.hikari:type=Pool (*,*";

    private static final String DB_HOST = "DB_HOST";

    @Autowired
    private DbDnsIpAddress dnsIpAddress;

    private static Logger logger = LoggerFactory.getLogger(ScheduledDnsLookup.class);

    @ScheduledLogging
    @Scheduled(fixedRate = 15000)
    public void performDnsLookup() throws ScheduledTaskException {
        String dnsUrl = System.getenv(DB_HOST);
        try {
            if (dnsUrl == null) {
                throw new ScheduledTaskException(ErrorCode.DataError,
                        "Database DNS is not provided. Please verify the configuration");
            }

            InetAddress inetAddress = java.net.InetAddress.getByName(dnsUrl);
            String ipAddress = inetAddress.getHostAddress();
            String currentIpAddress = dnsIpAddress.getIpAddress();
            /* This is in initial state */
            if (currentIpAddress == null) {
                dnsIpAddress.setIpAddress(ipAddress);
                return;
            }

            if ((ipAddress != null) && (!ipAddress.equalsIgnoreCase(currentIpAddress))) {
                logger.debug("Switched Database IP Address from {} to {}", currentIpAddress, ipAddress);
                softEvictConnectionPool();
                dnsIpAddress.setIpAddress(ipAddress);
            }
        } catch (UnknownHostException e) {
            logger.warn("Database DNS %s is not resolvable to an IP Address", dnsUrl);
        }
    }

    private void softEvictConnectionPool() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName queryObj = new ObjectName(JMX_HIKARI_DB_POOL_LOOKUP);
            Set<ObjectInstance> objects = mBeanServer.queryMBeans(queryObj, null);
            for (ObjectInstance objectInstance : objects) {
                ObjectName poolObject = objectInstance.getObjectName();
                HikariPoolMXBean poolProxy = JMX.newMXBeanProxy(mBeanServer, poolObject, HikariPoolMXBean.class);
                logger.debug("database connection pool is soft evicted for connections");
                poolProxy.softEvictConnections();
            }
        } catch (Exception e) {
            logger.warn("Error encountered in evicting DB connection pool", e);
        }
    }
}
