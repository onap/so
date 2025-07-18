package org.onap.aaiclient.client;

import java.io.Serializable;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import org.apache.cxf.jaxrs.client.cache.Entry;
import org.apache.cxf.jaxrs.client.cache.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheLogger implements CacheEntryExpiredListener<Key, Entry>, CacheEntryCreatedListener<Key, Entry>,
        CacheEntryUpdatedListener<Key, Entry>, CacheEntryRemovedListener<Key, Entry>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CacheLogger.class);

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends Key, ? extends Entry>> events)
            throws CacheEntryListenerException {
        for (CacheEntryEvent<? extends Key, ? extends Entry> event : events) {
            logger.debug("{} expired key: {}", event.getSource().getName(), event.getKey().getUri());
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends Key, ? extends Entry>> events)
            throws CacheEntryListenerException {

        for (CacheEntryEvent<? extends Key, ? extends Entry> event : events) {
            logger.debug("{} removed key: {}", event.getSource().getName(), event.getKey().getUri());
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends Key, ? extends Entry>> events)
            throws CacheEntryListenerException {
        for (CacheEntryEvent<? extends Key, ? extends Entry> event : events) {
            logger.debug("{} updated key: {}", event.getSource().getName(), event.getKey().getUri());
        }
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends Key, ? extends Entry>> events)
            throws CacheEntryListenerException {
        for (CacheEntryEvent<? extends Key, ? extends Entry> event : events) {
            logger.debug("{} created key: {}", event.getSource().getName(), event.getKey().getUri());
        }
    }

}
