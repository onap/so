package org.openecomp.mso.adapters.vdu;

public class VduArtifact {
	
	// Enumerate the types of artifacts permitted.  This may need to be a variable string
	// value if arbitrary (cloud-specific) artifacts may be attached to VDUs in ASDC.
	public enum ArtifactType {
		MAIN_TEMPLATE, NESTED_TEMPLATE, CONFIG_FILE, SCRIPT_FILE, TEXT_FILE, ENVIRONMENT
	}
	
	private String name;
	private byte[] content;
	private ArtifactType type;
	
	// Default constructor
	public VduArtifact() {}
	
	// Fully specified constructor
	public VduArtifact (String name, byte[] content, ArtifactType type) {
		this.name = name;
		this.content = content;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public void setName (String name) {
		this.name = name;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public ArtifactType getType() {
		return type;
	}
	public void setType(ArtifactType type) {
		this.type = type;
	}	
	
}