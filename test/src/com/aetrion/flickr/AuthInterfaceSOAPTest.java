/*
 * Copyright (c) 2005 Aetrion LLC.
 */

package com.aetrion.flickr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.util.IOUtilities;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * @author Anthony Eden
 */
public class AuthInterfaceSOAPTest extends TestCase {

    Flickr flickr = null;
    Properties properties = null;

    public void setUp() throws ParserConfigurationException, IOException {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            properties = new Properties();
            properties.load(in);

            Flickr.debugStream = true;
            SOAP soap = new SOAP(properties.getProperty("host"));
            flickr = new Flickr(properties.getProperty("apiKey"), soap);
            
            RequestContext requestContext = RequestContext.getRequestContext();
            requestContext.setSharedSecret(properties.getProperty("secret"));
        } finally {
            IOUtilities.close(in);
        }
    }

    public void testGetFrob() throws FlickrException, IOException, SAXException {
        AuthInterface authInterface = flickr.getAuthInterface();
        String frob = authInterface.getFrob();
        assertNotNull(frob);
    }

    public void testReadAuthentication() throws FlickrException, IOException, SAXException,
            BrowserLaunchingInitializingException, BrowserLaunchingExecutionException, UnsupportedOperatingSystemException {
        testAuthentication(Permission.READ);
    }

    public void testWriteAuthentication() throws FlickrException, IOException, BrowserLaunchingInitializingException,
            SAXException, BrowserLaunchingExecutionException, UnsupportedOperatingSystemException {
        testAuthentication(Permission.WRITE);
    }

    public void testDeleteAuthentication() throws FlickrException, IOException, BrowserLaunchingInitializingException,
            SAXException, BrowserLaunchingExecutionException, UnsupportedOperatingSystemException {
        testAuthentication(Permission.DELETE);
    }

    private void testAuthentication(Permission permission) throws FlickrException, IOException, SAXException,
            BrowserLaunchingInitializingException, BrowserLaunchingExecutionException, UnsupportedOperatingSystemException {
        AuthInterface authInterface = flickr.getAuthInterface();
        String frob = authInterface.getFrob();
        URL url = authInterface.buildAuthenticationUrl(permission, frob);

        BrowserLauncher launcher = new BrowserLauncher(null);
        launcher.openURLinBrowser(url.toString());

        // display a dialog
        final JDialog d = new JDialog();
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                d.dispose();
            }
        });
        d.getContentPane().add(continueButton);
        d.setModal(true);
        d.pack();
        d.setVisible(true);

        Auth auth = authInterface.getToken(frob);
//        System.out.println("Token: " + authentication.getToken());
//        System.out.println("Permission: " + authentication.getPermission());
        assertNotNull(auth.getToken());
        assertEquals(permission, auth.getPermission());

        Auth checkedAuth = authInterface.checkToken(auth.getToken());
        assertEquals(auth.getToken(), checkedAuth.getToken());
        assertEquals(auth.getPermission(), checkedAuth.getPermission());
    }

    public void testCheckToken() throws FlickrException, IOException, SAXException {
        String token = properties.getProperty("token");
        AuthInterface authInterface = flickr.getAuthInterface();
        Auth checkedAuth = authInterface.checkToken(token);
        assertEquals(token, checkedAuth.getToken());
    }

}
