package org.onap.aaiclient.client.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "aai")
public class AAIConfigurationProperties implements AAIProperties {

    /**
     * Default URI used when the configured URI is {@code null}.
     */
    public static final URL DEFAULT_URI;

    static {
        try {
            DEFAULT_URI = new URL("http://localhost:8080");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private URL endpoint;
    private String systemName;
    private AAIVersion defaultVersion;
    private String auth;
    private String key;

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return (this.endpoint != null) ? this.endpoint : DEFAULT_URI;
    }

    @Override
    public String getSystemName() {
        return this.systemName;
    }

    @Override
    public AAIVersion getDefaultVersion() {
        return this.defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = AAIVersion.valueOf(defaultVersion);
    }

    @Override
    public String getAuth() {
        return this.auth;
    }

    @Override
    public String getKey() {
        return this.key;
    }

}
