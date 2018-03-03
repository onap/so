/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
 
package org.openecomp.mso.bpmn.core;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openecomp.mso.logger.MsoLogger;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;

/**
 * Sets up mso.bpmn.properties and mso.bpmn.urn.properties for unit tests.
 */
public class PropertyConfigurationSetup {

	private static Path msoConfigPath = null;
	private static Path bpmnPropertiesPath = null;
	private static Path bpmnUrnPropertiesPath = null;
	private static boolean modifiedConfiguration = false;
	
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);

	/**
	 * Ensures that the the PropertyConfiguration is initialized and that the
	 * property data is reset to initial values.  Any extra properties that are
	 * specified will be merged with the initial values.  The following example
	 * shows how a test can specify a replacement URN mapping property.
	 * <pre>
	 *     Map<String, String> urnProperties =
	 *         PropertyConfigurationSetup.createBpmnUrnProperties();
	 *     urnProperties.add("mso.po.timeout", "PT1M");
	 *     PropertyConfiguration.init(urnProperties);
	 * </pre>
	 * @param args one or more maps created with createBpmnProperties()
	 *        and/or createBpmnUrnProperties()
	 */
	public static synchronized void init(Object ... args) throws IOException {

		Map<String, String> extraBpmnProperties = null;
		Map<String, String> extraBpmnUrnProperties = null;
		
		boolean propertiesSpecified = false;

		for (Object arg : args) {
			@SuppressWarnings("unchecked")
			Map<String, String> properties = (Map<String, String>) arg;

			String type = properties.get("PROPERTIES-TYPE");

			if (PropertyConfiguration.MSO_BPMN_PROPERTIES.equals(type)) {
				if (properties.size() > 1) {
					extraBpmnProperties = properties;
					propertiesSpecified = false;
				}
			} else if (PropertyConfiguration.MSO_BPMN_URN_PROPERTIES.equals(type)) {
				if (properties.size() > 1) {
					extraBpmnUrnProperties = properties;
					propertiesSpecified = false;
				}
			} else {
				throw new IllegalArgumentException("Not a supported PROPERTIES-TYPE map");
			}
		}

		// There are three cases in which we need to change the existing configuration:
		//     1) There is no existing configuration, i.e. first time setup
		//     2) The existing configuration was modified, i.e. it has non-default values
		//     3) Non-default values are specified for this initialization

		if (msoConfigPath == null || modifiedConfiguration || propertiesSpecified) {
			modifiedConfiguration = propertiesSpecified;

			Path bpmnPropertiesSourcePath = Paths.get("src", "test", "resources", "mso.bpmn.properties");
			Path bpmnUrnPropertiesSourcePath = Paths.get("src", "test", "resources", "mso.bpmn.urn.properties");

			if (msoConfigPath == null) {
				// Initialize from scratch.
				msoConfigPath = Files.createTempDirectory("mso-config-path-");
				System.setProperty("mso.config.path", msoConfigPath.toString());
				msoConfigPath.toFile().deleteOnExit();

				bpmnPropertiesPath = msoConfigPath.resolve("mso.bpmn.properties");
				mergeCopy(bpmnPropertiesSourcePath, extraBpmnProperties, bpmnPropertiesPath);
				bpmnPropertiesPath.toFile().deleteOnExit();

				bpmnUrnPropertiesPath = msoConfigPath.resolve("mso.bpmn.urn.properties");
				mergeCopy(bpmnUrnPropertiesSourcePath, extraBpmnUrnProperties, bpmnUrnPropertiesPath);
				bpmnUrnPropertiesPath.toFile().deleteOnExit();

				PropertyConfiguration.getInstance();
			} else {
				// Just reset the data.
				PropertyConfiguration.getInstance().clearCache();
				mergeCopy(bpmnPropertiesSourcePath, extraBpmnProperties, bpmnPropertiesPath);
				mergeCopy(bpmnUrnPropertiesSourcePath, extraBpmnUrnProperties, bpmnUrnPropertiesPath);
			}
		}
	}

	/**
	 * Resets the PropertyConfiguration to its initial state, as if it had never
	 * been started. Note that this is a very expensive option and should not
	 * be needed by most unit tests.
	 * @throws IOException
	 */
	public static synchronized void nuke() throws IOException {
		if (msoConfigPath == null) {
			return;
		}

		PropertyConfiguration.getInstance().shutDown();

		bpmnUrnPropertiesPath.toFile().delete();
		bpmnUrnPropertiesPath = null;
		
		bpmnPropertiesPath.toFile().delete();
		bpmnPropertiesPath = null;

		msoConfigPath.toFile().delete();
		msoConfigPath = null;
		
		System.setProperty("mso.config.path", null);

		modifiedConfiguration = false;
	}
	
	/**
	 * Create a map to hold properties to be added to mso.bpmn.properties.
	 */
	public static Map<String, String> createBpmnProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("PROPERTIES-TYPE", PropertyConfiguration.MSO_BPMN_PROPERTIES);
		return properties;
	}

	/**
	 * Create a map to hold properties to be added to mso.bpmn.urn.properties.
	 */
	public static Map<String, String> createBpmnUrnProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("PROPERTIES-TYPE", PropertyConfiguration.MSO_BPMN_URN_PROPERTIES);
		return properties;
	}

	/**
	 * Adds (or replaces) the specified values in the mso.bpmn.urn.properties file.
	 * Note that properties added this way may take some time to be loaded by the
	 * PropertyConfiguration, just like they do when a property file is updated on
	 * a real MSO system. This method will optionally wait for the new properties
	 * to be loaded.  Timeout results in an IOException.
	 * @param values new properties
	 * @param wait maximum amount of time to wait for new properties to be loaded,
	 *             in milliseconds.  A value of zero means, "Do not wait."
	 * @throws IOException
	 */
	public static synchronized void addProperties(Map<String, String> properties, long wait)
			throws IOException, InterruptedException {

		if (msoConfigPath == null) {
			throw new IllegalStateException();
		}

		String type = properties.get("PROPERTIES-TYPE");
		Path path;

		if (PropertyConfiguration.MSO_BPMN_PROPERTIES.equals(type)) {
			path = bpmnPropertiesPath;
		} else if (PropertyConfiguration.MSO_BPMN_URN_PROPERTIES.equals(type)) {
			path = bpmnUrnPropertiesPath;
		} else {
			throw new IllegalArgumentException("Not a supported PROPERTIES-TYPE map");
		}

		String oldTimestamp = PropertyConfiguration.getInstance().getProperties(type)
			.get(PropertyConfiguration.TIMESTAMP_PROPERTY);

		modifiedConfiguration = true;
		addProperties(properties, path);

		if (wait <= 0) {
			return;
		}

		long endTime = System.currentTimeMillis() + wait;

		while (true) {
			Thread.sleep(250);

			String newTimestamp = PropertyConfiguration.getInstance().getProperties(type)
				.get(PropertyConfiguration.TIMESTAMP_PROPERTY);

			if (newTimestamp != oldTimestamp) {
				return;
			}

			long now = System.currentTimeMillis();

			if (now >= endTime) {
				throw new IOException("Timed out after " + wait
					+ "ms waiting for PropertyConfiguration change");
			}
		}
	}

	/**
	 * Helper method that adds properties to the specified file.
	 */
	private static void addProperties(Map<String, String> values, Path path)
			throws IOException {

		FileReader fileReader = null;
		FileOutputStream outputStream = null;

		try {
			fileReader = new FileReader(path.toFile());
			Properties properties = new Properties();
			properties.load(fileReader);

			for (String key : values.keySet()) {
				if (!key.equals("PROPERTIES-TYPE")) {
					properties.setProperty(key, values.get(key));
				}
			}

			outputStream = new FileOutputStream(path.toFile());
			properties.store(outputStream, "Custom Test Properties");
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					LOGGER.debug("Exception :",e);
				}
			}

			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					LOGGER.debug("Exception :",e);
				}
			}
		}
	}

	/**
	 * Helper method that copies properties from the specified source file, and
	 * optionally merges them with the specified extra values, then writes the
	 * whole mess to the destination file.
	 */
	private static void mergeCopy(Path sourcePath, Map<String, String> extraValues, Path destPath)
			throws IOException {
		if (extraValues == null || extraValues.isEmpty()) {
			Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
			return;
		}

		FileReader fileReader = null;
		FileOutputStream outputStream = null;

		try {
			fileReader = new FileReader(sourcePath.toFile());
			Properties properties = new Properties();
			properties.load(fileReader);

			for (String key : extraValues.keySet()) {
				if (!key.equals("PROPERTIES-TYPE")) {
					properties.setProperty(key, extraValues.get(key));
				}
			}

			outputStream = new FileOutputStream(destPath.toFile());
			properties.store(outputStream, "Custom Test Properties");
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					LOGGER.debug("Exception :",e);
				}
			}

			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					LOGGER.debug("Exception :",e);
				}
			}
		}
	}
}