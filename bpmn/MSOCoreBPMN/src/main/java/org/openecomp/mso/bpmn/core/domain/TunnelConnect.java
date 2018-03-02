package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * This class represents the specifics of a tunnel
 * cross connect piece of a resource
 *
 * @author cb645j
 *
 *TODO This may change to house both isp speeds
 */
@JsonRootName("tunnelConnect")
public class TunnelConnect extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String upBandwidth;
	private String downBandwidth;
	private String upBandwidth2;
	private String downBandwidth2;


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUpBandwidth() {
		return upBandwidth;
	}
	public void setUpBandwidth(String upBandwidth) {
		this.upBandwidth = upBandwidth;
	}
	public String getDownBandwidth() {
		return downBandwidth;
	}
	public void setDownBandwidth(String downBandwidth) {
		this.downBandwidth = downBandwidth;
	}
	public String getUpBandwidth2() {
		return upBandwidth2;
	}
	public void setUpBandwidth2(String upBandwidth2) {
		this.upBandwidth2 = upBandwidth2;
	}
	public String getDownBandwidth2() {
		return downBandwidth2;
	}
	public void setDownBandwidth2(String downBandwidth2) {
		this.downBandwidth2 = downBandwidth2;
	}

}
