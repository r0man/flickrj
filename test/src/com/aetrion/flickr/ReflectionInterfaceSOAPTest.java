/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package com.aetrion.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import com.aetrion.flickr.reflection.ReflectionInterface;
import com.aetrion.flickr.reflection.Method;
import com.aetrion.flickr.util.IOUtilities;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

/**
 * @author Anthony Eden
 */
public class ReflectionInterfaceSOAPTest extends TestCase {

    Flickr flickr = null;
    Properties properties = null;

    public void setUp() throws ParserConfigurationException, IOException {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            properties = new Properties();
            properties.load(in);

            SOAP soap = new SOAP(properties.getProperty("host"));
            flickr = new Flickr(properties.getProperty("apiKey"), soap);
            
        } finally {
            IOUtilities.close(in);
        }
    }

    public void testGetMethodInfo() throws FlickrException, IOException, SAXException {
        String methodName = "flickr.urls.lookupGroup";
        ReflectionInterface reflectionInterface = flickr.getReflectionInterface();
        Method method = reflectionInterface.getMethodInfo(methodName);
        assertNotNull(method);
        assertEquals(methodName, method.getName());
    }

    public void testGetMethods() throws FlickrException, IOException, SAXException {
        ReflectionInterface reflectionInterface = flickr.getReflectionInterface();
        Collection methods = reflectionInterface.getMethods();
        assertNotNull(methods);
        assertTrue("There are no methods in the method list", methods.size() > 0);
    }

}
