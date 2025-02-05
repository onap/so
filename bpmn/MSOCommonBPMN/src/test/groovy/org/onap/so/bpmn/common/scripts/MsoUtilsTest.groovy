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

package org.onap.so.bpmn.common.scripts

import groovy.util.slurpersupport.NodeChild
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.assertEquals

// JUnit 4

class MsoUtilsTest {
		   
		def utils = new MsoUtils()
		def origXmlResponse = null
		// Expected rebuilds
		def expected_buildElements = "<tns2:internet-service-change-details><tns2:internet-evc-speed-value>10</tns2:internet-evc-speed-value><tns2:internet-evc-speed-units>Kbps</tns2:internet-evc-speed-units><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.15</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>2</tns2:request-index><tns2:v4-next-hop-address>192.168.1.16</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.16</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes></tns2:internet-service-change-details>"
	    def expected_rebuildDhcp = "<tns2:dhcp><tns2:v4-dhcp-server-enabled>Y</tns2:v4-dhcp-server-enabled><tns2:v6-dhcp-server-enabled>N</tns2:v6-dhcp-server-enabled><tns2:use-v4-default-pool>Y</tns2:use-v4-default-pool><tns2:excluded-v4-dhcp-addresses-from-default-pool><tns2:excluded-v4-address>192.168.1.7</tns2:excluded-v4-address></tns2:excluded-v4-dhcp-addresses-from-default-pool><tns2:excluded-v4-dhcp-addresses-from-default-pool><tns2:excluded-v4-address>192.168.1.8</tns2:excluded-v4-address></tns2:excluded-v4-dhcp-addresses-from-default-pool><tns2:v4-dhcp-pools><tns2:v4-dhcp-pool-prefix>192.155.2.3</tns2:v4-dhcp-pool-prefix><tns2:v4-dhcp-pool-prefix-length>28</tns2:v4-dhcp-pool-prefix-length><tns2:excluded-v4-addresses><tns2:excluded-v4-address>192.168.1.5</tns2:excluded-v4-address></tns2:excluded-v4-addresses><tns2:excluded-v4-addresses><tns2:excluded-v4-address>192.168.1.6</tns2:excluded-v4-address></tns2:excluded-v4-addresses><tns2:v4-dhcp-relay-gateway-address>2.2.2.1</tns2:v4-dhcp-relay-gateway-address><tns2:v4-dhcp-relay-next-hop-address>1.1.1.1</tns2:v4-dhcp-relay-next-hop-address></tns2:v4-dhcp-pools><tns2:v4-dhcp-pools><tns2:v4-dhcp-pool-prefix>192.155.2.4</tns2:v4-dhcp-pool-prefix><tns2:v4-dhcp-pool-prefix-length>28</tns2:v4-dhcp-pool-prefix-length><tns2:excluded-v4-addresses><tns2:excluded-v4-address>192.168.1.6</tns2:excluded-v4-address></tns2:excluded-v4-addresses><tns2:excluded-v4-addresses><tns2:excluded-v4-address>192.168.1.7</tns2:excluded-v4-address></tns2:excluded-v4-addresses><tns2:v4-dhcp-relay-gateway-address>2.2.2.2</tns2:v4-dhcp-relay-gateway-address><tns2:v4-dhcp-relay-next-hop-address>1.1.1.2</tns2:v4-dhcp-relay-next-hop-address></tns2:v4-dhcp-pools><tns2:use-v6-default-pool>N</tns2:use-v6-default-pool><tns2:excluded-v6-dhcp-addresses-from-default-pool><tns2:excluded-v6-address>1:5</tns2:excluded-v6-address></tns2:excluded-v6-dhcp-addresses-from-default-pool><tns2:excluded-v6-dhcp-addresses-from-default-pool><tns2:excluded-v6-address>1:6</tns2:excluded-v6-address></tns2:excluded-v6-dhcp-addresses-from-default-pool><tns2:v6-dhcp-pools><tns2:v6-dhcp-pool-prefix>0:0</tns2:v6-dhcp-pool-prefix><tns2:v6-dhcp-pool-prefix-length>28</tns2:v6-dhcp-pool-prefix-length><tns2:excluded-v6-addresses><tns2:excluded-v6-address>1:1</tns2:excluded-v6-address></tns2:excluded-v6-addresses><tns2:excluded-v6-addresses><tns2:excluded-v6-address>2:2</tns2:excluded-v6-address></tns2:excluded-v6-addresses><tns2:v6-dhcp-relay-gateway-address>3:3</tns2:v6-dhcp-relay-gateway-address><tns2:v6-dhcp-relay-next-hop-address>4:4</tns2:v6-dhcp-relay-next-hop-address></tns2:v6-dhcp-pools><tns2:v6-dhcp-pools><tns2:v6-dhcp-pool-prefix>0:0</tns2:v6-dhcp-pool-prefix><tns2:v6-dhcp-pool-prefix-length>28</tns2:v6-dhcp-pool-prefix-length><tns2:excluded-v6-addresses><tns2:excluded-v6-address>1:1</tns2:excluded-v6-address></tns2:excluded-v6-addresses><tns2:excluded-v6-addresses><tns2:excluded-v6-address>2:2</tns2:excluded-v6-address></tns2:excluded-v6-addresses><tns2:v6-dhcp-relay-gateway-address>3:3</tns2:v6-dhcp-relay-gateway-address><tns2:v6-dhcp-relay-next-hop-address>4:4</tns2:v6-dhcp-relay-next-hop-address></tns2:v6-dhcp-pools></tns2:dhcp>" 
	    def expected_rebuildFirewallLite = "<tns2:firewall-lite><tns2:stateful-firewall-lite-v4-enabled>Y</tns2:stateful-firewall-lite-v4-enabled><tns2:stateful-firewall-lite-v6-enabled>N</tns2:stateful-firewall-lite-v6-enabled><tns2:v4-firewall-packet-filters><tns2:v4-firewall-prefix>0.0.0.1</tns2:v4-firewall-prefix><tns2:v4-firewall-prefix-length>1</tns2:v4-firewall-prefix-length><tns2:allow-icmp-ping>Y</tns2:allow-icmp-ping><tns2:udp-ports><tns2:port-number>1</tns2:port-number></tns2:udp-ports><tns2:tcp-ports><tns2:port-number>1</tns2:port-number></tns2:tcp-ports></tns2:v4-firewall-packet-filters><tns2:v4-firewall-packet-filters><tns2:v4-firewall-prefix>0.0.0.2</tns2:v4-firewall-prefix><tns2:v4-firewall-prefix-length>2</tns2:v4-firewall-prefix-length><tns2:allow-icmp-ping>Y</tns2:allow-icmp-ping><tns2:udp-ports><tns2:port-number>2</tns2:port-number></tns2:udp-ports><tns2:tcp-ports><tns2:port-number>2</tns2:port-number></tns2:tcp-ports></tns2:v4-firewall-packet-filters><tns2:v6-firewall-packet-filters><tns2:v6-firewall-prefix>:</tns2:v6-firewall-prefix><tns2:v6-firewall-prefix-length>0</tns2:v6-firewall-prefix-length><tns2:allow-icmp-ping>Y</tns2:allow-icmp-ping><tns2:udp-ports><tns2:port-number>3</tns2:port-number></tns2:udp-ports><tns2:tcp-ports><tns2:port-number>3</tns2:port-number></tns2:tcp-ports></tns2:v6-firewall-packet-filters><tns2:v6-firewall-packet-filters><tns2:v6-firewall-prefix>:</tns2:v6-firewall-prefix><tns2:v6-firewall-prefix-length>1</tns2:v6-firewall-prefix-length><tns2:allow-icmp-ping>Y</tns2:allow-icmp-ping><tns2:udp-ports><tns2:port-number>4</tns2:port-number></tns2:udp-ports><tns2:tcp-ports><tns2:port-number>4</tns2:port-number></tns2:tcp-ports></tns2:v6-firewall-packet-filters></tns2:firewall-lite>" 
	    def expected_rebuildInternetEvcAccess = "<tns2:internet-evc-access-information><tns2:internet-evc-speed-value>8</tns2:internet-evc-speed-value><tns2:internet-evc-speed-units>Mbps</tns2:internet-evc-speed-units><tns2:ip-version>ds</tns2:ip-version></tns2:internet-evc-access-information>"
	    def expected_rebuildInternetServiceChangeDetails = "<tns:internet-service-change-details><tns2:internet-evc-speed-value>10</tns2:internet-evc-speed-value><tns2:internet-evc-speed-units>Kbps</tns2:internet-evc-speed-units><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.15</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>2</tns2:request-index><tns2:v4-next-hop-address>192.168.1.16</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.16</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:2da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:2da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes></tns:internet-service-change-details>"
	    def expected_rebuildInternetServiceChangeDetailsWithVrLanParams = "<tns:internet-service-change-details><tns2:internet-evc-speed-value>10</tns2:internet-evc-speed-value><tns2:internet-evc-speed-units>Kbps</tns2:internet-evc-speed-units><tns2:v4-vr-lan-address>10.10.7.14</tns2:v4-vr-lan-address><tns2:v4-vr-lan-prefix-length>10</tns2:v4-vr-lan-prefix-length><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.15</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>2</tns2:request-index><tns2:v4-next-hop-address>192.168.1.16</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.16</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:2da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:2da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes></tns:internet-service-change-details>"
	    def expected_rebuildL2Home = "<tns2:l2-homing-information><tns2:evc-name>AS/VLXM/003717//SW</tns2:evc-name><tns2:topology>MultiPoint</tns2:topology><tns2:preferred-aic-clli>MTSNJA4LCP1</tns2:preferred-aic-clli></tns2:l2-homing-information>"
	    def expected_rebuildL2HomeFor_aic_clli = "<tns2:l2-homing-information><tns2:evc-name>AS/VLXM/003717//SW</tns2:evc-name><tns2:topology>MultiPoint</tns2:topology><tns2:preferred-aic-clli>MTSNJA4LCP1</tns2:preferred-aic-clli><tns2:aic-version>2.5</tns2:aic-version></tns2:l2-homing-information>"
	    def expected_rebuildNat = "<tns2:nat><tns2:v4-nat-enabled>Y</tns2:v4-nat-enabled><tns2:v4-nat-mapping-entries><tns2:v4-nat-internal>0.0.0.0</tns2:v4-nat-internal><tns2:v4-nat-next-hop-address>0.0.0.0</tns2:v4-nat-next-hop-address><tns2:v4-nat-external>0.0.0.0</tns2:v4-nat-external></tns2:v4-nat-mapping-entries><tns2:v4-nat-mapping-entries><tns2:v4-nat-internal>0.0.0.1</tns2:v4-nat-internal><tns2:v4-nat-next-hop-address>0.0.0.1</tns2:v4-nat-next-hop-address><tns2:v4-nat-external>0.0.0.1</tns2:v4-nat-external></tns2:v4-nat-mapping-entries></tns2:nat>"
	    def expected_rebuildPat = "<tns2:pat><tns2:v4-pat-enabled>N</tns2:v4-pat-enabled><tns2:use-v4-default-pool>Y</tns2:use-v4-default-pool><tns2:v4-pat-pools><tns2:v4-pat-pool-prefix>192.168.1.44</tns2:v4-pat-pool-prefix><tns2:v4-pat-pool-prefix-length>0</tns2:v4-pat-pool-prefix-length><tns2:v4-pat-pool-next-hop-address>192.168.1.5</tns2:v4-pat-pool-next-hop-address></tns2:v4-pat-pools><tns2:v4-pat-pools><tns2:v4-pat-pool-prefix>192.168.1.45</tns2:v4-pat-pool-prefix><tns2:v4-pat-pool-prefix-length>28</tns2:v4-pat-pool-prefix-length><tns2:v4-pat-pool-next-hop-address>192.168.1.6</tns2:v4-pat-pool-next-hop-address></tns2:v4-pat-pools></tns2:pat>"
	    def expected_rebuildStaticRoutes = "<tns2:static-routes><tns2:v4-static-routes><tns2:v4-static-route-prefix>255.255.252.1</tns2:v4-static-route-prefix><tns2:v4-static-route-prefix-length>28</tns2:v4-static-route-prefix-length><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address></tns2:v4-static-routes><tns2:v4-static-routes><tns2:v4-static-route-prefix>255.255.252.2</tns2:v4-static-route-prefix><tns2:v4-static-route-prefix-length>28</tns2:v4-static-route-prefix-length><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address></tns2:v4-static-routes><tns2:v4-static-routes><tns2:v4-static-route-prefix>255.255.252.3</tns2:v4-static-route-prefix><tns2:v4-static-route-prefix-length>28</tns2:v4-static-route-prefix-length><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address></tns2:v4-static-routes><tns2:v6-static-routes><tns2:v6-static-route-prefix>2001:1890:12e3:2da::</tns2:v6-static-route-prefix><tns2:v6-static-route-prefix-length>28</tns2:v6-static-route-prefix-length><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address></tns2:v6-static-routes><tns2:v6-static-routes><tns2:v6-static-route-prefix>2001:1890:12e3:2da::</tns2:v6-static-route-prefix><tns2:v6-static-route-prefix-length>28</tns2:v6-static-route-prefix-length><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address></tns2:v6-static-routes></tns2:static-routes>"
	    def expected_rebuildUcpeVmsServiceInformation = "<tns2:ucpe-vms-service-information><tns2:transport-service-information><tns2:transport-service-type>AVPN</tns2:transport-service-type><tns2:access-circuit-info><tns2:access-circuit-id>1</tns2:access-circuit-id><tns2:dual-mode>Active</tns2:dual-mode></tns2:access-circuit-info><tns2:access-circuit-info><tns2:access-circuit-id>2</tns2:access-circuit-id><tns2:dual-mode>Standby</tns2:dual-mode></tns2:access-circuit-info></tns2:transport-service-information><tns2:ucpe-information><tns2:ucpe-host-name>hostname</tns2:ucpe-host-name><tns2:ucpe-activation-code>activecode</tns2:ucpe-activation-code><tns2:out-of-band-management-modem>OOB</tns2:out-of-band-management-modem></tns2:ucpe-information><tns2:vnf-list><tns2:vnf-information><tns2:vnf-instance-id>1</tns2:vnf-instance-id><tns2:vnf-sequence-number>1</tns2:vnf-sequence-number><tns2:vnf-type>ZZ</tns2:vnf-type><tns2:vnf-vendor>JUNIPER</tns2:vnf-vendor><tns2:vnf-model>MODEL1</tns2:vnf-model><tns2:vnf-id>1</tns2:vnf-id><tns2:prov-status>1</tns2:prov-status><tns2:operational-state>1</tns2:operational-state><tns2:orchestration-status>1</tns2:orchestration-status><tns2:equipment-role>1</tns2:equipment-role></tns2:vnf-information><tns2:vnf-information><tns2:vnf-instance-id>2</tns2:vnf-instance-id><tns2:vnf-sequence-number>2</tns2:vnf-sequence-number><tns2:vnf-type>HY</tns2:vnf-type><tns2:vnf-vendor>JUNIPER</tns2:vnf-vendor><tns2:vnf-model>MODEL2</tns2:vnf-model><tns2:vnf-id>2</tns2:vnf-id><tns2:prov-status>2</tns2:prov-status><tns2:operational-state>2</tns2:operational-state><tns2:orchestration-status>2</tns2:orchestration-status><tns2:equipment-role>2</tns2:equipment-role></tns2:vnf-information></tns2:vnf-list></tns2:ucpe-vms-service-information>"
	    def expected_rebuildVrLan = "<tns2:vr-lan><tns2:routing-protocol>none</tns2:routing-protocol><tns2:vr-lan-interface><tns2:vr-designation>primary</tns2:vr-designation><tns2:v4-vr-lan-prefix>10.192.27.254</tns2:v4-vr-lan-prefix><tns2:v4-vr-lan-prefix-length>24</tns2:v4-vr-lan-prefix-length><tns2:v6-vr-lan-prefix>2620:0:10d0:f:ffff:ffff:ffff:fffe</tns2:v6-vr-lan-prefix><tns2:v6-vr-lan-prefix-length>64</tns2:v6-vr-lan-prefix-length><tns2:v4-vce-loopback-address>162.200.3.144</tns2:v4-vce-loopback-address><tns2:v6-vce-wan-address>2001:1890:12e3:2da::</tns2:v6-vce-wan-address><tns2:v4-public-lan-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.2</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.1</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.72</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.71</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.68</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.67</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes></tns2:v4-public-lan-prefixes><tns2:v6-public-lan-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:2da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:3da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:4da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes></tns2:v6-public-lan-prefixes><tns2:dhcp><tns2:v4-dhcp-server-enabled>Y</tns2:v4-dhcp-server-enabled><tns2:v6-dhcp-server-enabled>N</tns2:v6-dhcp-server-enabled><tns2:use-v4-default-pool>Y</tns2:use-v4-default-pool><tns2:excluded-v4-dhcp-addresses-from-default-pool><tns2:excluded-v4-address>192.168.1.7</tns2:excluded-v4-address></tns2:excluded-v4-dhcp-addresses-from-default-pool><tns2:excluded-v4-dhcp-addresses-from-default-pool><tns2:excluded-v4-address>192.168.1.8</tns2:excluded-v4-address></tns2:excluded-v4-dhcp-addresses-from-default-pool><tns2:v4-dhcp-pools><tns2:v4-dhcp-pool-prefix>192.155.2.3</tns2:v4-dhcp-pool-prefix><tns2:v4-dhcp-pool-prefix-length>28</tns2:v4-dhcp-pool-prefix-length><tns2:excluded-v4-addresses><tns2:excluded-v4-address>192.168.1.5</tns2:excluded-v4-address></tns2:excluded-v4-addresses><tns2:excluded-v4-addresses><tns2:excluded-v4-address>192.168.1.6</tns2:excluded-v4-address></tns2:excluded-v4-addresses><tns2:v4-dhcp-relay-gateway-address>2.2.2.1</tns2:v4-dhcp-relay-gateway-address><tns2:v4-dhcp-relay-next-hop-address>1.1.1.1</tns2:v4-dhcp-relay-next-hop-address></tns2:v4-dhcp-pools><tns2:v4-dhcp-pools><tns2:v4-dhcp-pool-prefix>192.155.2.4</tns2:v4-dhcp-pool-prefix><tns2:v4-dhcp-pool-prefix-length>28</tns2:v4-dhcp-pool-prefix-length><tns2:excluded-v4-addresses><tns2:excluded-v4-address>192.168.1.6</tns2:excluded-v4-address></tns2:excluded-v4-addresses><tns2:excluded-v4-addresses><tns2:excluded-v4-address>192.168.1.7</tns2:excluded-v4-address></tns2:excluded-v4-addresses><tns2:v4-dhcp-relay-gateway-address>2.2.2.2</tns2:v4-dhcp-relay-gateway-address><tns2:v4-dhcp-relay-next-hop-address>1.1.1.2</tns2:v4-dhcp-relay-next-hop-address></tns2:v4-dhcp-pools><tns2:use-v6-default-pool>N</tns2:use-v6-default-pool><tns2:excluded-v6-dhcp-addresses-from-default-pool><tns2:excluded-v6-address>1:5</tns2:excluded-v6-address></tns2:excluded-v6-dhcp-addresses-from-default-pool><tns2:excluded-v6-dhcp-addresses-from-default-pool><tns2:excluded-v6-address>1:6</tns2:excluded-v6-address></tns2:excluded-v6-dhcp-addresses-from-default-pool><tns2:v6-dhcp-pools><tns2:v6-dhcp-pool-prefix>0:0</tns2:v6-dhcp-pool-prefix><tns2:v6-dhcp-pool-prefix-length>28</tns2:v6-dhcp-pool-prefix-length><tns2:excluded-v6-addresses><tns2:excluded-v6-address>1:1</tns2:excluded-v6-address></tns2:excluded-v6-addresses><tns2:excluded-v6-addresses><tns2:excluded-v6-address>2:2</tns2:excluded-v6-address></tns2:excluded-v6-addresses><tns2:v6-dhcp-relay-gateway-address>3:3</tns2:v6-dhcp-relay-gateway-address><tns2:v6-dhcp-relay-next-hop-address>4:4</tns2:v6-dhcp-relay-next-hop-address></tns2:v6-dhcp-pools><tns2:v6-dhcp-pools><tns2:v6-dhcp-pool-prefix>0:0</tns2:v6-dhcp-pool-prefix><tns2:v6-dhcp-pool-prefix-length>28</tns2:v6-dhcp-pool-prefix-length><tns2:excluded-v6-addresses><tns2:excluded-v6-address>1:1</tns2:excluded-v6-address></tns2:excluded-v6-addresses><tns2:excluded-v6-addresses><tns2:excluded-v6-address>2:2</tns2:excluded-v6-address></tns2:excluded-v6-addresses><tns2:v6-dhcp-relay-gateway-address>3:3</tns2:v6-dhcp-relay-gateway-address><tns2:v6-dhcp-relay-next-hop-address>4:4</tns2:v6-dhcp-relay-next-hop-address></tns2:v6-dhcp-pools></tns2:dhcp><tns2:pat><tns2:v4-pat-enabled>N</tns2:v4-pat-enabled><tns2:use-v4-default-pool>Y</tns2:use-v4-default-pool><tns2:v4-pat-pools><tns2:v4-pat-pool-prefix>192.168.1.44</tns2:v4-pat-pool-prefix><tns2:v4-pat-pool-prefix-length>0</tns2:v4-pat-pool-prefix-length><tns2:v4-pat-pool-next-hop-address>192.168.1.5</tns2:v4-pat-pool-next-hop-address></tns2:v4-pat-pools><tns2:v4-pat-pools><tns2:v4-pat-pool-prefix>192.168.1.45</tns2:v4-pat-pool-prefix><tns2:v4-pat-pool-prefix-length>28</tns2:v4-pat-pool-prefix-length><tns2:v4-pat-pool-next-hop-address>192.168.1.6</tns2:v4-pat-pool-next-hop-address></tns2:v4-pat-pools></tns2:pat><tns2:nat><tns2:v4-nat-enabled>Y</tns2:v4-nat-enabled><tns2:v4-nat-mapping-entries><tns2:v4-nat-internal>0.0.0.0</tns2:v4-nat-internal><tns2:v4-nat-next-hop-address>0.0.0.0</tns2:v4-nat-next-hop-address><tns2:v4-nat-external>0.0.0.0</tns2:v4-nat-external></tns2:v4-nat-mapping-entries><tns2:v4-nat-mapping-entries><tns2:v4-nat-internal>0.0.0.1</tns2:v4-nat-internal><tns2:v4-nat-next-hop-address>0.0.0.1</tns2:v4-nat-next-hop-address><tns2:v4-nat-external>0.0.0.1</tns2:v4-nat-external></tns2:v4-nat-mapping-entries></tns2:nat><tns2:firewall-lite><tns2:stateful-firewall-lite-v4-enabled>Y</tns2:stateful-firewall-lite-v4-enabled><tns2:stateful-firewall-lite-v6-enabled>N</tns2:stateful-firewall-lite-v6-enabled><tns2:v4-firewall-packet-filters><tns2:v4-firewall-prefix>0.0.0.1</tns2:v4-firewall-prefix><tns2:v4-firewall-prefix-length>1</tns2:v4-firewall-prefix-length><tns2:allow-icmp-ping>Y</tns2:allow-icmp-ping><tns2:udp-ports><tns2:port-number>1</tns2:port-number></tns2:udp-ports><tns2:tcp-ports><tns2:port-number>1</tns2:port-number></tns2:tcp-ports></tns2:v4-firewall-packet-filters><tns2:v4-firewall-packet-filters><tns2:v4-firewall-prefix>0.0.0.2</tns2:v4-firewall-prefix><tns2:v4-firewall-prefix-length>2</tns2:v4-firewall-prefix-length><tns2:allow-icmp-ping>Y</tns2:allow-icmp-ping><tns2:udp-ports><tns2:port-number>2</tns2:port-number></tns2:udp-ports><tns2:tcp-ports><tns2:port-number>2</tns2:port-number></tns2:tcp-ports></tns2:v4-firewall-packet-filters><tns2:v6-firewall-packet-filters><tns2:v6-firewall-prefix>:</tns2:v6-firewall-prefix><tns2:v6-firewall-prefix-length>0</tns2:v6-firewall-prefix-length><tns2:allow-icmp-ping>Y</tns2:allow-icmp-ping><tns2:udp-ports><tns2:port-number>3</tns2:port-number></tns2:udp-ports><tns2:tcp-ports><tns2:port-number>3</tns2:port-number></tns2:tcp-ports></tns2:v6-firewall-packet-filters><tns2:v6-firewall-packet-filters><tns2:v6-firewall-prefix>:</tns2:v6-firewall-prefix><tns2:v6-firewall-prefix-length>1</tns2:v6-firewall-prefix-length><tns2:allow-icmp-ping>Y</tns2:allow-icmp-ping><tns2:udp-ports><tns2:port-number>4</tns2:port-number></tns2:udp-ports><tns2:tcp-ports><tns2:port-number>4</tns2:port-number></tns2:tcp-ports></tns2:v6-firewall-packet-filters></tns2:firewall-lite><tns2:static-routes><tns2:v4-static-routes><tns2:v4-static-route-prefix>255.255.252.1</tns2:v4-static-route-prefix><tns2:v4-static-route-prefix-length>28</tns2:v4-static-route-prefix-length><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address></tns2:v4-static-routes><tns2:v4-static-routes><tns2:v4-static-route-prefix>255.255.252.2</tns2:v4-static-route-prefix><tns2:v4-static-route-prefix-length>28</tns2:v4-static-route-prefix-length><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address></tns2:v4-static-routes><tns2:v4-static-routes><tns2:v4-static-route-prefix>255.255.252.3</tns2:v4-static-route-prefix><tns2:v4-static-route-prefix-length>28</tns2:v4-static-route-prefix-length><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address></tns2:v4-static-routes><tns2:v6-static-routes><tns2:v6-static-route-prefix>2001:1890:12e3:2da::</tns2:v6-static-route-prefix><tns2:v6-static-route-prefix-length>28</tns2:v6-static-route-prefix-length><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address></tns2:v6-static-routes><tns2:v6-static-routes><tns2:v6-static-route-prefix>2001:1890:12e3:2da::</tns2:v6-static-route-prefix><tns2:v6-static-route-prefix-length>28</tns2:v6-static-route-prefix-length><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address></tns2:v6-static-routes></tns2:static-routes></tns2:vr-lan-interface></tns2:vr-lan>"
	    def expected_rebuildVrLanInterfacePartial = "<tns2:vr-designation>primary</tns2:vr-designation><tns2:v4-vr-lan-prefix>10.192.27.254</tns2:v4-vr-lan-prefix><tns2:v4-vr-lan-prefix-length>24</tns2:v4-vr-lan-prefix-length><tns2:v6-vr-lan-prefix>2620:0:10d0:f:ffff:ffff:ffff:fffe</tns2:v6-vr-lan-prefix><tns2:v6-vr-lan-prefix-length>64</tns2:v6-vr-lan-prefix-length><tns2:v4-vce-loopback-address>162.200.3.144</tns2:v4-vce-loopback-address><tns2:v6-vce-wan-address>2001:1890:12e3:2da::</tns2:v6-vce-wan-address><tns2:v4-public-lan-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.2</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.1</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.72</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.71</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.68</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.67</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v4-next-hop-address>192.168.1.15</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.15</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes><tns2:t-provided-v4-lan-public-prefixes><tns2:request-index>2</tns2:request-index><tns2:v4-next-hop-address>192.168.1.16</tns2:v4-next-hop-address><tns2:v4-lan-public-prefix>192.168.1.16</tns2:v4-lan-public-prefix><tns2:v4-lan-public-prefix-length>28</tns2:v4-lan-public-prefix-length></tns2:t-provided-v4-lan-public-prefixes></tns2:v4-public-lan-prefixes><tns2:v6-public-lan-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:2da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:3da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:4da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:2da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes><tns2:t-provided-v6-lan-public-prefixes><tns2:request-index>1</tns2:request-index><tns2:v6-next-hop-address>2001:1890:12e3:2da::</tns2:v6-next-hop-address><tns2:v6-lan-public-prefix>2001:1890:12e3:2da::</tns2:v6-lan-public-prefix><tns2:v6-lan-public-prefix-length>28</tns2:v6-lan-public-prefix-length></tns2:t-provided-v6-lan-public-prefixes></tns2:v6-public-lan-prefixes>"
		
