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

    @Test
    public void validateSolution_success() throws BadResponseException {
        SniroValidator.validateSolution("{statusMessage:key}");
    }

    @Test
    public void validateSolution_emptyResponse() {
        try {
            SniroValidator.validateSolution("");
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains("Sniro Managers asynchronous response is empty");
        }
    }

    @Test
    public void validateSolution_errorResponseWithoutMessage() {
        try {
            SniroValidator.validateSolution("{\"serviceException\":{\"text\":\"\"}}");
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains(
                    "Sniro Managers asynchronous response contains a service exception: error message not provided");
        }
    }

    @Test
    public void validateSolution_errorResponseWithErrorMessage() {
        String message = "An error occurred";
        try {
            SniroValidator.validateSolution("{\"serviceException\":{\"text\":\"" + message + "\"}}");
        } catch (BadResponseException e) {
            assertThat(e.getMessage())
                    .contains("Sniro Managers asynchronous response contains a service exception: " + message);
        }
    }

    @Test
    public void validateReleaseResponse_success() throws BadResponseException {
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("status", "success");
        new SniroValidator().validateReleaseResponse(testMap);
    }

    @Test
    public void validateReleaseResponse_emptyResponse() {
        try {
            new SniroValidator().validateReleaseResponse(new LinkedHashMap<>());
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains("Sniro Conductors response is empty");
        }
    }

    @Test
    public void validateReleaseResponse_errorResponseWithErrorMessage() {
        String message = "An error occurred";
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("status", "failed");
        testMap.put("message", message);
        try {
            new SniroValidator().validateReleaseResponse(testMap);
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains("Sniro Conductors synchronous response indicates failed: " + message);
        }
    }

    @Test
    public void validateReleaseResponse_errorResponseWithNoMessage() {
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("status", "failed");
        testMap.put("message", "");
        try {
            new SniroValidator().validateReleaseResponse(testMap);
        } catch (BadResponseException e) {
            assertThat(e.getMessage())
                    .contains("Sniro Conductors synchronous response indicates failed: error message not provided");
        }
    }

    @Test
    public void validateReleaseResponse_responseWithoutStatus() {
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("statusMessage", "");
        try {
            new SniroValidator().validateReleaseResponse(testMap);
        } catch (BadResponseException e) {
            assertThat(e.getMessage()).contains("Sniro Conductors synchronous response does not contain: status");
        }
    }
}
