package org.onap.so.bpmn.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CommonTimestampGenerator {

    private final DateTimeFormatter formatter;

    public CommonTimestampGenerator(String format) {
        this.formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
    }

    public CommonTimestampGenerator() {
        this.formatter = null;
    }

    public String generateCurrentTimestamp() {
        if (formatter != null) {
            return formatter.format(Instant.now());
        } else {
            return Instant.now().toString();
        }
    }
}
