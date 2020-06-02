package org.onap.so.adapters.tasks.audit;

import org.junit.Test;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import java.util.Optional;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractAuditServiceTest extends AbstractAuditService {

    private AAIObjectAuditList getAuditListWithObjectWithExistenceStateOf(boolean existState) {
        AAIObjectAudit auditObject = new AAIObjectAudit();
        AAIObjectAuditList auditList = new AAIObjectAuditList();

        auditObject.setDoesObjectExist(existState);
        auditList.getAuditList().add(auditObject);

        return auditList;
    }

    @Test
    public void didCreateAuditFail_shouldReturnFalse_whenGivenEmptyAudit() {
        assertFalse(didCreateAuditFail(Optional.empty()));
    }

    @Test
    public void didCreateAuditFail_shouldReturnTrue_whenGivenNotExistingObject() {
        assertTrue(didCreateAuditFail(Optional.of(getAuditListWithObjectWithExistenceStateOf(false))));
    }

    @Test
    public void didCreateAuditFail_shouldReturnFalse_whenGivenExistingObject() {
        assertFalse(didCreateAuditFail(Optional.of(getAuditListWithObjectWithExistenceStateOf(true))));
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
