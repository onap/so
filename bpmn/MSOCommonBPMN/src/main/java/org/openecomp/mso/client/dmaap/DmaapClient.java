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

package org.openecomp.mso.client.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

public abstract class DmaapClient {
	
	protected final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
	protected final Map<String, String> msoProperties;
	protected final Properties properties;
	public DmaapClient(String filepath) throws FileNotFoundException, IOException {
		Resource resource = new ClassPathResource(filepath);
		DmaapProperties dmaapProperties = DmaapPropertiesLoader.getInstance().getImpl();
		if (dmaapProperties == null) {
			dmaapProperties = new DefaultDmaapPropertiesImpl();
		}
		this.msoProperties = dmaapProperties.getProperties();
		this.properties = new Properties();
		this.properties.load(resource.getInputStream());
		this.properties.put("password", this.deobfuscatePassword(this.getPassword()));
		this.properties.put("username", this.getUserName());
		this.properties.put("topic", this.getTopic());
	}
	protected String deobfuscatePassword(String password) {
		
		try {
			return new String(Base64.getDecoder().decode(password.getBytes()));
		} catch(IllegalArgumentException iae) {
			
			return password;
		}
	}
	
	
	public abstract String getUserName();
	public abstract String getPassword();
	public abstract String getTopic();
}
