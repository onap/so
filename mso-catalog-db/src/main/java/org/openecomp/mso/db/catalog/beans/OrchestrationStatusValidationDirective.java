package org.openecomp.mso.db.catalog.beans;

//TODO find this file a new location?
public enum OrchestrationStatusValidationDirective {
	SILENT_SUCCESS("SilentSuccess"), CONTINUE("Continue"), FAIL("Fail");

	private final String name;

	private OrchestrationStatusValidationDirective(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
