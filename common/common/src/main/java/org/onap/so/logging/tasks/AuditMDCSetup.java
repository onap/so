package org.onap.so.logging.tasks;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.camunda.bpm.client.task.ExternalTask;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.MdcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class AuditMDCSetup {

    private static final Logger logger = LoggerFactory.getLogger(AuditMDCSetup.class);

    private MDCSetup mdcSetup = new MDCSetup();

    public void setupMDC(ExternalTask externalTask) {
        MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        String msoRequestId = externalTask.getVariable("mso-request-id");
        if (msoRequestId != null && !msoRequestId.isEmpty()) {
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, msoRequestId);
        }
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, externalTask.getTopicName());
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, ONAPComponents.OPENSTACK_ADAPTER.toString());
        setResponseCode(ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, UUID.randomUUID().toString());
        setElapsedTime();
        mdcSetup.setServerFQDN();
        logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
    }

    public void setElapsedTime() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        MDC.put(ONAPLogConstants.MDCs.ELAPSED_TIME, Long.toString(System.currentTimeMillis() - ZonedDateTime
                .parse(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP), timeFormatter).toInstant().toEpochMilli()));
    }

    public void setResponseCode(String code) {
        MDC.put(MdcConstants.OPENSTACK_STATUS_CODE, code);
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, code);
    }

    public void clearClientMDCs() {
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
        MDC.remove(MdcConstants.OPENSTACK_STATUS_CODE);
        MDC.remove(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP);
        MDC.remove(ONAPLogConstants.MDCs.ELAPSED_TIME);
        MDC.remove(ONAPLogConstants.MDCs.PARTNER_NAME);
    }
}
