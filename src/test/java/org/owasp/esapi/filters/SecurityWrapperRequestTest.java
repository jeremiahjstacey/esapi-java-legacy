/**
 * OWASP Enterprise Security API (ESAPI)
 *
 * This file is part of the Open Web Application Security Project (OWASP)
 * Enterprise Security API (ESAPI) project. For details, please see
 * <a href="http://www.owasp.org/index.php/ESAPI">http://www.owasp.org/index.php/ESAPI</a>.
 *
 * Copyright (c) 2008-2018 - The OWASP Foundation
 *
 * The ESAPI is published by OWASP under the BSD license. You should read and accept the
 * LICENSE before you use, modify, and/or redistribute this software.
 *
 */

package org.owasp.esapi.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.SecurityConfiguration;
import org.owasp.esapi.Validator;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for {@link SecurityWrapperRequest}.
 * <br/>
 * This test uses static context mocking! This can affect certain behaviors if it is executed in a JVM container with
 * other tests depending on the same static reference - Which is going to be everything.
 * <br/>
 * This may affect some test environments, mostly IDE's. It is not expected that this impacts the Maven build, as
 * surefire plugin isolates JVM's for tests during that phase.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ESAPI.class)
public class SecurityWrapperRequestTest {
    private static final String ESAPI_VALIDATOR_GETTER_METHOD_NAME = "validator";
    private static final String ESAPY_SECURITY_CONFIGURATION_GETTER_METHOD_NAME = "securityConfiguration";
    private static final String SECURITY_CONFIGURATION_QUERY_STRING_LENGTH_KEY_NAME = "HttpUtilities.URILENGTH";
    private static final String SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME = "HttpUtilities.httpQueryParamValueLength";

    private static final int SECURITY_CONFIGURATION_TEST_LENGTH = 255;

