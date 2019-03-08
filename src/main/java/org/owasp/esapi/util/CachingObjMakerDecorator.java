package org.owasp.esapi.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.owasp.esapi.errors.ConfigurationException;

public class CachingObjMakerDecorator implements ObjMaker {

    private final ObjMaker delegate;

    private final Map<String, Object> instMap = new ConcurrentHashMap<>();

    public CachingObjMakerDecorator(ObjMaker maker) {
        this.delegate = maker;
    }

    @Override
    public <T> T make(String className, String typeName) throws ConfigurationException {
        synchronized (className) {
            @SuppressWarnings("unchecked")
            T cached = (T) instMap.get(className);
            if (cached == null) {
                cached = delegate.make(className, typeName);
                instMap.put(className, cached);
            }
            return cached; 
        }

    }

}
