package org.onap.aaiclient.client.aai.caseformat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassNameMapper {

    private static Map<String, String> upperCamelToLowerHyphen;
    private static Map<String, String> lowerHyphenToUpperCamel;
    private static Set<String> aaiClassNames;

    private ClassNameMapper() {
        upperCamelToLowerHyphen = new HashMap<>(30 * 200); // initial capacity 30 versions with roughly 200 classes
        lowerHyphenToUpperCamel = new HashMap<>(30 * 200); // initial capacity 30 versions with roughly 200 classes
        aaiClassNames = getAllClasses();
        this.initUpperCamelToLowerHyphen();
        this.initLowerHyphenToUpperCamel();
        aaiClassNames = null;
    }

    private static class InstanceHolder {
        private static final ClassNameMapper INSTANCE;
        static {
            try {
                INSTANCE = new ClassNameMapper();
            } catch (Exception e) {
                log.error("Unable to initialize ClassNameMapper", e);
                throw new RuntimeException("Unable to initialize ClassNameMapper", e);
            }
        }
    }

    public static ClassNameMapper getInstance() {
        return InstanceHolder.INSTANCE;
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
        Reflections reflections = new Reflections("org.onap.aai.domain", Scanners.SubTypes.filterResultsBy(s -> true));

        return reflections.getSubTypesOf(Object.class).stream().map(Class::getSimpleName).collect(Collectors.toSet());
    }

    public <T> String toLowerHyphen(Class<T> clazz) {
        return upperCamelToLowerHyphen.get(clazz.getSimpleName());
    }

    public <T> String toUpperCamel(Class<T> clazz) {
        return lowerHyphenToUpperCamel.get(clazz.getSimpleName());
    }

}
