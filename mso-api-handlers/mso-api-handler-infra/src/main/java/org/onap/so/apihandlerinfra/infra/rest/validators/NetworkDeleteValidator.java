package org.onap.so.apihandlerinfra.infra.rest.validators;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.beans.factory.annotation.Autowired;


public class NetworkDeleteValidator implements RequestValidator {

    @Autowired
    AAIDataRetrieval aaiDataRetrieval;

    @Override
    public boolean shouldRunFor(String requestUri, ServiceInstancesRequest request, Actions action) {
        return Pattern.compile("[Vv][5-8]/serviceInstances/[^/]+/networks/[^/]+").matcher(requestUri).matches()
                && action.equals(Action.deleteInstance);

    }

    @Override
    public Optional<String> validate(Map<String, String> instanceIdMap, ServiceInstancesRequest request,
            Map<String, String> queryParams) {
        if (aaiDataRetrieval.isNetworkRelatedToModules(instanceIdMap.get("networkInstanceId"))) {
            return Optional.of("Cannot delete network it is still related to existing vf-modules");
        } else {
            return Optional.empty();
        }
    }
}
