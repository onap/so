package org.openecomp.mso.client.aai;

import com.google.common.base.CaseFormat;

public interface AAIObjectName {

	public String typeName();
	public String typeName(CaseFormat format);
}
