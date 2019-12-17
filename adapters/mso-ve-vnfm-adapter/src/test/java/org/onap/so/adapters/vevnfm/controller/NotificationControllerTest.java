package org.onap.so.adapters.vevnfm.controller;

import org.junit.Test;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class NotificationControllerTest {

    private final NotificationController controller = new NotificationController();

    @Test
    public void testReceiveNotification() {
        final VnfLcmOperationOccurrenceNotification notification
                = new VnfLcmOperationOccurrenceNotification();
        final ResponseEntity response = controller.receiveNotification(notification);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
