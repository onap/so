package org.onap.so.apihandlerinfra.infra.rest.validators;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.beans.factory.annotation.Autowired;


public class VnfDeleteValidator implements RequestValidator {

    @Autowired
    AAIDataRetrieval aaiDataRetrieval;

    @Override
    public boolean shouldRunFor(String requestUri, ServiceInstancesRequest request, Actions action) {
        return Pattern.compile("[Vv][5-8]/serviceInstances/[^/]+/vnfs/[^/]+").matcher(requestUri).matches()
                && action.equals(Action.deleteInstance);
    }

    @Override
    public Optional<String> validate(Map<String, String> instanceIdMap, ServiceInstancesRequest request,
            Map<String, String> queryParams) {
        if (aaiDataRetrieval.isVnfRelatedToVolumes(instanceIdMap.get("vnfInstanceId"))) {
            return Optional.of("Cannot delete vnf it is still related to existing volume groups");
        } else {
            return Optional.empty();
        }
    }

}
