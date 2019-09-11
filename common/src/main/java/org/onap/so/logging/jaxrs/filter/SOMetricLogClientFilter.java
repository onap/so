package org.onap.so.logging.jaxrs.filter;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.filter.base.MetricLogClientFilter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.MdcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


public class SOMetricLogClientFilter extends MetricLogClientFilter {

    protected static Logger logger = LoggerFactory.getLogger(MDCSetup.class);
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");

    private MDCSetup mdcSetup = new MDCSetup();

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        try {
            mdcSetup.setLogTimestamp();
            mdcSetup.setElapsedTimeInvokeTimestamp();
            mdcSetup.setResponseStatusCode(responseContext.getStatus());
            mdcSetup.setResponseDescription(responseContext.getStatus());
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(responseContext.getStatus()));
            logger.info(INVOKE_RETURN, "InvokeReturn");
            mdcSetup.clearClientMDCs();
            setOpenStackResponseCode();
        } catch (Exception e) {
            logger.warn("Error in JAX-RS request,response client filter", e);
        }
    }

    protected void setOpenStackResponseCode() {
        if (MDC.get(MdcConstants.OPENSTACK_STATUS_CODE) != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, MDC.get(MdcConstants.OPENSTACK_STATUS_CODE));
        }
    }
}
