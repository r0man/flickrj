/*
 * Copyright (c) 2007 Martin Goebel
 */
package com.aetrion.flickr.prefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.auth.AuthUtilities;

/**
 * Requesting preferences for the current authenticated user.
 *
 * @author Martin Goebel
 * @version $Id: PrefsInterface.java,v 1.6 2008/06/28 22:30:04 x-mago Exp $
 */
public class PrefsInterface {
    public static final String METHOD_GET_CONTENT_TYPE = "flickr.prefs.getContentType";
    public static final String METHOD_GET_HIDDEN = "flickr.prefs.getHidden";
    public static final String METHOD_GET_SAFETY_LEVEL = "flickr.prefs.getSafetyLevel";
    public static final String METHOD_GET_PRIVACY = "flickr.prefs.getPrivacy";
    public static final String METHOD_GET_GEO_PERMS = "flickr.prefs.getGeoPerms";

    private String apiKey;
    private String sharedSecret;
    private Transport transportAPI;

    /**
     * Construct a PrefsInterface.
     *
     * @param apiKey The API key
     * @param transportAPI The Transport interface
     */
    public PrefsInterface(
        String apiKey,
        String sharedSecret,
        Transport transportAPI
    ) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transportAPI = transportAPI;
    }

    /**
     * Returns the default content type preference for the user.
     *
     * @see com.aetrion.flickr.Flickr#CONTENTTYPE_OTHER
     * @see com.aetrion.flickr.Flickr#CONTENTTYPE_PHOTO
     * @see com.aetrion.flickr.Flickr#CONTENTTYPE_SCREENSHOT
     * @return The content-type
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public String getContentType() throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_CONTENT_TYPE));
        parameters.add(new Parameter("api_key", apiKey));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element personElement = response.getPayload();
        return personElement.getAttribute("content_type");
    }

    /**
     * Returns the default privacy level for geographic information attached to the user's photos.
     *
     * @return privacy-level
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_NO_FILTER
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_PUBLIC
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FAMILY
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS_FAMILY
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_PRIVATE
     */
    public int getGeoPerms() throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_GEO_PERMS));
        parameters.add(new Parameter("api_key", apiKey));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        int perm = -1;
        Element personElement = response.getPayload();
        String geoPerms = personElement.getAttribute("geoperms");
        try {
            perm = Integer.parseInt(geoPerms);
        } catch (NumberFormatException e) {
            throw new FlickrException("0", "Unable to parse geoPermission");
        }
        return perm;
    }

    /**
     * Returns the default hidden preference for the user.
     *
     * @return boolean hidden or not
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public boolean getHidden() throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_HIDDEN));
        parameters.add(new Parameter("api_key", apiKey));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element personElement = response.getPayload();
        return personElement.getAttribute("hidden").equals("1") ? true : false ;
    }

    /**
     * Returns the default safety level preference for the user.
     *
     * @see com.aetrion.flickr.Flickr#SAFETYLEVEL_MODERATE
     * @see com.aetrion.flickr.Flickr#SAFETYLEVEL_RESTRICTED
     * @see com.aetrion.flickr.Flickr#SAFETYLEVEL_SAFE
     * @return The current users safety-level
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public String getSafetyLevel() throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_SAFETY_LEVEL));
        parameters.add(new Parameter("api_key", apiKey));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element personElement = response.getPayload();
        return personElement.getAttribute("safety_level");
    }

    /**
     * Returns the default privacy level preference for the user.
     *
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_NO_FILTER
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_PUBLIC
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS_FAMILY
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FAMILY
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     * @return privacyLevel
     */
    public int getPrivacy() throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_PRIVACY));
        parameters.add(new Parameter("api_key", apiKey));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element personElement = response.getPayload();
        return Integer.parseInt(personElement.getAttribute("privacy"));
    }
}
