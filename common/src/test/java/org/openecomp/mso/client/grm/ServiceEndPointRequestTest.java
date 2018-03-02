package org.openecomp.mso.client.grm;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openecomp.mso.client.grm.beans.OperationalInfo;
import org.openecomp.mso.client.grm.beans.Property;
import org.openecomp.mso.client.grm.beans.ServiceEndPoint;
import org.openecomp.mso.client.grm.beans.ServiceEndPointRequest;
import org.openecomp.mso.client.grm.beans.Version;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceEndPointRequestTest {

	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testMarshall() throws Exception {
		
		String expected = 
				"{\"serviceEndPoint\":{\"name\":\"TEST.ECOMP_PSL.Inventory\",\"version\":{\"major\":1,\"minor\":0,\"patch\":\"0\"}," +
				"\"hostAddress\":\"127.0.0.1\",\"listenPort\":\"8080\",\"latitude\":\"37.7022\",\"longitude\":\"121.9358\"," + 
				"\"contextPath\":\"/\",\"routeOffer\":\"TEST\",\"operationalInfo\":{\"createdBy\":\"edge\",\"updatedBy\":\"edge\"}," + 
				"\"properties\":[{\"name\":\"Environment\",\"value\":\"TEST\"},{\"name\":\"cpfrun_cluster_name\"," + 
				"\"value\":\"testcase_cluster_no_cluster\"}]},\"env\":\"DEV\"}";
		
		Version ver = new Version();
		ver.setMajor(1);
		ver.setMinor(0);
		ver.setPatch("0");
		
		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setName("TEST.ECOMP_PSL.Inventory");
		sep.setVersion(ver);
		sep.setHostAddress("127.0.0.1");
		sep.setListenPort("8080");
		sep.setLatitude("37.7022");
		sep.setLongitude("121.9358");
		sep.setContextPath("/");
		sep.setRouteOffer("TEST");
		
		OperationalInfo operInfo = new OperationalInfo();
		operInfo.setCreatedBy("edge");
		operInfo.setUpdatedBy("edge");
		
		sep.setOperationalInfo(operInfo);
		
		Property prop1 = new Property();
		prop1.setName("Environment");
		prop1.setValue("TEST");
		
		Property prop2 = new Property();
		prop2.setName("cpfrun_cluster_name");
		prop2.setValue("testcase_cluster_no_cluster");
		
		List<Property> props = new ArrayList<Property>();
		props.add(prop1);
		props.add(prop2);
		
		sep.setProperties(props);

		ServiceEndPointRequest request = new ServiceEndPointRequest();
		request.setEnv("DEV");
		request.setServiceEndPoint(sep);
		
		assertEquals(expected, mapper.writeValueAsString(request));
	}
}
