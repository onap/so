package org.onap.so.logging.jaxrs.filter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MDCSetup {
    
    protected static Logger logger = LoggerFactory.getLogger(MDCSetup.class); 
    
    private static final String INSTANCE_UUID = UUID.randomUUID().toString();
    
    public void setInstanceUUID(){
        MDC.put(ONAPLogConstants.MDCs.INSTANCE_UUID, INSTANCE_UUID);
    }

    public void setServerFQDN(){
        String serverFQDN = "";
        InetAddress addr= null;
        try {
            addr = InetAddress.getLocalHost();
            serverFQDN = addr.toString();
        } catch (UnknownHostException e) {
            logger.warn("Cannot Resolve Host Name");
            serverFQDN = "";
        }
        MDC.put(ONAPLogConstants.MDCs.SERVER_FQDN, serverFQDN);
    }

    public void setClientIPAddress(HttpServletRequest httpServletRequest){
        String remoteIpAddress = "";
        if (httpServletRequest != null) {
            remoteIpAddress = httpServletRequest.getRemoteAddr();
        } 
        MDC.put(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS, remoteIpAddress);
    }

    public void setEntryTimeStamp() {
        MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP,ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
    }
}