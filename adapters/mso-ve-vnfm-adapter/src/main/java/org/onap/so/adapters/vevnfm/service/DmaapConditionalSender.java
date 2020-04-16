package org.onap.so.adapters.vevnfm.service;

import org.apache.logging.log4j.util.Strings;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;
import org.onap.so.adapters.vevnfm.constant.NotificationVnfFilterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DmaapConditionalSender {

    private static final Logger logger = LoggerFactory.getLogger(DmaapConditionalSender.class);

    private final NotificationVnfFilterType notificationVnfFilterType;
    private final AaiConnection aaiConnection;
    private final DmaapService dmaapService;

    public DmaapConditionalSender(final ConfigProperties configProperties, final AaiConnection aaiConnection,
            final DmaapService dmaapService) {
        this.notificationVnfFilterType = configProperties.getNotificationVnfFilterType();
        this.aaiConnection = aaiConnection;
        this.dmaapService = dmaapService;
    }

    public void send(final VnfLcmOperationOccurrenceNotification notification) {
        final String vnfInstanceId = notification.getVnfInstanceId();
        boolean sent = false;

        switch (notificationVnfFilterType) {
            case ALL:
                dmaapService.send(notification, aaiConnection.checkGenericVnfId(vnfInstanceId));
                sent = true;
                break;
            case AAI_CHECKED:
                final String genericId = aaiConnection.checkGenericVnfId(vnfInstanceId);
                if (Strings.isNotBlank(genericId)) {
                    dmaapService.send(notification, genericId);
                    sent = true;
                }
                break;
            case NONE:
                break;
            default:
                throw new IllegalArgumentException(
                        "The value of VnfNotificationFilterType is not supported: " + notificationVnfFilterType);
        }

        if (sent) {
            logger.info("The info with the VNF id '{}' is sent to DMaaP", vnfInstanceId);
        } else {
            logger.info("This VNF id '{}' is not sent to DMaaP", vnfInstanceId);
        }
    }
}
