package org.onap.so.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ch.qos.logback.classic.spi.LoggingEvent;

@ExtendWith(MockitoExtension.class)
public class MaskLogStatementsTest {

    private MaskLogStatements maskLogStatements;

    @BeforeEach
    public void setUp() {
        maskLogStatements = new MaskLogStatements();
    }

    @Test
    public void testDoLayoutShouldMaskAuthorizationToken() {
        String logMessage = "Authorization: Bearer mySecretToken123";

        LoggingEvent loggingEvent = new LoggingEvent();
        loggingEvent.setMessage(logMessage);

        String maskedMessage = maskLogStatements.doLayout(loggingEvent);

        assertEquals("Authorization: Bearer ****************", maskedMessage);
    }
}
