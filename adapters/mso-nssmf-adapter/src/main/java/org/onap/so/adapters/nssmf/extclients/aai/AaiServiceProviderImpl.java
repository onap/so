/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.nssmf.extclients.aai;


import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrThirdpartySdncList;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AaiServiceProviderImpl implements AaiServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(AaiServiceProviderImpl.class);

    private final AaiClientProvider aaiClientProvider;

    @Autowired
    public AaiServiceProviderImpl(final AaiClientProvider aaiClientProvider) {
        this.aaiClientProvider = aaiClientProvider;
    }

    @Override
    public EsrThirdpartySdncList invokeGetThirdPartySdncList() {
        return aaiClientProvider.getAaiClient()
                .get(EsrThirdpartySdncList.class, AAIUriFactory.createResourceUri(AAIObjectType.THIRDPARTY_SDNC_LIST))
                .orElseGet(() -> {
                    logger.debug("No VNFMs in AAI");
                    return null;
                });
    }

    @Override
    public EsrSystemInfoList invokeGetThirdPartySdncEsrSystemInfo(String sdncId) {
        return aaiClientProvider.getAaiClient()
                .get(EsrSystemInfoList.class,
                        AAIUriFactory.createResourceUri(AAIObjectType.THIRDPARTY_SDNC_SYSTEM_INFO_LIST, sdncId))
                .orElseGet(() -> {
                    logger.debug("VNFM not found in AAI");
                    return null;
                });
    }

}
