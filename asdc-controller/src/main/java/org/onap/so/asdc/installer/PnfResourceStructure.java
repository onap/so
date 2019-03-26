/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.installer;

import java.io.UnsupportedEncodingException;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;

/**
 * This class represents the PNF resource structure.
 */
public class PnfResourceStructure extends ResourceStructure {

    public PnfResourceStructure(INotificationData notificationData, IResourceInstance resourceInstance) {
        super(notificationData, resourceInstance);
        this.resourceType = ResourceType.PNF_RESOURCE;
    }

    @Override
    public void addArtifactToStructure(IDistributionClient distributionClient, IArtifactInfo artifactinfo,
        IDistributionClientDownloadResult clientResult) throws UnsupportedEncodingException {

    }

    @Override
    public void prepareInstall() throws ArtifactInstallerException {

    }
}
