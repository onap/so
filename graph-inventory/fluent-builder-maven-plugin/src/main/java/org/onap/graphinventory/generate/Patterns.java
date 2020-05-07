package org.onap.graphinventory.generate;

import java.util.regex.Pattern;

public class Patterns {

    public static final Pattern pluralPattern = Pattern.compile(".*(?<partial>/(?<name>[^{]*$))");
    public static final Pattern singularPattern = Pattern.compile(".*(?<partial>/(?<name>[^/{}]*)/\\{.*$)");
    public static final Pattern topLevelPattern = Pattern.compile("^/([^/]+)/.*");
    public static final Pattern urlTemplatePattern = Pattern.compile("\\{([^}.]+(?:\\.([^}]+))?)\\}");
}
