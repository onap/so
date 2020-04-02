package org.onap.so.apihandlerinfra.infra.rest.validators;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.beans.factory.annotation.Autowired;


public class ServiceInstanceDeleteValidator implements RequestValidator {

    @Autowired
    AAIDataRetrieval aaiDataRetrieval;

    @Override
    public boolean shouldRunFor(String requestUri, ServiceInstancesRequest request, Actions action) {
        return Pattern.compile("[Vv][5-8]/serviceInstances/[^/]+").matcher(requestUri).matches()
                && action.equals(Action.deleteInstance);
    }

    @Override
    public Optional<String> validate(Map<String, String> instanceIdMap, ServiceInstancesRequest request,
            Map<String, String> queryParams) {
        if (aaiDataRetrieval.isServiceRelatedToGenericVnf(instanceIdMap.get("serviceInstanceId"))) {
            return Optional.of("Cannot delete service it is still related to existing vf-modules");
        } else if (aaiDataRetrieval.isServiceRelatedToNetworks(instanceIdMap.get("serviceInstanceId"))) {
            return Optional.of("Cannot delete service it is still related to existing networks");
        } else if (aaiDataRetrieval.isServiceRelatedToConfiguration(instanceIdMap.get("serviceInstanceId"))) {
            return Optional.of("Cannot delete service it is still related to existing configurations");
        } else {
            return Optional.empty();
        }
    }
}
