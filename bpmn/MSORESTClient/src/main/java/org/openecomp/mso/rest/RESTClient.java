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

package org.openecomp.mso.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.util.EntityUtils;

import org.openecomp.mso.logger.MsoLogger;
/**
 * Client used to send RESTFul requests.
 * <p>
 * Many of the methods return a reference to the 'this,' thereby allowing 
 * method chaining. 
 * <br>
 * An example of usage can be found below:
 * <pre>
 * RESTClient client;
 * try {
 *     client = new RESTClient("http://www.openecomp.org");
 *     APIResponse response = client
 *         .setHeader("Accept", "application/json")
 *         .setHeader("Clientid", "clientid")
 *         .setHeader("header", "value")
 *         .httpPost("postbody");
 *     if (response.getStatusCode() == 200) {
 *         System.out.println("Success!");
 *     }
 *  } catch (RESTException re) {
 *      // Handle Exception
 *  }
 * </pre>
 *
 * @version 1.0
 * @since 1.0
 */
public class RESTClient {
	
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
    private final String proxyHost;
    private final int proxyPort;

    private final String URL;

    private final LinkedHashMap<String, List<String>> headers;
    private final LinkedHashMap<String, List<String>> parameters;
    
    private HttpEntity httpEntity;

    /**
     * Internal method used to build an APIResponse using the specified 
     * HttpResponse object.
     *
     * @param response response wrapped inside an APIResponse object
     * @return api response
     */
    private APIResponse buildResponse(HttpResponse response) 
            throws RESTException {

        return new APIResponse(response);
    }

    /**
     * Used to release any resources used by the connection.
     * @param response HttpResponse object used for releasing the connection
     * @throws RESTException if unable to release connection
     *
     */
    private void releaseConnection(HttpResponse response) throws RESTException {
        try {
            EntityUtils.consume(response.getEntity());
        } catch (IOException ioe) {
            throw new RESTException(ioe);
        }
    }

    /**
     * Sets headers to the http message.
     *
     * @param httpMsg http message to set headers for
     */
    private void addInternalHeaders(AbstractHttpMessage httpMsg) {
        if (headers.isEmpty()) {
            return;
        }

        final Set<String> keySet = headers.keySet();
        for (final String key : keySet) {
            final List<String> values = headers.get(key);
            for (final String value : values) {
                httpMsg.addHeader(key, value);
            }
        }
    }

