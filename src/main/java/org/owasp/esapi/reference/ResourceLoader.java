package org.owasp.esapi.reference;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

enum ResourceLoader implements ResourceProvider {
    ROOT(""),
    CUSTOM_DIRECTORY("org.owasp.esapi.resources"){
        @Override
        public String getPath() {
            String propertyName = super.getPath();
            return System.getProperty(propertyName);
        }
        @Override
        public File getFile(String filename) {
            return getFileFromPath(filename);
        }
    },
    ESAPI_DIRECTORY ("esapi"),
    DOT_ESAPI_DIRECTORY (".esapi"),
    RESOURCES("resources"),
    SRC_MAIN_RESOURCES("src/main/resources"),
    USER_HOME_DIR ("user.home") {
        @Override
        public String getPath() {
            String propertyName = super.getPath();
            return System.getProperty(propertyName);
        }
        @Override
        public File getFile(String filename) {
            File rval = getFileFromPath(".esapi" + "/" + filename);
            if (rval == null) {
                rval = getFileFromPath("esapi" + "/" + filename);
            }
            return rval;
        }
    };
    
    protected final String pathRef;
    
    private ResourceLoader(String pathContext) {
        this.pathRef = pathContext;
    }
    public String getPath() {
        return pathRef;
    }
    
    public InputStream getResourceAsStream(ClassLoader loader, String filename) {
        String fullPath = fullPathTo(filename);         
        return fullPath == null ? null : loader.getResourceAsStream(fullPath);
    }
    
    public File getFile( String filename)  {
        String fixedPath = fullPathTo(filename);
        return fixedPath == null ? null : getSystemResourceFile(fixedPath);
    } 
    
    protected final File getFileFromPath(String filename) {
        File rval = null;
        String basePath = getPath();
        if (basePath != null) {
            File file = new File (basePath, filename);
            rval = file.exists() && file.canRead() ? file : null;
        }
        return rval;
    }
    
    protected final File getSystemResourceFile(String fullPath) {
        File rval = null;
        URL fileUrl = ClassLoader.getSystemResource(fullPath);
        if (fileUrl != null) {
            File file = new File(fileUrl.getFile());
            if (file.exists()) {
                rval = file;                    
            }
        }
        return rval;
    }
    
    protected final String fullPathTo(String filename) {
        String basePath = getPath();
        String fullPath = basePath == null || filename == null ? null : basePath;
        if (fullPath != null) {
            fullPath += fullPath.endsWith("/") || fullPath.isEmpty() ? filename : "/" + filename; 
        }
        return fullPath;
    }
    
}