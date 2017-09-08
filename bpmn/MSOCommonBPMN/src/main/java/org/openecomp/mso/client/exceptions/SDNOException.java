package org.openecomp.mso.client.exceptions;


public class SDNOException extends Exception {

	private static final long serialVersionUID = 6189163383568887383L;

	public SDNOException() {
		super();
	}
	
	public SDNOException(String string) {
		super(string);
	}

	public SDNOException(Exception e) {
		super(e);
	}
}
