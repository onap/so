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

package org.openecomp.mso.bpmn.common.scripts

import static org.mockito.Mockito.*

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapter

import org.openecomp.mso.bpmn.mock.FileUtil

@RunWith(MockitoJUnitRunner.class)
public class SDNCAdapterTest {
	
	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
		System.setProperty("jboss.qualified.host.name","myhost.com")
	}

	
	def workflowResponse = """<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
                                                 xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1"
                                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <sdncadapterworkflow:response-data>
      <tag0:CallbackHeader>
         <tag0:RequestId>testRequestId</tag0:RequestId>
         <tag0:ResponseCode>200</tag0:ResponseCode>
         <tag0:ResponseMessage>OK</tag0:ResponseMessage>
      </tag0:CallbackHeader>
      <tag0:RequestData xsi:type="xs:string">				&lt;layer3-service-list xmlns="com:att:sdnctl:l3api"&gt;
					&lt;service-instance-id&gt;FK/VLXM/003717//SW_INTERNET&lt;/service-instance-id&gt;
					&lt;service-status&gt;
						&lt;rpc-name&gt;service-configuration-operation&lt;/rpc-name&gt;
						&lt;rpc-action&gt;activate&lt;/rpc-action&gt;
						&lt;request-status&gt;synccomplete&lt;/request-status&gt;
						&lt;final-indicator&gt;N&lt;/final-indicator&gt;
						&lt;l3sdn-action&gt;Layer3ServiceActivateRequest&lt;/l3sdn-action&gt;
						&lt;l3sdn-subaction&gt;SUPP&lt;/l3sdn-subaction&gt;
						&lt;response-timestamp&gt;2015-04-28T21:32:11.386Z&lt;/response-timestamp&gt;
					&lt;/service-status&gt;
					&lt;service-data&gt;
						&lt;internet-evc-access-information&gt;
							&lt;ip-version&gt;ds&lt;/ip-version&gt;
							&lt;internet-evc-speed-value&gt;8&lt;/internet-evc-speed-value&gt;
							&lt;internet-evc-speed-units&gt;Mbps&lt;/internet-evc-speed-units&gt;
						&lt;/internet-evc-access-information&gt;
						&lt;vr-lan xmlns="com:att:sdnctl:l3api"&gt;
							&lt;vr-lan-interface&gt;
								&lt;static-routes&gt;
									&lt;v6-static-routes&gt;
										&lt;v6-next-hop-address&gt;2001:1890:12e3:2da::&lt;/v6-next-hop-address&gt;
										&lt;v6-static-route-prefix&gt;2001:1890:12e3:2da::&lt;/v6-static-route-prefix&gt;
										&lt;v6-static-route-prefix-length&gt;28&lt;/v6-static-route-prefix-length&gt;
									&lt;/v6-static-routes&gt;
									&lt;v4-static-routes&gt;
										&lt;v4-static-route-prefix&gt;255.255.252.1&lt;/v4-static-route-prefix&gt;
										&lt;v4-next-hop-address&gt;192.168.1.15&lt;/v4-next-hop-address&gt;
										&lt;v4-static-route-prefix-length&gt;28&lt;/v4-static-route-prefix-length&gt;
									&lt;/v4-static-routes&gt;
									&lt;v6-static-routes&gt;
										&lt;v6-next-hop-address&gt;2001:1890:12e3:2da::&lt;/v6-next-hop-address&gt;
										&lt;v6-static-route-prefix&gt;2001:1890:12e3:2da::&lt;/v6-static-route-prefix&gt;
										&lt;v6-static-route-prefix-length&gt;28&lt;/v6-static-route-prefix-length&gt;
									&lt;/v6-static-routes&gt;
									&lt;v4-static-routes&gt;
										&lt;v4-static-route-prefix&gt;255.255.252.2&lt;/v4-static-route-prefix&gt;
										&lt;v4-next-hop-address&gt;192.168.1.15&lt;/v4-next-hop-address&gt;
										&lt;v4-static-route-prefix-length&gt;28&lt;/v4-static-route-prefix-length&gt;
									&lt;/v4-static-routes&gt;
									&lt;v4-static-routes&gt;
										&lt;v4-static-route-prefix&gt;255.255.252.3&lt;/v4-static-route-prefix&gt;
										&lt;v4-next-hop-address&gt;192.168.1.15&lt;/v4-next-hop-address&gt;
										&lt;v4-static-route-prefix-length&gt;28&lt;/v4-static-route-prefix-length&gt;
									&lt;/v4-static-routes&gt;
								&lt;/static-routes&gt;
								&lt;dhcp&gt;
									&lt;v6-dhcp-server-enabled&gt;N&lt;/v6-dhcp-server-enabled&gt;
									&lt;v4-dhcp-server-enabled&gt;Y&lt;/v4-dhcp-server-enabled&gt;
									&lt;use-v6-default-pool&gt;N&lt;/use-v6-default-pool&gt;
									&lt;excluded-v4-dhcp-addresses-from-default-pool&gt;
									  &lt;excluded-v4-address&gt;192.168.1.7&lt;/excluded-v4-address&gt;
									&lt;/excluded-v4-dhcp-addresses-from-default-pool&gt;
									&lt;excluded-v4-dhcp-addresses-from-default-pool&gt;
									  &lt;excluded-v4-address&gt;192.168.1.8&lt;/excluded-v4-address&gt;
									&lt;/excluded-v4-dhcp-addresses-from-default-pool&gt;
									&lt;v4-dhcp-pools&gt;
										&lt;v4-dhcp-relay-next-hop-address&gt;1.1.1.1&lt;/v4-dhcp-relay-next-hop-address&gt;
										&lt;v4-dhcp-pool-prefix-length&gt;28&lt;/v4-dhcp-pool-prefix-length&gt;
										&lt;excluded-v4-addresses&gt;
											&lt;excluded-v4-address&gt;192.168.1.5&lt;/excluded-v4-address&gt;
										&lt;/excluded-v4-addresses&gt;
										&lt;v4-dhcp-relay-gateway-address&gt;2.2.2.1&lt;/v4-dhcp-relay-gateway-address&gt;
										&lt;excluded-v4-addresses&gt;
											&lt;excluded-v4-address&gt;192.168.1.6&lt;/excluded-v4-address&gt;
										&lt;/excluded-v4-addresses&gt;
										&lt;v4-dhcp-pool-prefix&gt;192.155.2.3&lt;/v4-dhcp-pool-prefix&gt;
									&lt;/v4-dhcp-pools&gt;
									&lt;v4-dhcp-pools&gt;
										&lt;v4-dhcp-relay-next-hop-address&gt;1.1.1.2&lt;/v4-dhcp-relay-next-hop-address&gt;
										&lt;v4-dhcp-pool-prefix-length&gt;28&lt;/v4-dhcp-pool-prefix-length&gt;
										&lt;excluded-v4-addresses&gt;
											&lt;excluded-v4-address&gt;192.168.1.6&lt;/excluded-v4-address&gt;
										&lt;/excluded-v4-addresses&gt;
										&lt;v4-dhcp-relay-gateway-address&gt;2.2.2.2&lt;/v4-dhcp-relay-gateway-address&gt;
										&lt;excluded-v4-addresses&gt;
											&lt;excluded-v4-address&gt;192.168.1.7&lt;/excluded-v4-address&gt;
										&lt;/excluded-v4-addresses&gt;
										&lt;v4-dhcp-pool-prefix&gt;192.155.2.4&lt;/v4-dhcp-pool-prefix&gt;
									&lt;/v4-dhcp-pools&gt;									
									&lt;use-v4-default-pool&gt;Y&lt;/use-v4-default-pool&gt;
									&lt;excluded-v6-dhcp-addresses-from-default-pool&gt;
									  &lt;excluded-v6-address&gt;1:5&lt;/excluded-v6-address&gt;
									&lt;/excluded-v6-dhcp-addresses-from-default-pool&gt;
									&lt;excluded-v6-dhcp-addresses-from-default-pool&gt;
									  &lt;excluded-v6-address&gt;1:6&lt;/excluded-v6-address&gt;
									&lt;/excluded-v6-dhcp-addresses-from-default-pool&gt;
									&lt;v6-dhcp-pools&gt;
										&lt;v6-dhcp-relay-next-hop-address&gt;4:4&lt;/v6-dhcp-relay-next-hop-address&gt;
										&lt;v6-dhcp-pool-prefix-length&gt;28&lt;/v6-dhcp-pool-prefix-length&gt;
										&lt;excluded-v6-addresses&gt;
											&lt;excluded-v6-address&gt;1:1&lt;/excluded-v6-address&gt;
										&lt;/excluded-v6-addresses&gt;
										&lt;v6-dhcp-relay-gateway-address&gt;3:3&lt;/v6-dhcp-relay-gateway-address&gt;
										&lt;excluded-v6-addresses&gt;
											&lt;excluded-v6-address&gt;2:2&lt;/excluded-v6-address&gt;
										&lt;/excluded-v6-addresses&gt;
										&lt;v6-dhcp-pool-prefix&gt;0:0&lt;/v6-dhcp-pool-prefix&gt;
									&lt;/v6-dhcp-pools&gt;
									&lt;v6-dhcp-pools&gt;
										&lt;v6-dhcp-relay-next-hop-address&gt;4:4&lt;/v6-dhcp-relay-next-hop-address&gt;
										&lt;v6-dhcp-pool-prefix-length&gt;28&lt;/v6-dhcp-pool-prefix-length&gt;
										&lt;excluded-v6-addresses&gt;
											&lt;excluded-v6-address&gt;1:1&lt;/excluded-v6-address&gt;
										&lt;/excluded-v6-addresses&gt;
										&lt;v6-dhcp-relay-gateway-address&gt;3:3&lt;/v6-dhcp-relay-gateway-address&gt;
										&lt;excluded-v6-addresses&gt;
											&lt;excluded-v6-address&gt;2:2&lt;/excluded-v6-address&gt;
										&lt;/excluded-v6-addresses&gt;
										&lt;v6-dhcp-pool-prefix&gt;0:0&lt;/v6-dhcp-pool-prefix&gt;
									&lt;/v6-dhcp-pools&gt;									
								&lt;/dhcp&gt;
								&lt;firewall-lite&gt;
									&lt;stateful-firewall-lite-v6-enabled&gt;N&lt;/stateful-firewall-lite-v6-enabled&gt;
									&lt;stateful-firewall-lite-v4-enabled&gt;Y&lt;/stateful-firewall-lite-v4-enabled&gt;
									&lt;v4-firewall-packet-filters&gt;
									  &lt;v4-firewall-prefix&gt;0.0.0.1&lt;/v4-firewall-prefix&gt;
									  &lt;v4-firewall-prefix-length&gt;1&lt;/v4-firewall-prefix-length&gt;
									  &lt;allow-icmp-ping&gt;Y&lt;/allow-icmp-ping&gt;
									  &lt;udp-ports&gt;
									    &lt;port-number&gt;1&lt;/port-number&gt;
									  &lt;/udp-ports&gt;
									  &lt;tcp-ports&gt;
									    &lt;port-number&gt;1&lt;/port-number&gt;
									  &lt;/tcp-ports&gt;
									&lt;/v4-firewall-packet-filters&gt;
									&lt;v4-firewall-packet-filters&gt;
									  &lt;v4-firewall-prefix&gt;0.0.0.2&lt;/v4-firewall-prefix&gt;
									  &lt;v4-firewall-prefix-length&gt;2&lt;/v4-firewall-prefix-length&gt;
									  &lt;allow-icmp-ping&gt;Y&lt;/allow-icmp-ping&gt;
									  &lt;udp-ports&gt;
									    &lt;port-number&gt;2&lt;/port-number&gt;
									  &lt;/udp-ports&gt;
									  &lt;tcp-ports&gt;
									    &lt;port-number&gt;2&lt;/port-number&gt;
									  &lt;/tcp-ports&gt;
									&lt;/v4-firewall-packet-filters&gt;
									&lt;v6-firewall-packet-filters&gt;
									  &lt;v6-firewall-prefix&gt;:&lt;/v6-firewall-prefix&gt;
									  &lt;v6-firewall-prefix-length&gt;0&lt;/v6-firewall-prefix-length&gt;
									  &lt;allow-icmp-ping&gt;Y&lt;/allow-icmp-ping&gt;
									  &lt;udp-ports&gt;
									    &lt;port-number&gt;3&lt;/port-number&gt;
									  &lt;/udp-ports&gt;
									  &lt;tcp-ports&gt;
									    &lt;port-number&gt;3&lt;/port-number&gt;
									  &lt;/tcp-ports&gt;
									&lt;/v6-firewall-packet-filters&gt;
									&lt;v6-firewall-packet-filters&gt;
									  &lt;v6-firewall-prefix&gt;:&lt;/v6-firewall-prefix&gt;
									  &lt;v6-firewall-prefix-length&gt;1&lt;/v6-firewall-prefix-length&gt;
									  &lt;allow-icmp-ping&gt;Y&lt;/allow-icmp-ping&gt;
									  &lt;udp-ports&gt;
									    &lt;port-number&gt;4&lt;/port-number&gt;
									  &lt;/udp-ports&gt;
									  &lt;tcp-ports&gt;
									    &lt;port-number&gt;4&lt;/port-number&gt;
									  &lt;/tcp-ports&gt;
									&lt;/v6-firewall-packet-filters&gt;
								&lt;/firewall-lite&gt;
								&lt;pat&gt;
									&lt;v4-pat-pools&gt;
										&lt;v4-pat-pool-prefix&gt;192.168.1.44&lt;/v4-pat-pool-prefix&gt;
										&lt;v4-pat-pool-next-hop-address&gt;192.168.1.5&lt;/v4-pat-pool-next-hop-address&gt;
										&lt;v4-pat-pool-prefix-length&gt;0&lt;/v4-pat-pool-prefix-length&gt;
									&lt;/v4-pat-pools&gt;
									&lt;use-v4-default-pool&gt;Y&lt;/use-v4-default-pool&gt;
									&lt;v4-pat-enabled&gt;N&lt;/v4-pat-enabled&gt;
									&lt;v4-pat-pools&gt;
										&lt;v4-pat-pool-prefix&gt;192.168.1.45&lt;/v4-pat-pool-prefix&gt;
										&lt;v4-pat-pool-next-hop-address&gt;192.168.1.6&lt;/v4-pat-pool-next-hop-address&gt;
										&lt;v4-pat-pool-prefix-length&gt;28&lt;/v4-pat-pool-prefix-length&gt;
									&lt;/v4-pat-pools&gt;
								&lt;/pat&gt;
								&lt;nat&gt;
								  &lt;v4-nat-enabled&gt;Y&lt;/v4-nat-enabled&gt;
								  &lt;v4-nat-mapping-entries&gt;
								    &lt;v4-nat-internal&gt;0.0.0.0&lt;/v4-nat-internal&gt;
								    &lt;v4-nat-next-hop-address&gt;0.0.0.0&lt;/v4-nat-next-hop-address&gt;
								    &lt;v4-nat-external&gt;0.0.0.0&lt;/v4-nat-external&gt;
								  &lt;/v4-nat-mapping-entries&gt;
								  &lt;v4-nat-mapping-entries&gt;
								    &lt;v4-nat-internal&gt;0.0.0.1&lt;/v4-nat-internal&gt;
								    &lt;v4-nat-next-hop-address&gt;0.0.0.1&lt;/v4-nat-next-hop-address&gt;
								    &lt;v4-nat-external&gt;0.0.0.1&lt;/v4-nat-external&gt;
								  &lt;/v4-nat-mapping-entries&gt;
								&lt;/nat&gt;
								&lt;vr-designation&gt;primary&lt;/vr-designation&gt;
								&lt;v4-vce-loopback-address&gt;162.200.3.144&lt;/v4-vce-loopback-address&gt;
								&lt;v6-vr-lan-prefix-length&gt;64&lt;/v6-vr-lan-prefix-length&gt;
								&lt;v6-vce-wan-address&gt;2001:1890:12e3:2da::&lt;/v6-vce-wan-address&gt;
								&lt;v6-vr-lan-prefix&gt;2620:0:10d0:f:ffff:ffff:ffff:fffe&lt;/v6-vr-lan-prefix&gt;
								&lt;v4-vr-lan-prefix-length&gt;24&lt;/v4-vr-lan-prefix-length&gt;
								&lt;v4-vr-lan-prefix&gt;10.192.27.254&lt;/v4-vr-lan-prefix&gt;
								&lt;v4-public-lan-prefixes&gt;
									&lt;t-provided-v4-lan-public-prefixes&gt;
										&lt;request-index&gt;1&lt;/request-index&gt;
										&lt;v4-next-hop-address&gt;192.168.1.2&lt;/v4-next-hop-address&gt;
										&lt;v4-lan-public-prefix&gt;192.168.1.1&lt;/v4-lan-public-prefix&gt;
										&lt;v4-lan-public-prefix-length&gt;28&lt;/v4-lan-public-prefix-length&gt;
									&lt;/t-provided-v4-lan-public-prefixes&gt;
									&lt;t-provided-v4-lan-public-prefixes&gt;
										&lt;request-index&gt;1&lt;/request-index&gt;
										&lt;v4-next-hop-address&gt;192.168.1.72&lt;/v4-next-hop-address&gt;
										&lt;v4-lan-public-prefix&gt;192.168.1.71&lt;/v4-lan-public-prefix&gt;
										&lt;v4-lan-public-prefix-length&gt;28&lt;/v4-lan-public-prefix-length&gt;
									&lt;/t-provided-v4-lan-public-prefixes&gt;
									&lt;t-provided-v4-lan-public-prefixes&gt;
										&lt;request-index&gt;1&lt;/request-index&gt;
										&lt;v4-next-hop-address&gt;192.168.1.68&lt;/v4-next-hop-address&gt;
										&lt;v4-lan-public-prefix&gt;192.168.1.67&lt;/v4-lan-public-prefix&gt;
										&lt;v4-lan-public-prefix-length&gt;28&lt;/v4-lan-public-prefix-length&gt;
									&lt;/t-provided-v4-lan-public-prefixes&gt;
								&lt;/v4-public-lan-prefixes&gt;
								&lt;v6-public-lan-prefixes&gt;
									&lt;t-provided-v6-lan-public-prefixes&gt;
										&lt;request-index&gt;1&lt;/request-index&gt;
										&lt;v6-next-hop-address&gt;2001:1890:12e3:2da::&lt;/v6-next-hop-address&gt;
										&lt;v6-lan-public-prefix&gt;2001:1890:12e3:2da::&lt;/v6-lan-public-prefix&gt;
										&lt;v6-lan-public-prefix-length&gt;28&lt;/v6-lan-public-prefix-length&gt;
									&lt;/t-provided-v6-lan-public-prefixes&gt;
									&lt;t-provided-v6-lan-public-prefixes&gt;
										&lt;request-index&gt;1&lt;/request-index&gt;
										&lt;v6-next-hop-address&gt;2001:1890:12e3:2da::&lt;/v6-next-hop-address&gt;
										&lt;v6-lan-public-prefix&gt;2001:1890:12e3:3da::&lt;/v6-lan-public-prefix&gt;
										&lt;v6-lan-public-prefix-length&gt;28&lt;/v6-lan-public-prefix-length&gt;
									&lt;/t-provided-v6-lan-public-prefixes&gt;
									&lt;t-provided-v6-lan-public-prefixes&gt;
										&lt;request-index&gt;1&lt;/request-index&gt;
										&lt;v6-next-hop-address&gt;2001:1890:12e3:2da::&lt;/v6-next-hop-address&gt;
										&lt;v6-lan-public-prefix&gt;2001:1890:12e3:4da::&lt;/v6-lan-public-prefix&gt;
										&lt;v6-lan-public-prefix-length&gt;28&lt;/v6-lan-public-prefix-length&gt;
									&lt;/t-provided-v6-lan-public-prefixes&gt;
								&lt;/v6-public-lan-prefixes&gt;
							&lt;/vr-lan-interface&gt;
							&lt;routing-protocol&gt;none&lt;/routing-protocol&gt;
						&lt;/vr-lan&gt;
&lt;ucpe-vms-service-information&gt;				
 &lt;transport-service-information&gt;
    &lt;transport-service-type&gt;AVPN&lt;/transport-service-type&gt;
	&lt;access-circuit-info&gt;
	   &lt;access-circuit-id&gt;1&lt;/access-circuit-id&gt;
	   &lt;dual-mode&gt;Active&lt;/dual-mode&gt;
	&lt;/access-circuit-info&gt;
	&lt;access-circuit-info&gt;
	   &lt;access-circuit-id&gt;2&lt;/access-circuit-id&gt;
	   &lt;dual-mode&gt;Standby&lt;/dual-mode&gt;
	&lt;/access-circuit-info&gt;	
 &lt;/transport-service-information&gt;
 &lt;ucpe-information&gt;
    &lt;ucpe-host-name&gt;hostname&lt;/ucpe-host-name&gt;
    &lt;ucpe-activation-code&gt;activecode&lt;/ucpe-activation-code&gt;
    &lt;out-of-band-management-modem&gt;OOB&lt;/out-of-band-management-modem&gt;
  &lt;/ucpe-information&gt;
  &lt;vnf-list&gt;
	&lt;vnf-information&gt; 
		&lt;vnf-instance-id&gt;1&lt;/vnf-instance-id&gt; 
		&lt;vnf-sequence-number&gt;1&lt;/vnf-sequence-number&gt;
		&lt;vnf-type&gt;ZZ&lt;/vnf-type&gt;
		&lt;vnf-vendor&gt;JUNIPER&lt;/vnf-vendor&gt;
		&lt;vnf-model&gt;MODEL1&lt;/vnf-model&gt;
		&lt;vnf-id&gt;1&lt;/vnf-id&gt;
		&lt;prov-status&gt;1&lt;/prov-status&gt;
		&lt;operational-state&gt;1&lt;/operational-state&gt;
		&lt;orchestration-status&gt;1&lt;/orchestration-status&gt;
		&lt;equipment-role&gt;1&lt;/equipment-role&gt;
    &lt;/vnf-information&gt;
	&lt;vnf-information&gt; 
		&lt;vnf-instance-id&gt;2&lt;/vnf-instance-id&gt; 
		&lt;vnf-sequence-number&gt;2&lt;/vnf-sequence-number&gt;
		&lt;vnf-type&gt;HY&lt;/vnf-type&gt;
		&lt;vnf-vendor&gt;JUNIPER&lt;/vnf-vendor&gt;
		&lt;vnf-model&gt;MODEL2&lt;/vnf-model&gt;
		&lt;vnf-id&gt;2&lt;/vnf-id&gt;
		&lt;prov-status&gt;2&lt;/prov-status&gt;
		&lt;operational-state&gt;2&lt;/operational-state&gt;
		&lt;orchestration-status&gt;2&lt;/orchestration-status&gt;
		&lt;equipment-role&gt;2&lt;/equipment-role&gt;
    &lt;/vnf-information&gt;					
  &lt;/vnf-list&gt;
 &lt;/ucpe-vms-service-information&gt;										
						&lt;request-information&gt;
							&lt;request-action&gt;Layer3ServiceActivateRequest&lt;/request-action&gt;
							&lt;order-number&gt;4281555&lt;/order-number&gt;
							&lt;request-id&gt;155415ab-b4a7-4382-b4c6-d17d9sm42855&lt;/request-id&gt;
							&lt;notification-url&gt;https://csi-tst-q22.it.com:22443/Services/com/cingular/csi/sdn/SendManagedNetworkStatusNotification.jws&lt;/notification-url&gt;
							&lt;source&gt;OMX&lt;/source&gt;
							&lt;order-version&gt;1&lt;/order-version&gt;
						&lt;/request-information&gt;
						&lt;sdnc-request-header&gt;
							&lt;svc-action&gt;activate&lt;/svc-action&gt;
							&lt;svc-notification-url&gt;https://msojra.mtsnjdcp1.aic.cip.com:8443/adapters/rest/SDNCNotify&lt;/svc-notification-url&gt;
							&lt;svc-request-id&gt;5b1f3c5d-cdf9-488d-8a4b-d3f1229d7760&lt;/svc-request-id&gt;
						&lt;/sdnc-request-header&gt;
						&lt;l2-homing-information&gt;
							&lt;topology&gt;MultiPoint&lt;/topology&gt;
							&lt;preferred-aic-clli&gt;MTSNJA4LCP1&lt;/preferred-aic-clli&gt;
							&lt;evc-name&gt;AS/VLXM/003717//SW&lt;/evc-name&gt;
						&lt;/l2-homing-information&gt;
						&lt;service-information&gt;
							&lt;service-instance-id&gt;FK/VLXM/003717//SW_INTERNET&lt;/service-instance-id&gt;
							&lt;subscriber-name&gt;ST E2E Test42855_1300004281555&lt;/subscriber-name&gt;
							&lt;service-type&gt;SDN-ETHERNET-INTERNET&lt;/service-type&gt;
						&lt;/service-information&gt;
						&lt;internet-service-change-details&gt;
							&lt;internet-evc-speed-value&gt;10&lt;/internet-evc-speed-value&gt;
							&lt;internet-evc-speed-units&gt;Kbps&lt;/internet-evc-speed-units&gt;
							&lt;t-provided-v4-lan-public-prefixes&gt;
							   &lt;request-index&gt;1&lt;/request-index&gt;
							   &lt;v4-next-hop-address&gt;192.168.1.15&lt;/v4-next-hop-address&gt;
							   &lt;v4-lan-public-prefix&gt;192.168.1.15&lt;/v4-lan-public-prefix&gt;
							   &lt;v4-lan-public-prefix-length&gt;28&lt;/v4-lan-public-prefix-length&gt;
							&lt;/t-provided-v4-lan-public-prefixes&gt;
							&lt;t-provided-v4-lan-public-prefixes&gt;
							   &lt;request-index&gt;2&lt;/request-index&gt;
							   &lt;v4-next-hop-address&gt;192.168.1.16&lt;/v4-next-hop-address&gt;
							   &lt;v4-lan-public-prefix&gt;192.168.1.16&lt;/v4-lan-public-prefix&gt;
							   &lt;v4-lan-public-prefix-length&gt;28&lt;/v4-lan-public-prefix-length&gt;
							&lt;/t-provided-v4-lan-public-prefixes&gt;
							&lt;t-provided-v6-lan-public-prefixes&gt;
							   &lt;request-index&gt;1&lt;/request-index&gt;
							   &lt;v6-next-hop-address&gt;2001:1890:12e3:2da::&lt;/v6-next-hop-address&gt;
							   &lt;v6-lan-public-prefix&gt;2001:1890:12e3:2da::&lt;/v6-lan-public-prefix&gt;
							   &lt;v6-lan-public-prefix-length&gt;28&lt;/v6-lan-public-prefix-length&gt;
							&lt;/t-provided-v6-lan-public-prefixes&gt;
							&lt;t-provided-v6-lan-public-prefixes&gt;
							   &lt;request-index&gt;1&lt;/request-index&gt;
							   &lt;v6-next-hop-address&gt;2001:1890:12e3:2da::&lt;/v6-next-hop-address&gt;
							   &lt;v6-lan-public-prefix&gt;2001:1890:12e3:2da::&lt;/v6-lan-public-prefix&gt;
							   &lt;v6-lan-public-prefix-length&gt;28&lt;/v6-lan-public-prefix-length&gt;
							&lt;/t-provided-v6-lan-public-prefixes&gt;
						&lt;/internet-service-change-details&gt;
					&lt;/service-data&gt;
				&lt;/layer3-service-list&gt;
</tag0:RequestData>
   </sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

	String sdncAdapterRequest = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
			<SOAP-ENV:Body>
			<aetgt:SDNCAdapterRequest xmlns:aetgt="http://org.openecomp/workflow/sdnc/adapter/schema/v1" xmlns:sdncadaptersc="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
			<sdncadapter:RequestHeader xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
			<sdncadapter:RequestId>745b1b50-e39e-4685-9cc8-c71f0bde8bf0</sdncadapter:RequestId>
			<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
			<sdncadapter:SvcOperation>services/layer3-service-list/AS%2FVLXM%2F000199%2F%2FSB_INTERNET</sdncadapter:SvcOperation>
			<sdncadapter:CallbackUrl>http://myhost.com:28080/mso/sdncAdapterCallbackServiceImpl</sdncadapter:CallbackUrl>
			</sdncadapter:RequestHeader>
			<sdncadaptersc:RequestData><rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              contentType="text/xml">
   <service-request xmlns="http://org.openecomp/mso/request/details/schema/v1">
      <request-information>
         <request-id>12570a36-7388-4c0a-bec4-189ce3kg9956</request-id>
         <request-action>GetLayer3ServiceDetailsRequest</request-action>
         <source>OMX</source>
      </request-information>
      <service-information>
         <service-type>SDN-ETHERNET-INTERNET</service-type>
         <service-instance-id>PD/VLXM/003717//SW_INTERNET</service-instance-id>
      </service-information>
   </service-request>
</rest:payload></sdncadaptersc:RequestData></aetgt:SDNCAdapterRequest></SOAP-ENV:Body></SOAP-ENV:Envelope>"""
	
