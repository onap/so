package org.openecomp.mso.client.appc;
import org.openecomp.appc.client.lcm.api.ResponseHandler;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;

public class ApplicationControllerCallback<T> implements ResponseHandler<T>  {

	@Override
	public void onResponse(T response) {
		System.out.println("ON RESPONSE");
		
	}

	@Override
	public void onException(AppcClientException exception) {
		System.out.println("ON EXCEPTION");
		
	}

}
