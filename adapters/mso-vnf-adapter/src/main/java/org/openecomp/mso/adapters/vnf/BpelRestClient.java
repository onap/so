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

package org.openecomp.mso.adapters.vnf;


import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

/**
 * This is the class that is used to POST replies from the MSO adapters to the BPEL engine.
 * It can be configured via property file, or modified using the member methods.
 * The properties to use are:
 * org.openecomp.mso.adapters.vnf.bpelauth  encrypted authorization string to send to BEPL engine
 * org.openecomp.mso.adapters.vnf.sockettimeout socket timeout value
 * org.openecomp.mso.adapters.vnf.connecttimeout connect timeout value
 * org.openecomp.mso.adapters.vnf.retrycount number of times to retry failed connections
 * org.openecomp.mso.adapters.vnf.retryinterval interval (in seconds) between retries
 * org.openecomp.mso.adapters.vnf.retrylist list of response codes that will trigger a retry (the special code
 * 			900 means "connection was not established")
 */
public class BpelRestClient {
	public  static final String MSO_PROP_VNF_ADAPTER     = "MSO_PROP_VNF_ADAPTER";
	private static final String PROPERTY_DOMAIN          = "org.openecomp.mso.adapters.vnf";
	private static final String BPEL_AUTH_PROPERTY       = PROPERTY_DOMAIN+".bpelauth";
	private static final String SOCKET_TIMEOUT_PROPERTY  = PROPERTY_DOMAIN+".sockettimeout";
	private static final String CONN_TIMEOUT_PROPERTY    = PROPERTY_DOMAIN+".connecttimeout";
	private static final String RETRY_COUNT_PROPERTY     = PROPERTY_DOMAIN+".retrycount";
	private static final String RETRY_INTERVAL_PROPERTY  = PROPERTY_DOMAIN+".retryinterval";
	private static final String RETRY_LIST_PROPERTY      = PROPERTY_DOMAIN+".retrylist";
	private static final String ENCRYPTION_KEY           = "aa3871669d893c7fb8abbcda31b88b4f";
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

	/** Default socket timeout (in seconds) */
	public static final int DEFAULT_SOCKET_TIMEOUT = 5;
	/** Default connect timeout (in seconds) */
	public static final int DEFAULT_CONNECT_TIMEOUT = 5;
	/** By default, retry up to five times */
	public static final int DEFAULT_RETRY_COUNT = 5;
	/** Default interval to wait between retries (in seconds), negative means use backoff algorithm */
	public static final int DEFAULT_RETRY_INTERVAL = -15;
	/** Default list of response codes to trigger a retry */
	public static final String DEFAULT_RETRY_LIST = "408,429,500,502,503,504,900";	// 900 is "connection failed"
	/** Default credentials */
	public static final String DEFAULT_CREDENTIALS = "";

	// Properties of the BPEL client -- all are configurable
	private int socketTimeout;
	private int connectTimeout;
	private int retryCount;
	private int retryInterval;
	private Set<Integer> retryList;
	private String credentials;

	// last response from BPEL engine
	private int lastResponseCode;
	private String lastResponse;

	/**
	 * Create a client to send results to the BPEL engine, using configuration from the
	 * MSO_PROP_VNF_ADAPTER properties.
	 */
	public BpelRestClient() {
		socketTimeout  = DEFAULT_SOCKET_TIMEOUT;
		connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		retryCount     = DEFAULT_RETRY_COUNT;
		retryInterval  = DEFAULT_RETRY_INTERVAL;
		setRetryList(DEFAULT_RETRY_LIST);
		credentials    = DEFAULT_CREDENTIALS;
		lastResponseCode = 0;
		lastResponse = "";

		try {
			MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
			MsoJavaProperties jp = msoPropertiesFactory.getMsoJavaProperties (MSO_PROP_VNF_ADAPTER);
			socketTimeout  = jp.getIntProperty(SOCKET_TIMEOUT_PROPERTY, DEFAULT_SOCKET_TIMEOUT);
			connectTimeout = jp.getIntProperty(CONN_TIMEOUT_PROPERTY,   DEFAULT_CONNECT_TIMEOUT);
			retryCount     = jp.getIntProperty(RETRY_COUNT_PROPERTY,    DEFAULT_RETRY_COUNT);
			retryInterval  = jp.getIntProperty(RETRY_INTERVAL_PROPERTY, DEFAULT_RETRY_INTERVAL);
			setRetryList(jp.getProperty(RETRY_LIST_PROPERTY, DEFAULT_RETRY_LIST));
			credentials    = jp.getEncryptedProperty(BPEL_AUTH_PROPERTY, DEFAULT_CREDENTIALS, ENCRYPTION_KEY);
		} catch (MsoPropertiesException e) {
			String error = "Unable to get properties:" + MSO_PROP_VNF_ADAPTER;
			LOGGER.error (MessageEnum.RA_CONFIG_EXC, error, "Camunda", "", MsoLogger.ErrorCode.AvailabilityError, "MsoPropertiesException - Unable to get properties", e);
		}
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		int newRetryCount = retryCount;
		if (newRetryCount < 0)
			newRetryCount = DEFAULT_RETRY_COUNT;
		this.retryCount = newRetryCount;
	}