def sdncAdapterResponse = """<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
<sdncadapterworkflow:response-data>
<tag0:CallbackHeader xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
   <tag0:RequestId>39542e39-ccc3-4d1a-8b79-04ce88526613</tag0:RequestId>
   <tag0:ResponseCode>404</tag0:ResponseCode>
   <tag0:ResponseMessage>Error processing request to SDNC. Not Found.
			https://sdncodl.us.aic.cip.com:8443/restconf/config/L3SDN-API:services/layer3-service-list/MVM%2FVLXP%2F000855%2F%2FShakeout.
			SDNC Returned-[error-type:application, error-tag:data-missing,
			error-message:Request could not be completed because the relevant
			data model content does not exist.]</tag0:ResponseMessage>
</tag0:CallbackHeader>
</sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>
"""

def workflowErrorResponse = """<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
								<aetgt:ErrorMessage>Received error from SDN-C: Error processing request to SDNC. Not Found.
			https://sdncodl.us.aic.cip.com:8443/restconf/config/L3SDN-API:services/layer3-service-list/MVM%2FVLXP%2F000855%2F%2FShakeout.
			SDNC Returned-[error-type:application, error-tag:data-missing,
			error-message:Request could not be completed because the relevant
			data model content does not exist.]</aetgt:ErrorMessage>
								<aetgt:ErrorCode>5300</aetgt:ErrorCode>
								<aetgt:SourceSystemErrorCode>404</aetgt:SourceSystemErrorCode>
								</aetgt:WorkflowException>"""

