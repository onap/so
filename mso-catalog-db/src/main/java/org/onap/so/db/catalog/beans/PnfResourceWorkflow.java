package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@Table(name = "pnf_resource_to_workflow")
public class PnfResourceWorkflow implements Serializable {

    private static final long serialVersionUID = 4897166645148426088L;

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @BusinessKey
    @Column(name = "PNF_RESOURCE_MODEL_UUID")
    private String pnfResourceModelUUID;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PNF_RESOURCE_MODEL_UUID", updatable = false, insertable = false)
    private PnfResource pnfResource;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_ID")
    private Workflow workflow;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pnfResourceModelUUID", pnfResourceModelUUID).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PnfResourceWorkflow)) {
            return false;
        }
        PnfResourceWorkflow castOther = (PnfResourceWorkflow) other;
        return new EqualsBuilder().append(pnfResourceModelUUID, castOther.pnfResourceModelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pnfResourceModelUUID).toHashCode();
    }

    public Integer getID() {
        return ID;
    }

    public String getPnfResourceModelUUID() {
        return pnfResourceModelUUID;
    }

    public void setPnfResourceModelUUID(String pnfResourceModelUUID) {
        this.pnfResourceModelUUID = pnfResourceModelUUID;
    }

    @LinkedResource
    public PnfResource getPnfResource() {
        return pnfResource;
    }

    public void setPnfResource(PnfResource pnfResource) {
        this.pnfResource = pnfResource;
    }

    @LinkedResource
    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }
}
