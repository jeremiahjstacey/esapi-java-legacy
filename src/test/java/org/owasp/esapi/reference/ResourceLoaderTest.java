package org.owasp.esapi.reference;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ClassLoader.class, ResourceLoader.class})
public class ResourceLoaderTest {
    private static final String ESAPI_RESOURCES_PROP = "org.owasp.esapi.resources";
    private static final String USER_HOME_PROP = "user.home";
            
    @Rule
    public TemporaryFolder testScopeConfig= new TemporaryFolder();
    @Rule
    public TestName testName = new TestName();

    private File resourceFile;
    private String resourceName;

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(ClassLoader.class);
        resourceName = testName.getMethodName() + ".txt";
        resourceFile = testScopeConfig.newFile(resourceName);
        resourceFile.createNewFile();
    }

    @Test
    public void testCustomDirectorySystemProperty() throws Exception {
        verifyAsSystemProperty(ResourceLoader.CUSTOM_DIRECTORY, ESAPI_RESOURCES_PROP);
    }

    @Test
    public void testUserHomeESAPISystemProperty() throws Exception {
        //Verify that file can be found under {{ user.home }}/esapi
        File esapi = testScopeConfig.newFolder("esapi");
        resourceFile = new File(esapi, resourceName);
        resourceFile.createNewFile();
        verifyAsSystemProperty(ResourceLoader.USER_HOME_DIR, USER_HOME_PROP);
    }

    @Test
    public void testUserHomeDOTESAPISystemProperty() throws Exception {
      //Verify that file can be found under {{ user.home }}/.esapi
        File dotesapi = testScopeConfig.newFolder(".esapi");
        resourceFile = new File(dotesapi, resourceName);
        resourceFile.createNewFile();
        verifyAsSystemProperty(ResourceLoader.USER_HOME_DIR, "user.home");
    }

    @Test
    public void testRootClasspathLookup() throws Exception {
        verifyStaticPathResource(ResourceLoader.ROOT);
    }

    @Test
    public void testEsapiClasspathLookup() throws Exception {
        verifyStaticPathResource(ResourceLoader.ESAPI_DIRECTORY);
    }

    @Test
    public void testDotEsapiClasspathLookup() throws Exception {
        verifyStaticPathResource(ResourceLoader.DOT_ESAPI_DIRECTORY);
    }

    @Test
    public void testResourcesClasspathLookup() throws Exception {
        verifyStaticPathResource(ResourceLoader.RESOURCES);
    }

    @Test
    public void testSrcResourcesClasspathLookup() throws Exception {
        verifyStaticPathResource(ResourceLoader.SRC_MAIN_RESOURCES);
    }

    public void verifyStaticPathResource(ResourceLoader resourceLoader) throws Exception {
        URL url = resourceFile.toURI().toURL();
        
        ArgumentCaptor<String> filePathCapture = ArgumentCaptor.forClass(String.class);
        PowerMockito.when(ClassLoader.getSystemResource(filePathCapture.capture())).thenReturn(url);
        
        File file = resourceLoader.getFile(resourceName);
        Assert.assertNotNull(file);
        Assert.assertEquals(resourceFile, file);
        
        String requestPath = filePathCapture.getValue();

        Assert.assertTrue(resourceLoader.name() + " Classloader Request should start with the correct directory path",
                requestPath.startsWith(resourceLoader.getPath()));
        Assert.assertTrue(resourceLoader.name() + " Classloader Request should end with the requested resource file",
                requestPath.endsWith(resourceName));
    }

    private File verifyAsSystemProperty(ResourceLoader resourceLoader, String propertyName) {
        String currentProperty = System.getProperty(propertyName);
        try {
            System.setProperty(propertyName,  testScopeConfig.getRoot().getAbsolutePath());

            File file = resourceLoader.getFile(resourceName);

            Assert.assertNotNull(file);
            Assert.assertEquals("Should resolve the testResource file reference from the configured property", resourceFile, file);

            return file;
        } finally {
            if (currentProperty == null) {
                System.clearProperty(propertyName);
            } else {
                System.setProperty(propertyName, currentProperty);
            }
        }
    }
}
