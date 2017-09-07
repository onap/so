package org.openecomp.mso.client.dmaap;

public interface Consumer {

	/**
	 * Should this consumer continue to consume messages from the topic?
	 * @return
	 */
	public boolean continuePolling();
	/**
	 * Process a message from a DMaaP topic
	 * 
	 * @param message
	 * @throws Exception
	 */
	public void processMessage(String message) throws Exception;
	/**
	 * Has the request been accepted by the receiving system?
	 * Should the consumer move to processing messages?
	 * 
	 * @param message
	 * @return
	 */
	public boolean isAccepted(String message);
	/**
	 * The request id to filter messages on
	 * @return
	 */
	public String getRequestId();
	/**
	 * Logic that defines when the consumer should stop processing messages
	 */
	public void stopProcessingMessages();
}
