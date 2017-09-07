package org.openecomp.mso.client.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRConsumer;

public class DmaapConsumer {

	private final MRConsumer mrConsumer;
	public DmaapConsumer() {
		mrConsumer = null;
	}
	public DmaapConsumer (String filepath) throws FileNotFoundException, IOException {
		
		mrConsumer = MRClientFactory.createConsumer(filepath);
	}
	
	
	public MRConsumer getMRConsumer() {
		return mrConsumer;
	}
	public boolean consume(Consumer consumer) throws Exception {
		boolean accepted = false;
		while (consumer.continuePolling()) {
			for (String message : this.getMRConsumer().fetch()) {
				if (!accepted && consumer.isAccepted(message)) {
					accepted = true;
				} 
				if (accepted) {
					consumer.processMessage(message);
				}
			}
		}
		
		return true;
	}
	
}
