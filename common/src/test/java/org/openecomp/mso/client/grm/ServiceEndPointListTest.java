package org.openecomp.mso.client.grm;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.client.grm.beans.ServiceEndPoint;
import org.openecomp.mso.client.grm.beans.ServiceEndPointList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceEndPointListTest {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	
	@Test
	public void testUnmarshall() throws Exception {
		String endpointsJson = getFileContentsAsString("__files/grm/endpoints.json");
		ServiceEndPointList sel = mapper.readValue(endpointsJson, ServiceEndPointList.class);
		
		List<ServiceEndPoint> list = sel.getServiceEndPointList();
		ServiceEndPoint se = list.get(0);
		
		assertEquals(3, list.size());
		assertEquals("dummy.pod.ns.dummy-pod3", se.getName());
		assertEquals(Integer.valueOf(1), Integer.valueOf(se.getVersion().getMajor()));
		assertEquals(Integer.valueOf(0), Integer.valueOf(se.getVersion().getMinor()));
		assertEquals(Integer.valueOf(0), Integer.valueOf(se.getVersion().getPatch()));
		assertEquals("135.144.120.218", se.getHostAddress());
		assertEquals("32004", se.getListenPort());
		assertEquals("37.7022", se.getLatitude());
		assertEquals("121.9358", se.getLongitude());
		assertEquals("/", se.getContextPath());
		assertEquals("edge", se.getOperationalInfo().getCreatedBy());
		assertEquals("edge", se.getOperationalInfo().getUpdatedBy());
		assertEquals("Environment", se.getProperties().get(0).getName());
		assertEquals("DEV", se.getProperties().get(0).getValue());
	}
	
	protected String getFileContentsAsString(String fileName) {

		String content = "";
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			File file = new File(classLoader.getResource(fileName).getFile());
			content = new String(Files.readAllBytes(file.toPath()));
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception encountered reading " + fileName + ". Error: " + e.getMessage());
		}
		return content;
	}
}
