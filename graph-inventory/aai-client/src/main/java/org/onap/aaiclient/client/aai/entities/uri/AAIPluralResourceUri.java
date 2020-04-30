package org.onap.aaiclient.client.aai.entities.uri;

import org.onap.aaiclient.client.aai.AAIObjectPlurals;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventoryPluralResourceUri;

public interface AAIPluralResourceUri extends AAIBaseResourceUri<AAIPluralResourceUri, AAIObjectPlurals>,
        GraphInventoryPluralResourceUri<AAIPluralResourceUri, AAIObjectPlurals> {

}
