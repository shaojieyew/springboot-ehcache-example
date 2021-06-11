package app;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEventLogger
        implements CacheEventListener<Object, Object> {

    private final static Logger log =  LoggerFactory.getLogger(CacheEventLogger.class);

    public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
        log.info("{},{},{}",cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}