package org.onap.so.adapters.audit;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("auditList", auditList).toString();
    }

    public List<AAIObjectAudit> getAuditList() {
        return auditList;
    }

}
