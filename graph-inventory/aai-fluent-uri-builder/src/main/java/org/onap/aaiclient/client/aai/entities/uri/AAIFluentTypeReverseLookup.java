package org.onap.aaiclient.client.aai.entities.uri;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.graphinventory.GraphInventoryFluentType;
import org.onap.aaiclient.client.graphinventory.entities.uri.parsers.UriParserSpringImpl;
import com.google.common.base.CaseFormat;

public class AAIFluentTypeReverseLookup {

    public AAIObjectType fromName(String name, String uri) {

        String className = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);

        uri = uri.replaceFirst(".*?/v\\d+", "");
        try {
            Class<? extends GraphInventoryFluentType.Info> clazz =
                    (Class<? extends GraphInventoryFluentType.Info>) Class
                            .forName("org.onap.aaiclient.client.generated.fluentbuilders." + className + "$Info");

            GraphInventoryFluentType.Info type = clazz.getConstructor().newInstance();

            Optional<String> parentTemplate = findParentPath(type, uri);
            if (parentTemplate.isPresent()) {
                return new AAIObjectType(parentTemplate.get(), type.getPartialUri(), type.getName(), false);
            } else {
                // fallback to enum lookup
                return AAIObjectType.fromTypeName(name);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        }
        return AAIObjectType.UNKNOWN;
    }

    protected Optional<String> findParentPath(GraphInventoryFluentType.Info type, String uri) {

        List<UriParserSpringImpl> parsers =
                type.getPaths().stream().map(path -> new UriParserSpringImpl(path)).collect(Collectors.toList());

        for (UriParserSpringImpl parser : parsers) {
            if (parser.isMatch(uri)) {
                String partialUriReplacer = type.getPartialUri().replaceAll("\\{[^}]+\\}", "[^/]+");
                return Optional.of(parser.getTemplate().replaceFirst(partialUriReplacer + "$", ""));
            }
        }

        return Optional.empty();
    }
}
