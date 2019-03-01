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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CloudSiteCatalogUtils {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CloudSiteCatalogUtils.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	
	@Autowired
	private CatalogDbClient catalogDbClient;
	
	
	public void getIdentityUrlFromCloudSite(DelegateExecution execution) {
		String cloudRegionId = (String) execution.getVariable("lcpCloudRegionId");
	
		if (cloudRegionId != null) {
			Optional<CloudSite> cloudSite = getCloudSite(cloudRegionId);
			if (!cloudSite.isPresent()) {
				msoLogger.debug("Cloud Region with cloudRegionId " + cloudRegionId + " not found in Catalog DB");
				exceptionUtil.buildAndThrowWorkflowException(execution, 404, "Cloud Region with cloudRegionId " + cloudRegionId + " not found in Catalog DB");
			}
			
			if (cloudSite.get().getIdentityService() == null)	 {
				msoLogger.debug("No identityService found for Cloud Region with cloudRegionId " + cloudRegionId + " in Catalog DB");
				exceptionUtil.buildAndThrowWorkflowException(execution, 404, "No identityService found for Cloud Region with cloudRegionId " + cloudRegionId + " in Catalog DB");
			}			
			String identityUrl = cloudSite.get().getIdentityService().getIdentityUrl();
			
			msoLogger.debug("identityUrl from Catalog DB is: " + identityUrl);
			execution.setVariable("identityUrl", identityUrl);
		}
	}
	
	protected Optional<CloudSite> getCloudSite(String id) {
		if (id == null) {
			return Optional.empty();
		}
		CloudSite cloudSite = catalogDbClient.getCloudSite(id);

		if (cloudSite != null) {
			return Optional.of(cloudSite);
		} else {
			return(Optional.of(catalogDbClient.getCloudSiteByClliAndAicVersion(id,"2.5")));			
		}
	}
}