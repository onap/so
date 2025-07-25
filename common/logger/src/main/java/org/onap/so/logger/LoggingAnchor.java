package org.onap.so.logger;

import java.util.Collections;
import java.util.stream.Collectors;

public class LoggingAnchor {
    public static final String ONE = Collections.nCopies(1, "{}").stream().collect(Collectors.joining(" "));

    public static final String TWO = Collections.nCopies(2, "{}").stream().collect(Collectors.joining(" "));

    public static final String THREE = Collections.nCopies(3, "{}").stream().collect(Collectors.joining(" "));

    public static final String FOUR = Collections.nCopies(4, "{}").stream().collect(Collectors.joining(" "));

    public static final String FIVE = Collections.nCopies(5, "{}").stream().collect(Collectors.joining(" "));

    public static final String SIX = Collections.nCopies(6, "{}").stream().collect(Collectors.joining(" "));

    public static final String SEVEN = Collections.nCopies(7, "{}").stream().collect(Collectors.joining(" "));

    public static final String EIGHT = Collections.nCopies(8, "{}").stream().collect(Collectors.joining(" "));

    public static final String NINE = Collections.nCopies(9, "{}").stream().collect(Collectors.joining(" "));

    public static final String TEN = Collections.nCopies(10, "{}").stream().collect(Collectors.joining(" "));

    private LoggingAnchor() {}

}
