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
		element.setExternalKey(instanceGroupId);
		element.setPolicyInstanceName(policyInstanceName);
		element.setNamingType(namingType);
		element.setResourceName(instanceGroupName);
		element.setNamingIngredientsZeroOrMore(nfNamingCode);
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