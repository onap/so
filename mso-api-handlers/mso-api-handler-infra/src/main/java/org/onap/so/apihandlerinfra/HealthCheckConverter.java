package org.onap.so.apihandlerinfra;

import java.net.URI;
import jakarta.ws.rs.core.UriBuilder;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckConverter {


    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType.getType() == String.class && targetType.getType() == Subsystem.class) {
            return SoSubsystems.valueOf(((String) source).toUpperCase());
        } else if (sourceType.getType() == String.class && targetType.getType() == URI.class) {
            return UriBuilder.fromUri((String) source).build();
        } else {
            return source;
        }
    }

}