	    @Before
		public void setUp() { 
			def responseAsString = getFile("sdncadaptercallbackrequest.xml")
			def varrequestData=utils.getNodeText(responseAsString,"RequestData")
			origXmlResponse = utils.getNodeXml(varrequestData, "layer3-service-list").drop(38).trim()

		}
	
		@Test
		public void testBuildL2Home() {
			def rebuildL2Home = utils.buildL2HomingInformation(origXmlResponse)
			println " rebuildL2Home: "
			println "  actual    - " + rebuildL2Home 
			println "  expected  - " + expected_rebuildL2Home
			assertEquals("rebuildL2Home - expected vs actual", expected_rebuildL2Home, rebuildL2Home)
		}

		@Test
		public void testBuildInternetEvcAccess() {
			def rebuildInternetEvcAccess = utils.buildInternetEvcAccessInformation(origXmlResponse)
			println " rebuildInternetEvcAccess: "
			println "  actual    - " + rebuildInternetEvcAccess
			println "  expected  - " + expected_rebuildInternetEvcAccess
			assertEquals("rebuildInternetEvcAccess - expected vs actual", expected_rebuildInternetEvcAccess, rebuildInternetEvcAccess)
		}

		@Test
		public void testBuildInternetServiceChangeDetails() {
			def rebuildInternetServiceChangeDetails = utils.buildInternetServiceChangeDetails(origXmlResponse)
			println " rebuildInternetServiceChangeDetails: "
			println "  actual    - " + rebuildInternetServiceChangeDetails
			println "  expected  - " + expected_rebuildInternetServiceChangeDetails
			assertEquals("rebuildInternetServiceChangeDetails - expected vs actual", expected_rebuildInternetServiceChangeDetails, rebuildInternetServiceChangeDetails)
		}

