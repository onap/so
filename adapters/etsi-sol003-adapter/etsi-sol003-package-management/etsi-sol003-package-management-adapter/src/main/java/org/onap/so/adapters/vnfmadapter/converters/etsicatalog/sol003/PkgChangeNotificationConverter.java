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

package org.onap.so.adapters.vnfmadapter.converters.etsicatalog.sol003;

import static org.slf4j.LoggerFactory.getLogger;
import org.onap.so.adapters.vnfmadapter.etsicatalog.notification.model.PkgChangeNotification;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.notification.model.VnfPackageChangeNotification;
import org.slf4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

/**
 * Converter to convert from an Etsi Catalog Manager {@link PkgChangeNotification} Object to its equivalent SOL003
 * {@link VnfPackageChangeNotification} Object
 *
 * @author andrew.a.lamb@est.tech
 */
@Service
public class PkgChangeNotificationConverter extends AbstractPkgNotificationConverter
        implements Converter<PkgChangeNotification, VnfPackageChangeNotification> {
    private static final Logger logger = getLogger(PkgChangeNotificationConverter.class);

    /**
     * Convert a {@link PkgChangeNotification} Object to an {@link VnfPackageChangeNotification} Object
     * 
     * @param pkgChangeNotification The PkgChangeNotification Object to Convert
     * @return The Converted VnfPackageChangeNotification Object
     */
    @Override
    public VnfPackageChangeNotification convert(final PkgChangeNotification pkgChangeNotification) {
        logger.info("Converting PkgChangeNotification\n{}", pkgChangeNotification.toString());
        final VnfPackageChangeNotification vnfPackageChangeNotification = new VnfPackageChangeNotification();
        vnfPackageChangeNotification.setId(pkgChangeNotification.getId());

        if (pkgChangeNotification.getNotificationType() != null) {
            vnfPackageChangeNotification.setNotificationType(VnfPackageChangeNotification.NotificationTypeEnum
                    .fromValue(pkgChangeNotification.getNotificationType().getValue()));
        }

        vnfPackageChangeNotification.setSubscriptionId(pkgChangeNotification.getSubscriptionId());
        vnfPackageChangeNotification.setTimeStamp(pkgChangeNotification.getTimeStamp());
        vnfPackageChangeNotification.setVnfPkgId(pkgChangeNotification.getVnfPkgId());

        vnfPackageChangeNotification.setVnfdId(pkgChangeNotification.getVnfdId());

        if (pkgChangeNotification.getChangeType() != null) {
            vnfPackageChangeNotification.setChangeType(VnfPackageChangeNotification.ChangeTypeEnum
                    .fromValue(pkgChangeNotification.getChangeType().getValue()));
        }

        if (pkgChangeNotification.getOperationalState() != null) {
            vnfPackageChangeNotification.setOperationalState(VnfPackageChangeNotification.OperationalStateEnum
                    .fromValue(pkgChangeNotification.getOperationalState().getValue()));
        }

        vnfPackageChangeNotification.setLinks(convert((pkgChangeNotification.getLinks())));

        return vnfPackageChangeNotification;
    }

}
