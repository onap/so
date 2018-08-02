package org.onap.so.adapters.requestsdb;

import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.controller.InstanceNameDuplicateCheckRequest;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.PathParam;
import java.util.List;
import java.util.Map;

@RestController
public class InfraActiveRequestsRepositoryCustomController {

    @Autowired
    InfraActiveRequestsRepository infraActiveRequestsRepository;
    @RequestMapping(method = RequestMethod.POST, value = "/infraActiveRequests/getCloudOrchestrationFiltersFromInfraActive")
    public List<InfraActiveRequests> getCloudOrchestrationFiltersFromInfraActive(@RequestBody Map<String, String> orchestrationMap){
        return infraActiveRequestsRepository.getCloudOrchestrationFiltersFromInfraActive(orchestrationMap);
    }
    @RequestMapping(method = RequestMethod.POST, value = "/getOrchestrationFiltersFromInfraActive")
    public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive(@RequestBody Map<String, List<String>> orchestrationMap){
        return  infraActiveRequestsRepository.getOrchestrationFiltersFromInfraActive(orchestrationMap);
    }
    @RequestMapping(method = RequestMethod.GET, value = "/infraActiveRequests/checkVnfIdStatus/{nsInstanceId}")
    public InfraActiveRequests checkVnfIdStatus(@PathParam("nsInstanceId") String operationalEnvironmentId){
        return infraActiveRequestsRepository.checkVnfIdStatus(operationalEnvironmentId);
    }
    @RequestMapping(method = RequestMethod.POST, value = "/infraActiveRequests/checkInstanceNameDuplicate")
    public InfraActiveRequests checkInstanceNameDuplicate(@RequestBody InstanceNameDuplicateCheckRequest instanceNameDuplicateCheckRequest){
        return infraActiveRequestsRepository.checkInstanceNameDuplicate(instanceNameDuplicateCheckRequest.getInstanceIdMap(), instanceNameDuplicateCheckRequest.getInstanceName(),instanceNameDuplicateCheckRequest.getRequestScope());
    }
}