        @Test
        public void testBuildInternetServiceChangeDetailsWithVrLanParams() {
            def responseAsString = getFile("sdncadaptercallbackrequest_with_aic_version.xml")
            def varrequestData=utils.getNodeText(responseAsString,"RequestData")
            def xmlResponse = utils.getNodeXml(varrequestData, "layer3-service-list").drop(38).trim()
            def rebuildInternetServiceChangeDetails = utils.buildInternetServiceChangeDetails(xmlResponse)
            println " rebuildInternetServiceChangeDetails: "
            println "  actual    - " + rebuildInternetServiceChangeDetails
            println "  expected  - " + expected_rebuildInternetServiceChangeDetails
            assertEquals("rebuildInternetServiceChangeDetails - expected vs actual",  expected_rebuildInternetServiceChangeDetailsWithVrLanParams, rebuildInternetServiceChangeDetails)
        }
		
		// Coming v100
		@Test
		@Ignore
		public void testBuildUcpeVmsServiceInformation() {
			def rebuildUcpeVmsServiceInformation = utils.buildUcpeVmsServiceInformation(origXmlResponse)
			println " rebuildUcpeVmsServiceInformation: "
			println "  actual    - " + rebuildUcpeVmsServiceInformation
			println "  expected  - " + expected_rebuildUcpeVmsServiceInformation
			assertEquals("rebuildUcpeVmsServiceInformation - expected vs actual", expected_rebuildUcpeVmsServiceInformation, rebuildUcpeVmsServiceInformation)
		}

