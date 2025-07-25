package org.onap.so.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.onap.so.logger.MaskLogStatements;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class MaskLogStatementsTest {

    private LoggerContext lc = new LoggerContext();
    private Logger logger = lc.getLogger(MaskLogStatementsTest.class);

    @Test
    public void verifyOpenStackPayload() throws IOException {
        String payload =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/logging/openstack-payload.json")));

        ILoggingEvent event = makeLoggingEvent(payload);

        MaskLogStatements mask = new MaskLogStatements();

        mask.setContext(lc);
        mask.setPattern("%m");
        mask.start();
        String result = mask.doLayout(event);

        assertTrue(result.matches("(?s).*?\"password\"\\s?:\\s?\"\\*+\".*"));

    }

    @Test
    public void maskAuthHeaderTest() {
        String msg = "Headers     : [Accept:\"application/json\", Authorization:\"Basic dklfjeaklfjdkalf\","
                + "Content-Type:\"application/json\", Content-Length:\"10\","
                + "X-RequestID:\"db2a0462-69d0-499f-93ec-e2a064ef1f59\", X-TransactionID:\"db2a0462-69d0-499f-93ec-e2a064ef1f59\","
                + "X-ECOMP-RequestID:\"db2a0462-69d0-499f-93ec-e2a064ef1f59\", X-ONAP-PartnerName:\"SO.APIH\","
                + "X-InvocationID:\"885e4f99-6f24-4f17-ab1b-584b37715b49\"]";

        String expected = "Headers     : [Accept:\"application/json\", Authorization:\"Basic ****************\","
                + "Content-Type:\"application/json\", Content-Length:\"10\","
                + "X-RequestID:\"db2a0462-69d0-499f-93ec-e2a064ef1f59\", X-TransactionID:\"db2a0462-69d0-499f-93ec-e2a064ef1f59\","
                + "X-ECOMP-RequestID:\"db2a0462-69d0-499f-93ec-e2a064ef1f59\", X-ONAP-PartnerName:\"SO.APIH\","
                + "X-InvocationID:\"885e4f99-6f24-4f17-ab1b-584b37715b49\"]";
        ILoggingEvent event = makeLoggingEvent(msg);

        MaskLogStatements mask = new MaskLogStatements();

        mask.setContext(lc);
        mask.setPattern("%m");
        mask.start();
        String result = mask.doLayout(event);

        assertEquals(expected, result);
    }

    @Test
    public void maskAuthHeaderObjectStringTest() {
        String msg = "Headers: {Accept=[text/plain, application/json, application/*+json, */*],"
                + "Authorization=[Basic aaaaa],"
                + "connection=[keep-alive], Content-Length=[217], content-type=[application/xml],"
                + "host=[mso-bpmn-infra-svc:9200], user-agent=[Java/11.0.6]}";
        String expected = "Headers: {Accept=[text/plain, application/json, application/*+json, */*],"
                + "Authorization=[Basic -----],"
                + "connection=[keep-alive], Content-Length=[217], content-type=[application/xml],"
                + "host=[mso-bpmn-infra-svc:9200], user-agent=[Java/11.0.6]}";
        ILoggingEvent event = makeLoggingEvent(msg);

        MaskLogStatements mask = new MaskLogStatements();

        mask.setContext(lc);
        mask.setPattern("%m");
        mask.setMaskChar("-");
        mask.start();
        String result = mask.doLayout(event);

        assertEquals(expected, result);
    }

    private ILoggingEvent makeLoggingEvent(String message) {
        return new LoggingEvent(MaskLogStatementsTest.class.getName(), logger, Level.INFO, message, null, null);
    }
}
