package org.openecomp.mso.yangDecoder.base;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

public final class MockSchemaService implements DOMSchemaService, SchemaContextProvider {

    private SchemaContext schemaContext;

    ListenerRegistry<SchemaContextListener> listeners = ListenerRegistry.create();

    @Override
    public synchronized SchemaContext getGlobalContext() {
        return schemaContext;
    }

    @Override
    public synchronized SchemaContext getSessionContext() {
        return schemaContext;
    }

    @Override
    public ListenerRegistration<SchemaContextListener> registerSchemaContextListener(
            final SchemaContextListener listener) {
        return listeners.register(listener);
    }

    @Override
    public synchronized SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public synchronized void changeSchema(final SchemaContext newContext) {
        schemaContext = newContext;
        for (ListenerRegistration<SchemaContextListener> listener : listeners) {
            listener.getInstance().onGlobalContextUpdated(schemaContext);
        }
    }
}