		@Test
		public void testBuildElements() {
			// testing utility codes: buildElements() & buildElementsUnblunded()
			def internetServiceChangeDetails = utils.getNodeXml(origXmlResponse, "internet-service-change-details").drop(38).trim()
			def buildElements = ''
			buildElements = "<tns2:internet-service-change-details>"
			buildElements += utils.buildElements(internetServiceChangeDetails, ["internet-evc-speed-value"], "")
			buildElements += utils.buildElements(internetServiceChangeDetails, ["internet-evc-speed-units"], "")
			def tProvidedV4LanPublicPrefixesChangesList = ["request-index", "v4-next-hop-address", "v4-lan-public-prefix", "v4-lan-public-prefix-length"]
			buildElements += utils.buildElementsUnbounded(internetServiceChangeDetails, tProvidedV4LanPublicPrefixesChangesList, "t-provided-v4-lan-public-prefixes")
			buildElements += "</tns2:internet-service-change-details>"
			println " buildElements: "
			println "  actual    - " + buildElements
			println "  expected  - " + expected_buildElements
			assertEquals("buildElements - expected vs actual", expected_buildElements, buildElements)
		} 
		
		@Test
		public void testBuildVrLan() {
			def rebuildVrLan = utils.buildVrLan(origXmlResponse)
			println " rebuildVrLans: "
			println "  actual    - " + rebuildVrLan
			println "  expected  - " + expected_rebuildVrLan
			assertEquals("rebuildVrLan - expected vs actual", expected_rebuildVrLan, rebuildVrLan)
		}
		
