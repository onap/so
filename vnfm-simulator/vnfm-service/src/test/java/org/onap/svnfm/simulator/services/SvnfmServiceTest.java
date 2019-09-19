/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

package org.onap.svnfm.simulator.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.model.VnfInstance;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.repository.VnfOperationRepository;
import org.onap.svnfm.simulator.repository.VnfmRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;

public class SvnfmServiceTest {

    private static final String VNFD_ID = "vnfdId";
    private static final String VNF_INSTANCE_NAME = "vnfInstanceName";
    private static final String VNF_INSTANCE_ID = "vnfInstanceId";
    private static final String OPERATION_ID = "operationId";
    private SvnfmService testedObject;
    private CacheManager cacheManagerMock;
    private VnfmHelper vnfmHelperMock;
    private VnfmRepository vnfmRepositoryMock;
    private VnfOperationRepository vnfOperationRepositoryMock;

    @Before
    public void setup() {
        vnfmRepositoryMock = mock(VnfmRepository.class);
        vnfOperationRepositoryMock = mock(VnfOperationRepository.class);
        vnfmHelperMock = mock(VnfmHelper.class);
        ApplicationConfig applicationConfigMock = mock(ApplicationConfig.class);
        cacheManagerMock = mock(CacheManager.class);
        Vnfds vnfdsMock = mock(Vnfds.class);
        SubscriptionService subscriptionServiceMock = mock(SubscriptionService.class);

        testedObject = new SvnfmService(vnfmRepositoryMock, vnfOperationRepositoryMock, vnfmHelperMock,
                applicationConfigMock, cacheManagerMock, vnfdsMock, subscriptionServiceMock);
    }

    @Test
    public void getVnfSuccessful() {
        // given
        Cache cacheMock = mock(Cache.class);
        SimpleValueWrapper simpleValueWrapperMock = mock(SimpleValueWrapper.class);
        when(cacheManagerMock.getCache(Constant.IN_LINE_RESPONSE_201_CACHE)).thenReturn(cacheMock);
        when(cacheMock.get(VNF_INSTANCE_ID)).thenReturn(simpleValueWrapperMock);
        when(simpleValueWrapperMock.get()).thenReturn(getInlineResponse(VNFD_ID, VNF_INSTANCE_NAME));
        // when
        InlineResponse201 result = testedObject.getVnf(VNF_INSTANCE_ID);
        // then
        assertThat(result.getVnfdId()).isEqualTo(VNFD_ID);
        assertThat(result.getVnfInstanceName()).isEqualTo(VNF_INSTANCE_NAME);
    }

    @Test
    public void getVnf_ifCacheNullThenReturnNull() {
        // given
        when(cacheManagerMock.getCache(Constant.IN_LINE_RESPONSE_201_CACHE)).thenReturn(null);
        // then
        assertThat(testedObject.getVnf("any")).isNull();
    }

    @Test
    public void getVnf_ifWrapperNullThenReturnNull() {
        // given
        Cache cacheMock = mock(Cache.class);
        when(cacheManagerMock.getCache(Constant.IN_LINE_RESPONSE_201_CACHE)).thenReturn(cacheMock);
        when(cacheMock.get(VNF_INSTANCE_ID)).thenReturn(null);
        // then
        assertThat(testedObject.getVnf(VNF_INSTANCE_ID)).isNull();
    }

    @Test
    public void getVnf_ifResultIsNullThenReturnNull() {
        // when
        Cache cacheMock = mock(Cache.class);
        SimpleValueWrapper simpleValueWrapperMock = mock(SimpleValueWrapper.class);
        when(cacheManagerMock.getCache(Constant.IN_LINE_RESPONSE_201_CACHE)).thenReturn(cacheMock);
        when(cacheMock.get(VNF_INSTANCE_ID)).thenReturn(simpleValueWrapperMock);
        when(simpleValueWrapperMock.get()).thenReturn(null);
        // then
        assertThat(testedObject.getVnf(VNF_INSTANCE_ID)).isNull();
    }

