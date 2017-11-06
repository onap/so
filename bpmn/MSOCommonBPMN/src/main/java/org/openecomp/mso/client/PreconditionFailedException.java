package org.openecomp.mso.client;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class PreconditionFailedException extends WebApplicationException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PreconditionFailedException(String message) {
        super(message, Response.Status.PRECONDITION_FAILED);
    }
}
