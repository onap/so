package org.openecomp.mso.bpmn.common.scripts

import org.apache.commons.lang3.StringUtils

class GenericUtils extends StringUtils{

	@Override
	public static boolean isBlank(final CharSequence cs) {
		int strLen
		if (cs == null || (strLen = cs.length()) == 0 || cs.equals("null"))  {
			return true
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false
			}
		}
		return true
	}
}
