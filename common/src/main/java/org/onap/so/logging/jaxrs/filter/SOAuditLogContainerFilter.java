package org.onap.so.logging.jaxrs.filter;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.HttpHeadersConstants;
import org.onap.so.logger.LogConstants;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.onap.logging.filter.base.AuditLogContainerFilter;

@Priority(1)
@PreMatching
@Component
public class SOAuditLogContainerFilter extends AuditLogContainerFilter {

    private static final String ORIGINAL_PARTNER_NAME = "OriginalPartnerName";

    @Override
    protected void additionalPreHandling(ContainerRequestContext request) {
        request.setProperty("requestId", MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
        MDC.put(ORIGINAL_PARTNER_NAME, MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
        String requestorId = request.getHeaders().getFirst("X-RequestorID");
        if (requestorId != null) {
            MDC.put(HttpHeadersConstants.REQUESTOR_ID, requestorId);
        }
        MDC.put(LogConstants.URI_BASE, request.getUriInfo().getBaseUri().toString());
    }
}
