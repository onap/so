package org.onap.so.client;


import java.util.concurrent.TimeUnit;
import javax.cache.configuration.Factory;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.expiry.TouchedExpiryPolicy;

public class CacheFactory implements Factory<ExpiryPolicy> {

    private static final long serialVersionUID = 8948728679233836929L;

    private final CacheProperties props;

    public CacheFactory(CacheProperties props) {
        this.props = props;
    }

    @Override
    public ExpiryPolicy create() {
        return TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.MILLISECONDS, props.getMaxAge())).create();
    }

}
