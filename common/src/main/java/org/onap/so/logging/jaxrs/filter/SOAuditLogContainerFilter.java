package org.onap.so.logging.jaxrs.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MultivaluedMap;
import org.onap.logging.filter.base.AuditLogContainerFilter;
import org.onap.logging.filter.base.Constants;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.HttpHeadersConstants;
import org.onap.so.logger.LogConstants;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Priority(1)
@PreMatching
@Component
public abstract class SOAuditLogContainerFilter {

    private static final String ORIGINAL_PARTNER_NAME = "OriginalPartnerName";


    protected void additionalPreHandling(ContainerRequestContext request) {
        request.setProperty("requestId", MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
        MDC.put(ORIGINAL_PARTNER_NAME, MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
        String requestorId = request.getHeaders().getFirst("X-RequestorID");
        if (requestorId != null) {
            MDC.put(HttpHeadersConstants.REQUESTOR_ID, requestorId);
        }
        MDC.put(LogConstants.URI_BASE, request.getUriInfo().getBaseUri().toString());
    }


    protected void additionalPostHandling(ContainerResponseContext response) {
        MultivaluedMap<String, Object> responseHeaders = response.getHeaders();
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        responseHeaders.add(ONAPLogConstants.Headers.REQUEST_ID, requestId);
        responseHeaders.add(Constants.HttpHeaders.HEADER_REQUEST_ID, requestId);
        responseHeaders.add(Constants.HttpHeaders.TRANSACTION_ID, requestId);
        responseHeaders.add(Constants.HttpHeaders.ECOMP_REQUEST_ID, requestId);
        // responseHeaders.add(ONAPLogConstants.Headers.PARTNER_NAME, getProperty(Constants.Property.PARTNER_NAME));
        responseHeaders.add(ONAPLogConstants.Headers.INVOCATION_ID,
                MDC.get(ONAPLogConstants.MDCs.SERVER_INVOCATION_ID));
    }
}