		@Test
		public void testBuildVrLanInterfacePartial() {
			def rebuildVrLanInterfacePartial = utils.buildVrLanInterfacePartial(origXmlResponse)
			println " rebuildVrLanInterfacePartial: "
			println "  actual    - " + rebuildVrLanInterfacePartial
			println "  expected  - " + expected_rebuildVrLanInterfacePartial
			assertEquals("rebuildVrLanInterfacePartial - expected vs actual", expected_rebuildVrLanInterfacePartial, rebuildVrLanInterfacePartial)
		}
		
		@Test
		public void testBuildDhcp() {
			def rebuildDhcp = utils.buildDhcp(origXmlResponse)
			println " rebuildDhcp: "
			println "  actual    - " + rebuildDhcp
			println "  expected  - " + expected_rebuildDhcp
			assertEquals("rebuildDhcp - expected vs actual", expected_rebuildDhcp, rebuildDhcp)
		}
		
		@Test
		public void testBuildPat() {
			def rebuildPat = utils.buildPat(origXmlResponse)
			println " rebuildPat: "
			println "  actual    - " + rebuildPat
			println "  expected  - " + expected_rebuildPat
			assertEquals("rebuildPat - expected vs actual", expected_rebuildPat, rebuildPat)
		}
		
		@Test
		public void testBuildNat() {
			def rebuildNat = utils.buildNat(origXmlResponse)
			println " rebuildNat: "
			println "  actual    - " + rebuildNat
			println "  expected  - " + expected_rebuildNat
			assertEquals("rebuildNat - expected vs actual", expected_rebuildNat, rebuildNat)
		}
		
