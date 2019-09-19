package org.onap.so.logging.jaxrs.filter;

import java.io.IOException;
import java.nio.charset.Charset;
import org.onap.logging.filter.spring.SpringClientFilter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.MdcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class SOSpringClientFilter extends SpringClientFilter implements ClientHttpRequestInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");

    @Override
    protected void post(HttpRequest request, ClientHttpResponse response) {
        setLogTimestamp();
        setElapsedTimeInvokeTimestamp();
        try {
            setResponseStatusCode(response.getRawStatusCode());
            int statusCode = response.getRawStatusCode();
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(statusCode));
            setResponseDescription(statusCode);
        } catch (IOException e) {
            logger.error("Unable to get statusCode from response");
        }


        logger.info(INVOKE_RETURN, "InvokeReturn");
        clearClientMDCs();
        setOpenStackResponseCode();
    }

    protected void setOpenStackResponseCode() {
        if (MDC.get(MdcConstants.OPENSTACK_STATUS_CODE) != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, MDC.get(MdcConstants.OPENSTACK_STATUS_CODE));
        }
    }
}
