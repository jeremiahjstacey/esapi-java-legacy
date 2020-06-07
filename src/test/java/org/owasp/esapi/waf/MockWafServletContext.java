package org.owasp.esapi.waf;

import java.io.File;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.http.MockServletContext;

public class MockWafServletContext extends MockServletContext {

	public String getRealPath(String s) {
	    String resourceLookup = s.endsWith("log4j.xml") ? "log4j.xml" : s;
        File resource = ESAPI.securityConfiguration().getResourceFile(resourceLookup);
        return resource.exists() && resource.canRead() ? resource.getAbsolutePath() : null;
	}
	
}
