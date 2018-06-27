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

import java.util.UUID;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.openecomp.mso.adapters.vnf.vmfm.MsoVnfmAdapterImpl;
import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.AAIVersion;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.springframework.util.StringUtils;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Responsible for selecting a VNF adapter for a VNF
 */
public class VnfAdapterRestUtils
{
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private static CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
	private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

	/**
	 * Collection of supported VNF adapter operation modes
	 */
	private enum SupportedAdapters {
		CLOUDIFY{
			@Override
			MsoVnfAdapter getAdapter() {
				return new MsoVnfCloudifyAdapterImpl (msoPropertiesFactory, cloudConfigFactory);
			}
		},
		HEAT{
			@Override
			MsoVnfAdapter getAdapter() {
				return new MsoVnfAdapterImpl (msoPropertiesFactory, cloudConfigFactory);
			}
		},
		VNFM{
			@Override
			MsoVnfAdapter getAdapter() {
				return new MsoVnfmAdapterImpl();
			}
		},
		PLUGIN {
			@Override
			MsoVnfAdapter getAdapter() {
				return new MsoVnfPluginAdapterImpl (msoPropertiesFactory, cloudConfigFactory);
			}
		};
		abstract MsoVnfAdapter getAdapter();
	}

	/**
	 * Choose which implementation of VNF Adapter to use, based on the orchestration mode.
	 * Currently, the tree supported orchestrators are HEAT, CLOUDIFY and VNFM.
	 *
	 * @param mode the requested mode of the VNF adapter (can be empty)
	 * @param cloudSiteId the identifier of the cloud site
	 * @param vnfName the name of the generic VNF in A&AI
	 * @return the selected adapter
	 */
	@Deprecated//the name of the VNF is not necessary unique within A&AI
	public static MsoVnfAdapter getVnfAdapterImplByVnfName (String mode, String cloudSiteId, String vnfName, String requestId) {
		LOGGER.debug ("Entered GetVnfAdapterImpl: mode=" + mode + ", cloudSite=" + cloudSiteId);
		SupportedAdapters selectedMode = getSelectedAdapter(mode, cloudSiteId, getVnfByName(vnfName, requestId));
		LOGGER.debug ("GetVnfAdapterImpl: mode=" + selectedMode);
		return selectedMode.getAdapter();
	}

	/**
	 * Choose which implementation of VNF Adapter to use, based on the orchestration mode.
	 * Currently, the tree supported orchestrators are HEAT, CLOUDIFY and VNFM.
	 *
	 * @param mode the requested mode of the VNF adapter (can be empty)
	 * @param cloudSiteId the identifier of the cloud site
	 * @param vnfId the identifier of the generic VNF in A&AI
	 * @return the selected adapter
	 */
	public static MsoVnfAdapter getVnfAdapterImplByVnfId (String mode, String cloudSiteId, String vnfId, String requestId) {
		LOGGER.debug ("Entered GetVnfAdapterImpl: mode=" + mode + ", cloudSite=" + cloudSiteId);
		SupportedAdapters selectedMode = getSelectedAdapter(mode, cloudSiteId, getVnfById(vnfId, requestId));
		LOGGER.debug ("GetVnfAdapterImpl: mode=" + selectedMode);
		return selectedMode.getAdapter();
	}

	private static SupportedAdapters getSelectedAdapter(String mode, String cloudSiteId, Optional<GenericVnf> genericVnf) {
		if (mode == null) {
			Optional<CloudSite> cloudSite = cloudConfigFactory.getCloudConfig().getCloudSite(cloudSiteId);
			//generic VNF is not present in case of the volume group
			if(genericVnf.isPresent()) {
				if (isNotEmpty(genericVnf.get().getNfType())) {
					return SupportedAdapters.VNFM;
				}
			}
			if (cloudSite.isPresent()) {
				LOGGER.debug("Got CloudSite: " + cloudSite.toString());
				if (cloudSite.get().getCloudifyManager() != null) {
					return SupportedAdapters.CLOUDIFY;
				} else {
					return SupportedAdapters.HEAT;
				}
			}
			return SupportedAdapters.PLUGIN;
		}
		else{
			return SupportedAdapters.valueOf(mode.toUpperCase());
		}
	}

	private static Optional<GenericVnf> getVnfById(String vnfId, String requestId) {
		if(StringUtils.isEmpty(vnfId)){
			return empty();
		}
		UUID requestUuid;
		try {
			requestUuid = UUID.fromString(requestId);
		} catch (IllegalArgumentException e) {
			LOGGER.debug("could not parse uuid: " + requestId + " creating valid uuid automatically");
			requestUuid = UUID.randomUUID();
		}
		GenericVnfs genericVnfs = new AAIResourcesClient(AAIVersion.V12, requestUuid).get(GenericVnfs.class,
				AAIUriFactory.createResourceUri(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-id", vnfId));
		if (genericVnfs.getGenericVnf().size() == 0) {
			throw new IndexOutOfBoundsException("No VNF exists with " + vnfId + " identifier");
		}
		return of(genericVnfs.getGenericVnf().get(0));
	}

	 public static Optional<GenericVnf> getVnfByName(String vnfName, String requestId) {
		if(StringUtils.isEmpty(vnfName)){
			return empty();
		}
		UUID requestUuid;
		try {
			requestUuid = UUID.fromString(requestId);
		} catch (IllegalArgumentException e) {
			LOGGER.debug("could not parse uuid: " + requestId + " creating valid uuid automatically");
			requestUuid = UUID.randomUUID();
		}
		GenericVnfs genericVnfs = new AAIResourcesClient(AAIVersion.V12, requestUuid).get(GenericVnfs.class,
				AAIUriFactory.createResourceUri(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName));
		if (genericVnfs.getGenericVnf().size() > 1) {
			throw new IndexOutOfBoundsException("Multiple Generic Vnfs Returned");
		}
		return of(genericVnfs.getGenericVnf().get(0));
	}

}
