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

package org.onap.so.adapters.network;


import java.util.List;
import java.util.Map;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.Subnet;
/**
 * This webservice defines the Asynchronous versions of NETWORK adapter calls.
 * The notification messages for final responses are documented elsewhere
 * (by the client service WSDL).
 *
 */
@WebService (name="NetworkAdapterAsync", targetNamespace="http://org.onap.so/networkA")
public interface MsoNetworkAdapterAsync
{
	/**
	 * This is the "Create NETWORK" Web Service Endpoint definition.
	 */
	@WebMethod
	@Oneway
	public void createNetworkA (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="networkType") @XmlElement(required=true) String networkType,
							@WebParam(name="modelCustomizationUuid") String modelCustomizationUuid,
							@WebParam(name="networkName") @XmlElement(required=true) String networkName,
							@WebParam(name="physicalNetworkName") String physicalNetworkName,
							@WebParam(name="vlans") List<Integer> vlans,
							@WebParam(name="failIfExists") Boolean failIfExists,
							@WebParam(name="backout") Boolean backout,
							@WebParam(name="subnets") List<Subnet> subnets,
							@WebParam(name="networkParams") Map<String, String> networkParams,
							@WebParam(name="messageId") @XmlElement(required=true) String messageId,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name="notificationUrl") @XmlElement(required=true) String notificationUrl );

	@WebMethod
	@Oneway
	public void updateNetworkA (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
						@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
						@WebParam(name="networkType") @XmlElement(required=true) String networkType,
						@WebParam(name="modelCustomizationUuid") String modelCustomizationUuid,
						@WebParam(name="networkId") @XmlElement(required=true) String networkId,
						@WebParam(name="networkName") @XmlElement(required=true) String networkName,
						@WebParam(name="physicalNetworkName") @XmlElement(required=true) String physicalNetworkName,
						@WebParam(name="vlans") @XmlElement(required=true) List<Integer> vlans,
						@WebParam(name="subnets") List<Subnet> subnets,
						@WebParam(name="networkParams") Map<String, String> networkParams,
						@WebParam(name="messageId") @XmlElement(required=true) String messageId,
						@WebParam(name="request") MsoRequest msoRequest,
						@WebParam(name="notificationUrl") @XmlElement(required=true) String notificationUrl );

	@WebMethod
	@Oneway
	public void queryNetworkA (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="networkNameOrId") @XmlElement(required=true) String networkNameOrId,
							@WebParam(name="messageId") @XmlElement(required=true) String messageId,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name="notificationUrl") @XmlElement(required=true) String notificationUrl );

	@WebMethod
	@Oneway
	public void deleteNetworkA (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="networkType") @XmlElement(required=true) String networkType,
							@WebParam(name="modelCustomizationUuid") String modelCustomizationUuid,
							@WebParam(name="networkId") @XmlElement(required=true) String networkId,
							@WebParam(name="messageId") @XmlElement(required=true) String messageId,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name="notificationUrl") @XmlElement(required=true) String notificationUrl );

	@WebMethod
	@Oneway
	public void rollbackNetworkA (@WebParam(name="rollback") @XmlElement(required=true) NetworkRollback rollback,
						@WebParam(name="messageId") @XmlElement(required=true) String messageId,
						@WebParam(name="notificationUrl") @XmlElement(required=true) String notificationUrl );

	@WebMethod
	public void healthCheckA ();
}
