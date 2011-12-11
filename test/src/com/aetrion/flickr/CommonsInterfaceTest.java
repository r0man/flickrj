/* Copyright 2004, Aetrion LLC.  All Rights Reserved. */

package com.aetrion.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.commons.CommonsInterface;
import com.aetrion.flickr.commons.Institution;
import com.aetrion.flickr.util.IOUtilities;

/**
 * @author mago
 * @version $Id: CommonsInterfaceTest.java,v 1.1 2009/06/30 18:48:59 x-mago Exp $
 */
public class CommonsInterfaceTest extends TestCase {
    Flickr flickr = null;
    Properties properties = null;

    public void setUp() throws ParserConfigurationException, IOException, FlickrException, SAXException {
        //Flickr.debugStream = true;

        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            properties = new Properties();
            properties.load(in);

            REST rest = new REST();
            rest.setHost(properties.getProperty("host"));

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

    public void testGetInstitutions() throws FlickrException, IOException, SAXException {
        CommonsInterface iface = flickr.getCommonsInterface();
        ArrayList list = iface.getInstitutions();
        assertNotNull(list);
        Iterator it = list.iterator();
        boolean museumFound = false;
        while (it.hasNext()) {
            Institution inst = (Institution) it.next();
            if (inst.getName().equals("Brooklyn Museum")) {
                assertEquals(
                    1211958000000L,
                    inst.getDateLaunch().getTime()
                );
                assertEquals(
                    "http://www.brooklynmuseum.org/",
                    inst.getSiteUrl()
                );
                assertEquals(
                    "http://www.brooklynmuseum.org/flickr_commons.php",
                    inst.getLicenseUrl()
                );
                assertEquals(
                    "http://flickr.com/photos/brooklyn_museum/",
                    inst.getFlickrUrl()
                );
                museumFound = true;
            }
        }
        assertTrue(museumFound);
    }
}
