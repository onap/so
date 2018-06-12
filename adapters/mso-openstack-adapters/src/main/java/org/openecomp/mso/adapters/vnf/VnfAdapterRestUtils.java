/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
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

package org.openecomp.mso.adapters.vnf;

import java.util.Optional;

import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VnfAdapterRestUtils
{
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, VnfAdapterRestUtils.class);
	
	@Autowired
	private CloudConfig cloudConfig;
	
	@Autowired
	private MsoVnfCloudifyAdapterImpl cloudifyImpl;
	
	@Autowired
	private MsoVnfAdapterImpl vnfImpl;
	
	/*
	 * Choose which implementation of VNF Adapter to use, based on the orchestration mode.
	 * Currently, the two supported orchestrators are HEAT and CLOUDIFY.
	 */
	public MsoVnfAdapter getVnfAdapterImpl (String mode, String cloudSiteId)
	{
		// First, determine the orchestration mode to use.
		// If was explicitly provided as a parameter, use that.  Else if specified for the
		// cloudsite, use that.  Otherwise, the default is the (original) HEAT-based impl.

		LOGGER.debug ("Entered GetVnfAdapterImpl: mode=" + mode + ", cloudSite=" + cloudSiteId);

		if (mode == null) {
			// Didn't get an explicit mode type requested.
			// Use the CloudSite to determine which Impl to use, based on whether the target cloutSite
			// has a CloudifyManager assigned to it
			Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
			if (cloudSite.isPresent()) {
				LOGGER.debug("Got CloudSite: " + cloudSite.toString());
				if (cloudConfig.getCloudifyManager(cloudSite.get().getCloudifyId()) != null) {
					mode = "CLOUDIFY";
				} else {
					mode = "HEAT";
				}
			}
		}

		LOGGER.debug ("GetVnfAdapterImpl: mode=" + mode);

		MsoVnfAdapter vnfAdapter = null;
		
		// TODO:  Make this more dynamic (e.g. Service Loader)
		if ("CLOUDIFY".equalsIgnoreCase(mode)) {
			LOGGER.debug ("GetVnfAdapterImpl: Return Cloudify Adapter");
			vnfAdapter = cloudifyImpl;
		}
		else if ("HEAT".equalsIgnoreCase(mode)) {
			LOGGER.debug ("GetVnfAdapterImpl: Return Heat Adapter");
			vnfAdapter = vnfImpl;
		}
		else {
			// Don't expect this, but default is the HEAT adapter
			LOGGER.debug ("GetVnfAdapterImpl: Return Default (Heat) Adapter");
			vnfAdapter = vnfImpl;
		}
		
		return vnfAdapter;
	}

}