    /**
     * Builds the query part of a URL.
     *
     * @return query
     */
    private String buildQuery() {
        if (this.parameters.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String charSet = "UTF-8";
        try {
            Iterator<String> keyitr = this.parameters.keySet().iterator();
            for (int i = 0; keyitr.hasNext(); ++i) {
                if (i > 0) {
                    sb.append("&");
                }

                final String name = keyitr.next();
                final List<String> values = this.parameters.get(name);
                for(final String value : values) {
                    sb.append(URLEncoder.encode(name, charSet));
                    sb.append("=");
                    sb.append(URLEncoder.encode(value, charSet));
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.debug("Exception :", e);
        }
        return sb.toString();
    }

    /**
     * Creates an http client that can be used for sending http requests.
     *
     * @return created http client
     *
     * @throws RESTException if unable to create http client.
     */
    private CloseableHttpClient createClient() throws RESTException {
        //TODO - we may want to trust self signed certificate at some point - add implementation here
        HttpClientBuilder clientBuilder;

		try {
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
					(SSLSocketFactory) SSLSocketFactory.getDefault(),
					new HostNameVerifier());
			Registry<ConnectionSocketFactory> registry = RegistryBuilder
					.<ConnectionSocketFactory> create()
					.register("http",
							PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslSocketFactory).build();
			PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
					registry);
			clientBuilder = HttpClientBuilder.create().setConnectionManager(
					manager);
		} catch (Exception ex) {
			LOGGER.debug("Exception :", ex);
			throw new RESTException(ex.getMessage());
		}
		clientBuilder.disableRedirectHandling();

		if ((this.proxyHost != null) && (this.proxyPort != -1)) {
			HttpHost proxy = new HttpHost(this.proxyHost, this.proxyPort);
			clientBuilder.setProxy(proxy);
		}

		return clientBuilder.build();
    }

    /**
     * Creates a RESTClient with the specified URL, proxy host, and proxy port.
     *
     * @param URL URL to send request to
     * @param proxyHost proxy host to use for sending request
     * @param proxyPort proxy port to use for sendin request
     *
     * @throws RESTException if unable to create a RESTClient
     */
    public RESTClient(String URL, String proxyHost, int proxyPort)
            throws RESTException {
        this(new RESTConfig(URL, proxyHost, proxyPort));
    }

    /**
     * Creates a RESTClient with the specified URL. No proxy host nor port will
     * be used. 
     *
     * @param URL URL to send request to
     *
     * @throws RESTException if unable to create a RESTClient
     */
    public RESTClient(String URL) throws RESTException {
        this(new RESTConfig(URL));
    }
    
    /**
     * Creates a RESTClient with the RESTConfig object.
     *
     * @param RESTConfig config to use for sending request
     *
     * @throws RESTException if unable to create a RESTClient
     */
    public RESTClient(RESTConfig cfg) throws RESTException {
        this.headers = new LinkedHashMap<String, List<String>>();
        this.parameters = new LinkedHashMap<String, List<String>>();
        this.URL = cfg.getURL();
        this.proxyHost = cfg.getProxyHost();
        this.proxyPort = cfg.getProxyPort();
    }

    /**
     * Adds parameter to be sent during http request.
     * <p>
     * Does not remove any parameters with the same name, thus allowing 
     * duplicates.
     *
     * @param name name of parameter
     * @param value value of parametr
     * @return a reference to 'this', which can be used for method chaining
     */
    public RESTClient addParameter(String name, String value) {
        if (!parameters.containsKey(name)) {
            parameters.put(name, new ArrayList<String>());
        }

        List<String> values = parameters.get(name);
        values.add(value);

        return this;
    }

    /**
     * Sets parameter to be sent during http request.
     * <p>
     * Removes any parameters with the same name, thus disallowing duplicates.
     *
     * @param name name of parameter
     * @param value value of parametr
     * @return a reference to 'this', which can be used for method chaining
     */
    public RESTClient setParameter(String name, String value) {
        if (parameters.containsKey(name)) {
            parameters.get(name).clear();
        }

        addParameter(name, value);

        return this;
    }

    /**
     * Adds http header to be sent during http request.
     * <p>
     * Does not remove any headers with the same name, thus allowing 
     * duplicates.
     *
     * @param name name of header 
     * @param value value of header 
     * @return a reference to 'this', which can be used for method chaining
     */
    public RESTClient addHeader(String name, String value) {
        if (!headers.containsKey(name)) {
            headers.put(name, new ArrayList<String>());
        }

        List<String> values = headers.get(name);
        values.add(value);

        return this;
    }

    /**
     * Sets http header to be sent during http request.
     * <p>
     * Does not remove any headers with the same name, thus allowing 
     * duplicates.
     *
     * @param name name of header 
     * @param value value of header 
     * @return a reference to 'this', which can be used for method chaining
     */
    public RESTClient setHeader(String name, String value) {
        if (headers.containsKey(name)) {
            headers.get(name).clear();
        }

        addHeader(name, value);

        return this;
    }
    
    /**
     * Convenience method for adding the authorization header using the 
     * specified OAuthToken object.
     *
     * @param token token to use for setting authorization
     * @return a reference to 'this,' which can be used for method chaining
     */
    public RESTClient addAuthorizationHeader(String token) {
        this.addHeader("Authorization", token);
        return this;
    }

    /**
     * Alias for httpGet().
     *
     * @see RESTClient#httpGet()
     */
    public APIResponse get() throws RESTException {
        return httpGet();
    }

    /**
     * Sends an http GET request using the parameters and headers previously
     * set.
     *
     * @return api response
     *
     * @throws RESTException if request was unsuccessful
     */
    public APIResponse httpGet() throws RESTException {
        HttpResponse response = null;

        try (CloseableHttpClient httpClient = createClient()) {
            String query = "";
            if (!buildQuery().equals("")) {
                query = "?" + buildQuery();
            }
            HttpGet httpGet = new HttpGet(this.getURL() + query);
            addInternalHeaders(httpGet);

            response = httpClient.execute(httpGet);

            APIResponse apiResponse = buildResponse(response);
            return apiResponse;
        } catch (IOException ioe) {
            throw new RESTException(ioe);
        } finally {
            if (response != null) {
                this.releaseConnection(response);
            }
        }
    }

    /**
     * Alias for httpPost()
     *
     * @see RESTClient#httpPost()
     */
    public APIResponse post() throws RESTException {
        return httpPost();
    }

    /**
     * Sends an http POST request.
     * <p>
     * POST body will be set to the values set using add/setParameter()
     *
     * @return api response
     *
     * @throws RESTException if POST was unsuccessful
     */
    public APIResponse httpPost() throws RESTException {
            APIResponse response = httpPost(buildQuery()); 
            return response;
    }

    /**
     * Sends an http POST request using the specified body.
     *
     * @return api response
     *
     * @throws RESTException if POST was unsuccessful
     */
    public APIResponse httpPost(String body) throws RESTException {
        HttpResponse response = null;
        try (CloseableHttpClient httpClient = createClient()) {
            HttpPost httpPost = new HttpPost(this.getURL());
            addInternalHeaders(httpPost);
            if (body != null && !body.equals("")) {
                httpEntity = new StringEntity(body);
                httpPost.setEntity(new StringEntity(body));
            }

            response = httpClient.execute(httpPost);

            return buildResponse(response);
        } catch (IOException e) {
            throw new RESTException(e);
        } finally {
            if (response != null) {
                this.releaseConnection(response);
            }
        }
    }

    /**
     * 
     * @param body Data to PUT
     * @return API response
     * @throws RESTException 
     */
    public APIResponse httpPut(String body) throws RESTException {
        HttpResponse response = null;
        try (CloseableHttpClient httpClient = createClient()) {

            String query = "";
            if (!buildQuery().equals("")) {
                query = "?" + buildQuery();
            }
            HttpPut httpPut = new HttpPut(this.getURL() + query);
            addInternalHeaders(httpPut);
            if (body != null && !body.equals("")) {
                httpEntity = new StringEntity(body);
                httpPut.setEntity(httpEntity);
            }

            response = httpClient.execute(httpPut);

            return buildResponse(response);
        } catch (IOException e) {
            throw new RESTException(e);
        } finally {
            if (response != null) {
                this.releaseConnection(response);
            }
        }
    }

    /**
     * Alias for httpPatch().
     *
     * @see RESTClient#httpPatch()
     */
    public APIResponse patch(String body) throws RESTException {
        return httpPatch(body);
    }

    /**
     * 
     * @param body Data to PATCH
     * @return API response
     * @throws RESTException 
     */
    public APIResponse httpPatch(String body) throws RESTException {
        HttpResponse response = null;
        try (CloseableHttpClient httpClient = createClient()) {
            String query = "";
            if (!buildQuery().equals("")) {
                query = "?" + buildQuery();
            }
            HttpPatch httpPatch = new HttpPatch(this.getURL() + query);
            addInternalHeaders(httpPatch);
            if (body != null && !body.equals("")) {
                httpEntity = new StringEntity(body);
                httpPatch.setEntity(httpEntity);
            }

            response = httpClient.execute(httpPatch);

            return buildResponse(response);
        } catch (IOException e) {
            throw new RESTException(e);
        } finally {
            if (response != null) {
                this.releaseConnection(response);
            }
        }
    }

    /**
     * Alias for httpDelete().
     *
     * @see RESTClient#httpDelete()
     */
    public APIResponse delete() throws RESTException {
        return httpDelete();
    }

    /**
     * Sends an http DELETE request using the parameters and headers previously
     * set.
     *
     * @return api response
     *
     * @throws RESTException if request was unsuccessful
     */
    public APIResponse httpDelete() throws RESTException {
    	return httpDelete(null);
    }

    /**
     * Sends an http DELETE request with a body, using the parameters and headers
     * previously set.
     *
     * @return api response
     *
     * @throws RESTException if request was unsuccessful
     */
    public APIResponse httpDelete(String body) throws RESTException {
        HttpResponse response = null;

        try (CloseableHttpClient httpClient = createClient()){

            String query = "";
            if (!buildQuery().equals("")) {
                query = "?" + buildQuery();
            }
            HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(this.getURL() + query);
            addInternalHeaders(httpDelete);
 
            if (body != null && !body.equals("")) {
                httpEntity = new StringEntity(body);
                httpDelete.setEntity(httpEntity);
            }

            response = httpClient.execute(httpDelete);

            APIResponse apiResponse = buildResponse(response);
            return apiResponse;
        } catch (IOException ioe) {
            throw new RESTException(ioe);
        } finally {
            if (response != null) {
                this.releaseConnection(response);
            }
        }
    }

    public String getURL() {
        return URL;
    }
    public LinkedHashMap<String,List<String>> getHeaders() {
        return headers;
    }
    public LinkedHashMap<String,List<String>> getParameters() {
        return parameters;
    }
    public HttpEntity getHttpEntity() {
        return httpEntity;
    }

	
	/**
	 * Allows inclusion of a request body with DELETE.
	 */
	private class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
	    public static final String METHOD_NAME = "DELETE";
	 
	    public String getMethod() {
	        return METHOD_NAME;
	    }
	 
	    public HttpDeleteWithBody(final String uri) {
	        super();
	        setURI(URI.create(uri));
	    }
	 
	    public HttpDeleteWithBody(final URI uri) {
	        super();
	        setURI(uri);
	    }
	 
	    public HttpDeleteWithBody() {
	        super();
	    }
	}
}
