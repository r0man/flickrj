/* Copyright 2004, Aetrion LLC.  All Rights Reserved. */

package com.aetrion.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.groups.members.Member;
import com.aetrion.flickr.groups.members.MembersInterface;
import com.aetrion.flickr.groups.members.MembersList;
import com.aetrion.flickr.util.IOUtilities;

/**
 * @author mago
 * @version $Id: MembersInterfaceTest.java,v 1.3 2009/07/11 20:30:27 x-mago Exp $
 */
public class MembersInterfaceTest extends TestCase {

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

    public void testGetList() throws FlickrException, IOException, SAXException {
        MembersInterface iface = flickr.getMembersInterface();
        // Group: Urban fragments
        String id = "64262537@N00";
        Set memberTypes = new HashSet();
        memberTypes.add(Member.TYPE_MEMBER);
        memberTypes.add(Member.TYPE_ADMIN);
        memberTypes.add(Member.TYPE_MODERATOR);
        MembersList list = iface.getList(id, memberTypes, 50, 1);
        assertNotNull(list);
        assertEquals(50, list.size());
        Member m = (Member) list.get(10);
        assertTrue(m.getId().indexOf("@") > 0);
        assertTrue(m.getUserName().length() > 0);
        assertTrue(m.getIconFarm() > -1);
        assertTrue(m.getIconServer() > -1);
    }
}
