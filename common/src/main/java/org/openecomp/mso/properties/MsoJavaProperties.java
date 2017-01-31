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

package org.openecomp.mso.properties;


import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import org.openecomp.mso.utils.CryptoUtils;

public class MsoJavaProperties extends AbstractMsoProperties {

	
	private Properties msoProperties = new Properties();


	public MsoJavaProperties() {
		
	}

	public synchronized void setProperty(String key,String value) {
		msoProperties.setProperty(key, value);
	}
	
	public synchronized String getProperty(String key, String defaultValue) {
		if (msoProperties.containsKey(key)) {
			return msoProperties.getProperty(key);
		} else {
			return defaultValue;
		}
	}

	public synchronized int getIntProperty(String key, int defaultValue) {

		int value = defaultValue;
		if (msoProperties.containsKey(key)) {
			try {
				value = Integer.parseInt(msoProperties.getProperty(key));
			} catch (NumberFormatException e) {
				LOGGER.debug("Exception while parsing integer: " + msoProperties.getProperty(key), e);
			}
		}
		return value;

	}

	public synchronized boolean getBooleanProperty(String key, boolean defaultValue) {

		if (msoProperties.containsKey(key)) {
			return Boolean.parseBoolean(msoProperties.getProperty(key));
		} else {
			return defaultValue;
		}

	}

	public synchronized String getEncryptedProperty(String key, String defaultValue, String encryptionKey) {

		if (msoProperties.containsKey(key)) {
			try {
				return CryptoUtils.decrypt(msoProperties.getProperty(key), encryptionKey);
			} catch (GeneralSecurityException e) {
				LOGGER.debug("Exception while decrypting property: " + msoProperties.getProperty(key), e);
			}
		}
		return defaultValue;

	}

	public synchronized int size() {
		return this.msoProperties.size();
	}
	

	@Override
	protected synchronized void reloadPropertiesFile() throws IOException {
		this.loadPropertiesFile(this.propertiesFileName);
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
	
		propertiesFileName = propertiesPath;
		try {
			msoProperties.clear();
			reader = new FileReader(propertiesPath);
			msoProperties.load(reader);

		} finally {
			this.automaticRefreshInMinutes = this.getIntProperty(RELOAD_TIME_PROPERTY, DEFAULT_RELOAD_TIME_MIN);
			// Always close the file
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				LOGGER.debug("Exception while closing reader for file:" + propertiesPath, e);
			}
		}
	}

	@Override
	public synchronized MsoJavaProperties clone() {
		MsoJavaProperties msoCopy = new MsoJavaProperties();
		msoCopy.msoProperties.putAll(msoProperties);
		msoCopy.propertiesFileName = this.propertiesFileName;
		msoCopy.automaticRefreshInMinutes = this.automaticRefreshInMinutes;
		return msoCopy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((msoProperties == null) ? 0 : msoProperties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MsoJavaProperties other = (MsoJavaProperties) obj;
		if (!msoProperties.equals(other.msoProperties)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {

		StringBuffer response = new StringBuffer();
		response.append("Config file " + propertiesFileName + "(Timer:" + automaticRefreshInMinutes + "mins):"
				+ System.getProperty("line.separator"));
		for (Object key : this.msoProperties.keySet()) {
			String propertyName = (String) key;
			response.append(propertyName);
			response.append("=");
			response.append(this.msoProperties.getProperty(propertyName));
			response.append(System.getProperty("line.separator"));
		}
		response.append(System.getProperty("line.separator"));
		response.append(System.getProperty("line.separator"));
		return response.toString();
	}
}
