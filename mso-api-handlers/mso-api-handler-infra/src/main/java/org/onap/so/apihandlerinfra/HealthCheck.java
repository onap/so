package org.onap.so.apihandlerinfra;

import java.net.URI;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mso.health.enpoints")
public class HealthCheck {

    private Subsystem subsystem;
    private URI uri;
    private HealthCheckStatus status = HealthCheckStatus.DOWN;

    public HealthCheck() {

    }

    public HealthCheck(String subsystem, String uri) {
        this.subsystem = SoSubsystems.valueOf(subsystem.toUpperCase());
        this.uri = UriBuilder.fromUri(uri).build();
    }

    public HealthCheck(Subsystem subsystem, URI uri) {
        this.subsystem = subsystem;
        this.uri = uri;
    }

    public HealthCheck(Subsystem subsystem, URI uri, HealthCheckStatus status) {
        this.subsystem = subsystem;
        this.uri = uri;
        this.status = status;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(Subsystem component) {
        this.subsystem = component;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("subsystem", subsystem).append("uri", uri).append("status", status)
                .toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HealthCheck)) {
            return false;
        }
        HealthCheck castOther = (HealthCheck) other;
        return new EqualsBuilder().append(subsystem, castOther.subsystem).append(uri, castOther.uri)
                .append(status, castOther.status).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(subsystem).append(uri).append(status).toHashCode();
    }

}
