/* Copyright 2004, Aetrion LLC.  All Rights Reserved. */

package com.aetrion.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.blogs.BlogsInterface;
import com.aetrion.flickr.blogs.Service;
import com.aetrion.flickr.util.IOUtilities;

/**
 * @author Anthony Eden
 */
public class BlogsInterfaceTest extends TestCase {

    Flickr flickr = null;

    public void setUp() throws ParserConfigurationException, IOException, FlickrException, SAXException {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            Properties properties = new Properties();
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
            auth.setPermission(Permission.READ); 
            requestContext.setAuth(auth);
        } finally {
            IOUtilities.close(in);
        }
    }

    public void testGetList() throws FlickrException, IOException, SAXException {
        BlogsInterface blogsInterface = flickr.getBlogsInterface();
        Collection blogs = blogsInterface.getList();
        assertNotNull(blogs);
        assertEquals(0, blogs.size());
    }

    public void testGetServices() throws FlickrException, IOException, SAXException {
        BlogsInterface blogsInterface = flickr.getBlogsInterface();
        Collection services = blogsInterface.getServices();
        Iterator it = services.iterator();
        boolean bloggerFound = false;
        while (it.hasNext()) {
            Service ser = (Service) it.next();
            if (ser.getId().equals("beta.blogger.com") &&
                ser.getName().equals("Blogger")) {
                bloggerFound = true;
            }
            //System.out.println(ser.getId() + " " + ser.getName());
        }
        assertTrue(bloggerFound);
    }

    public void testPostImage() {
        // TODO: implement this test
    }

}
