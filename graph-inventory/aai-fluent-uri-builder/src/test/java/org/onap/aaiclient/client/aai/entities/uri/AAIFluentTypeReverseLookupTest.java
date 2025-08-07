package org.onap.aaiclient.client.aai.entities.uri;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;
import org.onap.aaiclient.client.aai.AAIObjectType;

public class AAIFluentTypeReverseLookupTest {


    @Test
    public void reverseParseEntryUri() {
        String cloudRegion =
                "http://localhost:8888/aai/v38/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region-id}";
        String newvce = "/aai/v9/network/newvces/newvce/{vnf-id2}";

        AAIFluentTypeReverseLookup lookup = new AAIFluentTypeReverseLookup();
        AAIObjectType type = lookup.fromName("cloud-region", cloudRegion);

        assertEquals("cloud-region", type.typeName());
        assertEquals("/cloud-regions/cloud-region/{cloud-owner}/{cloud-region-id}", type.partialUri());
        assertEquals("/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region-id}",
                type.uriTemplate());

        type = lookup.fromName("newvce", newvce);

        assertEquals("newvce", type.typeName());
        assertEquals("/newvces/newvce/{vnf-id2}", type.partialUri());
        assertEquals("/network/newvces/newvce/{vnf-id2}", type.uriTemplate());

        type = lookup.fromName("unknown-type-of-something", "/some/endpoint");

        assertEquals("unknown", type.typeName());

    }

    @Test
    public void reverseParseTest() {

        String pserverParent =
                "/aai/v9/cloud-infrastructure/pservers/pserver/{hostname}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{l-interface.interface-name}";
        String cloudRegionParent =
                "http://localhost:8888/aai/v38/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region-id}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}/l-interfaces/l-interface/{interface-name}";
        String newVceParent = "/network/newvces/newvce/{vnf-id2}/l-interfaces/l-interface/{l-interface.interface-name}";


        AAIFluentTypeReverseLookup lookup = new AAIFluentTypeReverseLookup();

        AAIObjectType type = lookup.fromName("l-interface", pserverParent);

        assertEquals("l-interface", type.typeName());
        assertEquals("/l-interfaces/l-interface/{l-interface.interface-name}", type.partialUri());
        assertEquals(
                "/cloud-infrastructure/pservers/pserver/{hostname}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{l-interface.interface-name}",
                type.uriTemplate());

        type = lookup.fromName("l-interface", cloudRegionParent);

        assertEquals("l-interface", type.typeName());
        assertEquals("/l-interfaces/l-interface/{l-interface.interface-name}", type.partialUri());
        assertEquals(
                "/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region-id}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}/l-interfaces/l-interface/{l-interface.interface-name}",
                type.uriTemplate());

        type = lookup.fromName("l-interface", newVceParent);

        assertEquals("l-interface", type.typeName());
        assertEquals("/l-interfaces/l-interface/{l-interface.interface-name}", type.partialUri());
        assertEquals("/network/newvces/newvce/{vnf-id2}/l-interfaces/l-interface/{l-interface.interface-name}",
                type.uriTemplate());



    }
}
