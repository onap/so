package org.onap.so.client.sniro;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.so.client.exception.BadResponseException;

public class SniroValidatorTest {

    @Test
    public void validateDemandsResponse_success() throws BadResponseException {
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("requestStatus", "accepted");
        new SniroValidator().validateDemandsResponse(testMap);
    }

    @Test
    public void validateDemandsResponse_emptyResponse() {
        try {
            new SniroValidator().validateDemandsResponse(new LinkedHashMap<>());
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains("Sniro Managers synchronous response is empty");
        }
    }

    @Test
    public void validateDemandsResponse_responseWithErrorMessage() {
        String message = "An error occurred";
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("requestStatus", "not_accepted");
        testMap.put("statusMessage", message);
        try {
            new SniroValidator().validateDemandsResponse(testMap);
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains("Sniro Managers synchronous response indicates failed: " + message);
        }
    }

    @Test
    public void validateDemandsResponse_responseWithoutMessage() {
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("requestStatus", "not_accepted");
        testMap.put("statusMessage", "");
        try {
            new SniroValidator().validateDemandsResponse(testMap);
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains("error message not provided");
        }
    }

    @Test
    public void validateDemandsResponse_responseWithoutRequestStatus() {
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("statusMessage", "");
        try {
            new SniroValidator().validateDemandsResponse(testMap);
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains("Sniro Managers synchronous response does not contain: request status");
        }
    }
}
