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

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.MDC;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Loads the property configuration from file system and refreshes the
 * properties when the property gets changed.
 *
 * WARNING: automatic refreshes might not work on network filesystems.
 */
public class PropertyConfiguration {

	/**
	 * The base name of the MSO BPMN properties file (mso.bpmn.properties).
	 */
	public static final String MSO_BPMN_PROPERTIES = "mso.bpmn.properties";

	/**
	 * The base name of the MSO BPMN URN-Mappings properties file (mso.bpmn.urn.properties).
	 */
	public static final String MSO_BPMN_URN_PROPERTIES = "mso.bpmn.urn.properties";

	/**
	 * The base name of the MSO Topology properties file (topology.properties).
	 */
	public static final String MSO_TOPOLOGY_PROPERTIES = "topology.properties";
	/**
	 * The name of the meta-property holding the time the properties were loaded
	 * from the file.
	 */
	public static final String TIMESTAMP_PROPERTY = "mso.properties.timestamp";

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);

	private static final List<String> SUPPORTED_FILES =
		Arrays.asList(MSO_BPMN_PROPERTIES, MSO_BPMN_URN_PROPERTIES, MSO_TOPOLOGY_PROPERTIES);

	private volatile String msoConfigPath = null;

	private final ConcurrentHashMap<String, Map<String, String>> propFileCache =
		new ConcurrentHashMap<>();

	private final Object CACHELOCK = new Object();
	private FileWatcherThread fileWatcherThread = null;

	// The key is the file name
	private Map<String, TimerTask> timerTaskMap = new HashMap<>();

	/**
     * Private Constructor.
     */
    private PropertyConfiguration() {
        startUp();
    }
	   		
	/**
	 * Singleton holder pattern eliminates locking when accessing the instance
	 * and still provides for lazy initialization.
	 */
	private static class PropertyConfigurationInstanceHolder {
		private static PropertyConfiguration instance = new PropertyConfiguration();
	}

	/**
	 * Gets the one and only instance of this class.
	 */
	public static PropertyConfiguration getInstance() {
		return PropertyConfigurationInstanceHolder.instance;
	}

	/**
	 * Returns the list of supported files.
	 */
	public static List<String> supportedFiles() {
		return new ArrayList<>(SUPPORTED_FILES);
	}

	/**
	 * May be called to restart the PropertyConfiguration if it was previously shut down.
	 */
	public synchronized void startUp() {
		msoConfigPath = System.getProperty("mso.config.path");

		if (msoConfigPath == null) {
			LOGGER.debug("mso.config.path JVM system property is not set");
			return;
		}

		try {
			Path directory = FileSystems.getDefault().getPath(msoConfigPath);
			WatchService watchService = FileSystems.getDefault().newWatchService();
			directory.register(watchService, ENTRY_MODIFY);

			LOGGER.info(MessageEnum.BPMN_GENERAL_INFO, "BPMN", "Starting FileWatcherThread");
			LOGGER.debug("Starting FileWatcherThread");
			fileWatcherThread = new FileWatcherThread(watchService);
			fileWatcherThread.start();
		} catch (Exception e) {
			LOGGER.debug("Error occurred while starting FileWatcherThread:", e);
			LOGGER.error(
				MessageEnum.BPMN_GENERAL_EXCEPTION,
				"BPMN",
				"Property Configuration",
				MsoLogger.ErrorCode.UnknownError,
				"Error occurred while starting FileWatcherThread:" + e);
		}
	}

	/**
	 * May be called to shut down the PropertyConfiguration.  A shutDown followed
	 * by a startUp will reset the PropertyConfiguration to its initial state.
	 */
	public synchronized void shutDown() {
		if (fileWatcherThread != null) {
			LOGGER.debug("Shutting down FileWatcherThread " + System.identityHashCode(fileWatcherThread));
			fileWatcherThread.shutdown();

			long waitInSeconds = 10;

			try {
				fileWatcherThread.join(waitInSeconds * 1000);
			} catch (InterruptedException e) {
				LOGGER.debug("FileWatcherThread " + System.identityHashCode(fileWatcherThread)
					+ " shutdown did not occur within " + waitInSeconds + " seconds",e);
			}

			LOGGER.debug("Finished shutting down FileWatcherThread " + System.identityHashCode(fileWatcherThread));
			fileWatcherThread = null;
		}

		clearCache();
		msoConfigPath = null;
	}

	public synchronized boolean isFileWatcherRunning() {
		return fileWatcherThread != null;
	}

	public void clearCache() {
		synchronized(CACHELOCK) {
			propFileCache.clear();
		}
	}

	public int cacheSize() {
		return propFileCache.size();
	}

	// TODO: throw IOException?
	public Map<String, String> getProperties(String fileName) {
		Map<String, String> properties = propFileCache.get(fileName);

		if (properties == null) {
			if (!SUPPORTED_FILES.contains(fileName)) {
				throw new IllegalArgumentException("Not a supported property file: " + fileName);
			}

			if (msoConfigPath == null) {
				LOGGER.debug("mso.config.path JVM system property must be set to load " + fileName);

				LOGGER.error(
						MessageEnum.BPMN_GENERAL_EXCEPTION,
						"BPMN",
						MDC.get(fileName),
						MsoLogger.ErrorCode.UnknownError,
						"mso.config.path JVM system property must be set to load " + fileName);

				return null;
			}

			try {
				properties = readProperties(new File(msoConfigPath, fileName));
			} catch (Exception e) {
				LOGGER.debug("Error loading " + fileName);

				LOGGER.error(
						MessageEnum.BPMN_GENERAL_EXCEPTION,
						"BPMN",
						MDC.get(fileName),
						MsoLogger.ErrorCode.UnknownError,
						"Error loading " + fileName, e);

				return null;
			}
		}

		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Reads properties from the specified file, updates the property file cache, and
	 * returns the properties in a map.
	 * @param file the file to read
	 * @return a map of properties
	 */
	private Map<String, String> readProperties(File file) throws IOException {
		String fileName = file.getName();
		LOGGER.debug("Reading " + fileName);

		Map<String, String> properties = new HashMap<>();
		Properties newProperties = new Properties();

		try (FileReader reader = new FileReader(file)) {
			newProperties.load(reader);
		}
		catch (Exception e) {
			LOGGER.debug("Exception :",e);
		}

		for (Entry<Object, Object> entry : newProperties.entrySet()) {
			properties.put(entry.getKey().toString(), entry.getValue().toString());
		}

		properties.put(TIMESTAMP_PROPERTY, String.valueOf(System.currentTimeMillis()));

		synchronized(CACHELOCK) {
			propFileCache.put(fileName, properties);
		}

		return properties;
	}

	/**
	 * File watcher thread which monitors a directory for file modification.
	 */
	private class FileWatcherThread extends Thread {
		private final WatchService watchService;
		private final Timer timer = new Timer("FileWatcherTimer");

		public FileWatcherThread(WatchService service) {
			this.watchService = service;
		}

		public void shutdown() {
			interrupt();
		}

		@Override
		public void run() {
			LOGGER.info(MessageEnum.BPMN_GENERAL_INFO, "BPMN",
				"FileWatcherThread started");

			LOGGER.debug("Started FileWatcherThread " + System.identityHashCode(fileWatcherThread));

			try {
				WatchKey watchKey = null;

				while (!isInterrupted()) {
					try {
						if (watchKey != null) {
							watchKey.reset();
						}

						watchKey = watchService.take();

						for (WatchEvent<?> event : watchKey.pollEvents()) {
							@SuppressWarnings("unchecked")
							WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;

							if ("EVENT_OVERFLOW".equals(pathEvent.kind())) {
								LOGGER.debug("Ignored overflow event for " + msoConfigPath);
								continue;
							}

							String fileName = pathEvent.context().getFileName().toString();

							if (!SUPPORTED_FILES.contains(fileName)) {
								LOGGER.debug("Ignored modify event for " + fileName);
								continue;
							}

							LOGGER.debug("Configuration file has changed: " + fileName);

							LOGGER.info(MessageEnum.BPMN_GENERAL_INFO, "BPMN",
									"Configuation file has changed: " + fileName);

							// There's a potential problem here. The MODIFY event is
							// triggered as soon as somebody starts writing the file but
							// there's no obvious way to know when the write is done.  If we
							// read the file while the write is still in progress, then the
							// cache can really be messed up. As a workaround, we use a timer
							// to sleep for at least one second, and then we sleep for as long
							// as it takes for the file's lastModified time to stop changing.
							// The timer has another benefit: it consolidates multiple events
							// that we seem to receive when a file is modified.

							synchronized(timerTaskMap) {
								TimerTask task = timerTaskMap.get(fileName);

								if (task != null) {
									task.cancel();
								}

								File file = new File(msoConfigPath, fileName);
								task = new DelayTimerTask(timer, file, 1000);
								timerTaskMap.put(fileName, task);
							}
						}
					} catch (InterruptedException e) {
						LOGGER.debug("InterruptedException :",e);
						break;
					} catch (ClosedWatchServiceException e) {
						LOGGER.info(
								MessageEnum.BPMN_GENERAL_INFO,
								"BPMN",
								"FileWatcherThread shut down because the watch service was closed");
						LOGGER.debug("ClosedWatchServiceException :",e);
						break;
					} catch (Exception e) {
						LOGGER.error(
								MessageEnum.BPMN_GENERAL_EXCEPTION,
								"BPMN",
								"Property Configuration",
								MsoLogger.ErrorCode.UnknownError,
								"FileWatcherThread caught unexpected " + e.getClass().getSimpleName(), e);
					}

				}
			} finally {
				timer.cancel();

				synchronized(timerTaskMap) {
					timerTaskMap.clear();
				}

				try {
					watchService.close();
				} catch (IOException e) {
					LOGGER.debug("FileWatcherThread caught " + e.getClass().getSimpleName()
						+ " while closing the watch service",e);
				}

				LOGGER.info(MessageEnum.BPMN_GENERAL_INFO, "BPMN",
					"FileWatcherThread stopped");
			}
		}
	}

	private class DelayTimerTask extends TimerTask {
		private final File file;
		private final long lastModifiedTime;
		private final Timer timer;

		public DelayTimerTask(Timer timer, File file, long delay) {
			this.timer = timer;
			this.file = file;
			this.lastModifiedTime = file.lastModified();
			timer.schedule(this, delay);
		}

		@Override
		public void run() {
			try {
				long newLastModifiedTime = file.lastModified();

				if (newLastModifiedTime == lastModifiedTime) {
					try {
						readProperties(file);
					} catch (Exception e) {
						LOGGER.error(
							MessageEnum.BPMN_GENERAL_EXCEPTION,
							"BPMN",
							"Property Configuration",
							MsoLogger.ErrorCode.UnknownError,
							"Unable to reload " + file, e);
					}
				} else {
					LOGGER.debug("Delaying reload of " + file + " by 1 second");

					synchronized(timerTaskMap) {
						TimerTask task = timerTaskMap.get(file.getName());

						if (task != null && task != this) {
							task.cancel();
						}

						task = new DelayTimerTask(timer, file, 1000);
						timerTaskMap.put(file.getName(), task);
					}
				}
			} finally {
				synchronized(timerTaskMap) {
					TimerTask task = timerTaskMap.get(file.getName());

					if (task == this) {
						timerTaskMap.remove(file.getName());
					}
				}
			}
		}
	}
}
