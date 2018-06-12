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

package org.openecomp.mso.openstack.utils;

import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.cloud.ServerType;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MsoTenantUtilsFactory {

	private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, MsoTenantUtilsFactory.class);
	@Autowired
	private CloudConfig cloudConfig;
	@Autowired
	private MsoKeystoneUtils keystoneUtils;
	@Autowired
	private MsoOrmUtils ormUtils;
	
	// based on Cloud IdentityServerType returns ORM or KEYSTONE Utils
	public MsoTenantUtils getTenantUtils(String cloudSiteId) throws MsoCloudSiteNotFound {
		CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
				() -> new MsoCloudSiteNotFound(cloudSiteId));

		return getTenantUtilsByServerType(cloudConfig.getIdentityService(cloudSite.getIdentityServiceId()).getIdentityServerType());
	}

	public MsoTenantUtils getTenantUtilsByServerType(ServerType serverType) {

		MsoTenantUtils tenantU = null;
		if (ServerType.KEYSTONE.equals(serverType)) {
			tenantU = keystoneUtils;
		} else if (ServerType.ORM.equals(serverType)) {
			tenantU = ormUtils;
		}
		return tenantU;
	}
}
