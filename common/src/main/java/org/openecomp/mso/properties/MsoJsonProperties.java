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

package org.openecomp.mso.properties;


import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.openecomp.mso.utils.CryptoUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class MsoJsonProperties extends AbstractMsoProperties {
	
	protected ObjectMapper mapper = new ObjectMapper();
	
	protected JsonNode jsonRootNode = mapper.createObjectNode();
	
	protected MsoJsonProperties() {
		
	}

	public synchronized JsonNode getJsonRootNode () {
		return this.jsonRootNode;
	}

	/**
	 * This method is used to get the text encrypted in the string value of the node.
	 * @param jsonNode The JsonNode containing the strig to decode
	 * @param defaultValue The default value in case of issue
	 * @param encryptionKey The encryption Key in AES 128 bits
	 * @return the String decrypted
	 */
	public synchronized String getEncryptedProperty(JsonNode jsonNode, String defaultValue, String encryptionKey) {

		if (jsonNode.isTextual()) {
			try {
				return CryptoUtils.decrypt(jsonNode.asText(), encryptionKey);
			} catch (GeneralSecurityException e) {
				LOGGER.debug("Exception while decrypting property: " + jsonNode.asText(), e);
			}
		} 
		
		return defaultValue;
	}
	
	/**
	 * This method load a properties file from a source path.
	 *
	 * @param propertiesPath The path to the file
	 * @throws IOException In case of issues during the opening
	 */
	@Override
	protected synchronized void loadPropertiesFile(String propertiesPath) throws IOException {

		FileReader reader = null;
	
		this.propertiesFileName = propertiesPath;
		
		try {
			// Clean
			this.jsonRootNode = mapper.createObjectNode();
			
			System.out.println("ROBD: path=" + propertiesPath);
			reader = new FileReader(propertiesPath);
			
			// Try a tree load
			this.jsonRootNode = mapper.readValue(reader, JsonNode.class);


		} finally {
			JsonNode reloadJsonProp = this.jsonRootNode.get(RELOAD_TIME_PROPERTY);
			if (reloadJsonProp != null) {
				this.automaticRefreshInMinutes = reloadJsonProp.asInt(DEFAULT_RELOAD_TIME_MIN); 
			} else {
				this.automaticRefreshInMinutes = DEFAULT_RELOAD_TIME_MIN;
			}
			
			// Always close the file
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				LOGGER.debug("Exception while closing reader for file:" + propertiesFileName, e);
			}
		}
	}

	@Override
	public synchronized MsoJsonProperties clone() {
		MsoJsonProperties msoCopy = new MsoJsonProperties();
		
		ObjectMapper newMapper = new ObjectMapper();
		try {
			msoCopy.jsonRootNode = newMapper.createObjectNode();
			msoCopy.jsonRootNode = newMapper.readValue(this.jsonRootNode.toString(), JsonNode.class);
		} catch (JsonParseException e) {
			LOGGER.debug("JsonParseException when cloning the object:" + this.propertiesFileName, e);
		} catch (JsonMappingException e) {
			LOGGER.debug("JsonMappingException when cloning the object:" + this.propertiesFileName, e);
		} catch (IOException e) {
			LOGGER.debug("IOException when cloning the object:" + this.propertiesFileName, e);
		} 
		
		msoCopy.propertiesFileName = this.propertiesFileName;
		msoCopy.automaticRefreshInMinutes = this.automaticRefreshInMinutes;
		return msoCopy;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jsonRootNode == null) ? 0 : jsonRootNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MsoJsonProperties other = (MsoJsonProperties) obj;
		if (jsonRootNode == null) {
			if (other.jsonRootNode != null)
				return false;
		} else if (!jsonRootNode.equals(other.jsonRootNode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Config file " + propertiesFileName + "(Timer:" + automaticRefreshInMinutes + "mins):" + System
			.getProperty("line.separator") + this.jsonRootNode.toString() + System.getProperty("line.separator")
			+ System.getProperty("line.separator");
	}

}