def workflowErrorResponse1 = """<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>Invalid Callback Response from SDNC Adapter</aetgt:ErrorMessage>
					<aetgt:ErrorCode>5300</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""
			
def     enhancedCallbackRequestData =
    """<tag0:RequestData xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:type="xs:string">				<layer3-service-list xmlns="com:att:sdnctl:l3api">
					<service-instance-id>FK/VLXM/003717//SW_INTERNET</service-instance-id>
					<service-status>
						<rpc-name>service-configuration-operation</rpc-name>
						<rpc-action>activate</rpc-action>
						<request-status>synccomplete</request-status>
						<final-indicator>N</final-indicator>
						<l3sdn-action>Layer3ServiceActivateRequest</l3sdn-action>
						<l3sdn-subaction>SUPP</l3sdn-subaction>
						<response-timestamp>2015-04-28T21:32:11.386Z</response-timestamp>
					</service-status>
					<service-data>
						<internet-evc-access-information>
							<ip-version>ds</ip-version>
							<internet-evc-speed-value>8</internet-evc-speed-value>
							<internet-evc-speed-units>Mbps</internet-evc-speed-units>
						</internet-evc-access-information>
						<vr-lan xmlns="com:att:sdnctl:l3api">
							<vr-lan-interface>
								<static-routes>
									<v6-static-routes>
										<v6-next-hop-address>2001:1890:12e3:2da::</v6-next-hop-address>
										<v6-static-route-prefix>2001:1890:12e3:2da::</v6-static-route-prefix>
										<v6-static-route-prefix-length>28</v6-static-route-prefix-length>
									</v6-static-routes>
									<v4-static-routes>
										<v4-static-route-prefix>255.255.252.1</v4-static-route-prefix>
										<v4-next-hop-address>192.168.1.15</v4-next-hop-address>
										<v4-static-route-prefix-length>28</v4-static-route-prefix-length>
									</v4-static-routes>
									<v6-static-routes>
										<v6-next-hop-address>2001:1890:12e3:2da::</v6-next-hop-address>
										<v6-static-route-prefix>2001:1890:12e3:2da::</v6-static-route-prefix>
										<v6-static-route-prefix-length>28</v6-static-route-prefix-length>
									</v6-static-routes>
									<v4-static-routes>
										<v4-static-route-prefix>255.255.252.2</v4-static-route-prefix>
										<v4-next-hop-address>192.168.1.15</v4-next-hop-address>
										<v4-static-route-prefix-length>28</v4-static-route-prefix-length>
									</v4-static-routes>
									<v4-static-routes>
										<v4-static-route-prefix>255.255.252.3</v4-static-route-prefix>
										<v4-next-hop-address>192.168.1.15</v4-next-hop-address>
										<v4-static-route-prefix-length>28</v4-static-route-prefix-length>
									</v4-static-routes>
								</static-routes>
								<dhcp>
									<v6-dhcp-server-enabled>N</v6-dhcp-server-enabled>
									<v4-dhcp-server-enabled>Y</v4-dhcp-server-enabled>
									<use-v6-default-pool>N</use-v6-default-pool>
									<excluded-v4-dhcp-addresses-from-default-pool>
									  <excluded-v4-address>192.168.1.7</excluded-v4-address>
									</excluded-v4-dhcp-addresses-from-default-pool>
									<excluded-v4-dhcp-addresses-from-default-pool>
									  <excluded-v4-address>192.168.1.8</excluded-v4-address>
									</excluded-v4-dhcp-addresses-from-default-pool>
									<v4-dhcp-pools>
										<v4-dhcp-relay-next-hop-address>1.1.1.1</v4-dhcp-relay-next-hop-address>
										<v4-dhcp-pool-prefix-length>28</v4-dhcp-pool-prefix-length>
										<excluded-v4-addresses>
											<excluded-v4-address>192.168.1.5</excluded-v4-address>
										</excluded-v4-addresses>
										<v4-dhcp-relay-gateway-address>2.2.2.1</v4-dhcp-relay-gateway-address>
										<excluded-v4-addresses>
											<excluded-v4-address>192.168.1.6</excluded-v4-address>
										</excluded-v4-addresses>
										<v4-dhcp-pool-prefix>192.155.2.3</v4-dhcp-pool-prefix>
									</v4-dhcp-pools>
									<v4-dhcp-pools>
										<v4-dhcp-relay-next-hop-address>1.1.1.2</v4-dhcp-relay-next-hop-address>
										<v4-dhcp-pool-prefix-length>28</v4-dhcp-pool-prefix-length>
										<excluded-v4-addresses>
											<excluded-v4-address>192.168.1.6</excluded-v4-address>
										</excluded-v4-addresses>
										<v4-dhcp-relay-gateway-address>2.2.2.2</v4-dhcp-relay-gateway-address>
										<excluded-v4-addresses>
											<excluded-v4-address>192.168.1.7</excluded-v4-address>
										</excluded-v4-addresses>
										<v4-dhcp-pool-prefix>192.155.2.4</v4-dhcp-pool-prefix>
									</v4-dhcp-pools>									
									<use-v4-default-pool>Y</use-v4-default-pool>
									<excluded-v6-dhcp-addresses-from-default-pool>
									  <excluded-v6-address>1:5</excluded-v6-address>
									</excluded-v6-dhcp-addresses-from-default-pool>
									<excluded-v6-dhcp-addresses-from-default-pool>
									  <excluded-v6-address>1:6</excluded-v6-address>
									</excluded-v6-dhcp-addresses-from-default-pool>
									<v6-dhcp-pools>
										<v6-dhcp-relay-next-hop-address>4:4</v6-dhcp-relay-next-hop-address>
										<v6-dhcp-pool-prefix-length>28</v6-dhcp-pool-prefix-length>
										<excluded-v6-addresses>
											<excluded-v6-address>1:1</excluded-v6-address>
										</excluded-v6-addresses>
										<v6-dhcp-relay-gateway-address>3:3</v6-dhcp-relay-gateway-address>
										<excluded-v6-addresses>
											<excluded-v6-address>2:2</excluded-v6-address>
										</excluded-v6-addresses>
										<v6-dhcp-pool-prefix>0:0</v6-dhcp-pool-prefix>
									</v6-dhcp-pools>
									<v6-dhcp-pools>
										<v6-dhcp-relay-next-hop-address>4:4</v6-dhcp-relay-next-hop-address>
										<v6-dhcp-pool-prefix-length>28</v6-dhcp-pool-prefix-length>
										<excluded-v6-addresses>
											<excluded-v6-address>1:1</excluded-v6-address>
										</excluded-v6-addresses>
										<v6-dhcp-relay-gateway-address>3:3</v6-dhcp-relay-gateway-address>
										<excluded-v6-addresses>
											<excluded-v6-address>2:2</excluded-v6-address>
										</excluded-v6-addresses>
										<v6-dhcp-pool-prefix>0:0</v6-dhcp-pool-prefix>
									</v6-dhcp-pools>									
								</dhcp>
								<firewall-lite>
									<stateful-firewall-lite-v6-enabled>N</stateful-firewall-lite-v6-enabled>
									<stateful-firewall-lite-v4-enabled>Y</stateful-firewall-lite-v4-enabled>
									<v4-firewall-packet-filters>
									  <v4-firewall-prefix>0.0.0.1</v4-firewall-prefix>
									  <v4-firewall-prefix-length>1</v4-firewall-prefix-length>
									  <allow-icmp-ping>Y</allow-icmp-ping>
									  <udp-ports>
									    <port-number>1</port-number>
									  </udp-ports>
									  <tcp-ports>
									    <port-number>1</port-number>
									  </tcp-ports>
									</v4-firewall-packet-filters>
									<v4-firewall-packet-filters>
									  <v4-firewall-prefix>0.0.0.2</v4-firewall-prefix>
									  <v4-firewall-prefix-length>2</v4-firewall-prefix-length>
									  <allow-icmp-ping>Y</allow-icmp-ping>
									  <udp-ports>
									    <port-number>2</port-number>
									  </udp-ports>
									  <tcp-ports>
									    <port-number>2</port-number>
									  </tcp-ports>
									</v4-firewall-packet-filters>
									<v6-firewall-packet-filters>
									  <v6-firewall-prefix>:</v6-firewall-prefix>
									  <v6-firewall-prefix-length>0</v6-firewall-prefix-length>
									  <allow-icmp-ping>Y</allow-icmp-ping>
									  <udp-ports>
									    <port-number>3</port-number>
									  </udp-ports>
									  <tcp-ports>
									    <port-number>3</port-number>
									  </tcp-ports>
									</v6-firewall-packet-filters>
									<v6-firewall-packet-filters>
									  <v6-firewall-prefix>:</v6-firewall-prefix>
									  <v6-firewall-prefix-length>1</v6-firewall-prefix-length>
									  <allow-icmp-ping>Y</allow-icmp-ping>
									  <udp-ports>
									    <port-number>4</port-number>
									  </udp-ports>
									  <tcp-ports>
									    <port-number>4</port-number>
									  </tcp-ports>
									</v6-firewall-packet-filters>
								</firewall-lite>
								<pat>
									<v4-pat-pools>
										<v4-pat-pool-prefix>192.168.1.44</v4-pat-pool-prefix>
										<v4-pat-pool-next-hop-address>192.168.1.5</v4-pat-pool-next-hop-address>
										<v4-pat-pool-prefix-length>0</v4-pat-pool-prefix-length>
									</v4-pat-pools>
									<use-v4-default-pool>Y</use-v4-default-pool>
									<v4-pat-enabled>N</v4-pat-enabled>
									<v4-pat-pools>
										<v4-pat-pool-prefix>192.168.1.45</v4-pat-pool-prefix>
										<v4-pat-pool-next-hop-address>192.168.1.6</v4-pat-pool-next-hop-address>
										<v4-pat-pool-prefix-length>28</v4-pat-pool-prefix-length>
									</v4-pat-pools>
								</pat>
								<nat>
								  <v4-nat-enabled>Y</v4-nat-enabled>
								  <v4-nat-mapping-entries>
								    <v4-nat-internal>0.0.0.0</v4-nat-internal>
								    <v4-nat-next-hop-address>0.0.0.0</v4-nat-next-hop-address>
								    <v4-nat-external>0.0.0.0</v4-nat-external>
								  </v4-nat-mapping-entries>
								  <v4-nat-mapping-entries>
								    <v4-nat-internal>0.0.0.1</v4-nat-internal>
								    <v4-nat-next-hop-address>0.0.0.1</v4-nat-next-hop-address>
								    <v4-nat-external>0.0.0.1</v4-nat-external>
								  </v4-nat-mapping-entries>
								</nat>
								<vr-designation>primary</vr-designation>
								<v4-vce-loopback-address>162.200.3.144</v4-vce-loopback-address>
								<v6-vr-lan-prefix-length>64</v6-vr-lan-prefix-length>
								<v6-vce-wan-address>2001:1890:12e3:2da::</v6-vce-wan-address>
								<v6-vr-lan-prefix>2620:0:10d0:f:ffff:ffff:ffff:fffe</v6-vr-lan-prefix>
								<v4-vr-lan-prefix-length>24</v4-vr-lan-prefix-length>
								<v4-vr-lan-prefix>10.192.27.254</v4-vr-lan-prefix>
								<v4-public-lan-prefixes>
									<t-provided-v4-lan-public-prefixes>
										<request-index>1</request-index>
										<v4-next-hop-address>192.168.1.2</v4-next-hop-address>
										<v4-lan-public-prefix>192.168.1.1</v4-lan-public-prefix>
										<v4-lan-public-prefix-length>28</v4-lan-public-prefix-length>
									</t-provided-v4-lan-public-prefixes>
									<t-provided-v4-lan-public-prefixes>
										<request-index>1</request-index>
										<v4-next-hop-address>192.168.1.72</v4-next-hop-address>
										<v4-lan-public-prefix>192.168.1.71</v4-lan-public-prefix>
										<v4-lan-public-prefix-length>28</v4-lan-public-prefix-length>
									</t-provided-v4-lan-public-prefixes>
									<t-provided-v4-lan-public-prefixes>
										<request-index>1</request-index>
										<v4-next-hop-address>192.168.1.68</v4-next-hop-address>
										<v4-lan-public-prefix>192.168.1.67</v4-lan-public-prefix>
										<v4-lan-public-prefix-length>28</v4-lan-public-prefix-length>
									</t-provided-v4-lan-public-prefixes>
								</v4-public-lan-prefixes>
								<v6-public-lan-prefixes>
									<t-provided-v6-lan-public-prefixes>
										<request-index>1</request-index>
										<v6-next-hop-address>2001:1890:12e3:2da::</v6-next-hop-address>
										<v6-lan-public-prefix>2001:1890:12e3:2da::</v6-lan-public-prefix>
										<v6-lan-public-prefix-length>28</v6-lan-public-prefix-length>
									</t-provided-v6-lan-public-prefixes>
									<t-provided-v6-lan-public-prefixes>
										<request-index>1</request-index>
										<v6-next-hop-address>2001:1890:12e3:2da::</v6-next-hop-address>
										<v6-lan-public-prefix>2001:1890:12e3:3da::</v6-lan-public-prefix>
										<v6-lan-public-prefix-length>28</v6-lan-public-prefix-length>
									</t-provided-v6-lan-public-prefixes>
									<t-provided-v6-lan-public-prefixes>
										<request-index>1</request-index>
										<v6-next-hop-address>2001:1890:12e3:2da::</v6-next-hop-address>
										<v6-lan-public-prefix>2001:1890:12e3:4da::</v6-lan-public-prefix>
										<v6-lan-public-prefix-length>28</v6-lan-public-prefix-length>
									</t-provided-v6-lan-public-prefixes>
								</v6-public-lan-prefixes>
							</vr-lan-interface>
							<routing-protocol>none</routing-protocol>
						</vr-lan>
