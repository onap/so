package org.onap.so.client.aai;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.so.client.aai.entities.DSLNode;
import org.onap.so.client.aai.entities.DSLQueryBuilder;
import org.onap.so.client.aai.entities.__;

public class DSLQueryBuilderTest {

	
	@Test
	public void whereTest() {
		DSLQueryBuilder<DSLNode, DSLNode> builder = new DSLQueryBuilder<>(new DSLNode(AAIObjectType.CLOUD_REGION,
				__.key("cloud-owner", "att-nc"),
				__.key("cloud-region-id", "test")));
		
		builder.to(__.node(AAIObjectType.VLAN_TAG)).where(
				__.node(AAIObjectType.OWNING_ENTITY,
						__.key("owning-entity-name", "name")
					)	
				).to(__.node(AAIObjectType.VLAN_TAG, __.key("vlan-id-outer", "108")).output());
		
		assertEquals("cloud-region('cloud-owner', 'att-nc')('cloud-region-id', 'test') > "
				+ "vlan-tag (> owning-entity('owning-entity-name', 'name')) > "
				+ "vlan-tag*('vlan-id-outer', '108')", builder.build());
	}
	
	@Test
	public void unionTest() {
		DSLQueryBuilder<DSLNode, DSLNode> builder = new DSLQueryBuilder<>(new DSLNode(AAIObjectType.GENERIC_VNF,
				__.key("vnf-id", "vnfId")).output());
		
		builder.union(__.node(AAIObjectType.PSERVER).output().to(__.node(AAIObjectType.COMPLEX).output()),
				__.node(AAIObjectType.VSERVER).to(__.node(AAIObjectType.PSERVER).output().to(__.node(AAIObjectType.COMPLEX).output())));
		
		assertEquals("generic-vnf*('vnf-id', 'vnfId') > " + "[ pserver* > complex*, "
				 + "vserver > pserver* > complex* ]", builder.build());
	}
	
	@Test
	public void whereUnionTest() {
		DSLQueryBuilder<DSLNode, DSLNode> builder = new DSLQueryBuilder<>(new DSLNode(AAIObjectType.GENERIC_VNF,
				__.key("vnf-id", "vnfId")).output());
		
		builder.where(
			__.union(
				__.node(AAIObjectType.PSERVER, __.key("hostname", "hostname1")),
				__.node(AAIObjectType.VSERVER).to(__.node(AAIObjectType.PSERVER, __.key("hostname", "hostname1")))));
		
		assertEquals("generic-vnf*('vnf-id', 'vnfId') (> [ pserver('hostname', 'hostname1'), "
				+ "vserver > pserver('hostname', 'hostname1') ])", builder.build());
	}
	
	@Test
	public void notNullTest() {
		DSLQueryBuilder<DSLNode, DSLNode> builder = new DSLQueryBuilder<>(new DSLNode(AAIObjectType.CLOUD_REGION,
				__.key("cloud-owner", "", "null").not()).output());
		
		assertEquals("cloud-region* !('cloud-owner', ' ', ' null ')", builder.build());
	}
}
