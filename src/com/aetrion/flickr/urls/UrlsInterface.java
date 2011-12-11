/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.urls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.auth.AuthUtilities;
import com.aetrion.flickr.groups.Group;

/**
 * Interface for testing Flickr connectivity.
 *
 * @author Anthony Eden
 */
public class UrlsInterface {

    public static final String METHOD_GET_GROUP = "flickr.urls.getGroup";
    public static final String METHOD_GET_USER_PHOTOS = "flickr.urls.getUserPhotos";
    public static final String METHOD_GET_USER_PROFILE = "flickr.urls.getUserProfile";
    public static final String METHOD_LOOKUP_GROUP = "flickr.urls.lookupGroup";
    public static final String METHOD_LOOKUP_USER = "flickr.urls.lookupUser";

    private String apiKey;
    private String sharedSecret;
    private Transport transport;

    /**
     * Construct a UrlsInterface.
     *
     * @param apiKey The API key
     * @param transportAPI The Transport interface
     */
    public UrlsInterface(
        String apiKey,
        String sharedSecret,
        Transport transportAPI
    ) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transport = transportAPI;
    }

    /**
     * Get the group URL for the specified group ID
     *
     * @param groupId The group ID
     * @return The group URL
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public String getGroup(String groupId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_GROUP));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("group_id", groupId));

        Response response = transport.post(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element payload = response.getPayload();
        return payload.getAttribute("url");
    }

    /**
     * Get the URL for the user's photos.
     *
     * @param userId The user ID
     * @return The user photo URL
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public String getUserPhotos(String userId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_USER_PHOTOS));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("user_id", userId));

        Response response = transport.post(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element payload = response.getPayload();
        return payload.getAttribute("url");
    }

    /**
     * Get the URL for the user's profile.
     *
     * @param userId The user ID
     * @return The URL
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public String getUserProfile(String userId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_USER_PROFILE));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("user_id", userId));

        Response response = transport.post(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element payload = response.getPayload();
        return payload.getAttribute("url");
    }

    /**
     * Lookup the group for the specified URL.
     *
     * @param url The url
     * @return The group
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Group lookupGroup(String url) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_LOOKUP_GROUP));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("url", url));

        Response response = transport.post(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Group group = new Group();
        Element payload = response.getPayload();
        Element groupnameElement = (Element) payload.getElementsByTagName("groupname").item(0);
        group.setId(payload.getAttribute("id"));
        group.setName(((Text) groupnameElement.getFirstChild()).getData());
        return group;
    }

    /**
     * Lookup the username for the specified User URL.
     *
     * @param url The user profile URL
     * @return The username
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public String lookupUser(String url)
      throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_LOOKUP_USER));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("url", url));

        Response response = transport.post(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element payload = response.getPayload();
        Element groupnameElement = (Element) payload.getElementsByTagName("username").item(0);
        return ((Text) groupnameElement.getFirstChild()).getData();
    }

}
