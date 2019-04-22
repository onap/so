package org.onap.so.objects.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class AAIObjectAuditList implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6712662349909726930L;
    private List<AAIObjectAudit> auditList = new ArrayList<>();
    private String auditType;
    private String heatStackName;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("auditList", auditList).toString();
    }

    public List<AAIObjectAudit> getAuditList() {
        return auditList;
    }


    public String getAuditType() {
        return auditType;
    }


    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    public String getHeatStackName() {
        return heatStackName;
    }

    public void setHeatStackName(String heatStackName) {
        this.heatStackName = heatStackName;
    }

}
