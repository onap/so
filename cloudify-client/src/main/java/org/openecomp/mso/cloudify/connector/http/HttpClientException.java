package org.openecomp.mso.cloudify.connector.http;

/*
 * Declare a RuntimeException since the Interface does not declare any
 * throwables.  Any caught exception will be wrapped in HttpClientException
 */
public class HttpClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HttpClientException (String s) {
		super (s);
	}
	
	public HttpClientException (Exception e) {
		super ("Caught nested exception in HttpClient", e);
	}
	
	public HttpClientException (String s, Exception e) {
		super (s, e);
	}
}
