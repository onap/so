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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.exceptions.RequiredExecutionVariableExeception;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.vnfmadapter.v1.model.DeleteVnfResponse;
import com.google.common.base.Optional;

/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 *
 */
public class EtsiVnfDeleteTaskTest extends BaseTaskTest {

  private static final String MODEL_INSTANCE_NAME = "MODEL_INSTANCE_NAME";

  private static final String CLOUD_OWNER = "CLOUD_OWNER";

  private static final String LCP_CLOUD_REGIONID = "RegionOnce";

  private static final String TENANT_ID = UUID.randomUUID().toString();

  private static final String VNF_ID = UUID.randomUUID().toString();

  private static final String VNF_NAME = "VNF_NAME";

  private static final String JOB_ID = UUID.randomUUID().toString();

  @Mock
  private VnfmAdapterServiceProvider mockedVnfmAdapterServiceProvider;

  @Mock
  private GeneralBuildingBlock buildingBlock;

  @Mock
  private RequestContext requestContext;

  private final BuildingBlockExecution stubbedxecution = new StubbedBuildingBlockExecution();

  @Test
  public void testInvokeVnfmAdapter() throws Exception {
    final EtsiVnfDeleteTask objUnderTest = getEtsiVnfDeleteTask();
    when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(getGenericVnf());
    when(mockedVnfmAdapterServiceProvider.invokeDeleteRequest(eq(VNF_ID))).thenReturn(getDeleteVnfResponse());
    objUnderTest.invokeVnfmAdapter(stubbedxecution);
    assertNotNull(stubbedxecution.getVariable(Constants.DELETE_VNF_RESPONSE_PARAM_NAME));
  }

  @Test
  public void testInvokeVnfmAdapterException() throws Exception {
    final EtsiVnfDeleteTask objUnderTest = getEtsiVnfDeleteTask();
    when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(getGenericVnf());
    when(mockedVnfmAdapterServiceProvider.invokeDeleteRequest(eq(VNF_ID))).thenReturn(Optional.absent());
    objUnderTest.invokeVnfmAdapter(stubbedxecution);
    assertNull(stubbedxecution.getVariable(Constants.DELETE_VNF_RESPONSE_PARAM_NAME));
    verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1213),
        any(Exception.class));
  }

  @Test
  public void testCheckVnfmFlag() throws Exception {
    final EtsiVnfDeleteTask objUnderTest = getEtsiVnfDeleteTask();
    when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(getGenericVnf());
    objUnderTest.checkVnfmFlag(stubbedxecution);
    assertTrue(stubbedxecution.getVariable(Constants.VNFM_FLAG_PARAM_NAME));
  }

  @Test
  public void testCheckVnfmFlagException() throws Exception {
    final EtsiVnfDeleteTask objUnderTest = getEtsiVnfDeleteTask();
    when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenThrow(RuntimeException.class);
    objUnderTest.checkVnfmFlag(stubbedxecution);
    final String actual = stubbedxecution.getVariable(Constants.VNFM_FLAG_PARAM_NAME);
    assertNull(actual);
    verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1210),
        any(Exception.class));
  }

  private Optional<DeleteVnfResponse> getDeleteVnfResponse() {
    final DeleteVnfResponse response = new DeleteVnfResponse();
    response.setJobId(JOB_ID);
    return Optional.of(response);
  }

  private GenericVnf getGenericVnf() {
    final GenericVnf genericVnf = new GenericVnf();
    genericVnf.setVnfId(VNF_ID);
    genericVnf.setModelInfoGenericVnf(getModelInfoGenericVnf());
    genericVnf.setVnfName(VNF_NAME);
    genericVnf.setSelflink("https://localhost:8080");
    return genericVnf;
  }

  private ModelInfoGenericVnf getModelInfoGenericVnf() {
    final ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
    modelInfoGenericVnf.setModelInstanceName(MODEL_INSTANCE_NAME);
    return modelInfoGenericVnf;
  }

  private EtsiVnfDeleteTask getEtsiVnfDeleteTask() {
    return new EtsiVnfDeleteTask(exceptionUtil, extractPojosForBB, mockedVnfmAdapterServiceProvider);
  }

  private class StubbedBuildingBlockExecution implements BuildingBlockExecution {

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

}