	public int getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	public String getCredentials() {
		return credentials;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	public String getRetryList() {
		if (retryList.isEmpty())
			return "";
		String t = retryList.toString();
		return t.substring(1, t.length()-1);
	}

	public void setRetryList(String retryList) {
		Set<Integer> s = new TreeSet<>();
		for (String t : retryList.split("[, ]")) {
			try {
				s.add(Integer.parseInt(t));
			} catch (NumberFormatException x) {
				// ignore
			}
		}
		this.retryList = s;
	}

	public int getLastResponseCode() {
		return lastResponseCode;
	}

	public String getLastResponse() {
		return lastResponse;
	}

	/**
	 * Post a response to the URL of the BPEL engine.  As long as the response code is one of those in
	 * the retryList, the post will be retried up to "retrycount" times with an interval (in seconds)
	 * of "retryInterval".  If retryInterval is negative, then each successive retry interval will be
	 * double the previous one.
	 * @param toBpelStr the content (XML or JSON) to post
	 * @param bpelUrl the URL to post to
	 * @param isxml true if the content is XML, otherwise assumed to be JSON
	 * @return true if the post succeeded, false if all retries failed
	 */
	public boolean bpelPost(final String toBpelStr, final String bpelUrl, final boolean isxml)  {
		debug("Sending response to BPEL: " + toBpelStr);
		int totalretries = 0;
		int retryint = retryInterval;
		while (true) {
			sendOne(toBpelStr, bpelUrl, isxml);
			// Note: really should handle response code 415 by switching between content types if needed
			if (!retryList.contains(lastResponseCode)) {
				debug("Got response code: " + lastResponseCode + ": returning.");
				return true;
			}
			if (totalretries >= retryCount) {
				debug("Retried " + totalretries + " times, giving up.");
				LOGGER.error(MessageEnum.RA_SEND_VNF_NOTIF_ERR, "Could not deliver response to BPEL after "+totalretries+" tries: "+toBpelStr, "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, "Could not deliver response to BPEL");
				return false;
			}
			totalretries++;
			int sleepinterval = retryint;
			if (retryint < 0) {
				// if retry interval is negative double the retry on each pass
				sleepinterval = -retryint;
				retryint *= 2;
			}
			debug("Sleeping for " + sleepinterval + " seconds.");
			try {
				Thread.sleep(sleepinterval * 1000L);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}
	private void debug(String m) {
		LOGGER.debug(m);
	}
	private void sendOne(final String toBpelStr, final String bpelUrl, final boolean isxml) {
		LOGGER.debug("Sending to BPEL server: "+bpelUrl);
		LOGGER.debug("Content is: "+toBpelStr);

		//POST
		HttpPost post = new HttpPost(bpelUrl);
		if (credentials != null && !credentials.isEmpty())
			post.addHeader("Authorization", "Basic " + DatatypeConverter.printBase64Binary(credentials.getBytes()));

        //ContentType
        ContentType ctype = isxml ? ContentType.APPLICATION_XML : ContentType.APPLICATION_JSON;
        post.setEntity(new StringEntity(toBpelStr, ctype));

        //Timeouts
		RequestConfig requestConfig = RequestConfig
			.custom()
			.setSocketTimeout(socketTimeout * 1000)
			.setConnectTimeout(connectTimeout * 1000)
			.build();
		post.setConfig(requestConfig);

        //Client 4.3+
        //Execute & GetResponse
		try(CloseableHttpClient client = HttpClients.createDefault()) {
			CloseableHttpResponse response = client.execute(post);
			if (response != null) {
				lastResponseCode = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
				lastResponse = (entity != null) ? EntityUtils.toString(entity) : "";
			} else {
				lastResponseCode = 900;
				lastResponse = "";
			}
		} catch (Exception e) {
			String error = "Error sending Bpel notification:" + toBpelStr;
			LOGGER.error (MessageEnum.RA_SEND_VNF_NOTIF_ERR, error, "Camunda", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - Error sending Bpel notification", e);
			lastResponseCode = 900;
			lastResponse = "";
		}
		LOGGER.debug("Response code from BPEL server: "+lastResponseCode);
		LOGGER.debug("Response body is: "+lastResponse);
	}

}
