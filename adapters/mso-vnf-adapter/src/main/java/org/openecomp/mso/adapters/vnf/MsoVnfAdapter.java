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


import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.Holder;

import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.adapters.vnf.exceptions.VnfAlreadyExists;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;
import org.openecomp.mso.entity.MsoRequest;

import java.util.Map;

@WebService (name="VnfAdapter", targetNamespace="http://org.openecomp.mso/vnf")
public interface MsoVnfAdapter
{
	/**
	 * This is the "Create VNF" Web Service Endpoint definition.
	 */
	@WebMethod
	public void createVnf (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="vnfType") @XmlElement(required=true) String vnfType,
							@WebParam(name="vnfVersion") @XmlElement(required=false) String vnfVersion,
							@WebParam(name="vnfName") @XmlElement(required=true) String vnfName,
							@WebParam(name="requestType") @XmlElement(required=false) String requestType,
							@WebParam(name="volumeGroupHeatStackId") @XmlElement(required=false) String volumeGroupHeatStackId,
							@WebParam(name="inputs") Map<String,String> inputs,
							@WebParam(name="failIfExists") Boolean failIfExists,
							@WebParam(name="backout") Boolean backout,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name="vnfId", mode=Mode.OUT) Holder<String> vnfId,
							@WebParam(name="outputs", mode=Mode.OUT) Holder<Map<String,String>> outputs,
							@WebParam(name="rollback", mode=Mode.OUT) Holder<VnfRollback> rollback )
		throws VnfException, VnfAlreadyExists;

	@WebMethod
	public void updateVnf (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="vnfType") @XmlElement(required=true) String vnfType,
							@WebParam(name="vnfVersion") @XmlElement(required=false) String vnfVersion,
							@WebParam(name="vnfName") @XmlElement(required=true) String vnfName,
							@WebParam(name="requestType") @XmlElement(required=false) String requestType,
							@WebParam(name="volumeGroupHeatStackId") @XmlElement(required=false) String volumeGroupHeatStackId,
							@WebParam(name="inputs") Map<String,String> inputs,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name="outputs", mode=Mode.OUT) Holder<Map<String,String>> outputs,
							@WebParam(name="rollback", mode=Mode.OUT) Holder<VnfRollback> rollback )
		throws VnfException;

	@WebMethod
	public void queryVnf (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="vnfName") @XmlElement(required=true) String vnfName,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name="vnfExists", mode=Mode.OUT) Holder<Boolean> vnfExists,
							@WebParam(name="vnfId", mode=Mode.OUT) Holder<String> vnfId,
							@WebParam(name="status", mode=Mode.OUT) Holder<VnfStatus> status,
							@WebParam(name="outputs", mode=Mode.OUT) Holder<Map<String,String>> outputs )
		throws VnfException;

	@WebMethod
	public void deleteVnf (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="vnfName") @XmlElement(required=true) String vnfName,
							@WebParam(name="request") MsoRequest msoRequest)
		throws VnfException;


	@WebMethod
	public void rollbackVnf (@WebParam(name="rollback") @XmlElement(required=true) VnfRollback rollback)
		throws VnfException;

	@WebMethod
	public void createVfModule (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="vnfType") @XmlElement(required=true) String vnfType,
							@WebParam(name="vnfVersion") @XmlElement(required=false) String vnfVersion,
							@WebParam(name="vnfName") @XmlElement(required=true) String vnfName,
							@WebParam(name="requestType") @XmlElement(required=false) String requestType,
							@WebParam(name="volumeGroupHeatStackId") @XmlElement(required=false) String volumeGroupHeatStackId,
							@WebParam(name="baseVfHeatStackId") @XmlElement(required=false) String baseVfHeatStackId,
							@WebParam(name = "modelCustomizationUuid") @XmlElement(required = false) String modelCustomizationUuid,
							@WebParam(name="inputs") Map<String,String> inputs,
							@WebParam(name="failIfExists") Boolean failIfExists,
							@WebParam(name="backout") Boolean backout,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name="vnfId", mode=Mode.OUT) Holder<String> vnfId,
							@WebParam(name="outputs", mode=Mode.OUT) Holder<Map<String,String>> outputs,
							@WebParam(name="rollback", mode=Mode.OUT) Holder<VnfRollback> rollback )
		throws VnfException, VnfAlreadyExists;

	@WebMethod
	public void deleteVfModule (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="vfName") @XmlElement(required=true) String vfName,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name = "vfModuleOutputs", mode = Mode.OUT) Holder<Map<String, String>> vfModuleOutputs)
		throws VnfException;

	@WebMethod
	public void updateVfModule (@WebParam(name="cloudSiteId") @XmlElement(required=true) String cloudSiteId,
							@WebParam(name="tenantId") @XmlElement(required=true) String tenantId,
							@WebParam(name="vnfType") @XmlElement(required=true) String vnfType,
							@WebParam(name="vnfVersion") @XmlElement(required=false) String vnfVersion,
							@WebParam(name="vnfName") @XmlElement(required=true) String vnfName,
							@WebParam(name="requestType") @XmlElement(required=false) String requestType,
							@WebParam(name="volumeGroupHeatStackId") @XmlElement(required=false) String volumeGroupHeatStackId,
							@WebParam(name="baseVfHeatStackId") @XmlElement(required=false) String baseVfHeatStackId,
							@WebParam(name="vfModuleStackId") @XmlElement(required=false) String vfModuleStackId,
							@WebParam(name = "modelCustomizationUuid") @XmlElement(required = false) String modelCustomizationUuid,
							@WebParam(name="inputs") Map<String,String> inputs,
							@WebParam(name="request") MsoRequest msoRequest,
							@WebParam(name="outputs", mode=Mode.OUT) Holder<Map<String,String>> outputs,
							@WebParam(name="rollback", mode=Mode.OUT) Holder<VnfRollback> rollback )
		throws VnfException;

	@WebMethod
	public void healthCheck ();
}
