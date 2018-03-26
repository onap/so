package org.openecomp.mso.db.catalog.test;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.utils.RecordNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


public class RecordNotFoundExceptionTest {

    @Test
    public void paramConstructor(){
        RecordNotFoundException ex = new RecordNotFoundException("Exceoption raised", new Exception());
        assertNotNull(ex);
        assertNotNull(ex.getMessage());
    }
}