		@Test
		public void testBuildFirewallLite() {
			def rebuildFirewallLite = utils.buildFirewallLite(origXmlResponse)
			println " rebuildFirewallLite: "
			println "  actual    - " + rebuildFirewallLite
			println "  expected  - " + expected_rebuildFirewallLite
			assertEquals("rebuildFirewallLite - expected vs actual", expected_rebuildFirewallLite, rebuildFirewallLite)
		}
		
		@Test
		public void testBuildStaticRoutes() {
			def rebuildStaticRoutes = utils.buildStaticRoutes(origXmlResponse)
			println " rebuildStaticRoutes: "
			println "  actual    - " + rebuildStaticRoutes
			println "  expected  - " + expected_rebuildStaticRoutes
			assertEquals("rebuildStaticRoutes - expected vs actual", expected_rebuildStaticRoutes, rebuildStaticRoutes)
		}
		
		@Test
		public void testGetBasicAuth(){
			def encodedAuth = utils.getBasicAuth(utils.encrypt("myString","07a7159d3bf51a0e53be7a8f89699be7"),"07a7159d3bf51a0e53be7a8f89699be7")
			assertEquals("Basic bXlTdHJpbmc=", encodedAuth)
		}
		
		@Test
		public void testEncrypt(){
			def encrypted = utils.encrypt("myString","07a7159d3bf51a0e53be7a8f89699be7")
			assertEquals("myString", utils.decrypt(encrypted,"07a7159d3bf51a0e53be7a8f89699be7"))
			
		}
		
