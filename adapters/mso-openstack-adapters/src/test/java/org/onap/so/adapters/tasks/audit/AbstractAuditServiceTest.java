package org.onap.so.adapters.tasks.audit;

import org.junit.Test;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractAuditServiceTest extends AbstractAuditService {

    private AAIObjectAuditList getAuditListWithObjectWithExistenceStateOf(boolean existenceState) {
        AAIObjectAudit auditObject = new AAIObjectAudit();
        AAIObjectAuditList auditList = new AAIObjectAuditList();

        auditObject.setDoesObjectExist(existenceState);
        auditList.getAuditList().add(auditObject);

        return auditList;
    }

    @Test
    public void didCreateAuditFail_shouldReturnFalse_whenGivenNull() {
        assertFalse(didCreateAuditFail(null));
    }

    @Test
    public void didCreateAuditFail_shouldReturnTrue_whenGivenNotExistingObject() {
        assertTrue(didCreateAuditFail(getAuditListWithObjectWithExistenceStateOf(false)));
    }

    @Test
    public void didCreateAuditFail_shouldReturnFalse_whenGivenExistingObject() {
        assertFalse(didCreateAuditFail(getAuditListWithObjectWithExistenceStateOf(true)));
    }

    @Test
    public void didDeleteAuditFail_shouldReturnFalse_whenGivenNull() {
        assertFalse(didDeleteAuditFail(null));
    }

    @Test
    public void didDeleteAuditFail_shouldReturnTrue_whenGivenExistingObject() {
        assertTrue(didDeleteAuditFail(getAuditListWithObjectWithExistenceStateOf(true)));
    }

    @Test
    public void didDeleteAuditFail_shouldReturnFalse_whenGivenNotExistingObject() {
        assertFalse(didDeleteAuditFail(getAuditListWithObjectWithExistenceStateOf(false)));
    }

}
