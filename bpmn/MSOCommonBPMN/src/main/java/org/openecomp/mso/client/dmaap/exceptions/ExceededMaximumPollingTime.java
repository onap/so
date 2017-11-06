package org.openecomp.mso.client.dmaap.exceptions;

public class ExceededMaximumPollingTime extends RuntimeException  {

	private static final long serialVersionUID = 2331207691092906423L;

	public ExceededMaximumPollingTime() {
		super();
	}
	
	public ExceededMaximumPollingTime(String message) {
		super(message);
	}
}
