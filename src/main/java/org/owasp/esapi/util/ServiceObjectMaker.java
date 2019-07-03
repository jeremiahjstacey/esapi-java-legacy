package org.owasp.esapi.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.owasp.esapi.errors.ConfigurationException;

public class ServiceObjectMaker implements ObjMaker{

    @Override
    public <T> T make(String className, String typeName) throws ConfigurationException {
        try {
            T instance = null;
            Class<?> theClass = Class.forName(className);
            ServiceLoader<T> loader = (ServiceLoader<T>) ServiceLoader.load(theClass);
            Iterator<T> itr = loader.iterator();
            while (itr.hasNext()) {
                if (instance == null) {
                    instance = itr.next();
                } else {
                    //LOG Multiple instances found.  Ignoring itr.next().getClass().getName();
                }
            }
            
            return instance;
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

}
