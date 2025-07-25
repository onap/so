package org.onap.so.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MaskLogStatements extends PatternLayout {

    private String patternsProperty;
    private String maskChar = "*";
    private Optional<Pattern> pattern = Optional.empty();
    private static final String authPatternString = "Authorization[: ]+(?:Bearer|Basic)\\s(\\S+)";
    private static final Pattern authPattern = Pattern.compile(authPatternString);
    private static final Pattern openstackPattern = Pattern.compile("\"password\"\\s?:\\s?\"(.*?)\"");

    public String getPatternsProperty() {
        return patternsProperty;
    }

    public void setPatternsProperty(String patternsProperty) {
        this.patternsProperty = patternsProperty;
        if (this.patternsProperty != null) {
            this.pattern = Optional.of(Pattern.compile(patternsProperty, Pattern.MULTILINE));
        }
    }

    public String getMaskChar() {
        return maskChar;
    }

    public void setMaskChar(String maskChar) {
        this.maskChar = maskChar;
    }


    protected Collection<Pattern> getPatterns() {
        return Arrays.asList(authPattern, openstackPattern);
    }

    @Override
    public String doLayout(ILoggingEvent event) {

        // final StringBuilder message = new StringBuilder(super.doLayout(event));
        final StringBuilder message = new StringBuilder(event.getFormattedMessage());
        List<Pattern> patterns = new ArrayList<>(getPatterns());
        if (pattern.isPresent()) {
            patterns.add(pattern.get());
        }
        patterns.forEach(p -> {
            Matcher matcher = p.matcher(message);
            while (matcher.find()) {
                int group = 1;
                while (group <= matcher.groupCount()) {
                    if (matcher.group(group) != null) {
                        for (int i = matcher.start(group); i < matcher.end(group); i++) {
                            message.setCharAt(i, maskChar.charAt(0));
                        }
                    }
                    group++;
                }
            }
        });
        return message.toString();
    }

}
