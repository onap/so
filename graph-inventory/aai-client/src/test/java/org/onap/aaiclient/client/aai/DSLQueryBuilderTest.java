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

package org.onap.aaiclient.client.aai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.entities.DSLNodeKey;
import org.onap.aaiclient.client.graphinventory.entities.DSLQueryBuilder;
import org.onap.aaiclient.client.graphinventory.entities.DSLStartNode;
import org.onap.aaiclient.client.graphinventory.entities.Node;
import org.onap.aaiclient.client.graphinventory.entities.Output;
import org.onap.aaiclient.client.graphinventory.entities.Start;
import org.onap.aaiclient.client.graphinventory.entities.TraversalBuilder;
import org.onap.aaiclient.client.graphinventory.entities.__;

public class DSLQueryBuilderTest {

    @Test
    public void whereTest() {
        DSLQueryBuilder<Start, Start> builder = TraversalBuilder.fragment(new DSLStartNode(Types.CLOUD_REGION,
                __.key("cloud-owner", "att-nc"), __.key("cloud-region-id", "test")));

        builder.to(__.node(Types.VLAN_TAG)).where(__.node(Types.OWNING_ENTITY, __.key("owning-entity-name", "name")))
                .to(__.node(Types.VLAN_TAG, __.key("vlan-id-outer", "108")).output());

        assertEquals("cloud-region('cloud-owner', 'att-nc')('cloud-region-id', 'test') > "
                + "vlan-tag (> owning-entity('owning-entity-name', 'name')) > " + "vlan-tag*('vlan-id-outer', '108')",
                builder.build().get());
    }

    @Test
    public void unionTest() {
        DSLQueryBuilder<Output, Output> builder =
                TraversalBuilder.traversal(new DSLStartNode(Types.GENERIC_VNF, __.key("vnf-id", "vnfId")).output());

        builder.union(__.node(Types.PSERVER).output().to(__.node(Types.COMPLEX).output()),
                __.node(Types.VSERVER).to(__.node(Types.PSERVER).output().to(__.node(Types.COMPLEX).output())));

        assertEquals(
                "generic-vnf*('vnf-id', 'vnfId') > " + "[ pserver* > complex*, " + "vserver > pserver* > complex* ]",
                builder.build().get());
    }

    @Test
    public void whereUnionTest() {
        DSLQueryBuilder<Output, Output> builder =
                TraversalBuilder.traversal(new DSLStartNode(Types.GENERIC_VNF, __.key("vnf-id", "vnfId")).output());

        builder.where(__.union(__.node(Types.PSERVER, __.key("hostname", "hostname1")),
                __.node(Types.VSERVER).to(__.node(Types.PSERVER, __.key("hostname", "hostname1")))));

        assertEquals("generic-vnf*('vnf-id', 'vnfId') (> [ pserver('hostname', 'hostname1'), "
                + "vserver > pserver('hostname', 'hostname1') ])", builder.build().get());
    }

    @Test
    public void notNullTest() {
        DSLQueryBuilder<Output, Output> builder = TraversalBuilder
                .traversal(new DSLStartNode(Types.CLOUD_REGION, __.key("cloud-owner", "", "null").not()).output());

        assertEquals("cloud-region* !('cloud-owner', ' ', ' null ')", builder.build().get());
    }

    @Test
    public void shortCutToTest() {
        DSLQueryBuilder<Output, Output> builder =
                TraversalBuilder.traversal(new DSLStartNode(Types.PSERVER, __.key("hostname", "my-hostname")).output());

        builder.to(Types.P_INTERFACE).to(Types.SRIOV_PF, __.key("pf-pci-id", "my-id"));
        assertEquals("pserver*('hostname', 'my-hostname') > p-interface > sriov-pf('pf-pci-id', 'my-id')",
                builder.build().get());
    }

