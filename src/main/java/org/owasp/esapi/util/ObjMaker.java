package org.owasp.esapi.util;

import org.owasp.esapi.errors.ConfigurationException;

public interface ObjMaker {
    <T> T make(String className, String typeName) throws ConfigurationException;
}
