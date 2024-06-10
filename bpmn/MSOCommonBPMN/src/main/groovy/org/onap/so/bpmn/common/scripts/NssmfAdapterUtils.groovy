/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.common.scripts

import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import jakarta.ws.rs.core.Response

/***
 * Utilities for accessing Catalog DB Adapter to retrieve Networks, VNF/VFModules, AllottedResources and complete ServiceResources information
 *
 */

class NssmfAdapterUtils {
    private static final Logger logger = LoggerFactory.getLogger( NssmfAdapterUtils.class);

	private HttpClientFactory httpClientFactory
	private MsoUtils utils
	private JsonUtils jsonUtils

    NssmfAdapterUtils(HttpClientFactory httpClientFactory, JsonUtils jsonUtils) {
		this.httpClientFactory = httpClientFactory
		this.utils = new MsoUtils()
		this.jsonUtils = jsonUtils
	}


    public <T> T sendPostRequestNSSMF (DelegateExecution execution, String endPoint, String nssmfRequest, Class<T> entityType) {
        try {

            String nssmfEndpoint = UrnPropertiesReader.getVariable("mso.adapters.nssmf.endpoint",execution)
            String queryEndpoint = nssmfEndpoint + endPoint
            def responseData
            HttpClient client = httpClientFactory.newJsonClient(new URL(queryEndpoint), ONAPComponents.EXTERNAL)
            String basicAuthCred = execution.getVariable("BasicAuthHeaderValue")
            client.addAdditionalHeader("Authorization", StringUtils.defaultIfEmpty(basicAuthCred, getBasicDBAuthHeader(execution)))

            logger.debug('sending POST to NSSMF endpoint: ' + endPoint)
            Response response = client.post(nssmfRequest)

            responseData = response.readEntity(entityType)
            if (responseData != null) {
                logger.debug("Received data from NSSMF: " + responseData)
            }

            logger.debug('Response code:' + response.getStatus())
            logger.debug('Response:' + System.lineSeparator() + responseData)
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                // parse response as needed
                return responseData
            }
            else {
                return null
            }
        }
        catch (Exception e) {
            logger.debug("ERROR WHILE QUERYING CATALOG DB: " + e.message)
            throw e
        }

    }

    public String sendPostRequestNSSMF (DelegateExecution execution, String endPoint, String nssmfRequest) {
        try {

            String nssmfEndpoint = UrnPropertiesReader.getVariable("mso.adapters.nssmf.endpoint",execution)
            String queryEndpoint = nssmfEndpoint + endPoint
            def responseData
            HttpClient client = httpClientFactory.newJsonClient(new URL(queryEndpoint), ONAPComponents.EXTERNAL)
            String basicAuthCred = execution.getVariable("BasicAuthHeaderValue")
            client.addAdditionalHeader("Authorization", StringUtils.defaultIfEmpty(basicAuthCred, getBasicDBAuthHeader(execution)))

            logger.debug('sending POST to NSSMF endpoint: ' + endPoint)
            Response response = client.post(nssmfRequest)

            responseData = response.readEntity(String.class)
            if (responseData != null) {
                logger.debug("Received data from NSSMF: " + responseData)
            }

            logger.debug('Response code:' + response.getStatus())
            logger.debug('Response:' + System.lineSeparator() + responseData)
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                // parse response as needed
                return responseData
            }
            else {
                return null
            }
        }
        catch (Exception e) {
            logger.debug("ERROR WHILE QUERYING CATALOG DB: " + e.message)
            throw e
        }

    }

    public String sendPostRequestNSSMF (DelegateExecution execution, String endPoint, NssmfAdapterNBIRequest nssmfRequest) {
        try {

            String nssmfEndpoint = UrnPropertiesReader.getVariable("mso.adapters.nssmf.endpoint",execution)
            String queryEndpoint = nssmfEndpoint + endPoint
            def responseData
            HttpClient client = httpClientFactory.newJsonClient(new URL(queryEndpoint), ONAPComponents.EXTERNAL)
            String basicAuthCred = execution.getVariable("BasicAuthHeaderValue")
            client.addAdditionalHeader("Authorization", StringUtils.defaultIfEmpty(basicAuthCred, getBasicDBAuthHeader(execution)))

            logger.debug('sending POST to NSSMF endpoint: ' + endPoint)
            Response response = client.post(nssmfRequest)

            responseData = response.readEntity(String.class)
            if (responseData != null) {
                logger.debug("Received data from NSSMF: " + responseData)
            }

            logger.debug('Response code:' + response.getStatus())
            logger.debug('Response:' + System.lineSeparator() + responseData)
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                // parse response as needed
                return responseData
            }
            else {
                return null
            }
        }
        catch (Exception e) {
            logger.debug("ERROR WHILE QUERYING CATALOG DB: " + e.message)
            throw e
        }

    }

    private String getBasicDBAuthHeader(DelegateExecution execution) {

        String encodedString = null
        try {
            String basicAuthValueDB = UrnPropertiesReader.getVariable("mso.adapters.db.auth", execution)
            logger.debug("DEBUG", " Obtained BasicAuth userid password for Catalog DB adapter: " + basicAuthValueDB)

            encodedString = utils.getBasicAuth(basicAuthValueDB, UrnPropertiesReader.getVariable("mso.msoKey", execution))
            execution.setVariable("BasicAuthHeaderValue", encodedString)
        } catch (IOException ex) {
            String dataErrorMessage = " Unable to encode Catalog DB user/password string - " + ex.getMessage()
            logger.error(dataErrorMessage)
        }
        return encodedString
    }


}
