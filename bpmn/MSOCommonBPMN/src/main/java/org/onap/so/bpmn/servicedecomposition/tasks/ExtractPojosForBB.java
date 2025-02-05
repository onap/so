/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * Modifications Copyright (c) 2019 Nokia
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

package org.onap.so.bpmn.servicedecomposition.tasks;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.Id;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.common.base.CaseFormat;

@Component
public class ExtractPojosForBB {

    private static final Logger logger = LoggerFactory.getLogger(ExtractPojosForBB.class);

    public <T> T extractByKey(BuildingBlockExecution execution, ResourceKey key) throws BBObjectNotFoundException {
        Optional<T> result = Optional.empty();
        GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
        String value = execution.getLookupMap().get(key);
        try {
            ServiceInstance serviceInstance;
            GenericVnf vnf;
            switch (key) {
                case SERVICE_INSTANCE_ID:
                    result = getServiceInstance(gBBInput, value);
                    break;
                case GENERIC_VNF_ID:
                    serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    result = lookupObjectInList(serviceInstance.getVnfs(), value);
                    break;
                case PNF:
                    serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    result = lookupObjectInList(serviceInstance.getPnfs(), value);
                    break;
                case NETWORK_ID:
                    serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    result = lookupObjectInList(serviceInstance.getNetworks(), value);
                    break;
                case VOLUME_GROUP_ID:
                    vnf = extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
                    result = lookupObjectInList(vnf.getVolumeGroups(), value);
                    break;
                case VF_MODULE_ID:
                    vnf = extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
                    result = lookupObjectInList(vnf.getVfModules(), value);
                    break;
                case ALLOTTED_RESOURCE_ID:
                    serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    result = lookupObjectInList(serviceInstance.getAllottedResources(), value);
                    break;
                case CONFIGURATION_ID:
                    serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    result = lookupObjectInList(serviceInstance.getConfigurations(), value);
                    break;
                case VPN_ID:
                    result = lookupObjectInList(gBBInput.getCustomer().getVpnBindings(), value);
                    break;
                case VPN_BONDING_LINK_ID:
                    serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    result = lookupObjectInList(serviceInstance.getVpnBondingLinks(), value);
                    break;
                case INSTANCE_GROUP_ID:
                    serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    result = lookupObjectInList(serviceInstance.getInstanceGroups(), value);
                    break;
            }
        } catch (Exception e) { // convert all other exceptions to object not found
            logger.warn(
                    "BBObjectNotFoundException in ExtractPojosForBB, BBObject {} was not found in gBBInput using reference value: {} {}",
                    key, value, e);
            throw new BBObjectNotFoundException(key, value);
        }
        return result.orElseThrow(() -> new BBObjectNotFoundException(key, value));
    }

    private <T> Optional<T> getServiceInstance(GeneralBuildingBlock gBBInput, String value) throws Exception {
        if (gBBInput.getCustomer().getServiceSubscription() == null && gBBInput.getServiceInstance() != null) {
            return Optional.of((T) gBBInput.getServiceInstance());
        } else if (gBBInput.getCustomer().getServiceSubscription() != null) {
            return lookupObjectInList(gBBInput.getCustomer().getServiceSubscription().getServiceInstances(), value);
        }
        return Optional.empty();
    }

    private <T> Optional<T> lookupObjectInList(List<?> list, String value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Optional<T> result = Optional.empty();
        for (Object obj : list) {
            result = findValue(obj, value);
            if (result.isPresent()) {
                break;
            }
        }
        return result;
    }

    private <T> Optional<T> findValue(Object obj, String value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                String fieldName = field.getName();
                fieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
                String fieldValue = (String) obj.getClass().getMethod("get" + fieldName).invoke(obj);
                if (fieldValue.equals(value)) {
                    return Optional.of((T) obj);
                }
            }
        }
        return Optional.empty();
    }
}
