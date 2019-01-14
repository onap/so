/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.namingservice;

import java.util.List;

import org.onap.namingservice.model.Deleteelement;
import org.onap.namingservice.model.Element;
import org.onap.namingservice.model.NameGenDeleteRequest;
import org.onap.namingservice.model.NameGenRequest;
import org.springframework.stereotype.Component;

@Component
public class NamingRequestObjectBuilder{
	
	public Element elementMapper(String instanceGroupId, String policyInstanceName, String namingType, String nfNamingCode, String instanceGroupName){
		Element element = new Element();
		element.put("external-key", instanceGroupId);
		element.put("policy-instance-name", policyInstanceName);
		element.put("naming-type", namingType);
		element.put("resource-name", instanceGroupName);
		element.put("nf-naming-code", nfNamingCode);
		return element;
	}
	public Deleteelement deleteElementMapper(String instanceGroupId){
		Deleteelement deleteElement = new Deleteelement();
		deleteElement.setExternalKey(instanceGroupId);
		return deleteElement;
	}
	public NameGenRequest nameGenRequestMapper(List<Element> elements){
		NameGenRequest nameGenRequest = new NameGenRequest();
		nameGenRequest.setElements(elements);
		return nameGenRequest;
	}
	public NameGenDeleteRequest nameGenDeleteRequestMapper(List<Deleteelement> deleteElements){
		NameGenDeleteRequest nameGenDeleteRequest = new NameGenDeleteRequest();
		nameGenDeleteRequest.setElements(deleteElements);
		return nameGenDeleteRequest;
	}
}
