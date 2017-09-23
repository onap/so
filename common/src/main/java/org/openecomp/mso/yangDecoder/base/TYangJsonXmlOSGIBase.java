package org.openecomp.mso.yangDecoder.base;

import org.openecomp.mso.yangDecoder.transform.api.ITransformJava2StringService;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;

public interface TYangJsonXmlOSGIBase {

	public abstract void init(BindingNormalizedNodeSerializer mappingService)
			throws Exception;

	public abstract ITransformJava2StringService getJava2StringService(
            String jsonorxml);

	public abstract void close();

}