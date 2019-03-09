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
        } catch (Exception ex) {
            String errMsg = null;
            // Because we are using reflection, we want to catch any checked or unchecked Exceptions and
            // re-throw them in a way we can handle them. Because using reflection to construct the object,
            // we can't have the compiler notify us of uncaught exceptions. For example, JavaEncryptor()
            // CTOR can throw [well, now it can] an EncryptionException if something goes wrong. That case
            // is taken care of here.
            //
            // CHECKME: Should we first catch RuntimeExceptions so we just let unchecked Exceptions go through
            //          unaltered???
            //
            errMsg = ex.toString() + " " + typeName + " class (" + className + ") CTOR threw exception.";
            throw new ConfigurationException(errMsg, ex);
        }
    }

}
