package org.onap.aaiclient.client.aai.entities.uri;

import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;

public class AAIClientUriFactory extends AAIUriFactory {
    /**
     * These can be retrieved without all their required keys but an HTTP call is required to do so
     *
     * @param type
     * @param values
     * @return
     */
    public static AAIResourceUri createResourceUri(AAISingleFragment fragment) {

        if (Types.SERVICE_INSTANCE.typeName().equals(fragment.get().build().typeName())) {
            return new ServiceInstanceUri(fragment);
        } else if (Types.ALLOTTED_RESOURCE.typeName().equals(fragment.get().build().typeName())) {
            return new AllottedResourceLookupUri(fragment);
        } else {
            return null;
        }
    }
}
