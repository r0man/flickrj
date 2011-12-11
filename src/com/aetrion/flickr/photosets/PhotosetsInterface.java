/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.photosets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.auth.AuthUtilities;
import com.aetrion.flickr.people.User;
import com.aetrion.flickr.photos.Extras;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoContext;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotoUtils;
import com.aetrion.flickr.util.StringUtilities;
import com.aetrion.flickr.util.XMLUtilities;

/**
 * Interface for working with photosets.
 *
 * @author Anthony Eden
 * @version $Id: PhotosetsInterface.java,v 1.27 2009/11/08 21:58:00 x-mago Exp $
 */
public class PhotosetsInterface {

    public static final String METHOD_ADD_PHOTO = "flickr.photosets.addPhoto";
    public static final String METHOD_CREATE = "flickr.photosets.create";
    public static final String METHOD_DELETE = "flickr.photosets.delete";
    public static final String METHOD_EDIT_META = "flickr.photosets.editMeta";
    public static final String METHOD_EDIT_PHOTOS = "flickr.photosets.editPhotos";
    public static final String METHOD_GET_CONTEXT = "flickr.photosets.getContext";
    public static final String METHOD_GET_INFO = "flickr.photosets.getInfo";
    public static final String METHOD_GET_LIST = "flickr.photosets.getList";
    public static final String METHOD_GET_PHOTOS = "flickr.photosets.getPhotos";
    public static final String METHOD_ORDER_SETS = "flickr.photosets.orderSets";
    public static final String METHOD_REMOVE_PHOTO = "flickr.photosets.removePhoto";

    private String apiKey;
    private String sharedSecret;
    private Transport transportAPI;

