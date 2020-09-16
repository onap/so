package org.onap.aaiclient.client.graphinventory.entities.uri;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.onap.aaiclient.client.graphinventory.Format;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectBase;
import org.onap.aaiclient.client.graphinventory.entities.uri.parsers.UriParser;
import org.onap.aaiclient.client.graphinventory.entities.uri.parsers.UriParserSpringImpl;
import org.onap.aaiclient.client.graphinventory.exceptions.IncorrectNumberOfUriKeys;
import org.springframework.web.util.UriUtils;

public abstract class SimpleBaseUri<T extends GraphInventoryResourceUri<?, ?>, Parent extends GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?>, S extends GraphInventoryObjectBase>
        implements GraphInventoryResourceUri<T, S> {

    private static final long serialVersionUID = -1011069933894179423L;
    protected transient UriBuilder internalURI;
    protected static final String relationshipAPI = "/relationship-list/relationship";
    protected static final String relatedTo = "/related-to";
    protected Object[] values;
    protected final S type;
    protected final Parent parentUri;
    protected final Map<String, Set<String>> queryParams = new HashMap<>();

    protected SimpleBaseUri(S type, Object... values) {
        this.type = type;
        this.internalURI = UriBuilder.fromPath(this.getTemplate(type));
        this.values = values;
        this.parentUri = null;
        validateValuesSize(this.getTemplate(type), values);
    }

    protected SimpleBaseUri(S type, URI uri) {
        if (!type.passThrough()) {
            this.type = type;
            this.internalURI = UriBuilder.fromPath(this.getTemplate(type));
            this.values =
                    this.getURIKeys(uri.getRawPath().replaceAll(getPrefixPattern().toString(), "")).values().toArray();
            this.parentUri = null;
        } else {
            this.type = type;
            this.internalURI = UriBuilder.fromPath(uri.getRawPath().replaceAll(getPrefixPattern().toString(), ""));
            this.values = new Object[0];
            this.parentUri = null;
        }

    }

    protected SimpleBaseUri(S type, UriBuilder builder, Object... values) {
        this.internalURI = builder;
        this.values = values;
        this.type = type;
        this.parentUri = null;

    }

    protected SimpleBaseUri(Parent parentUri, S childType, Object... childValues) {
        this.type = childType;
        this.internalURI = UriBuilder.fromUri(type.partialUri());
        this.values = childValues;
        this.parentUri = parentUri;

        validateValuesSize(childType.partialUri(), values);
    }

    protected SimpleBaseUri(SimpleBaseUri<T, Parent, S> copy) {
        this.type = copy.type;
        this.internalURI = copy.internalURI.clone();
        this.values = copy.values.clone();
        if (copy.parentUri != null) {
            this.parentUri = (Parent) copy.parentUri.clone();
        } else {
            this.parentUri = null;
        }
    }

    protected void setInternalURI(UriBuilder builder) {
        this.internalURI = builder;
    }

    @Override
    public T queryParam(String name, String... values) {
        this.internalURI = internalURI.queryParam(name, values);
        if (queryParams.containsKey(name)) {
            queryParams.get(name).addAll(Arrays.asList(values));
        } else {
            queryParams.put(name, Stream.of(values).collect(Collectors.toSet()));
        }
        return (T) this;
    }

    @Override
    public T replaceQueryParam(String name, String... values) {
        this.internalURI = internalURI.replaceQueryParam(name, values);
        queryParams.put(name, Stream.of(values).collect(Collectors.toSet()));
        return (T) this;
    }

    @Override
    public T resultIndex(int index) {
        this.internalURI = internalURI.replaceQueryParam("resultIndex", index);
        return (T) this;
    }

    @Override
    public T resultSize(int size) {
        this.internalURI = internalURI.replaceQueryParam("resultSize", size);
        return (T) this;
    }

    @Override
    public T limit(int size) {
        this.resultIndex(0).resultSize(size);
        return (T) this;
    }

    @Override
    public URI build() {
        return build(this.values);
    }

    protected URI build(Object... values) {

        // This is a workaround because resteasy does not encode URIs correctly
        final String[] encoded = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            encoded[i] = UriUtils.encode(values[i].toString(), StandardCharsets.UTF_8.toString());
        }
        if (this.parentUri != null) {
            return UriBuilder
                    .fromUri(this.parentUri.build().toString() + internalURI.buildFromEncoded(encoded).toString())
                    .build();
        } else {
            return internalURI.buildFromEncoded(encoded);
        }
    }

    @Override
    public Map<String, String> getURIKeys() {
        return this.getURIKeys(this.build().toString());
    }

    protected Map<String, String> getURIKeys(String uri) {
        UriParser parser;
        if (!("".equals(this.getTemplate(type)))) {
            parser = new UriParserSpringImpl(this.getTemplate(type));
        } else {
            return new HashMap<>();
        }


        return parser.parse(uri);
    }

    @Override
    public abstract T clone();

    @Override
    public S getObjectType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            return this.toString().equals(o.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.toString()).toHashCode();
    }


    @Override
    public T depth(Depth depth) {
        this.internalURI.replaceQueryParam("depth", depth.toString());
        return (T) this;
    }

    @Override
    public T nodesOnly(boolean nodesOnly) {
        if (nodesOnly) {
            this.internalURI.replaceQueryParam("nodes-only", "");
        }
        return (T) this;
    }

    @Override
    public T format(Format format) {
        this.internalURI.replaceQueryParam("format", format);
        return (T) this;
    }

    public void validateValuesSize(String template, Object... values) {
        UriParser parser = new UriParserSpringImpl(template);
        Set<String> variables = parser.getVariables();
        if (variables.size() != values.length) {
            throw new IncorrectNumberOfUriKeys(String.format("Expected %s variables: %s", variables.size(), variables));
        }
    }

    protected String getTemplate(GraphInventoryObjectBase type) {
        return type.uriTemplate();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(this.internalURI.toTemplate());
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        String uri = ois.readUTF();
        this.setInternalURI(UriBuilder.fromUri(uri));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(null, ToStringStyle.NO_CLASS_NAME_STYLE).append("type", type)
                .append("parentUri", parentUri).append("values", values).append("queryParams", queryParams).toString();
    }
}
