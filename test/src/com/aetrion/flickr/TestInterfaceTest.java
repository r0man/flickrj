/* Copyright 2004, Aetrion LLC.  All Rights Reserved. */

package com.aetrion.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import com.aetrion.flickr.people.User;
import com.aetrion.flickr.test.TestInterface;
import com.aetrion.flickr.util.IOUtilities;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Auth;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

/**
 * @author Anthony Eden
 * @version $Id: TestInterfaceTest.java,v 1.6 2008/01/28 23:01:45 x-mago Exp $
 */
public class TestInterfaceTest extends TestCase {

    Flickr flickr = null;
    Properties properties = null;

    public void setUp() throws ParserConfigurationException, IOException, FlickrException, SAXException {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            properties = new Properties();
            properties.load(in);

            REST rest = new REST();

            flickr = new Flickr(
                properties.getProperty("apiKey"),
                properties.getProperty("secret"),
                rest
            );

            RequestContext requestContext = RequestContext.getRequestContext();

            AuthInterface authInterface = flickr.getAuthInterface();
            Auth auth = authInterface.checkToken(properties.getProperty("token"));
            requestContext.setAuth(auth);
        } finally {
            IOUtilities.close(in);
        }
    }

    public void testEcho() throws FlickrException, IOException, SAXException {
        TestInterface iface = flickr.getTestInterface();
        List params = new ArrayList();
        params.add(new Parameter("test", "test"));
        Collection results = iface.echo(params);
        assertNotNull(results);
    }

    public void testLogin() throws FlickrException, IOException, SAXException {
        TestInterface iface = flickr.getTestInterface();
        User user = iface.login();
        assertNotNull(user);
    }

}
