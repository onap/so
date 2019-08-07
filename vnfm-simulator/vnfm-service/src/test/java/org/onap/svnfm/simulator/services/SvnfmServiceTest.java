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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.repository.VnfOperationRepository;
import org.onap.svnfm.simulator.repository.VnfmRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;

public class SvnfmServiceTest {

    private SvnfmService testedObject;
    private CacheManager cacheManagerMock;

    @Before
    public void setup() {
        VnfmRepository vnfmRepositoryMock = mock(VnfmRepository.class);
        VnfOperationRepository vnfOperationRepositoryMock = mock(VnfOperationRepository.class);
        VnfmHelper vnfmHelperMock = mock(VnfmHelper.class);
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
        String vnfId = "vnfId";
        String vnfInstanceName = "testVnf";
        Cache cacheMock = mock(Cache.class);
        SimpleValueWrapper simpleValueWrapperMock = mock(SimpleValueWrapper.class);
        when(cacheManagerMock.getCache(Constant.IN_LINE_RESPONSE_201_CACHE)).thenReturn(cacheMock);
        when(cacheMock.get(vnfId)).thenReturn(simpleValueWrapperMock);
        when(simpleValueWrapperMock.get()).thenReturn(prepareInlineResponse(vnfId, vnfInstanceName));
        // when
        InlineResponse201 result = testedObject.getVnf(vnfId);
        // then
        assertThat(result.getVnfdId()).isEqualTo(vnfId);
        assertThat(result.getVnfInstanceName()).isEqualTo(vnfInstanceName);
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
        String vnfId = "vnfId";
        Cache cacheMock = mock(Cache.class);
        when(cacheManagerMock.getCache(Constant.IN_LINE_RESPONSE_201_CACHE)).thenReturn(cacheMock);
        when(cacheMock.get(vnfId)).thenReturn(null);
        // then
        assertThat(testedObject.getVnf(vnfId)).isNull();
    }

    @Test
    public void getVnf_ifResultIsNullThenReturnNull() {
        // when
        String vnfId = "vnfId";
        Cache cacheMock = mock(Cache.class);
        SimpleValueWrapper simpleValueWrapperMock = mock(SimpleValueWrapper.class);
        when(cacheManagerMock.getCache(Constant.IN_LINE_RESPONSE_201_CACHE)).thenReturn(cacheMock);
        when(cacheMock.get(vnfId)).thenReturn(simpleValueWrapperMock);
        when(simpleValueWrapperMock.get()).thenReturn(null);
        // then
        assertThat(testedObject.getVnf(vnfId)).isNull();
    }

    private InlineResponse201 prepareInlineResponse(String vnfId, String vnfInstanceName) {
        InlineResponse201 response201 = new InlineResponse201();
        response201.setVnfdId(vnfId);
        response201.vnfInstanceName(vnfInstanceName);
        return response201;
    }
}
