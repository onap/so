package org.openecomp.mso.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openecomp.mso.bpmn.servicedecomposition.ShallowCopy;

public class RouteTableReference implements Serializable, ShallowCopy<RouteTableReference> {

	private static final long serialVersionUID = -698474994443040491L;
	
	@Id
	@JsonProperty("route-table-reference-id")
	private String routeTableReferenceId;
	@JsonProperty("route-table-reference-fqdn")
	private String routeTableReferenceFqdn;
	@JsonProperty("resource-version")
	private String resourceVersion;
	
	public String getRouteTableReferenceId() {
		return routeTableReferenceId;
	}
	public void setRouteTableReferenceId(String routeTableReferenceId) {
		this.routeTableReferenceId = routeTableReferenceId;
	}
	public String getRouteTableReferenceFqdn() {
		return routeTableReferenceFqdn;
	}
	public void setRouteTableReferenceFqdn(String routeTableReferenceFqdn) {
		this.routeTableReferenceFqdn = routeTableReferenceFqdn;
	}
	public String getResourceVersion() {
		return resourceVersion;
	}
	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof RouteTableReference)) {
			return false;
		}
		RouteTableReference castOther = (RouteTableReference) other;
		return new EqualsBuilder().append(routeTableReferenceId, castOther.routeTableReferenceId).isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(routeTableReferenceId).toHashCode();
	}
}
