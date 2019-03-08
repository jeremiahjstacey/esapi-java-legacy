package org.owasp.esapi.util;

import org.owasp.esapi.errors.ConfigurationException;

public class ReflectionObjectMaker implements ObjMaker{

    @Override
    public <T> T make(String className, String typeName) throws ConfigurationException {
        try {
            Class<?> theClass = Class.forName(className);
            return (T) theClass.newInstance();
        } catch (SecurityException e) {
            throw new ConfigurationException( "The SecurityManager has restricted the object factory from getting a reference to the implementation" +
                    "of the class [" + className + "]", e );
        }catch ( ClassNotFoundException ex ) {
            String errMsg = null;
            errMsg = ex.toString() + " " + typeName + " class (" + className + ") must be in class path.";
            throw new ConfigurationException(errMsg, ex);
        } catch( InstantiationException ex ) {
            String errMsg = null;
            errMsg = ex.toString() + " " + typeName + " class (" + className + ") must be concrete.";
            throw new ConfigurationException(errMsg, ex);
        } catch( IllegalAccessException ex ) {
            String errMsg = null;
            errMsg = ex.toString() + " " + typeName + " class (" + className + ") must have a public, no-arg constructor.";
            throw new ConfigurationException(errMsg, ex);
        } 
    }

}
