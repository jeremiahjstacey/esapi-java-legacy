package org.owasp.esapi.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.owasp.esapi.errors.ConfigurationException;

public class ObjMakerChain implements ObjMaker {

    private final List<ObjMaker> delegates;

    public ObjMakerChain(List<ObjMaker> makers) {
        this.delegates = new ArrayList<>(makers);
    }

    @Override
    public <T> T make(String className, String typeName) throws ConfigurationException {
        T result = null;
        Iterator<ObjMaker> makerItr = delegates.iterator();
        while (result == null && makerItr.hasNext()) {
            ObjMaker maker = makerItr.next();
            result = maker.make(className, typeName);
        }
        return result;
    }

}