    @Test
    public void limitTest() {
        DSLQueryBuilder<Output, Output> builder =
                TraversalBuilder.traversal(new DSLStartNode(Types.PSERVER, __.key("hostname", "my-hostname")).output());

        builder.to(Types.P_INTERFACE).limit(2).to(Types.SRIOV_PF, __.key("pf-pci-id", "my-id"));
        assertEquals("pserver*('hostname', 'my-hostname') > p-interface > sriov-pf('pf-pci-id', 'my-id') LIMIT 2",
                builder.build().get());
    }

    @Test
    public void equalsTest() {
        DSLQueryBuilder<Output, Output> builder =
                TraversalBuilder.traversal(new DSLStartNode(Types.PSERVER, __.key("hostname", "my-hostname")).output());

        builder.to(Types.P_INTERFACE).to(Types.SRIOV_PF, __.key("pf-pci-id", "my-id"));
        assertTrue(
                builder.equals("pserver*('hostname', 'my-hostname') > p-interface > sriov-pf('pf-pci-id', 'my-id')"));
        assertTrue(builder.equals(builder));
    }

    @Test
    public void mixedTypeTest() {
        DSLQueryBuilder<Start, Start> builder = TraversalBuilder.fragment(
                new DSLStartNode(Types.CLOUD_REGION, __.key("cloud-owner", "owner"), __.key("cloud-region-id", "id")));
        builder.to(__.node(Types.VLAN_TAG, __.key("vlan-id-outer", 167), __.key("my-boolean", true)).output());
        assertTrue(builder.equals(
                "cloud-region('cloud-owner', 'owner')('cloud-region-id', 'id') > vlan-tag*('vlan-id-outer', 167)('my-boolean', true)"));
    }

    @Test
    public void outputOnNodeLambdasTest() {
        DSLQueryBuilder<Start, Start> builder =
                TraversalBuilder.fragment(new DSLStartNode(Types.L_INTERFACE, new DSLNodeKey("interface-id", "myId")));

        builder.to(Types.VSERVER, __.key("vserver-name", "myName")).output().to(Types.P_INTERFACE).output();
        assertEquals("l-interface('interface-id', 'myId') > vserver*('vserver-name', 'myName') > p-interface*",
                builder.build().get());
    }

    @Test
    public void skipOutputOnUnionTest() {
        DSLQueryBuilder<Output, Output> builder =
                TraversalBuilder.traversal(new DSLStartNode(Types.GENERIC_VNF, __.key("vnf-id", "vnfId")).output());

        builder.union(__.node(Types.PSERVER).output().to(__.node(Types.COMPLEX).output()),
                __.node(Types.VSERVER).to(__.node(Types.PSERVER).output().to(__.node(Types.COMPLEX).output())))
                .output();

        assertEquals(
                "generic-vnf*('vnf-id', 'vnfId') > " + "[ pserver* > complex*, " + "vserver > pserver* > complex* ]",
                builder.build().get());
    }

    @Test
    public void selectOutputFilterTest() {
        DSLQueryBuilder<Output, Output> builder =
                TraversalBuilder.traversal(new DSLStartNode(Types.CLOUD_REGION, __.key("cloud-owner", "CloudOwner"))
                        .output("cloud-region-id", "a", "b"));
        builder.to(__.node(Types.PSERVER)).output("x", "y", "z");

        assertEquals("cloud-region{'cloud-region-id', 'a', 'b'}('cloud-owner', 'CloudOwner') > pserver{'x', 'y', 'z'}",
                builder.build().toString());
    }

    @Test
    public void selectOutputFilterOnNodeTest() {
        DSLStartNode node = new DSLStartNode(Types.CLOUD_REGION, __.key("cloud-owner", "CloudOwner"));
        DSLQueryBuilder<Start, Node> builder = TraversalBuilder.fragment(node).output("cloud-region-id");

        assertEquals("cloud-region{'cloud-region-id'}('cloud-owner', 'CloudOwner')", builder.build().toString());
    }
}
