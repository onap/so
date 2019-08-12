package org.onap.so.apihandlerinfra;

import java.net.URI;

public class HealthCheckSubsystem {

    private Subsystem subsystem;
    private URI uri;
    private HealthCheckStatus status;

    public HealthCheckSubsystem(Subsystem subsystem, URI uri, HealthCheckStatus status) {
        this.subsystem = subsystem;
        this.uri = uri;
        this.status = status;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(Subsystem subsystem) {
        this.subsystem = subsystem;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public HealthCheckStatus getStatus() {
        return status;
    }

    public void setStatus(HealthCheckStatus status) {
        this.status = status;
    }
}
