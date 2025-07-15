package org.onap.aaiclient.client;

import java.util.Optional;
import org.onap.so.aaiclient.api.entities.Relationships;
import lombok.Value;

@Value
public class AAIResultWrapper<T> {
    private final T result;
    private final Optional<Relationships> relationships;

}
