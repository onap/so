package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import java.io.IOException;
import java.util.Optional;
import org.onap.aai.domain.yang.Pnf;
import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.AaiConnection;

public class AaiConnectionThrowingException implements AaiConnection {

    @Override
    public Optional<Pnf> getEntryFor(String correlationId) throws IOException {
        throw new IOException();
    }

    @Override
    public void createEntry(String correlationId, Pnf entry) throws IOException {
        throw new IOException();
    }
}
