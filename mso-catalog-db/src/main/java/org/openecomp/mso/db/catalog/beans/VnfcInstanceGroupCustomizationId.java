package org.openecomp.mso.db.catalog.beans;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

public class VnfcInstanceGroupCustomizationId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1628277798979456195L;

	@BusinessKey
	private String modelCustomizationUUID;
	@BusinessKey
	private String modelUUID;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
				.append("modelUUID", modelUUID).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof VnfcInstanceGroupCustomizationId)) {
			return false;
		}
		VnfcInstanceGroupCustomizationId castOther = (VnfcInstanceGroupCustomizationId) other;
		return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID)
				.append(modelUUID, castOther.modelUUID).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(modelCustomizationUUID).append(modelUUID).toHashCode();
	}

	public String getModelCustomizationUUID() {
		return this.modelCustomizationUUID;
	}

	public void setModelCustomizationUUID(String modelCustomizationUUID) {
		this.modelCustomizationUUID = modelCustomizationUUID;
	}

	public String getModelUUID() {
		return this.modelUUID;
	}

	public void setModelUUID(String modelUUID) {
		this.modelUUID = modelUUID;
	}
}
