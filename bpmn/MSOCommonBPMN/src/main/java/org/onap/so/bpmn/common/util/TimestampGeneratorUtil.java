package org.onap.so.bpmn.common.util;

public final class TimestampGeneratorUtil {

    private static final String APPC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'.0Z'";
    private static final CommonTimestampGenerator APPC_TIMESTAMP_GENERATOR = new CommonTimestampGenerator(APPC_FORMAT);

    public static final CommonTimestampGenerator COMMON_GENERATOR = new CommonTimestampGenerator();

    private TimestampGeneratorUtil() {}

    public static String generateCurrentTimestamp(String contollerType) {
        if (contollerType.equals("APPC")) {
            return APPC_TIMESTAMP_GENERATOR.generateCurrentTimestamp();
        } else {
            return COMMON_GENERATOR.generateCurrentTimestamp();
        }
    }
}
