/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.montoring.rest.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onap.so.montoring.camunda.model.ActivityInstance;
import org.onap.so.montoring.camunda.model.ProcessDefinition;
import org.onap.so.montoring.camunda.model.ProcessInstance;
import org.onap.so.montoring.camunda.model.ProcessInstanceVariable;
import org.onap.so.montoring.configuration.camunda.CamundaRestUrlProvider;
import org.onap.so.montoring.model.ActivityInstanceDetail;
import org.onap.so.montoring.model.ProcessDefinitionDetail;
import org.onap.so.montoring.model.ProcessInstanceDetail;
import org.onap.so.montoring.model.ProcessInstanceIdDetail;
import org.onap.so.montoring.model.ProcessInstanceVariableDetail;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;

/**
 * @author waqas.ikram@ericsson.com
 */
@Service
public class CamundaProcessDataServiceProviderImpl implements CamundaProcessDataServiceProvider {
    private static final XLogger LOGGER = XLoggerFactory.getXLogger(CamundaProcessDataServiceProviderImpl.class);

    private final CamundaRestUrlProvider urlProvider;

    private final HttpRestServiceProvider httpRestServiceProvider;

    @Autowired
    public CamundaProcessDataServiceProviderImpl(final CamundaRestUrlProvider urlProvider,
            final HttpRestServiceProvider httpRestServiceProvider) {
        this.urlProvider = urlProvider;
        this.httpRestServiceProvider = httpRestServiceProvider;
    }

    @Override
    public Optional<ProcessInstanceIdDetail> getProcessInstanceIdDetail(final String requestId) {
        final String url = urlProvider.getHistoryProcessInstanceUrl(requestId);
        final Optional<ProcessInstance[]> processInstances =
                httpRestServiceProvider.getHttpResponse(url, ProcessInstance[].class);

        if (processInstances.isPresent()) {
            final ProcessInstance[] instances = processInstances.get();
            LOGGER.debug("found process instance for request id: {}, result size: {}", requestId, instances.length);

            if (instances.length > 0) {
                for (int index = 0; index < instances.length; index++) {
                    final ProcessInstance processInstance = instances[index];
                    if (processInstance.getSuperProcessInstanceId() == null) {
                        return Optional.of(new ProcessInstanceIdDetail(processInstance.getId()));
                    }
                    LOGGER.debug("found sub process instance id with super process instanceId: {}",
                            processInstance.getSuperProcessInstanceId());
                }
            }
        }
        LOGGER.error("Unable to find process intance for request id: {}", requestId);
        return Optional.absent();
    }

    @Override
    public Optional<ProcessInstanceDetail> getSingleProcessInstanceDetail(final String processInstanceId) {
        final String url = urlProvider.getSingleProcessInstanceUrl(processInstanceId);
        final Optional<ProcessInstance> processInstances =
                httpRestServiceProvider.getHttpResponse(url, ProcessInstance.class);

        if (processInstances.isPresent()) {
            final ProcessInstance processInstance = processInstances.get();

            final ProcessInstanceDetail instanceDetail =
                    new ProcessInstanceDetail(processInstance.getId(), processInstance.getProcessDefinitionId(),
                            processInstance.getProcessDefinitionName(), processInstance.getSuperProcessInstanceId());
            return Optional.of(instanceDetail);

        }
        LOGGER.error("Unable to find process intance for id: {}", processInstanceId);
        return Optional.absent();
    }


    @Override
    public Optional<ProcessDefinitionDetail> getProcessDefinition(final String processDefinitionId) {
        final String url = urlProvider.getProcessDefinitionUrl(processDefinitionId);
        final Optional<ProcessDefinition> response =
                httpRestServiceProvider.getHttpResponse(url, ProcessDefinition.class);
        if (response.isPresent()) {
            final ProcessDefinition processDefinition = response.get();
            final String xmlDefinition = processDefinition.getBpmn20Xml();
            if (xmlDefinition != null) {
                return Optional.of(new ProcessDefinitionDetail(processDefinitionId, xmlDefinition));
            }
        }
        LOGGER.error("Unable to find process definition for processDefinitionId: {}", processDefinitionId);
        return Optional.absent();
    }

    @Override
    public List<ActivityInstanceDetail> getActivityInstance(final String processInstanceId) {
        final String url = urlProvider.getActivityInstanceUrl(processInstanceId);
        final Optional<ActivityInstance[]> response =
                httpRestServiceProvider.getHttpResponse(url, ActivityInstance[].class);
        if (response.isPresent()) {
            final ActivityInstance[] activityInstances = response.get();
            final List<ActivityInstanceDetail> activityInstanceDetails = new ArrayList<>(activityInstances.length);
            for (int index = 0; index < activityInstances.length; index++) {

                final ActivityInstance activityInstance = activityInstances[index];

                activityInstanceDetails.add(new ActivityInstanceDetail.ActivityInstanceDetailBuilder()
                        .activityId(activityInstance.getActivityId()).activityName(activityInstance.getActivityName())
                        .activityType(activityInstance.getActivityType())
                        .calledProcessInstanceId(activityInstance.getCalledProcessInstanceId())
                        .startTime(activityInstance.getStartTime()).endTime(activityInstance.getEndTime())
                        .durationInMilliseconds(activityInstance.getDurationInMillis())
                        .processInstanceId(activityInstance.getProcessInstanceId()).build());

            }
            return activityInstanceDetails;
        }
        LOGGER.error("Unable to find activity intance detail for process instance id: {}", processInstanceId);
        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstanceVariableDetail> getProcessInstanceVariable(final String processInstanceId) {
        final String url = urlProvider.getProcessInstanceVariablesUrl(processInstanceId);
        final Optional<ProcessInstanceVariable[]> response =
                httpRestServiceProvider.getHttpResponse(url, ProcessInstanceVariable[].class);
        if (response.isPresent()) {
            final ProcessInstanceVariable[] instanceVariables = response.get();
            final List<ProcessInstanceVariableDetail> instanceVariableDetails =
                    new ArrayList<>(instanceVariables.length);
            for (int index = 0; index < instanceVariables.length; index++) {
                final ProcessInstanceVariable processInstanceVariable = instanceVariables[index];
                final ProcessInstanceVariableDetail instanceVariableDetail =
                        new ProcessInstanceVariableDetail(processInstanceVariable.getName(),
                                processInstanceVariable.getValue(), processInstanceVariable.getType());
                instanceVariableDetails.add(instanceVariableDetail);
            }
            return instanceVariableDetails;
        }
        LOGGER.error("Unable to find process intance variable details for process instance id: {}", processInstanceId);
        return Collections.emptyList();
    }

}
