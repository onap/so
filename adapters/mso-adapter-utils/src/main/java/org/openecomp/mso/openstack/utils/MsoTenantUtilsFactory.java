/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.openstack.utils;

import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.openstack.utils.MsoKeystoneUtils;


public class MsoTenantUtilsFactory {

	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private CloudConfigFactory cloudConfigFactory= new CloudConfigFactory(); 
	protected CloudConfig cloudConfig;
	protected MsoJavaProperties msoProps = null;
	private String msoPropID;

	public MsoTenantUtilsFactory (String msoPropID) {
		this.msoPropID = msoPropID;
	}

	//based on Cloud IdentityServerType returns ORM or KEYSTONE Utils
	public MsoTenantUtils getTenantUtils(String cloudSiteId) {

		// Obtain the cloud site information 
		cloudConfig = cloudConfigFactory.getCloudConfig();
		CloudSite cloudSite = cloudConfig.getCloudSite (cloudSiteId);

		MsoTenantUtils tenantU = null;
		if (cloudSite.getIdentityService().getIdentityServerType() == CloudIdentity.IdentityServerType.KEYSTONE)
		{
			tenantU = new MsoKeystoneUtils (msoPropID);
		}
		return tenantU;
	}
}
