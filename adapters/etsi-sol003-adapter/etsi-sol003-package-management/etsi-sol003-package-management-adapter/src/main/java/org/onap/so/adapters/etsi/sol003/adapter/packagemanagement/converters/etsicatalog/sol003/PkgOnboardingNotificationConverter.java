/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.converters.etsicatalog.sol003;

import static org.slf4j.LoggerFactory.getLogger;
import org.onap.so.adapters.etsi.sol003.adapter.etsicatalog.notification.model.PkgOnboardingNotification;
import org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.extclients.vnfm.notification.model.VnfPackageOnboardingNotification;
import org.slf4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

/**
 * Converter to convert from an Etsi Catalog Manager {@link PkgOnboardingNotification} Object to its equivalent SOL003
 * {@link VnfPackageOnboardingNotification} Object
 *
 * @author andrew.a.lamb@est.tech
 */
@Service
public class PkgOnboardingNotificationConverter extends AbstractPkgNotificationConverter
        implements Converter<PkgOnboardingNotification, VnfPackageOnboardingNotification> {
    private static final Logger logger = getLogger(PkgOnboardingNotificationConverter.class);

    /**
     * Convert a {@link PkgOnboardingNotification} Object to an {@link VnfPackageOnboardingNotification} Object
     * 
     * @param pkgOnboardingNotification The PkgOnboardingNotification Object to Convert
     * @return The Converted VnfPackageOnboardingNotification Object
     */
    @Override
    public VnfPackageOnboardingNotification convert(final PkgOnboardingNotification pkgOnboardingNotification) {
        logger.info("Converting PkgChangeNotification\n{}", pkgOnboardingNotification.toString());
        final VnfPackageOnboardingNotification vnfPackageOnboardingNotification =
                new VnfPackageOnboardingNotification();
        vnfPackageOnboardingNotification.setId(pkgOnboardingNotification.getId());

        if (pkgOnboardingNotification.getNotificationType() != null) {
            vnfPackageOnboardingNotification.setNotificationType(VnfPackageOnboardingNotification.NotificationTypeEnum
                    .fromValue(pkgOnboardingNotification.getNotificationType().getValue()));
        }

        vnfPackageOnboardingNotification.setSubscriptionId(pkgOnboardingNotification.getSubscriptionId());
        vnfPackageOnboardingNotification.setTimeStamp(pkgOnboardingNotification.getTimeStamp());
        vnfPackageOnboardingNotification.setVnfPkgId(pkgOnboardingNotification.getVnfPkgId());
        vnfPackageOnboardingNotification.setVnfdId(pkgOnboardingNotification.getVnfdId());

        vnfPackageOnboardingNotification.setLinks(convert((pkgOnboardingNotification.getLinks())));

        return vnfPackageOnboardingNotification;
    }

}