    public PhotosetsInterface(
        String apiKey,
        String sharedSecret,
        Transport transportAPI
    ) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transportAPI = transportAPI;
    }

    /**
     * Add a photo to the end of the photoset.
     * <p/>
     * Note: requires authentication with the new authentication API with 'write' permission.
     *
     * @param photosetId The photoset ID
     * @param photoId The photo ID
     */
    public void addPhoto(String photosetId, String photoId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_ADD_PHOTO));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photoset_id", photosetId));
        parameters.add(new Parameter("photo_id", photoId));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.post(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
    }

    /**
     * Create a new photoset.
     *
     * @param title The photoset title
     * @param description The photoset description
     * @param primaryPhotoId The primary photo id
     * @return The new Photset
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Photoset create(String title, String description, String primaryPhotoId)
            throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_CREATE));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("title", title));
        parameters.add(new Parameter("description", description));
        parameters.add(new Parameter("primary_photo_id", primaryPhotoId));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.post(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element photosetElement = (Element) response.getPayload();
        Photoset photoset = new Photoset();
        photoset.setId(photosetElement.getAttribute("id"));
        photoset.setUrl(photosetElement.getAttribute("url"));
        return photoset;
    }

    /**
     * Delete the specified photoset.
     *
     * @param photosetId The photoset ID
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public void delete(String photosetId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_DELETE));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photoset_id", photosetId));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.post(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
    }

    /**
     * Modify the meta-data for a photoset.
     *
     * @param photosetId The photoset ID
     * @param title A new title
     * @param description A new description (can be null)
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public void editMeta(String photosetId, String title, String description)
            throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_EDIT_META));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photoset_id", photosetId));
        parameters.add(new Parameter("title", title));
        if (description != null) {
            parameters.add(new Parameter("description", description));
        }
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.post(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
    }

    /**
     * Edit which photos are in the photoset.
     *
     * @param photosetId The photoset ID
     * @param primaryPhotoId The primary photo Id
     * @param photoIds The photo IDs for the photos in the set
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public void editPhotos(String photosetId, String primaryPhotoId, String[] photoIds)
            throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_EDIT_PHOTOS));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photoset_id", photosetId));
        parameters.add(new Parameter("primary_photo_id", primaryPhotoId));
        parameters.add(new Parameter("photo_ids", StringUtilities.join(photoIds, ",")));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.post(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
    }

    /**
     * Get a photo's context in the specified photo set.
     *
     * This method does not require authentication.
     *
     * @param photoId The photo ID
     * @param photosetId The photoset ID
     * @return The PhotoContext
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public PhotoContext getContext(String photoId, String photosetId)
            throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_CONTEXT));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photo_id", photoId));
        parameters.add(new Parameter("photoset_id", photosetId));

        if (AuthUtilities.isAuthenticated(parameters)) {
            parameters.add(
                new Parameter(
                    "api_sig",
                    AuthUtilities.getSignature(sharedSecret, parameters)
                )
            );
        }

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Collection payload = response.getPayloadCollection();
        Iterator iter = payload.iterator();
        PhotoContext photoContext = new PhotoContext();
        while (iter.hasNext()) {
            Element element = (Element) iter.next();
            String elementName = element.getTagName();
            if (elementName.equals("prevphoto")) {
                Photo photo = new Photo();
                photo.setId(element.getAttribute("id"));
                photoContext.setPreviousPhoto(photo);
            } else if (elementName.equals("nextphoto")) {
                Photo photo = new Photo();
                photo.setId(element.getAttribute("id"));
                photoContext.setNextPhoto(photo);
            } else if (elementName.equals("count")) {
            	// TODO: process this information
            } else {
                System.err.println("unsupported element name: " + elementName);
            }
        }
        return photoContext;
    }

    /**
     * Get the information for a specified photoset.
     *
     * This method does not require authentication.
     *
     * @param photosetId The photoset ID
     * @return The Photoset
     * @throws FlickrException
     * @throws IOException
     * @throws SAXException
     */
    public Photoset getInfo(String photosetId) throws FlickrException, IOException, SAXException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_INFO));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photoset_id", photosetId));

        if (AuthUtilities.isAuthenticated(parameters)) {
            parameters.add(
                new Parameter(
                    "api_sig",
                    AuthUtilities.getSignature(sharedSecret, parameters)
                )
            );
        }

        Response response = transportAPI.post(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element photosetElement = (Element)response.getPayload();
        Photoset photoset = new Photoset();
        photoset.setId(photosetElement.getAttribute("id"));

        User owner = new User();
        owner.setId(photosetElement.getAttribute("owner"));
        photoset.setOwner(owner);

        Photo primaryPhoto = new Photo();
        primaryPhoto.setId(photosetElement.getAttribute("primary"));
        primaryPhoto.setSecret(photosetElement.getAttribute("secret")); // TODO verify that this is the secret for the photo
        primaryPhoto.setServer(photosetElement.getAttribute("server")); // TODO verify that this is the server for the photo
        primaryPhoto.setFarm(photosetElement.getAttribute("farm"));
        photoset.setPrimaryPhoto(primaryPhoto);

        // TODO remove secret/server/farm from photoset?
        // It's rather related to the primaryPhoto, then to the photoset itself.
        photoset.setSecret(photosetElement.getAttribute("secret"));
        photoset.setServer(photosetElement.getAttribute("server"));
        photoset.setFarm(photosetElement.getAttribute("farm"));
        photoset.setPhotoCount(photosetElement.getAttribute("photos"));

        photoset.setTitle(XMLUtilities.getChildValue(photosetElement, "title"));
        photoset.setDescription(XMLUtilities.getChildValue(photosetElement, "description"));
        photoset.setPrimaryPhoto(primaryPhoto);

        return photoset;
    }

    /**
     * Get a list of all photosets for the specified user.
     *
     * This method does not require authentication.
     * But to get a Photoset into the list, that contains just private photos,
     * the call needs to be authenticated.
     *
     * @param userId The User id
     * @return The Photosets collection
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Photosets getList(String userId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST));
        parameters.add(new Parameter("api_key", apiKey));

        if (userId != null) {
            parameters.add(new Parameter("user_id", userId));
        }
        if (AuthUtilities.isAuthenticated(parameters)) {
            parameters.add(
                new Parameter(
                    "api_sig",
                    AuthUtilities.getSignature(sharedSecret, parameters)
                )
            );
        }

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Photosets photosetsObject = new Photosets();
        Element photosetsElement = response.getPayload();
        List photosets = new ArrayList();
        NodeList photosetElements = photosetsElement.getElementsByTagName("photoset");
        for (int i = 0; i < photosetElements.getLength(); i++) {
            Element photosetElement = (Element) photosetElements.item(i);
            Photoset photoset = new Photoset();
            photoset.setId(photosetElement.getAttribute("id"));

            User owner = new User();
            owner.setId(photosetElement.getAttribute("owner"));
            photoset.setOwner(owner);

            Photo primaryPhoto = new Photo();
            primaryPhoto.setId(photosetElement.getAttribute("primary"));
            primaryPhoto.setSecret(photosetElement.getAttribute("secret")); // TODO verify that this is the secret for the photo
            primaryPhoto.setServer(photosetElement.getAttribute("server")); // TODO verify that this is the server for the photo
            primaryPhoto.setFarm(photosetElement.getAttribute("farm"));
            photoset.setPrimaryPhoto(primaryPhoto);

            photoset.setSecret(photosetElement.getAttribute("secret"));
            photoset.setServer(photosetElement.getAttribute("server"));
            photoset.setFarm(photosetElement.getAttribute("farm"));
            photoset.setPhotoCount(photosetElement.getAttribute("photos"));

            photoset.setTitle(XMLUtilities.getChildValue(photosetElement, "title"));
            photoset.setDescription(XMLUtilities.getChildValue(photosetElement, "description"));

            photosets.add(photoset);
        }

        photosetsObject.setPhotosets(photosets);
        return photosetsObject;
    }

    /**
     * Get a collection of Photo objects for the specified Photoset.
     * 
     * This method does not require authentication.
     *
     * @see com.aetrion.flickr.photos.Extras
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_NO_FILTER
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_PUBLIC
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS_FAMILY
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FAMILY
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS
     * @param photosetId The photoset ID
     * @param extras Set of extra-fields
     * @param privacy_filter filter value for authenticated calls
     * @param perPage The number of photos per page
     * @param page The page offset
     * @return PhotoList The Collection of Photo objects
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public PhotoList getPhotos(String photosetId, Set extras,
      int privacy_filter, int perPage, int page)
      throws IOException, SAXException, FlickrException {
        PhotoList photos = new PhotoList();
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_PHOTOS));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photoset_id", photosetId));

        if (perPage > 0) {
            parameters.add(new Parameter("per_page", new Integer(perPage)));
        }

        if (page > 0) {
            parameters.add(new Parameter("page", new Integer(page)));
        }

        if (privacy_filter > 0) {
            parameters.add(new Parameter("privacy_filter", "" + privacy_filter));
        }

        if (extras != null && !extras.isEmpty()) {
            parameters.add(new Parameter(Extras.KEY_EXTRAS, StringUtilities.join(extras, ",")));
        }
        if (AuthUtilities.isAuthenticated(parameters)) {
            parameters.add(
                new Parameter(
                    "api_sig",
                    AuthUtilities.getSignature(sharedSecret, parameters)
                )
            );
        }

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element photoset = response.getPayload();
        NodeList photoElements = photoset.getElementsByTagName("photo");
        photos.setPage(photoset.getAttribute("page"));
        photos.setPages(photoset.getAttribute("pages"));
        photos.setPerPage(photoset.getAttribute("per_page"));
        photos.setTotal(photoset.getAttribute("total"));

        for (int i = 0; i < photoElements.getLength(); i++) {
            Element photoElement = (Element) photoElements.item(i);
            photos.add(PhotoUtils.createPhoto(photoElement, photoset));
        }

        return photos;
    }

    /**
     * Convenience method.
     *
     * Calls getPhotos() with Extras.MIN_EXTRAS and Flickr.PRIVACY_LEVEL_NO_FILTER.
     *
     * This method does not require authentication.
     *
     * @see com.aetrion.flickr.photos.Extras
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_NO_FILTER
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_PUBLIC
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS_FAMILY
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FAMILY
     * @see com.aetrion.flickr.Flickr#PRIVACY_LEVEL_FRIENDS
     * @param photosetId The photoset ID
     * @param perPage The number of photos per page
     * @param page The page offset
     * @return PhotoList The Collection of Photo objects
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public PhotoList getPhotos(String photosetId, int perPage, int page) 
      throws IOException, SAXException, FlickrException {
        return getPhotos(photosetId, Extras.MIN_EXTRAS, Flickr.PRIVACY_LEVEL_NO_FILTER, perPage, page);
    }

    /**
     * Set the order in which sets are returned for the user.
     *
     * This method requires authentication with 'write' permission.
     *
     * @param photosetIds An array of Ids
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public void orderSets(String[] photosetIds) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_ORDER_SETS));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photoset_ids", StringUtilities.join(photosetIds, ",")));

        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.post(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
    }

    /**
     * Remove a photo from the set.
     *
     * @param photosetId The photoset ID
     * @param photoId The photo ID
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public void removePhoto(String photosetId, String photoId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_REMOVE_PHOTO));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photoset_id", photosetId));
        parameters.add(new Parameter("photo_id", photoId));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.post(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
    }

}
