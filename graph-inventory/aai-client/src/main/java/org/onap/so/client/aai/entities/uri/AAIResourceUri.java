package org.onap.so.client.aai.entities.uri;

import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.graphinventory.entities.uri.GraphInventorySingleResourceUri;

public interface AAIResourceUri extends AAIBaseResourceUri<AAIResourceUri, AAIObjectType>,
        GraphInventorySingleResourceUri<AAIResourceUri, AAIPluralResourceUri, AAIObjectType, AAIObjectPlurals> {

}
