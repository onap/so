package org.openecomp.mso.cloudify.base.client;

/**
 * A common abstract parent of all Openstack Exception types, allowing
 * calling classes the choice to catch all error exceptions together.
 */
public abstract class CloudifyBaseException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/*
	 * Implement only the basic constructors
	 */
	public CloudifyBaseException () {}
	
	public CloudifyBaseException(String message) {
		super(message);
	}

	public CloudifyBaseException(String message, Throwable cause) {
		super(message, cause);
	}
}
