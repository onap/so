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

package org.onap.so.client.aai.entities.uri;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.junit.Test;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.client.graphinventory.entities.uri.SimpleUri;

public class AAISimpleUriTest {



    @Test
    public void relatedToTestPlural() {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");
        uri.relatedTo(AAIObjectPlurals.PSERVER);
        String uriOutput = uri.build().toString();
        assertEquals(true, uriOutput.contains("related-to"));
    }

    @Test
    public void relatedToTestSingular() {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");
        uri.relatedTo(AAIObjectType.PSERVER, "test2");
        String uriOutput = uri.build().toString();
        assertEquals(true, uriOutput.contains("related-to"));
    }

    @Test
    public void cloneTestSingular() {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");
        AAIResourceUri cloned = uri.clone();
        Map<String, String> keys = cloned.getURIKeys();
        assertThat(keys.values(), contains("test1"));
    }

    @Test
    public void cloneTestPlural() {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.GENERIC_VNF);
        AAIResourceUri cloned = uri.clone();
        Map<String, String> keys = cloned.getURIKeys();
        assertThat(keys.values(), empty());
    }

    @Test
    public void getKeysTest() {
        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, "cloud1", "cloud2", "tenant1", "vserver1");
        Map<String, String> keys = uri.getURIKeys();
        System.out.println(keys);
        System.out.println(uri.build());
        assertEquals("vserver1", keys.get("vserver-id"));
    }

    @Test
    public void getEncodedKeyTest() {
        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, "test1", "my value", "test3");
        Map<String, String> keys = uri.getURIKeys();

        assertEquals("my value", keys.get("service-type"));
    }

    @Test
    public void serializeTest() throws IOException, ClassNotFoundException {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");

        uri.depth(Depth.ONE);
        uri.limit(1);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
        objectOutputStream.writeObject(uri);
        objectOutputStream.flush();
        objectOutputStream.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        ObjectInputStream objectInputStream = new ObjectInputStream(bis);
        AAIResourceUri e2 = (AAIResourceUri) objectInputStream.readObject();
        objectInputStream.close();

        uri.queryParam("test", "value");
        e2.queryParam("test", "value");

        assertEquals(e2.build().toString(), uri.build().toString());
    }
}