    @Test
    public void test_createVnf_usingValidCreateVnfRequest_vnfInstanceCreatedSuccessfully() throws Exception {
        // given
        final CreateVnfRequest createVnfRequest = getCreateVnfRequest();
        final VnfInstance vnfInstance = getVnfInstance();
        final InlineResponse201 inlineResponse201 = getInlineResponse(VNFD_ID, VNF_INSTANCE_NAME);
        when(vnfmHelperMock.createVnfInstance(createVnfRequest, VNF_INSTANCE_ID)).thenReturn(vnfInstance);
        when(vnfmRepositoryMock.save(vnfInstance)).thenReturn(vnfInstance);
        when(vnfmHelperMock.getInlineResponse201(vnfInstance)).thenReturn(inlineResponse201);
        // when
        final InlineResponse201 result = testedObject.createVnf(createVnfRequest, VNF_INSTANCE_ID);
        // then
        assertThat(result.getVnfdId()).isEqualTo(VNFD_ID);
        assertThat(result.getVnfInstanceName()).isEqualTo(VNF_INSTANCE_NAME);
    }

    @Test
    public void test_createVnf_illegalAccessExceptionThrown_returnsNull() throws Exception {
        // given
        final CreateVnfRequest createVnfRequest = getCreateVnfRequest();
        final VnfInstance vnfInstance = getVnfInstance();
        when(vnfmHelperMock.createVnfInstance(createVnfRequest, VNF_INSTANCE_ID)).thenReturn(vnfInstance);
        when(vnfmRepositoryMock.save(vnfInstance)).thenReturn(vnfInstance);
        when(vnfmHelperMock.getInlineResponse201(vnfInstance)).thenThrow(new IllegalAccessException());
        // when
        final InlineResponse201 result = testedObject.createVnf(createVnfRequest, VNF_INSTANCE_ID);
        // then
        assertNull(result);
    }

    @Test
    public void test_getOperationStatus_usingValidOperationId_operationStatusRetrievedSuccessfully() {
        // given
        final OperationEnum operationEnum = OperationEnum.OPERATE;
        final VnfOperation vnfOperation = getVnfOperation(OPERATION_ID, operationEnum);
        final List<VnfOperation> vnfOperations = new ArrayList<>();
        vnfOperations.add(vnfOperation);
        when(vnfOperationRepositoryMock.findAll()).thenReturn(vnfOperations);
        // when
        final InlineResponse200 result = testedObject.getOperationStatus(OPERATION_ID);
        // then
        assertThat(result.getId()).isEqualTo(OPERATION_ID);
        assertThat(result.getOperation()).isEqualTo(operationEnum);
    }

    @Test
    public void test_getOperationStatus_usingInvalidOperationId_returnsNull() {
        // given
        final OperationEnum operationEnum = OperationEnum.OPERATE;
        final VnfOperation vnfOperation = getVnfOperation(OPERATION_ID, operationEnum);
        final List<VnfOperation> vnfOperations = new ArrayList<>();
        vnfOperations.add(vnfOperation);
        when(vnfOperationRepositoryMock.findAll()).thenReturn(vnfOperations);
        // when
        InlineResponse200 result = testedObject.getOperationStatus("invalidOperationId");
        // then
        assertNull(result);
    }

    private InlineResponse201 getInlineResponse(String vnfdId, String vnfInstanceName) {
        InlineResponse201 response201 = new InlineResponse201();
        response201.setVnfdId(vnfdId);
        response201.vnfInstanceName(vnfInstanceName);
        return response201;
    }

    private CreateVnfRequest getCreateVnfRequest() {
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(VNFD_ID);
        createVnfRequest.setVnfInstanceName(VNF_INSTANCE_NAME);
        return createVnfRequest;
    }

    private VnfInstance getVnfInstance() {
        final VnfInstance vnfInstance = new VnfInstance();
        vnfInstance.setVnfdId(VNFD_ID);
        vnfInstance.setVnfInstanceName(VNF_INSTANCE_NAME);
        return vnfInstance;
    }

    private VnfOperation getVnfOperation(String operationId, OperationEnum operationEnum) {
        final VnfOperation vnfOperation = new VnfOperation();
        vnfOperation.setId(operationId);
        vnfOperation.setOperation(operationEnum);
        return vnfOperation;
    }
}
