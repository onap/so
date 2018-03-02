package org.openecomp.mso.client.aai;

import com.google.common.base.CaseFormat;

public enum AAIObjectPlurals implements AAIObjectName, AAIObjectUriTemplate, AAIObjectUriPartial {

	GENERIC_VNF(AAINamespaceConstants.NETWORK, "/generic-vnfs"),
	PSERVER(AAINamespaceConstants.CLOUD_INFRASTRUCTURE, "/pservers");
	
	private final String uriTemplate;
	private final String partialUri;
	private AAIObjectPlurals(String parentUri, String partialUri) {
		this.uriTemplate = parentUri + partialUri;
		this.partialUri = partialUri;
	}
	
	@Override
	public String toString() {
		return this.uriTemplate();
	}

	@Override
	public String uriTemplate() {
		return this.uriTemplate;
	}

	@Override
	public String partialUri() {
		return this.partialUri;
	}

	@Override
	public String typeName() {
		return this.typeName(CaseFormat.LOWER_HYPHEN);
	}
	@Override
	public String typeName(CaseFormat format) {
		String enumName = this.name();
		if (this.equals(AAIObjectType.DEFAULT_CLOUD_REGION) || this.equals(AAIObjectType.DEFAULT_TENANT)) {
			enumName = enumName.replace("DEFAULT_", "");
		}
		
		return CaseFormat.UPPER_UNDERSCORE.to(format, enumName);
	}
}
