package org.onap.so.apihandlerinfra;

import java.net.URI;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;
import com.google.common.collect.ImmutableSet;

@Component
@ConfigurationPropertiesBinding
public class GenericStringConverter implements GenericConverter {

    @Autowired
    private HealthCheckConverter converter;

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {

        ConvertiblePair[] pairs = new ConvertiblePair[] {new ConvertiblePair(String.class, Subsystem.class),
                new ConvertiblePair(String.class, URI.class)};
        return ImmutableSet.copyOf(pairs);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        return converter.convert(source, sourceType, targetType);

    }
}
