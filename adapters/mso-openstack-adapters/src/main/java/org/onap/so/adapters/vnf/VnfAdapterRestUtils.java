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

package org.onap.so.adapters.vnf;

import java.util.Optional;

import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VnfAdapterRestUtils
{
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, VnfAdapterRestUtils.class);

	private static final String HEAT_MODE = "HEAT";
	private static final String CLOUDIFY_MODE = "CLOUDIFY";
	private static final String MULTICLOUD_MODE = "MULTICLOUD";

	@Autowired
	private CloudConfig cloudConfig;

	@Autowired
	private MsoVnfCloudifyAdapterImpl cloudifyImpl;

	@Autowired
	private MsoVnfAdapterImpl vnfImpl;

	@Autowired
	private MsoVnfPluginAdapterImpl vnfPluginImpl;

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
					mode = CLOUDIFY_MODE;
				} else if (MULTICLOUD_MODE.equalsIgnoreCase(cloudSite.get().getOrchestrator())) {
					mode = MULTICLOUD_MODE;
				}
				else {
					mode = HEAT_MODE;
				}
			}
		}

		LOGGER.debug ("GetVnfAdapterImpl: mode=" + mode);

		MsoVnfAdapter vnfAdapter = null;

		// TODO:  Make this more dynamic (e.g. Service Loader)
		if (CLOUDIFY_MODE.equalsIgnoreCase(mode)) {
			LOGGER.debug ("GetVnfAdapterImpl: Return Cloudify Adapter");
			vnfAdapter = cloudifyImpl;
		}
		else if (HEAT_MODE.equalsIgnoreCase(mode)) {
			LOGGER.debug ("GetVnfAdapterImpl: Return Heat Adapter");
			vnfAdapter = vnfImpl;
		}
		else if (MULTICLOUD_MODE.equalsIgnoreCase(mode)) {
			LOGGER.debug ("GetVnfAdapterImpl: Return Plugin (multicloud) Adapter");
			vnfAdapter = vnfPluginImpl;
		}
		else {
			// Don't expect this, but default is the HEAT adapter
			LOGGER.debug ("GetVnfAdapterImpl: Return Default (Heat) Adapter");
			vnfAdapter = vnfImpl;
		}

		return vnfAdapter;
	}

}
