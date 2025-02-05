/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.requestsdb;

import java.util.List;
import java.util.Map;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.controller.InstanceNameDuplicateCheckRequest;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfraActiveRequestsRepositoryCustomController {

    @Autowired
    InfraActiveRequestsRepository infraActiveRequestsRepository;

    @PostMapping(value = "/infraActiveRequests/getCloudOrchestrationFiltersFromInfraActive")
    public List<InfraActiveRequests> getCloudOrchestrationFiltersFromInfraActive(
            @RequestBody Map<String, String> orchestrationMap) {
        return infraActiveRequestsRepository.getCloudOrchestrationFiltersFromInfraActive(orchestrationMap);
    }

    @PostMapping(value = "/infraActiveRequests/getOrchestrationFiltersFromInfraActive")
    public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive(
            @RequestBody Map<String, List<String>> orchestrationMap) {
        return infraActiveRequestsRepository.getOrchestrationFiltersFromInfraActive(orchestrationMap);
    }

    @GetMapping(value = "/infraActiveRequests/checkVnfIdStatus/{operationalEnvironmentId}")
    public InfraActiveRequests checkVnfIdStatus(
            @PathVariable("operationalEnvironmentId") String operationalEnvironmentId) {
        return infraActiveRequestsRepository.checkVnfIdStatus(operationalEnvironmentId);
    }

    @PostMapping(value = "/infraActiveRequests/checkInstanceNameDuplicate")
    public InfraActiveRequests checkInstanceNameDuplicate(
            @RequestBody InstanceNameDuplicateCheckRequest instanceNameDuplicateCheckRequest) {
        return infraActiveRequestsRepository.checkInstanceNameDuplicate(
                instanceNameDuplicateCheckRequest.getInstanceIdMap(),
                instanceNameDuplicateCheckRequest.getInstanceName(),
                instanceNameDuplicateCheckRequest.getRequestScope());
    }

    @PostMapping(value = "/infraActiveRequests/v1/getInfraActiveRequests")
    public List<InfraActiveRequests> getInfraActiveRequests(@RequestBody Map<String, String[]> filters,
            @RequestParam("from") long startTime, @RequestParam("to") long endTime,
            @RequestParam(value = "maxResult", required = false) Integer maxResult) {
        return infraActiveRequestsRepository.getInfraActiveRequests(filters, startTime, endTime, maxResult);
    }

    @GetMapping(value = "/infraActiveRequests/getInProgressVolumeGroupsAndVfModules")
    public List<InfraActiveRequests> getInProgressVolumeGroupsAndVfModules() {
        return infraActiveRequestsRepository.getInProgressVolumeGroupsAndVfModules();
    }
}