		@Test
		public void testDecrypt(){
			def decrypted = utils.decrypt(utils.encrypt("myString","07a7159d3bf51a0e53be7a8f89699be7"), "07a7159d3bf51a0e53be7a8f89699be7")
			assertEquals("myString", decrypted)
		}
		
		@Test
		public void testGetPBGFList(){
			def responseAsString = getFile("sdncDeleteResponse.xml")
			def nodes = utils.getPBGFList("true", responseAsString)
			//assertEquals(2, nodes.size())
			while(!nodes.empty){
			def myBGFXML = nodes.remove(0)
				def myBGF= new XmlSlurper().parseText(myBGFXML)
				println "borderElmtId: " + myBGF.'border-element-id'
				println "vlanid: " + myBGF.'vlan-id' +"\n"
			}
		}

        @Test
        public void testBuildL2HomingInformation_with_aic_clli() {

            def responseAsString = getFile("sdncadaptercallbackrequest_with_aic_version.xml")
            def varrequestData=utils.getNodeText(responseAsString,"RequestData")
            def xmlResponse = utils.getNodeXml(varrequestData, "layer3-service-list").drop(38).trim()
            def rebuildL2Home = utils.buildL2HomingInformation(xmlResponse)
            println " rebuildL2Home: "
            println "  actual    - " + rebuildL2Home
            println "  expected  - " + expected_rebuildL2Home
            assertEquals("rebuildL2Home - expected vs actual", expected_rebuildL2HomeFor_aic_clli, rebuildL2Home)
        }
		
