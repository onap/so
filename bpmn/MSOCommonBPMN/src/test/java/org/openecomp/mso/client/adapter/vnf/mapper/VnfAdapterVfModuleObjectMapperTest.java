package org.openecomp.mso.client.adapter.vnf.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openecomp.mso.BaseTest;

public class VnfAdapterVfModuleObjectMapperTest{

	private VnfAdapterVfModuleObjectMapper mapper = new VnfAdapterVfModuleObjectMapper();
	
	@Test
	public void createVnfcSubInterfaceKeyTest() {
		
		assertEquals("type_0_subint_role_port_0", mapper.createVnfcSubInterfaceKey("type", 0, "role", 0));
	}
	
	@Test
	public void createGlobalVnfcSubInterfaceKeyTest() {
		
		assertEquals("type_subint_role_port_0", mapper.createGlobalVnfcSubInterfaceKey("type", "role", 0));
	}
	
	@Test
	public void addPairToMapTest() {
		Map<String, String> map = new HashMap<>();
		
		mapper.addPairToMap(map, "test", "_key", Arrays.asList("a", "b"));
		
		assertEquals("a,b", map.get("test_key"));
		
		mapper.addPairToMap(map, "test", "_key2", Arrays.asList());
		
		assertThat(map.containsKey("test_key2"), equalTo(false));
		
		mapper.addPairToMap(map, "test", "_key3", "myVal");
		
		assertEquals("myVal", map.get("test_key3"));
		
	}
	
}
