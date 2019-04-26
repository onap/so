package org.onap.svnfm.simulator.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    Collection<LccnSubscriptionRequest> subscriptions = new ArrayList<>();

    public void registerSubscription(final LccnSubscriptionRequest subscription) {
        subscriptions.add(subscription);
    }

    public Collection<LccnSubscriptionRequest> getSubscriptions() {
        return Collections.unmodifiableCollection(subscriptions);
    }
}