<ucpe-vms-service-information>				
 <transport-service-information>
    <transport-service-type>AVPN</transport-service-type>
	<access-circuit-info>
	   <access-circuit-id>1</access-circuit-id>
	   <dual-mode>Active</dual-mode>
	</access-circuit-info>
	<access-circuit-info>
	   <access-circuit-id>2</access-circuit-id>
	   <dual-mode>Standby</dual-mode>
	</access-circuit-info>	
 </transport-service-information>
 <ucpe-information>
    <ucpe-host-name>hostname</ucpe-host-name>
    <ucpe-activation-code>activecode</ucpe-activation-code>
    <out-of-band-management-modem>OOB</out-of-band-management-modem>
  </ucpe-information>
  <vnf-list>
	<vnf-information> 
		<vnf-instance-id>1</vnf-instance-id> 
		<vnf-sequence-number>1</vnf-sequence-number>
		<vnf-type>ZZ</vnf-type>
		<vnf-vendor>JUNIPER</vnf-vendor>
		<vnf-model>MODEL1</vnf-model>
		<vnf-id>1</vnf-id>
		<prov-status>1</prov-status>
		<operational-state>1</operational-state>
		<orchestration-status>1</orchestration-status>
		<equipment-role>1</equipment-role>
    </vnf-information>
	<vnf-information> 
		<vnf-instance-id>2</vnf-instance-id> 
		<vnf-sequence-number>2</vnf-sequence-number>
		<vnf-type>HY</vnf-type>
		<vnf-vendor>JUNIPER</vnf-vendor>
		<vnf-model>MODEL2</vnf-model>
		<vnf-id>2</vnf-id>
		<prov-status>2</prov-status>
		<operational-state>2</operational-state>
		<orchestration-status>2</orchestration-status>
		<equipment-role>2</equipment-role>
    </vnf-information>					
  </vnf-list>
 </ucpe-vms-service-information>										
						<request-information>
							<request-action>Layer3ServiceActivateRequest</request-action>
							<order-number>4281555</order-number>
							<request-id>155415ab-b4a7-4382-b4c6-d17d9sm42855</request-id>
							<notification-url>https://csi-tst-q22.it.com:22443/Services/com/cingular/csi/sdn/SendManagedNetworkStatusNotification.jws</notification-url>
							<source>OMX</source>
							<order-version>1</order-version>
						</request-information>
						<sdnc-request-header>
							<svc-action>activate</svc-action>
							<svc-notification-url>https://msojra.mtsnjdcp1.aic.cip.com:8443/adapters/rest/SDNCNotify</svc-notification-url>
							<svc-request-id>5b1f3c5d-cdf9-488d-8a4b-d3f1229d7760</svc-request-id>
						</sdnc-request-header>
						<l2-homing-information>
							<topology>MultiPoint</topology>
							<preferred-aic-clli>MTSNJA4LCP1</preferred-aic-clli>
							<evc-name>AS/VLXM/003717//SW</evc-name>
						</l2-homing-information>
						<service-information>
							<service-instance-id>FK/VLXM/003717//SW_INTERNET</service-instance-id>
							<subscriber-name>ST E2E Test42855_1300004281555</subscriber-name>
							<service-type>SDN-ETHERNET-INTERNET</service-type>
						</service-information>
						<internet-service-change-details>
							<internet-evc-speed-value>10</internet-evc-speed-value>
							<internet-evc-speed-units>Kbps</internet-evc-speed-units>
							<t-provided-v4-lan-public-prefixes>
							   <request-index>1</request-index>
							   <v4-next-hop-address>192.168.1.15</v4-next-hop-address>
							   <v4-lan-public-prefix>192.168.1.15</v4-lan-public-prefix>
							   <v4-lan-public-prefix-length>28</v4-lan-public-prefix-length>
							</t-provided-v4-lan-public-prefixes>
							<t-provided-v4-lan-public-prefixes>
							   <request-index>2</request-index>
							   <v4-next-hop-address>192.168.1.16</v4-next-hop-address>
							   <v4-lan-public-prefix>192.168.1.16</v4-lan-public-prefix>
							   <v4-lan-public-prefix-length>28</v4-lan-public-prefix-length>
							</t-provided-v4-lan-public-prefixes>
							<t-provided-v6-lan-public-prefixes>
							   <request-index>1</request-index>
							   <v6-next-hop-address>2001:1890:12e3:2da::</v6-next-hop-address>
							   <v6-lan-public-prefix>2001:1890:12e3:2da::</v6-lan-public-prefix>
							   <v6-lan-public-prefix-length>28</v6-lan-public-prefix-length>
							</t-provided-v6-lan-public-prefixes>
							<t-provided-v6-lan-public-prefixes>
							   <request-index>1</request-index>
							   <v6-next-hop-address>2001:1890:12e3:2da::</v6-next-hop-address>
							   <v6-lan-public-prefix>2001:1890:12e3:2da::</v6-lan-public-prefix>
							   <v6-lan-public-prefix-length>28</v6-lan-public-prefix-length>
							</t-provided-v6-lan-public-prefixes>
						</internet-service-change-details>
					</service-data>
				</layer3-service-list>
