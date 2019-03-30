/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.exceptions.RequiredExecutionVariableExeception;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;

/**
 *
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 *
 */
public class StubbedBuildingBlockExecution implements BuildingBlockExecution {

  private static final String CLOUD_OWNER = "CLOUD_OWNER";
  private static final String LCP_CLOUD_REGIONID = "RegionOnce";
  private static final String TENANT_ID = UUID.randomUUID().toString();
  private final Map<String, Serializable> execution = new HashMap<>();
  private final GeneralBuildingBlock generalBuildingBlock;

  StubbedBuildingBlockExecution() {
    generalBuildingBlock = getGeneralBuildingBlockValue();
  }

  @Override
  public GeneralBuildingBlock getGeneralBuildingBlock() {
    return generalBuildingBlock;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getVariable(final String key) {
    return (T) execution.get(key);
  }

  @Override
  public <T> T getRequiredVariable(final String key) throws RequiredExecutionVariableExeception {
    return null;
  }

  @Override
  public void setVariable(final String key, final Serializable value) {
    execution.put(key, value);
  }

  @Override
  public Map<ResourceKey, String> getLookupMap() {
    return Collections.emptyMap();
  }

  @Override
  public String getFlowToBeCalled() {
    return null;
  }

  public static String getTenantId() {
    return TENANT_ID;
  }

  private GeneralBuildingBlock getGeneralBuildingBlockValue() {
    final GeneralBuildingBlock buildingBlock = new GeneralBuildingBlock();
    buildingBlock.setCloudRegion(getCloudRegion());
    return buildingBlock;
  }

  private CloudRegion getCloudRegion() {
    final CloudRegion cloudRegion = new CloudRegion();
    cloudRegion.setCloudOwner(CLOUD_OWNER);
    cloudRegion.setLcpCloudRegionId(LCP_CLOUD_REGIONID);
    cloudRegion.setTenantId(TENANT_ID);
    return cloudRegion;
  }

}
