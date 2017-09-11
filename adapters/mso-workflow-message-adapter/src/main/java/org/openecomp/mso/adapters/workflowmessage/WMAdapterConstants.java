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
package org.openecomp.mso.adapters.workflowmessage;

public interface WMAdapterConstants {
	public static final String MSO_PROPERTIES_ID = "MSO_PROP_WORKFLOW_MESSAGE_ADAPTER";
	public static final String BPEL_URL_PROP = "org.openecomp.mso.adapters.workflow.message.bpelurl";

	// Once AAF enabled, the credential shall be get by triggering the CredentialConstants.getEncryptedPropValue
	public static final String BPEL_AUTH_PROP = "org.openecomp.mso.adapters.workflow.message.bpelauth";
	public static final String DEFAULT_BPEL_AUTH = "05FDA034C27D1CA51AAB8FAE512EDE45241E16FC8C137D292AA3A964431C82DB";
	public static final String ENCRYPTION_KEY = "aa3871669d893c7fb8abbcda31b88b4f";
}