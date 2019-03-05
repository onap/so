/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.audit;

import java.security.GeneralSecurityException;

import javax.annotation.PostConstruct;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AuditStackService {

	private static final Logger logger = LoggerFactory.getLogger(AuditStackService.class);

	@Autowired
	public Environment env;

	@Autowired
	private AuditCreateStackService auditCreateStack;
	
	@Autowired
	private AuditDeleteStackService auditDeleteStack;

	@PostConstruct
	public void auditAddAAIInventory() {
		String auth = "";
		try {
			auth = CryptoUtils.decrypt(env.getRequiredProperty("mso.auth"), env.getRequiredProperty("mso.msoKey"));
		} catch (IllegalStateException | GeneralSecurityException e) {
			logger.error("Error Decrypting Password", e);
		}
		ClientRequestInterceptor interceptor = new BasicAuthProvider(env.getRequiredProperty("mso.config.cadi.aafId"),
				auth);
		ExternalTaskClient client = ExternalTaskClient.create()
				.baseUrl(env.getRequiredProperty("mso.workflow.endpoint")).maxTasks(1).addInterceptor(interceptor)
				.asyncResponseTimeout(120000).build();
		client.subscribe("InventoryAddAudit").lockDuration(60000)
				.handler(auditCreateStack::executeExternalTask).open();
	}
	
	@PostConstruct
	public void auditDeleteAAIInventory() {
		String auth = "";
		try {
			auth = CryptoUtils.decrypt(env.getRequiredProperty("mso.auth"), env.getRequiredProperty("mso.msoKey"));
		} catch (IllegalStateException | GeneralSecurityException e) {
			logger.error("Error Decrypting Password", e);
		}
		ClientRequestInterceptor interceptor = new BasicAuthProvider(env.getRequiredProperty("mso.config.cadi.aafId"),
				auth);
		ExternalTaskClient client = ExternalTaskClient.create()
				.baseUrl(env.getRequiredProperty("mso.workflow.endpoint")).maxTasks(1).addInterceptor(interceptor)
				.asyncResponseTimeout(120000).build();
		client.subscribe("InventoryDeleteAudit").lockDuration(60000)
				.handler(auditDeleteStack::executeExternalTask).open();
	}

}
