/**
 * OWASP Enterprise Security API (ESAPI)
 *
 * This file is part of the Open Web Application Security Project (OWASP)
 * Enterprise Security API (ESAPI) project. For details, please see
 * <a href="http://www.owasp.org/index.php/ESAPI">http://www.owasp.org/index.php/ESAPI</a>.
 *
 * Copyright (c) 2007 - The OWASP Foundation
 *
 * The ESAPI is published by OWASP under the BSD license. You should read and accept the
 * LICENSE before you use, modify, and/or redistribute this software.
 *
 * @author Jeff Williams <a href="http://www.aspectsecurity.com">Aspect Security</a>
 * @created 2007
 */
package org.owasp.esapi.reference;

import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerFactory;
import org.owasp.esapi.LogFactory;
import org.owasp.esapi.util.ObjFactory;

/**
 * Reference implementation of the LogFactory interface. This implementation uses the Apache Log4J package, and marks each
 * log message with the currently logged in user and the word "SECURITY" for security related events. See the 
 * <a href="JavaLogFactory.JavaLogger.html">JavaLogFactory.JavaLogger</a> Javadocs for the details on the JavaLogger reference implementation.
 * 
 * At class initialization time, the file log4j.properties or log4j.xml file will be loaded from the classpath. This configuration file is 
 * fundamental to make log4j work for you. Please see http://logging.apache.org/log4j/1.2/manual.html for more information.
 *
 * Note that <b>you must specify the LogFactory</b> in either log4j.properties:
 *
 * <code>
 *     log4j.loggerFactory=org.owasp.esapi.reference.Log4JLoggerFactory
 * </code>
 *
 * or log4j.xml:
 * 
 * <code>
 *     &lt;loggerFactory class="org.owasp.esapi.reference.Log4JLoggerFactory"/&gt;

 * </code>
 * 
 * @author Mike H. Fauzy (mike.fauzy@aspectsecurity.com) <a href="http://www.aspectsecurity.com">Aspect Security</a>
 * @author Jim Manico (jim@manico.net) <a href="http://www.manico.net">Manico.net</a>
 * @author Jeff Williams (jeff.williams .at. aspectsecurity.com) <a href="http://www.aspectsecurity.com">Aspect Security</a>
 * @author August Detlefsen (augustd at codemagi dot com) <a href="http://www.codemagi.com">CodeMagi, Inc.</a>
 * @since June 1, 2007
 * @see org.owasp.esapi.LogFactory
 * @see org.owasp.esapi.reference.Log4JLogger
 * @see org.owasp.esapi.reference.Log4JLoggerFactory
 */
public class Log4JLogFactory implements LogFactory {

	//The Log4j logger factory to use
	LoggerFactory factory = new Log4JLoggerFactory();

	/**
     * Acquires the singleton reference to this type.
     * @return instance.
     * @deprecated Use {@link ObjFactory#make(Log4JLogFactory.class.getName(), String)} instead
     */
    @Deprecated
    public static LogFactory getInstance() {
        return ObjFactory.make(Log4JLogFactory.class.getName(), "Log4JLogFactory Singleton Reference");
    }
	
	protected Log4JLogFactory() {}
	
	/**
	* {@inheritDoc}
	*/
	public org.owasp.esapi.Logger getLogger(Class clazz) {
		return (org.owasp.esapi.Logger)LogManager.getLogger(clazz.getName(), factory);
    }

    /**
	* {@inheritDoc}
	*/
	public org.owasp.esapi.Logger getLogger(String moduleName) {
		return (org.owasp.esapi.Logger)LogManager.getLogger(moduleName, factory);
    }

}