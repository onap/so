package org.openecomp.mso.security;

import org.springframework.security.web.firewall.StrictHttpFirewall;

public class MSOSpringFirewall extends StrictHttpFirewall {

	
	public MSOSpringFirewall() {
		super();
		this.setAllowUrlEncodedSlash(true);
		this.setAllowSemicolon(true);
		this.setAllowUrlEncodedPercent(true);
	}
}
