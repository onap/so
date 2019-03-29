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

package org.onap.so.adapters.vnfmadapter.extclients.aai;

import com.google.common.base.Optional;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrVnfm;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrVnfmList;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrvnfmEsrsysteminfolist;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.GenericVnf;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AaiServiceProviderImpl implements AaiServiceProvider {

    private final AaiUrlProvider urlProvider;
    private final HttpRestServiceProvider httpServiceProvider;

    @Autowired
    public AaiServiceProviderImpl(final AaiUrlProvider urlProvider,
            @Qualifier("aaiServiceProvider") final HttpRestServiceProvider httpServiceProvider) {
        this.urlProvider = urlProvider;
        this.httpServiceProvider = httpServiceProvider;
    }

    @Override
    public Optional<GenericVnf> invokeGetGenericVnf(final String vnfId) {
        final String url = urlProvider.getGenericVnfUrl(vnfId);
        return httpServiceProvider.get(url, GenericVnf.class);
    }

    @Override
    public Optional<EsrVnfmList> invokeGetVnfms() {
        final String url = urlProvider.getVnfmsUrl();
        return httpServiceProvider.get(url, EsrVnfmList.class);
    }

    @Override
    public Optional<EsrVnfm> invokeGetVnfm(final String vnfmId) {
        final String url = urlProvider.getVnfmUrl(vnfmId);
        return httpServiceProvider.get(url, EsrVnfm.class);
    }

    @Override
    public Optional<EsrvnfmEsrsysteminfolist> invokeGetVnfmEsrSystemInfoList(final String vnfmId) {
        final String url = urlProvider.getVnfmEsrSystemInfoListUrl(vnfmId);
        return httpServiceProvider.get(url, EsrvnfmEsrsysteminfolist.class);
    }

    @Override
    public Optional<Void> invokePutGenericVnf(final GenericVnf vnf) {
        final String url = urlProvider.getGenericVnfUrl(vnf.getVnfId());
        return httpServiceProvider.put(vnf, url, Void.class);
    }

    @Override
    public <T> Optional<T> invokeGet(final String aaiResourcePath, final Class<T> clazz) {
        final String url = urlProvider.getResourceUrl(aaiResourcePath);
        return httpServiceProvider.get(url, clazz);
    }

}