</tag0:RequestData>
"""

def sdncAdapterResponseEmpty =
"""<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
   <sdncadapterworkflow:response-data/>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

def sdncAdapterResponseError =
"""<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
                                                 xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
   <sdncadapterworkflow:response-data>
      <tag0:CallbackHeader>
         <tag0:RequestId>39542e39-ccc3-4d1a-8b79-04ce88526613</tag0:RequestId>
         <tag0:ResponseCode>404</tag0:ResponseCode>
         <tag0:ResponseMessage>Error processing request to SDNC. Not Found.
			https://sdncodl.us.aic.cip.com:8443/restconf/config/L3SDN-API:services/layer3-service-list/MVM%2FVLXP%2F000855%2F%2FShakeout.
			SDNC Returned-[error-type:application, error-tag:data-missing,
			error-message:Request could not be completed because the relevant
			data model content does not exist.]</tag0:ResponseMessage>
      </tag0:CallbackHeader>
   </sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

				  								
	@Test
	public void testPreProcessRequest() {

		String sdncAdapterWorkflowRequest = FileUtil.readResourceFile("__files/SDN-ETHERNET-INTERNET/SDNCAdapterV1/sdncadapterworkflowrequest.xml")
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
		when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
		when(mockExecution.getVariable("sdncAdapterWorkflowRequest")).thenReturn(sdncAdapterWorkflowRequest)
		when(mockExecution.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn("http://someurl.someting.com:28080/mso/sdncAdapterCallbackServiceImpl")
		when(mockExecution.getVariable("URN_mso_use_qualified_host")).thenReturn("true")
		when(mockExecution.getProcessInstanceId()).thenReturn("745b1b50-e39e-4685-9cc8-c71f0bde8bf0")
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
	

		SDNCAdapter sdncAdapter = new SDNCAdapter()
		sdncAdapter.preProcessRequest(mockExecution)
		
		MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
		debugger.printInvocations(mockExecution)
		

		verify(mockExecution).setVariable("prefix","SDNCA_")
		verify(mockExecution).setVariable("sdncAdapterResponse","")
		verify(mockExecution).setVariable("asynchronousResponseTimeout",false)
		verify(mockExecution).setVariable("continueListening",false)
		verify(mockExecution).setVariable("BasicAuthHeaderValue","Basic cGFzc3dvcmQ=")
		verify(mockExecution).setVariable("serviceConfigActivate",false)
		verify(mockExecution).setVariable("SDNCA_requestId", "745b1b50-e39e-4685-9cc8-c71f0bde8bf0")
		verify(mockExecution).setVariable("SDNCA_SuccessIndicator",false)
		verify(mockExecution).setVariable("source","")
		verify(mockExecution).setVariable("sdncAdapterRequest", sdncAdapterRequest)	
	}
	
	@Test
	public void testProcessResponse()
	{
		String sdncAdapterCallbackResponse = FileUtil.readResourceFile("__files/SDN-ETHERNET-INTERNET/SDNCAdapterV1mock/sdncadaptercallbackrequest.xml")
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("sdncAdapterCallbackRequest")).thenReturn(sdncAdapterCallbackResponse)
		SDNCAdapter sdncAdapter = new SDNCAdapter()
		sdncAdapter.postProcessResponse(mockExecution)
		
//		MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
//		debugger.printInvocations(mockExecution)
		
		verify(mockExecution).getVariable("isDebugLogEnabled")
		verify(mockExecution,times(2)).getVariable("sdncAdapterCallbackRequest")
		verify(mockExecution).setVariable("sdncAdapterResponse",workflowResponse)
		verify(mockExecution).setVariable("enhancedCallbackRequestData",enhancedCallbackRequestData)
		verify(mockExecution).setVariable("continueListening",false)

	}	
	
	@Test
	public void testProcessResponse_ErrorCase_404()
	{
		String sdncAdapterCallbackErrorResponse = FileUtil.readResourceFile("sdncadaptercallbackrequest_404CallBack.xml")
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("sdncAdapterCallbackRequest")).thenReturn(sdncAdapterCallbackErrorResponse)
		SDNCAdapter sdncAdapter = new SDNCAdapter()
		sdncAdapter.postProcessResponse(mockExecution)
		
		verify(mockExecution, times(1)).getVariable("isDebugLogEnabled")
		verify(mockExecution,times(2)).getVariable("sdncAdapterCallbackRequest")
		verify(mockExecution).setVariable("sdncAdapterResponse", sdncAdapterResponseError)
		verify(mockExecution).setVariable("enhancedCallbackRequestData", "")
		verify(mockExecution).setVariable("continueListening",false)
		
	}
	
	@Test
	public void testProcessResponse_ErrorCase_InvalidCallback()
	{
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("sdncAdapterCallbackRequest")).thenReturn("<h1>Service Unavailable</h1>")
		SDNCAdapter sdncAdapter = new SDNCAdapter()
		sdncAdapter.postProcessResponse(mockExecution)
		
		verify(mockExecution).getVariable("isDebugLogEnabled")
		verify(mockExecution,times(2)).getVariable("sdncAdapterCallbackRequest")
		verify(mockExecution).setVariable("sdncAdapterResponse", sdncAdapterResponseEmpty)
		verify(mockExecution).setVariable("enhancedCallbackRequestData", "")
		verify(mockExecution).setVariable("continueListening",false)

	}
	
	@Test
	public void postProcessResponse()
	{
		
		String SDNCAdapterCallbackRequest = 
		"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<SDNCAdapterCallbackRequest
	xmlns="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
	<CallbackHeader>
		<RequestId>3bb02798-b344-4d28-9bca-1f029954d1c9</RequestId>
		<ResponseCode>404</ResponseCode>
		<ResponseMessage>Error processing request to SDNC. Not Found.
			https://sdncodl.us.infra.aic.net:8443/restconf/config/L3SDN-API:services/layer3-service-list/85%2FCSIP%2F141203%2FPT_CSI9999998693.
			SDNC Returned-[error-type:application, error-tag:data-missing,
			error-message:Request could not be completed because the relevant
			data model content does not exist ]</ResponseMessage>
	</CallbackHeader>
</SDNCAdapterCallbackRequest>"""
		
		String sdncAdapterResponse =
		"""<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
                                                 xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
   <sdncadapterworkflow:response-data>
      <tag0:CallbackHeader>
         <tag0:RequestId>3bb02798-b344-4d28-9bca-1f029954d1c9</tag0:RequestId>
         <tag0:ResponseCode>404</tag0:ResponseCode>
         <tag0:ResponseMessage>Error processing request to SDNC. Not Found.
			https://sdncodl.us.infra.aic.net:8443/restconf/config/L3SDN-API:services/layer3-service-list/85%2FCSIP%2F141203%2FPT_CSI9999998693.
			SDNC Returned-[error-type:application, error-tag:data-missing,
			error-message:Request could not be completed because the relevant
			data model content does not exist ]</tag0:ResponseMessage>
      </tag0:CallbackHeader>
   </sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("sdncAdapterCallbackRequest")).thenReturn(SDNCAdapterCallbackRequest)
		SDNCAdapter sdncAdapter = new SDNCAdapter()
		sdncAdapter.postProcessResponse(mockExecution)
		
		verify(mockExecution).getVariable("isDebugLogEnabled")
		verify(mockExecution,times(2)).getVariable("sdncAdapterCallbackRequest")
		verify(mockExecution).setVariable("sdncAdapterResponse", sdncAdapterResponse)
		verify(mockExecution).setVariable("enhancedCallbackRequestData", "")
		verify(mockExecution).setVariable("continueListening",false)

	}

}