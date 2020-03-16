package org.onap.so.client.aai.entities.uri;

import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryPluralResourceUri;

public interface AAIPluralResourceUri extends AAIBaseResourceUri<AAIPluralResourceUri, AAIObjectPlurals>,
        GraphInventoryPluralResourceUri<AAIPluralResourceUri, AAIObjectPlurals> {

}
