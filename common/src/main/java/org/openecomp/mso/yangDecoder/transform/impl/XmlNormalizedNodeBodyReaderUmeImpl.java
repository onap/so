package org.openecomp.mso.yangDecoder.transform.impl;

import org.opendaylight.netconf.sal.rest.impl.XmlNormalizedNodeBodyReader;
import org.opendaylight.netconf.sal.restconf.impl.InstanceIdentifierContext;

public class XmlNormalizedNodeBodyReaderUmeImpl extends
		XmlNormalizedNodeBodyReader {
 	InstanceIdentifierContext<?> iic; 
 	boolean  ispost=false;
 	@Override
	 protected InstanceIdentifierContext<?> getInstanceIdentifierContext() {
	        return iic;
	    }
 	@Override
	 protected boolean isPost() {
	        return ispost;
	    } 
	public void Set(InstanceIdentifierContext<?> iic,boolean ispost)
	{
		this.iic=iic;
		this.ispost=ispost;
	}
	 
}
