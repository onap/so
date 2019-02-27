/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import javax.persistence.Id;

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
	
	public <T> T extractByKey(BuildingBlockExecution execution, ResourceKey key, String value)
			throws BBObjectNotFoundException {

		Optional<T> result = Optional.empty();
		GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
		try {
			ServiceInstance serviceInstance;
			GenericVnf vnf;
			switch (key) {
				case SERVICE_INSTANCE_ID:
					result = lookupObjectInList(gBBInput.getCustomer().getServiceSubscription().getServiceInstances(), value);
					break;
				case GENERIC_VNF_ID:
					serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
					result = lookupObjectInList(serviceInstance.getVnfs(), value);
					break;
				case NETWORK_ID:
					serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
					result = lookupObjectInList(serviceInstance.getNetworks(), value);
					break;
				case VOLUME_GROUP_ID:
					vnf = extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
					result = lookupObjectInList(vnf.getVolumeGroups(), value);
					break;
				case VF_MODULE_ID:
					vnf = extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
					result = lookupObjectInList(vnf.getVfModules(), value);
					break;
				case ALLOTTED_RESOURCE_ID:
					serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
					result = lookupObjectInList(serviceInstance.getAllottedResources(), value);
					break;
				case CONFIGURATION_ID:
					serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
					result =  lookupObjectInList(serviceInstance.getConfigurations(), value);
					break;
				case VPN_ID:
					serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
					result = lookupObjectInList(gBBInput.getCustomer().getVpnBindings(), value);
					break;
				case VPN_BONDING_LINK_ID:
					serviceInstance = extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
                    result = lookupObjectInList(serviceInstance.getVpnBondingLinks(),value);
					break;
				default:
					throw new BBObjectNotFoundException(key, value);
			}
		} catch (BBObjectNotFoundException e) { // re-throw parent object not found
			throw e;
		} catch (Exception e) { // convert all other exceptions to object not found
			logger.warn("BBObjectNotFoundException in ExtractPojosForBB", "BBObject " + key + " was not found in "
				+ "gBBInput using reference value: " + value);
			throw new BBObjectNotFoundException(key, value);
		}
		
		if (result.isPresent()) {
			return result.get();
		} else {
			throw new BBObjectNotFoundException(key, value);
		}
	}
	
	protected <T> Optional<T> lookupObject(Object obj, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return findValue(obj, value);
	}

	protected <T> Optional<T> lookupObjectInList(List<?> list, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Optional<T> result = Optional.empty();
		for (Object obj : list) {
			result = findValue(obj, value);
			if (result.isPresent()) {
				break;
			}
		}
		return result;
		
	}
	
	protected <T> Optional<T> findValue(Object obj, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				String fieldName = field.getName();
				fieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
				String fieldValue = (String) obj.getClass().getMethod("get" + fieldName).invoke(obj);
				if (fieldValue.equals(value)) {
					return Optional.of((T)obj);
				}
			}
		}
		
		return Optional.empty();
	}
}
