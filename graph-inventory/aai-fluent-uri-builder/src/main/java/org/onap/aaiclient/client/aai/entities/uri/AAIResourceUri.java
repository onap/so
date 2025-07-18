package org.onap.aaiclient.client.aai.entities.uri;

import org.onap.aaiclient.client.aai.AAIObjectPlurals;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventorySingleResourceUri;

public interface AAIResourceUri extends AAIBaseResourceUri<AAIResourceUri, AAIObjectType>,
        GraphInventorySingleResourceUri<AAIResourceUri, AAIPluralResourceUri, AAIObjectType, AAIObjectPlurals, AAISingleFragment, AAIPluralFragment> {

}
