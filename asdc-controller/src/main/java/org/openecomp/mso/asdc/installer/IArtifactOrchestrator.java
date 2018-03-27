/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.asdc.installer;



import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;

public interface IArtifactOrchestrator {

	void installTheArtifact (INotificationData iNotif, IResourceInstance resource, IArtifactInfo artifact, IDistributionClientDownloadResult downloadResult) throws ArtifactInstallerException;

	boolean isArtifactAlreadyDeployed (INotificationData iNotif,IResourceInstance resource,IArtifactInfo artifact) throws ArtifactInstallerException;

}
