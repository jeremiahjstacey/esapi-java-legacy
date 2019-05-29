/*
 * OWASP Enterprise Security API (ESAPI)
 * 
 * This file is part of the Open Web Application Security Project (OWASP)
 * Enterprise Security API (ESAPI) project. For details, please see
 * <a href="http://www.owasp.org/index.php/ESAPI">http://www.owasp.org/index.php/ESAPI</a>.
 *
 * Copyright (c) 2009 - The OWASP Foundation
 */
package org.owasp.esapi.util;

import org.owasp.esapi.errors.ConfigurationException;

/**
 * A generic object factory to create an object of class T. T must be a concrete
 * class that has a no-argument public constructor or a implementor of the Singleton pattern
 * that has a no-arg static getInstance method. If the class being created has a getInstance
 * method, it will be used as a singleton and newInstance() will never be called on the
 * class no matter how many times it comes through this factory.
 *
 * <p>
 * Typical use is something like:
 * <pre>
 * 		import com.example.interfaces.DrinkingEstablishment;
 * 		import com.example.interfaces.Beer;
 * 		...
 * 		// Typically these would be populated from some Java properties file
 * 		String barName = "com.example.foo.Bar";
 * 		String beerBrand = "com.example.brewery.Guiness";
 * 		...
 * 		DrinkingEstablishment bar = ObjFactory.make(barName, "DrinkingEstablishment");
 * 		Beer beer = ObjFactory.make(beerBrand, "Beer");
 *		bar.drink(beer);	// Drink a Guiness beer at the foo Bar. :)
 *		...
 * </pre>
 * </p><p>
 *  Copyright (c) 2009 - The OWASP Foundation
 *  </p>
 * @author kevin.w.wall@gmail.com
 * @author Chris Schmidt ( chrisisbeef .at. gmail.com )
 */
public class ObjFactory {
    static enum ObjFactoryMode {
        CACHING (new CachingObjMakerDecorator(new ReflectionObjectMaker())),
        ALWAYS_NEW ( new ReflectionObjectMaker());

        private ObjMaker maker;

        private ObjFactoryMode(ObjMaker makerRef) {
            this.maker = makerRef;
        }

        public  ObjMaker getMaker() {
            return maker;
        }

        public static ObjMaker getSystemObjMaker() {
            String sysProp = System.getProperty("ObjFactory.MAKER", ALWAYS_NEW.name());
            //Logger
            ObjFactoryMode mode = ObjFactoryMode.valueOf(sysProp.toUpperCase()); 
            return mode.getMaker();
        }

    }

    private static final ObjMaker maker = ObjFactoryMode.getSystemObjMaker();

    /**
     * Create an object based on the <code>className</code> parameter.
     * 
     * @param className	The name of the class to construct. Should be a fully qualified name and
     * 					generally the same as type <code>T</code>
     * @param typeName	A type name used in error messages / exceptions.
     * @return	An object of type <code>className</code>, which is cast to type <code>T</code>.
     * @throws	ConfigurationException thrown if class name not found in class path, or does not
     * 			have a public, no-argument constructor, or is not a concrete class, or if it is
     * 			not a sub-type of <code>T</code> (or <code>T</code> itself). Usually this is
     * 			caused by a misconfiguration of the class names specified in the ESAPI.properties
     * 			file. Also thrown if the CTOR of the specified <code>className</code> throws
     * 			an <code>Exception</code> of some type.
     */
    public static <T> T make(String className, String typeName) throws ConfigurationException {
        return maker.make(className, typeName);
    }
}
