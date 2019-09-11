package org.onap.so.logging.jaxrs.filter;

import java.io.IOException;
import java.nio.charset.Charset;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.filter.spring.SpringClientFilter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.MdcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class SOSpringClientFilter extends SpringClientFilter implements ClientHttpRequestInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MDCSetup mdcSetup = new MDCSetup();
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");


    protected void processResponse(ClientHttpResponse response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("============================response begin==========================================");
            logger.debug("Status code  : {}", response.getStatusCode());
            logger.debug("Status text  : {}", response.getStatusText());
            logger.debug("Headers      : {}", response.getHeaders());
            logger.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            logger.debug("=======================response end=================================================");
        }
        mdcSetup.setLogTimestamp();
        mdcSetup.setElapsedTimeInvokeTimestamp();
        mdcSetup.setResponseStatusCode(response.getRawStatusCode());
        int statusCode = response.getRawStatusCode();
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(statusCode));
        mdcSetup.setResponseDescription(statusCode);
        logger.info(INVOKE_RETURN, "InvokeReturn");
        mdcSetup.clearClientMDCs();
        setOpenStackResponseCode();
    }

    protected void setOpenStackResponseCode() {
        if (MDC.get(MdcConstants.OPENSTACK_STATUS_CODE) != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, MDC.get(MdcConstants.OPENSTACK_STATUS_CODE));
        }
    }
}