    private static final String QUERY_STRING_CANONCALIZE_TYPE_KEY = "HTTPQueryString";
    private static final String PARAMETER_STRING_CANONCALIZE_TYPE_KEY = "HTTPParameterValue";
  

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private Validator mockValidator;
    @Mock
    private SecurityConfiguration mockSecConfig;

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(ESAPI.class);
        PowerMockito.when(ESAPI.class, ESAPI_VALIDATOR_GETTER_METHOD_NAME).thenReturn(mockValidator);
        PowerMockito.when(ESAPI.class, ESAPY_SECURITY_CONFIGURATION_GETTER_METHOD_NAME).thenReturn(mockSecConfig);
        //Is intrusion detection disabled?  A:  Yes, it is off.  
        //This logic is confusing:  True, the value is False...
        Mockito.when(mockSecConfig.getDisableIntrusionDetection()).thenReturn(true);
    }
    
    /**
     * Workflow test for happy-path getQueryString. Asserts delegation calls and parameters to delegate
     * behaviors.
     */
    @Test
    public void testGetQueryString() throws Exception {
        String originalQuery = "queryString";
        String canonicalizedResponse = "canonicalized_query";

        ArgumentCaptor<String> inputCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> lengthCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> allowNullCapture = ArgumentCaptor.forClass(Boolean.class);

        String context = anyString();
        String input = inputCapture.capture();
        String type = typeCapture.capture();
        Integer length = lengthCapture.capture();
        Boolean allowNull = allowNullCapture.capture();

        PowerMockito.when(mockValidator.getValidInput(context, input, type, length, allowNull)).thenReturn(
            canonicalizedResponse);
        PowerMockito.when(mockSecConfig.getIntProp(SECURITY_CONFIGURATION_QUERY_STRING_LENGTH_KEY_NAME)).thenReturn(
            SECURITY_CONFIGURATION_TEST_LENGTH);

        PowerMockito.when(mockRequest.getQueryString()).thenReturn(originalQuery);

        SecurityWrapperRequest request = new SecurityWrapperRequest(mockRequest);
        String rval = request.getQueryString();
        assertEquals(canonicalizedResponse, rval);

        String actualInput = inputCapture.getValue();
        String actualType = typeCapture.getValue();
        int actualLength = lengthCapture.getValue().intValue();
        boolean actualAllowNull = allowNullCapture.getValue().booleanValue();

        assertEquals(originalQuery, actualInput);
        assertEquals(QUERY_STRING_CANONCALIZE_TYPE_KEY, actualType);
        assertTrue(SECURITY_CONFIGURATION_TEST_LENGTH == actualLength);
        assertTrue(actualAllowNull);

        verify(mockValidator, times(1)).getValidInput(anyString(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(mockSecConfig, times(1)).getIntProp(SECURITY_CONFIGURATION_QUERY_STRING_LENGTH_KEY_NAME);
        verify(mockRequest, times(1)).getQueryString();
    }

    /**
     * Test for getQueryString when validation throws an Exception. 
     * <br/>
     * Asserts delegation calls and parameters to delegate behaviors.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetQueryStringCanonicalizeException() throws IntrusionException, ValidationException {
        String originalQuery = "queryString";

        ArgumentCaptor<String> inputCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> lengthCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> allowNullCapture = ArgumentCaptor.forClass(Boolean.class);

        String context = anyString();
        String input = inputCapture.capture();
        String type = typeCapture.capture();
        Integer length = lengthCapture.capture();
        Boolean allowNull = allowNullCapture.capture();

        PowerMockito.when(mockValidator.getValidInput(context, input, type, length, allowNull)).thenThrow(
            ValidationException.class);
        PowerMockito.when(mockSecConfig.getIntProp(SECURITY_CONFIGURATION_QUERY_STRING_LENGTH_KEY_NAME)).thenReturn(
            SECURITY_CONFIGURATION_TEST_LENGTH);

        PowerMockito.when(mockRequest.getQueryString()).thenReturn(originalQuery);

        SecurityWrapperRequest request = new SecurityWrapperRequest(mockRequest);
        String rval = request.getQueryString();

        assertTrue("SecurityWrapperRequest should return an empty String when an exception occurs in validation", rval
            .isEmpty());

        String actualInput = inputCapture.getValue();
        String actualType = typeCapture.getValue();
        int actualLength = lengthCapture.getValue().intValue();
        boolean actualAllowNull = allowNullCapture.getValue().booleanValue();

        assertEquals(originalQuery, actualInput);
        assertEquals(QUERY_STRING_CANONCALIZE_TYPE_KEY, actualType);
        assertTrue(SECURITY_CONFIGURATION_TEST_LENGTH == actualLength);
        assertTrue(actualAllowNull);

        verify(mockValidator, times(1)).getValidInput(anyString(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(mockSecConfig, times(1)).getIntProp(SECURITY_CONFIGURATION_QUERY_STRING_LENGTH_KEY_NAME);
        verify(mockRequest, times(1)).getQueryString();
    }
    @Test
    public void testGetParameterString() throws Exception{
        String originalParam = "aParameterValue";
        String parameterName = "SomeParameterName";
        String cannonicalParam="safeParameterValue";

        ArgumentCaptor<String> inputCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> lengthCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> allowNullCapture = ArgumentCaptor.forClass(Boolean.class);

        String context = anyString();
        String input = inputCapture.capture();
        String type = typeCapture.capture();
        Integer length = lengthCapture.capture();
        Boolean allowNull = allowNullCapture.capture();

        PowerMockito.when(mockValidator.getValidInput(context, input, type, length, allowNull)).thenReturn(
                cannonicalParam);
        PowerMockito.when(mockSecConfig.getIntProp(SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME)).thenReturn(
                SECURITY_CONFIGURATION_TEST_LENGTH);

        PowerMockito.when(mockRequest.getParameter(parameterName)).thenReturn(originalParam);

        SecurityWrapperRequest request = new SecurityWrapperRequest(mockRequest);
        String rval = request.getParameter(parameterName);
        assertEquals(cannonicalParam, rval);

        String actualInput = inputCapture.getValue();
        String actualType = typeCapture.getValue();
        int actualLength = lengthCapture.getValue().intValue();
        boolean actualAllowNull = allowNullCapture.getValue().booleanValue();

        assertEquals(originalParam, actualInput);
        assertEquals(PARAMETER_STRING_CANONCALIZE_TYPE_KEY, actualType);
        assertTrue(SECURITY_CONFIGURATION_TEST_LENGTH == actualLength);
        assertTrue(actualAllowNull);

        verify(mockValidator, times(1)).getValidInput(anyString(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(mockSecConfig, times(1)).getIntProp(SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME);
        verify(mockRequest, times(1)).getParameter(parameterName);
    }

    @Test
    public void testGetParameterStringBoolean() throws Exception{
        String originalParam = "aParameterValue";
        String parameterName = "SomeParameterName";
        String cannonicalParam="safeParameterValue";

        ArgumentCaptor<String> inputCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> lengthCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> allowNullCapture = ArgumentCaptor.forClass(Boolean.class);

        String context = anyString();
        String input = inputCapture.capture();
        String type = typeCapture.capture();
        Integer length = lengthCapture.capture();
        Boolean allowNull = allowNullCapture.capture();

        PowerMockito.when(mockValidator.getValidInput(context, input, type, length, allowNull)).thenReturn(
                cannonicalParam);
        PowerMockito.when(mockSecConfig.getIntProp(SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME)).thenReturn(
                SECURITY_CONFIGURATION_TEST_LENGTH);

        PowerMockito.when(mockRequest.getParameter(parameterName)).thenReturn(originalParam);

        SecurityWrapperRequest request = new SecurityWrapperRequest(mockRequest);
        String rval = request.getParameter(parameterName, false);
        assertEquals(cannonicalParam, rval);

        String actualInput = inputCapture.getValue();
        String actualType = typeCapture.getValue();
        int actualLength = lengthCapture.getValue().intValue();
        boolean actualAllowNull = allowNullCapture.getValue().booleanValue();

        assertEquals(originalParam, actualInput);
        assertEquals(PARAMETER_STRING_CANONCALIZE_TYPE_KEY, actualType);
        assertTrue(SECURITY_CONFIGURATION_TEST_LENGTH == actualLength);
        assertFalse(actualAllowNull);

        verify(mockValidator, times(1)).getValidInput(anyString(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(mockSecConfig, times(1)).getIntProp(SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME);
        verify(mockRequest, times(1)).getParameter(parameterName);
    }
    
    @Test
    public void testGetParameterStringBooleanInt() throws Exception{
        String originalParam = "aParameterValue";
        String parameterName = "SomeParameterName";
        String cannonicalParam="safeParameterValue";

        ArgumentCaptor<String> inputCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> lengthCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> allowNullCapture = ArgumentCaptor.forClass(Boolean.class);

        String context = anyString();
        String input = inputCapture.capture();
        String type = typeCapture.capture();
        Integer length = lengthCapture.capture();
        Boolean allowNull = allowNullCapture.capture();

        PowerMockito.when(mockValidator.getValidInput(context, input, type, length, allowNull)).thenReturn(
                cannonicalParam);
        
        PowerMockito.when(mockRequest.getParameter(parameterName)).thenReturn(originalParam);

        SecurityWrapperRequest request = new SecurityWrapperRequest(mockRequest);
        String rval = request.getParameter(parameterName, false, 75);
        assertEquals(cannonicalParam, rval);

        String actualInput = inputCapture.getValue();
        String actualType = typeCapture.getValue();
        int actualLength = lengthCapture.getValue().intValue();
        boolean actualAllowNull = allowNullCapture.getValue().booleanValue();

        assertEquals(originalParam, actualInput);
        assertEquals(PARAMETER_STRING_CANONCALIZE_TYPE_KEY, actualType);
        assertTrue(75 == actualLength);
        assertFalse(actualAllowNull);

        verify(mockValidator, times(1)).getValidInput(anyString(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(mockRequest, times(1)).getParameter(parameterName);
    }
    
    @Test
    public void testGetParameterStringBooleanIntString() throws Exception{
        String originalParam = "aParameterValue";
        String parameterName = "SomeParameterName";
        String cannonicalParam="safeParameterValue";

        ArgumentCaptor<String> inputCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> lengthCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> allowNullCapture = ArgumentCaptor.forClass(Boolean.class);

        String context = anyString();
        String input = inputCapture.capture();
        String type = typeCapture.capture();
        Integer length = lengthCapture.capture();
        Boolean allowNull = allowNullCapture.capture();

        PowerMockito.when(mockValidator.getValidInput(context, input, type, length, allowNull)).thenReturn(
                cannonicalParam);
        
        PowerMockito.when(mockRequest.getParameter(parameterName)).thenReturn(originalParam);

        SecurityWrapperRequest request = new SecurityWrapperRequest(mockRequest);
        String rval = request.getParameter(parameterName, false, 75, "customType");
        assertEquals(cannonicalParam, rval);

        String actualInput = inputCapture.getValue();
        String actualType = typeCapture.getValue();
        int actualLength = lengthCapture.getValue().intValue();
        boolean actualAllowNull = allowNullCapture.getValue().booleanValue();

        assertEquals(originalParam, actualInput);
        assertEquals("customType", actualType);
        assertTrue(75 == actualLength);
        assertFalse(actualAllowNull);

        verify(mockValidator, times(1)).getValidInput(anyString(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(mockRequest, times(1)).getParameter(parameterName);
    }

    
    @Test
    public void testGetParameterStringNullEvalPassthrough() throws Exception{
        String originalParam = "aParameterValue";
        String parameterName = "SomeParameterName";
        String cannonicalParam=null;

        ArgumentCaptor<String> inputCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> lengthCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> allowNullCapture = ArgumentCaptor.forClass(Boolean.class);

        String context = anyString();
        String input = inputCapture.capture();
        String type = typeCapture.capture();
        Integer length = lengthCapture.capture();
        Boolean allowNull = allowNullCapture.capture();

        PowerMockito.when(mockValidator.getValidInput(context, input, type, length, allowNull)).thenReturn(
                cannonicalParam);
        PowerMockito.when(mockSecConfig.getIntProp(SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME)).thenReturn(
                SECURITY_CONFIGURATION_TEST_LENGTH);

        PowerMockito.when(mockRequest.getParameter(parameterName)).thenReturn(originalParam);

        SecurityWrapperRequest request = new SecurityWrapperRequest(mockRequest);
        String rval = request.getParameter(parameterName);
        assertNull(rval);

        String actualInput = inputCapture.getValue();
        String actualType = typeCapture.getValue();
        int actualLength = lengthCapture.getValue().intValue();
        boolean actualAllowNull = allowNullCapture.getValue().booleanValue();

        assertEquals(originalParam, actualInput);
        assertEquals(PARAMETER_STRING_CANONCALIZE_TYPE_KEY, actualType);
        assertTrue(SECURITY_CONFIGURATION_TEST_LENGTH == actualLength);
        assertTrue(actualAllowNull);

        verify(mockValidator, times(1)).getValidInput(anyString(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(mockSecConfig, times(1)).getIntProp(SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME);
        verify(mockRequest, times(1)).getParameter(parameterName);
    }

    @Test
    public void testGetParameterStringNullOnException() throws Exception{
        String originalParam = "aParameterValue";
        String parameterName = "SomeParameterName";
        ValidationException ve = new ValidationException("Thrown from test Scope", "Test Exception is intentional.");

        ArgumentCaptor<String> inputCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> lengthCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> allowNullCapture = ArgumentCaptor.forClass(Boolean.class);

        String context = anyString();
        String input = inputCapture.capture();
        String type = typeCapture.capture();
        Integer length = lengthCapture.capture();
        Boolean allowNull = allowNullCapture.capture();

        PowerMockito.when(mockValidator.getValidInput(context, input, type, length, allowNull)).thenThrow(ve);
        PowerMockito.when(mockSecConfig.getIntProp(SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME)).thenReturn(
                SECURITY_CONFIGURATION_TEST_LENGTH);

        PowerMockito.when(mockRequest.getParameter(parameterName)).thenReturn(originalParam);

        SecurityWrapperRequest request = new SecurityWrapperRequest(mockRequest);
        String rval = request.getParameter(parameterName);
        assertNull(rval);

        String actualInput = inputCapture.getValue();
        String actualType = typeCapture.getValue();
        int actualLength = lengthCapture.getValue().intValue();
        boolean actualAllowNull = allowNullCapture.getValue().booleanValue();

        assertEquals(originalParam, actualInput);
        assertEquals(PARAMETER_STRING_CANONCALIZE_TYPE_KEY, actualType);
        assertTrue(SECURITY_CONFIGURATION_TEST_LENGTH == actualLength);
        assertTrue(actualAllowNull);

        verify(mockValidator, times(1)).getValidInput(anyString(), anyString(), anyString(), anyInt(), anyBoolean());
        verify(mockSecConfig, times(1)).getIntProp(SECURITY_CONFIGURATION_PARAMETER_STRING_LENGTH_KEY_NAME);
        verify(mockRequest, times(1)).getParameter(parameterName);
    }
}
