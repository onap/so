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

package org.openecomp.mso;


import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.properties.MsoJavaProperties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

/**
 */
public class HealthCheckUtilsTest {

    private static HealthCheckUtils utils;
    private static String port = "8080";
    private static String ip1 = "localhost";
    private static String ip2 = "192.3.2.1";
    private static String iptest = "test/";
    private static String apihUrl1 = "/api/healthcheck";
    private static String apihUrl2 = "/api/healthcheck2";
    private static String bpmnUrl1 = "/bpmn/healthcheck";
    private static String raUrl1 = "/tenants/healthcheck";
    private static String raUrl2 = "/vnf/healthcheck";
    private static String raUrl3 = "/sdnc/healthcheck";
    private static MsoJavaProperties properties;
    private static String sslEnable = "false";
    private static CloseableHttpClient client;
    private static CloseableHttpResponse nokRes, okRes;

    @BeforeClass
    public static void prepareMockvalues() {
        utils = Mockito.mock(HealthCheckUtils.class);
        client = Mockito.mock(CloseableHttpClient.class);
        nokRes = Mockito.mock(CloseableHttpResponse.class);
        okRes = Mockito.mock(CloseableHttpResponse.class);
        Mockito.when(nokRes.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_SERVICE_UNAVAILABLE, "FINE!"));
        ;
        Mockito.when(okRes.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));

        properties = new MsoJavaProperties();
        properties.setProperty("server-port", port);
        properties.setProperty("ssl-enable", sslEnable);
        properties.setProperty("apih-load-balancer", ip1);
        properties.setProperty("apih-healthcheck-urn", apihUrl1 + "," + apihUrl2);
        properties.setProperty("camunda-load-balancer", ip1);
        properties.setProperty("camunda-healthcheck-urn", bpmnUrl1);
        properties.setProperty("jra-load-balancer", ip1);
        properties.setProperty("jra-healthcheck-urn", raUrl1 + "," + raUrl2 + "," + raUrl3);
        properties.setProperty("apih-nodehealthcheck-urn", apihUrl1);
        properties.setProperty("camunda-nodehealthcheck-urn", bpmnUrl1);
        properties.setProperty("jra-nodehealthcheck-urn", raUrl1);

        Mockito.when(utils.loadTopologyProperties()).thenReturn(properties);
        Mockito.when(utils.verifyNodeHealthCheck(HealthCheckUtils.NodeType.APIH, null)).thenCallRealMethod();
        Mockito.when(utils.verifyNodeHealthCheck(HealthCheckUtils.NodeType.RA, null)).thenCallRealMethod();
        Mockito.when(utils.verifyGlobalHealthCheck(true, null)).thenCallRealMethod();
        Mockito.when(utils.verifyGlobalHealthCheck(false, null)).thenCallRealMethod();

        Mockito.when(utils.getFinalUrl(ip1, port, raUrl1, sslEnable)).thenCallRealMethod();
        Mockito.when(utils.getFinalUrl(ip1, port, raUrl1, null)).thenCallRealMethod();
        Mockito.when(utils.getFinalUrl(ip1, port, raUrl1, "true")).thenCallRealMethod();
        Mockito.when(utils.getFinalUrl(ip1, port, raUrl1, "otherValue")).thenCallRealMethod();
        Mockito.when(utils.getFinalUrl(ip1, port, raUrl1, "True")).thenCallRealMethod();
        Mockito.when(utils.getFinalUrl(ip1, port, raUrl1, "TRUE")).thenCallRealMethod();
        Mockito.when(utils.getFinalUrl(ip1, null, raUrl1, null)).thenCallRealMethod();
        Mockito.when(utils.getFinalUrl(iptest, null, raUrl1, null)).thenCallRealMethod();

        System.setProperty("jboss.qualified.host.name", ip1);
    }

    @Test
    public final void testVerifyNodeHealthCheck() {
        Mockito.when(utils.verifyLocalHealth(ip1, port, apihUrl1, sslEnable, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, port, apihUrl2, sslEnable, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip2, port, apihUrl2, sslEnable, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip2, port, apihUrl1, sslEnable, null)).thenReturn(false);
        Mockito.when(utils.verifyLocalHealth(ip1, port, raUrl1, sslEnable, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, port, raUrl2, sslEnable, null)).thenReturn(false);
        Mockito.when(utils.verifyLocalHealth(ip1, port, raUrl3, sslEnable, null)).thenReturn(true);

        assertTrue(utils.verifyNodeHealthCheck(HealthCheckUtils.NodeType.APIH, null));
        assertFalse(utils.verifyNodeHealthCheck(HealthCheckUtils.NodeType.RA, null));

        Mockito.when(utils.verifyLocalHealth(ip1, port, apihUrl1, sslEnable, null)).thenReturn(false);
        Mockito.when(utils.verifyLocalHealth(ip1, port, raUrl2, sslEnable, null)).thenReturn(true);
        assertFalse(utils.verifyNodeHealthCheck(HealthCheckUtils.NodeType.APIH, null));
        assertTrue(utils.verifyNodeHealthCheck(HealthCheckUtils.NodeType.RA, null));

        Mockito.when(utils.verifyLocalHealth(ip2, port, apihUrl1, sslEnable, null)).thenReturn(true);
        assertFalse(utils.verifyNodeHealthCheck(HealthCheckUtils.NodeType.APIH, null));
        assertTrue(utils.verifyNodeHealthCheck(HealthCheckUtils.NodeType.RA, null));

    }

    @Test
    public final void testVerifyGlobalHealthCheckBPMN() {

        // healthcheck of bpmn returns false
        Mockito.when(utils.verifyLocalHealth(ip1, null, bpmnUrl1, null, null)).thenReturn(false);
        Mockito.when(utils.verifyLocalHealth(ip1, null, apihUrl1, null, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, null, raUrl1, null, null)).thenReturn(true);

        // verify BPMN healthcheck
        assertFalse(utils.verifyGlobalHealthCheck(true, null));

        // do not verify BPMN healthcheck
        assertTrue(utils.verifyGlobalHealthCheck(false, null));

        Mockito.when(utils.verifyLocalHealth(ip1, null, bpmnUrl1, null, null)).thenReturn(true);
        assertTrue(utils.verifyGlobalHealthCheck(true, null));
    }

    @Test
    public final void testVerifyGlobalHealthCheckAPIH() {

        Mockito.when(utils.verifyLocalHealth(ip1, null, apihUrl1, null, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, null, raUrl1, null, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, null, bpmnUrl1, null, null)).thenReturn(true);
        assertTrue(utils.verifyGlobalHealthCheck(true, null));

        Mockito.when(utils.verifyLocalHealth(ip1, null, apihUrl1, null, null)).thenReturn(false);
        assertFalse(utils.verifyGlobalHealthCheck(true, null));
    }

    @Test
    public final void testVerifyGlobalHealthCheckRA() {
        // all health check apis returns true
        Mockito.when(utils.verifyLocalHealth(ip1, null, apihUrl1, null, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, null, raUrl1, null, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, null, bpmnUrl1, null, null)).thenReturn(true);
        assertTrue(utils.verifyGlobalHealthCheck(true, null));


        // 3rd ra api return false; others return true
        Mockito.when(utils.verifyLocalHealth(ip1, null, raUrl1, null, null)).thenReturn(false);
        assertFalse(utils.verifyGlobalHealthCheck(true, null));
    }

    @Test
    public final void testGetFinalUrl() {
        String finalUrl1 = utils.getFinalUrl(ip1, port, raUrl1, sslEnable);
        assertTrue(finalUrl1.equals("http://" + ip1 + ":" + port + raUrl1));

        String finalUrl2 = utils.getFinalUrl(ip1, port, raUrl1, "true");
        assertTrue(finalUrl2.equals("https://" + ip1 + ":" + port + raUrl1));

        String finalUrl3 = utils.getFinalUrl(ip1, port, raUrl1, null);
        assertTrue(finalUrl3.equals("http://" + ip1 + ":" + port + raUrl1));

        String finalUrl4 = utils.getFinalUrl(ip1, port, raUrl1, "otherValue");
        assertTrue(finalUrl4.equals("http://" + ip1 + ":" + port + raUrl1));

        String finalUrl5 = utils.getFinalUrl(ip1, port, raUrl1, "True");
        assertTrue(finalUrl5.equals("https://" + ip1 + ":" + port + raUrl1));

        String finalUrl6 = utils.getFinalUrl(ip1, port, raUrl1, "TRUE");
        assertTrue(finalUrl6.equals("https://" + ip1 + ":" + port + raUrl1));

        String finalUrl7 = utils.getFinalUrl(ip1, null, raUrl1, null);
        assertTrue(finalUrl7.equals(ip1 + raUrl1));

        String finalUrl8 = utils.getFinalUrl(iptest, null, raUrl1, null);
        assertTrue(finalUrl8.equals("test" + raUrl1));
    }

    @Test
    public final void testVerifyLocalHealth() {
        HealthCheckUtils tempUtil = Mockito.mock(HealthCheckUtils.class);

        Mockito.when(tempUtil.verifyLocalHealth(ip1, port, apihUrl1, sslEnable, null)).thenCallRealMethod();
        Mockito.when(tempUtil.getFinalUrl(ip1, port, apihUrl1, sslEnable)).thenCallRealMethod();
        Mockito.when(tempUtil.getHttpClient()).thenReturn(client);

        try {
            Mockito.when(client.execute(any(HttpUriRequest.class))).thenReturn(okRes);
            boolean res1 = tempUtil.verifyLocalHealth(ip1, port, apihUrl1, sslEnable, null);
            assertTrue(res1);

            Mockito.when(client.execute(any(HttpUriRequest.class))).thenReturn(nokRes);
            boolean res2 = tempUtil.verifyLocalHealth(ip1, port, apihUrl1, sslEnable, null);
            assertFalse(res2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public final void NullityCheck() {
        Mockito.when(utils.verifyLocalHealth(ip1, null, bpmnUrl1, null, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, null, apihUrl1, null, null)).thenReturn(true);
        Mockito.when(utils.verifyLocalHealth(ip1, null, raUrl1, null, null)).thenReturn(true);

        assertTrue(utils.verifyGlobalHealthCheck(true, null));

        // mising server-camunda parameter
        MsoJavaProperties newProperties1 = new MsoJavaProperties();
        Mockito.when(utils.loadTopologyProperties()).thenReturn(newProperties1);

        newProperties1.setProperty("apih-load-balancer", ip1);
        newProperties1.setProperty("apih-nodehealthcheck-urn", apihUrl1);
        newProperties1.setProperty("jra-load-balancer", ip1);
        newProperties1.setProperty("jra-nodehealthcheck-urn", raUrl1);

        assertFalse(utils.verifyGlobalHealthCheck(true, null));

        // mising apih-server-list parameter
        MsoJavaProperties newProperties2 = new MsoJavaProperties();
        Mockito.when(utils.loadTopologyProperties()).thenReturn(newProperties2);

        newProperties2.setProperty("server-port", port);
        newProperties2.setProperty("apih-nodehealthcheck-urn", apihUrl1);
        newProperties2.setProperty("camunda-load-balancer", ip1);
        newProperties2.setProperty("camunda-nodehealthcheck-urn", bpmnUrl1);
        newProperties2.setProperty("jra-load-balancer", ip1);
        newProperties2.setProperty("jra-nodehealthcheck-urn", raUrl1);

        assertFalse(utils.verifyGlobalHealthCheck(true, null));

        // mising jra-healthcheck-urn parameter
        MsoJavaProperties newProperties3 = new MsoJavaProperties();
        Mockito.when(utils.loadTopologyProperties()).thenReturn(newProperties3);

        newProperties3.setProperty("server-port", port);
        newProperties3.setProperty("apih-load-balancer", ip1);
        newProperties3.setProperty("apih-nodehealthcheck-urn", apihUrl1);
        newProperties3.setProperty("camunda-load-balancer", ip1);
        newProperties3.setProperty("camunda-nodehealthcheck-urn", bpmnUrl1);
        newProperties3.setProperty("jra-load-balancer", ip1);
        newProperties3.setProperty("jra-server-list", ip1);

        assertFalse(utils.verifyGlobalHealthCheck(true, null));

        Mockito.when(utils.loadTopologyProperties()).thenReturn(properties);
    }

}