package org.openecomp.mso.client.sdnc.mapper;

import java.util.Optional;

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.sdnc.beans.SDNCRequest;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;

public abstract class SDNCRequestMapper {
	
	protected final Optional<String> msoAction;
	protected final SDNCSvcOperation svcOperation;
	protected final SDNCSvcAction svcAction;
	protected final String requestAction;
	
	public SDNCRequestMapper (Optional<String> msoAction, SDNCSvcOperation svcOperation,
			SDNCSvcAction svcAction, String requestAction) {
		this.msoAction = msoAction;
		this.svcOperation = svcOperation;
		this.svcAction = svcAction;
		this.requestAction = requestAction;
	}
	
	public abstract SDNCRequest reqMapper (ServiceDecomposition serviceDecomp);
}
