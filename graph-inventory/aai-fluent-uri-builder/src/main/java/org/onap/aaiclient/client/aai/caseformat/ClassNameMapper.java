package org.onap.aaiclient.client.aai.caseformat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import com.google.common.base.CaseFormat;

public class ClassNameMapper {

    private static final ClassNameMapper INSTANCE = new ClassNameMapper();
    private static Map<String, String> upperCamelToLowerHyphen;
    private static Map<String, String> lowerHyphenToUpperCamel;
    private static Set<String> aaiClassNames;

    private ClassNameMapper() {
        upperCamelToLowerHyphen = new HashMap<>(30 * 200); // initial capacity 30 versions with roughly 200 classes
        aaiClassNames = getAllClasses();
        this.initUpperCamelToLowerHyphen();
        this.initLowerHyphenToUpperCamel();
        aaiClassNames = null;
    }

    public static ClassNameMapper getInstance() {
        return INSTANCE;
    }

    private void initUpperCamelToLowerHyphen() {
        Map<String, String> converted = aaiClassNames.stream().collect(
                Collectors.toMap(name -> name, name -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, name)));
        upperCamelToLowerHyphen.putAll(converted);
    }

    private void initLowerHyphenToUpperCamel() {
        Map<String, String> converted = aaiClassNames.stream().collect(
                Collectors.toMap(name -> name, name -> CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name)));
        lowerHyphenToUpperCamel.putAll(converted);
    }

    private Set<String> getAllClasses() {
        // return Set.of("fooBar", "fooBaz");
        Reflections reflections = new Reflections("org.onap.aai.domain", Scanners.SubTypes.filterResultsBy(s -> true));

        return reflections.getSubTypesOf(Object.class).stream().map(Class::getSimpleName).collect(Collectors.toSet());
    }

    // public String toLowerHyphen(String camelCase) {
    // return upperCamelToLowerHyphen.get(camelCase);
    // }
    public <T> String toLowerHyphen(Class<T> clazz) {
        return upperCamelToLowerHyphen.get(clazz.getSimpleName());
    }

    public <T> String toUpperCamel(Class<T> clazz) {
        return upperCamelToLowerHyphen.get(clazz.getSimpleName());
    }

}
