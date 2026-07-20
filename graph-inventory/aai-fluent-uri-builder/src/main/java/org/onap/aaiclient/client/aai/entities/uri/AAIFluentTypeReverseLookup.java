package org.onap.aaiclient.client.aai.entities.uri;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.graphinventory.GraphInventoryFluentType;
import org.onap.aaiclient.client.graphinventory.entities.uri.parsers.UriParserSpringImpl;
import com.google.common.base.CaseFormat;

public class AAIFluentTypeReverseLookup {

    private static final String GENERATED_PACKAGE = "org.onap.aaiclient.client.generated.fluentbuilders.";

    /** The AAI root + version prefix ({@code .../vN}) is a fixed regex; compile it once and reuse it. */
    private static final Pattern VERSION_PREFIX = Pattern.compile(".*?/v\\d+");

    /**
     * Resolving a type by name performs reflection ({@link Class#forName}) and compiles the type's path regexes. Both
     * are derived from a fixed, build-time-generated set, so results are cached per name. A negative result is cached
     * as {@link Optional#empty()} so an unknown name does not repeatedly incur reflection + exception cost. The cached
     * {@link ResolvedType} is immutable, so sharing it across threads is safe.
     */
    private static final ConcurrentHashMap<String, Optional<ResolvedType>> CACHE = new ConcurrentHashMap<>();

    public AAIObjectType fromName(String name, String uri) {

        final String strippedUri = VERSION_PREFIX.matcher(uri).replaceFirst("");

        Optional<ResolvedType> resolved = resolveType(name);
        if (resolved.isPresent()) {
            ResolvedType type = resolved.get();
            Optional<String> parentTemplate = type.findParentPath(strippedUri);
            if (parentTemplate.isPresent()) {
                return new AAIObjectType(parentTemplate.get(), type.getPartialUri(), type.getName(), false);
            } else {
                // fallback to enum lookup
                return AAIObjectType.fromTypeName(name);
            }
        }
        return AAIObjectType.UNKNOWN;
    }

    /**
     * Resolves (and caches) the generated fluent type metadata for the given A&AI object name. Returns
     * {@link Optional#empty()} when no matching generated type exists.
     */
    static Optional<ResolvedType> resolveType(String name) {
        return CACHE.computeIfAbsent(name, AAIFluentTypeReverseLookup::doResolve);
    }

    private static Optional<ResolvedType> doResolve(String name) {
        String className = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);
        try {
            @SuppressWarnings("unchecked")
            Class<? extends GraphInventoryFluentType.Info> clazz =
                    (Class<? extends GraphInventoryFluentType.Info>) Class
                            .forName(GENERATED_PACKAGE + className + "$Info");
            GraphInventoryFluentType.Info type = clazz.getConstructor().newInstance();
            return Optional.of(new ResolvedType(type));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return Optional.empty();
        }
    }

    /**
     * Cached, immutable resolution of a generated fluent type: the type's {@link GraphInventoryFluentType.Info}, its
     * pre-compiled path parsers, and the pre-compiled regex that strips the partial URI suffix off a matched template.
     */
    static final class ResolvedType {
        private final GraphInventoryFluentType.Info type;
        private final List<UriParserSpringImpl> parsers;
        private final Pattern partialUriSuffix;

        ResolvedType(GraphInventoryFluentType.Info type) {
            this.type = type;
            this.parsers = type.getPaths().stream().map(UriParserSpringImpl::forTemplate).collect(Collectors.toList());
            this.partialUriSuffix = Pattern.compile(type.getPartialUri().replaceAll("\\{[^}]+\\}", "[^/]+") + "$");
        }

        Optional<String> findParentPath(String uri) {
            for (UriParserSpringImpl parser : parsers) {
                if (parser.isMatch(uri)) {
                    return Optional.of(partialUriSuffix.matcher(parser.getTemplate()).replaceFirst(""));
                }
            }
            return Optional.empty();
        }

        String getPartialUri() {
            return type.getPartialUri();
        }

        String getName() {
            return type.getName();
        }
    }
}