		@Test
		public void testUnescapeNodeContents() {
			String output = "<a><b>AT&amp;T</b></a>"
			String noChangeInput = "<a><b>AT&amp;T</b></a>"
			String decodeWithNamespacesNodeInput = '<top xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema"><a xsi:type="xs:string">&lt;b&gt;AT&amp;amp;T&lt;/b&gt;</a></top>'
			String decodeWithNamespacesInput = '<a xsi:type="xs:string">&lt;b&gt;AT&amp;amp;T&lt;/b&gt;</a>'
			String decodeWithNamespacesOutput = '<a xsi:type="xs:string"><b>AT&amp;T</b></a>'
			String decodeWithoutNamespacesInput = '<a type="string">&lt;b&gt;AT&amp;amp;T&lt;/b&gt;</a>'
			String decodeWithoutNamespacesOutput = '<a type="string"><b>AT&amp;T</b></a>'
			String normalString = "<a>AT&amp;T</a>"
			NodeChild noChangeInputNode = new XmlSlurper().parseText(noChangeInput).'**'.find {it.name() == 'a'}
			NodeChild decodeWithNamespacesInputNode = new XmlSlurper().parseText(decodeWithNamespacesNodeInput).'**'.find {it.name() == 'a'}
			NodeChild decodeWithoutNamespacesInputNode = new XmlSlurper().parseText(decodeWithoutNamespacesInput).'**'.find {it.name() == 'a'}
			NodeChild normalStringNode = new XmlSlurper().parseText(normalString).'**'.find {it.name() == 'a'}
			
			assertEquals("no change", output, utils.unescapeNodeContents(noChangeInputNode, noChangeInput))
			assertEquals("single unescape", decodeWithNamespacesOutput, utils.unescapeNodeContents(decodeWithNamespacesInputNode, decodeWithNamespacesInput))
			assertEquals("single unescape", decodeWithoutNamespacesOutput, utils.unescapeNodeContents(decodeWithoutNamespacesInputNode, decodeWithoutNamespacesInput))
			assertEquals("unescape normal string", "AT&T", utils.unescapeNodeContents(normalStringNode, "AT&amp;T"))
		}
		
		@Test
		public void testXmlEncode() {
			
			String expected = "&amp;amp;";
			
			assertEquals("is double encoded", expected, MsoUtils.xmlEscape("&amp;"))
			
		}
		public String getFile(String fileName) {
			def SLASH = File.separator
			def pathBase = ' '
			def fileAsString = ''
			try {
				pathBase = new File(".").getCanonicalPath()
				//println "pathBase " + pathBase + "${SLASH}src${SLASH}test${SLASH}resources${SLASH}${fileName}"
				fileAsString = new File(pathBase, "${SLASH}src${SLASH}test${SLASH}resources${SLASH}${fileName}").getText()
			} catch (Exception ex) {
			    println " *** getFile error: " + ex.getStackTrace()
			} finally {
			    return fileAsString
			}
		}
				
		@Test
		public void testGetLowestUnusedIndex() {
			def responseAsString = getFile("vfModuleCount.xml")
			def index = utils.getLowestUnusedIndex(responseAsString)
			println " lowest module count test: "
			println "  actual    - " + index
			println "  expected  - " + "14"
			assertEquals("expected vs actual", "14", index)
		}
}