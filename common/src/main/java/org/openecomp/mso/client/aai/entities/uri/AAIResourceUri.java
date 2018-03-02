package org.openecomp.mso.client.aai.entities.uri;

import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIObjectType;

public interface AAIResourceUri extends AAIUri {

	public AAIResourceUri relationshipAPI();
	public AAIResourceUri relatedTo(AAIObjectPlurals plural);
	public AAIResourceUri relatedTo(AAIObjectType type, String... values);
	public AAIResourceUri resourceVersion(String version);
	public AAIResourceUri depth(Depth depth);
	public AAIResourceUri nodesOnly(boolean nodesOnly);
	public AAIResourceUri queryParam(String name, String... values);
	public AAIResourceUri clone();
}
