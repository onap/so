/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.adapters.vnfmadapter;

import org.onap.so.adapters.vnfmadapter.common.VnfmAdapterUrlProvider;
import org.onap.so.adapters.vnfmadapter.converters.etsicatalog.sol003.PkgChangeNotificationConverter;
import org.onap.so.adapters.vnfmadapter.converters.etsicatalog.sol003.PkgOnboardingNotificationConverter;
import org.onap.so.adapters.vnfmadapter.converters.etsicatalog.sol003.VnfPkgInfoConverter;
import org.onap.so.adapters.vnfmadapter.converters.sol003.etsicatalog.PkgmSubscriptionRequestConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class ConversionServiceConfiguration {

    private final VnfmAdapterUrlProvider vnfmAdapterUrlProvider;

    @Autowired
    public ConversionServiceConfiguration(final VnfmAdapterUrlProvider vnfmAdapterUrlProvider) {
        this.vnfmAdapterUrlProvider = vnfmAdapterUrlProvider;
    }

    @Bean
    public ConversionService conversionService() {
        final DefaultConversionService service = new DefaultConversionService();
        service.addConverter(new VnfPkgInfoConverter(vnfmAdapterUrlProvider));
        service.addConverter(new PkgmSubscriptionRequestConverter());
        service.addConverter(new PkgChangeNotificationConverter());
        service.addConverter(new PkgOnboardingNotificationConverter());
        return service;
    }

}
