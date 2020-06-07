package org.owasp.esapi.reference;

import java.io.File;
import java.io.InputStream;

interface ResourceProvider {
    
    File getFile(String filename);
    
    InputStream getResourceAsStream(ClassLoader loader, String filename);

}
