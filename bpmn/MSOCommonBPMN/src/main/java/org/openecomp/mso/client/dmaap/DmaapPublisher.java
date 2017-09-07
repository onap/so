package org.openecomp.mso.client.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.att.nsa.mr.client.MRBatchingPublisher;
import com.att.nsa.mr.client.MRClientFactory;

public class DmaapPublisher {
	
	private final long seconds;
	private final MRBatchingPublisher publisher;
	
	public DmaapPublisher(String filepath) throws FileNotFoundException, IOException {
		this.seconds = 20;
		this.publisher = MRClientFactory.createBatchingPublisher(filepath);
	}
	
	public DmaapPublisher(String filepath, long seconds) throws FileNotFoundException, IOException {
		this.seconds = seconds;
		this.publisher = MRClientFactory.createBatchingPublisher(filepath);
	}
	
	public void send(String json) throws IOException, InterruptedException {
		publisher.send(json);
		publisher.close(seconds, TimeUnit.SECONDS);
	}

}
