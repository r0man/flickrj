/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.groups.pools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.auth.AuthUtilities;
import com.aetrion.flickr.groups.Group;
import com.aetrion.flickr.photos.Extras;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoContext;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotoUtils;
import com.aetrion.flickr.util.StringUtilities;

/**
 * @author Anthony Eden
 * @version $Id: PoolsInterface.java,v 1.16 2011/07/02 19:00:59 x-mago Exp $
 */
public class PoolsInterface {

    public static final String METHOD_ADD = "flickr.groups.pools.add";
    public static final String METHOD_GET_CONTEXT = "flickr.groups.pools.getContext";
    public static final String METHOD_GET_GROUPS = "flickr.groups.pools.getGroups";
    public static final String METHOD_GET_PHOTOS = "flickr.groups.pools.getPhotos";
    public static final String METHOD_REMOVE = "flickr.groups.pools.remove";

    private String apiKey;
    private String sharedSecret;
    private Transport transport;

    public PoolsInterface(
        String apiKey,
        String sharedSecret,
        Transport transport
    ) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transport = transport;
    }

    /**
     * Add a photo to a group's pool.
     *
     * @param photoId The photo ID
     * @param groupId The group ID
     */
    public void add(String photoId, String groupId) throws IOException, SAXException,
            FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_ADD));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photo_id", photoId));
        parameters.add(new Parameter("group_id", groupId));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transport.post(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
    }

    /**
     * Get the context for a photo in the group pool.
     *
     * This method does not require authentication.
     *
     * @param photoId The photo ID
     * @param groupId The group ID
     * @return The PhotoContext
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public PhotoContext getContext(String photoId, String groupId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_CONTEXT));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photo_id", photoId));
        parameters.add(new Parameter("group_id", groupId));

        Response response = transport.get(transport.getPath(), parameters);
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
            } else {
                System.err.println("unsupported element name: " + elementName);
            }
        }
        return photoContext;
    }

    /**
     * Get a collection of all of the user's groups.
     *
     * @return A Collection of Group objects
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getGroups() throws IOException, SAXException, FlickrException {
        List groups = new ArrayList();

        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_GROUPS));
        parameters.add(new Parameter("api_key", apiKey));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transport.get(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element groupsElement = response.getPayload();
        NodeList groupNodes = groupsElement.getElementsByTagName("group");
        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element groupElement = (Element) groupNodes.item(i);
            Group group = new Group();
            group.setId(groupElement.getAttribute("id"));
            group.setName(groupElement.getAttribute("name"));
            group.setAdmin("1".equals(groupElement.getAttribute("admin")));
            group.setPrivacy(groupElement.getAttribute("privacy"));
            groups.add(group);
        }
        return groups;
    }

    /**
     * Get the photos for the specified group pool, optionally filtering by taf.
     *
     * This method does not require authentication.
     *
     * @see com.aetrion.flickr.photos.Extras
     * @param groupId The group ID
     * @param tags The optional tags (may be null)
     * @param extras Set of extra-attributes to include (may be null)
     * @param perPage The number of photos per page (0 to ignore)
     * @param page The page offset (0 to ignore)
     * @return A Collection of Photo objects
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public PhotoList getPhotos(String groupId, String[] tags, Set extras, int perPage, int page)
      throws IOException, SAXException, FlickrException {
        PhotoList photos = new PhotoList();

        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_PHOTOS));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("group_id", groupId));
        if (tags != null) {
            parameters.add(new Parameter("tags", StringUtilities.join(tags, " ")));
        }
        if (perPage > 0) {
            parameters.add(new Parameter("per_page", new Integer(perPage)));
        }
        if (page > 0) {
            parameters.add(new Parameter("page", new Integer(page)));
        }

        if (extras != null) {
            StringBuffer sb = new StringBuffer();
            Iterator it = extras.iterator();
            for (int i = 0; it.hasNext(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(it.next());
            }
            parameters.add(new Parameter(Extras.KEY_EXTRAS, sb.toString()));
        }
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transport.get(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element photosElement = response.getPayload();
        photos.setPage(photosElement.getAttribute("page"));
        photos.setPages(photosElement.getAttribute("pages"));
        photos.setPerPage(photosElement.getAttribute("perpage"));
        photos.setTotal(photosElement.getAttribute("total"));

        NodeList photoNodes = photosElement.getElementsByTagName("photo");
        for (int i = 0; i < photoNodes.getLength(); i++) {
            Element photoElement = (Element) photoNodes.item(i);
            photos.add(PhotoUtils.createPhoto(photoElement));
        }

        return photos;
    }

    /**
     * Convenience/Compatibility method.
     *
     * This method does not require authentication.
     *
     * @see com.aetrion.flickr.photos.Extras
     * @param groupId The group ID
     * @param tags The optional tags (may be null)
     * @param perPage The number of photos per page (0 to ignore)
     * @param page The page offset (0 to ignore)
     * @return A Collection of Photo objects
     */
    public PhotoList getPhotos(String groupId, String[] tags, int perPage, int page)
      throws IOException, SAXException, FlickrException {
        return getPhotos(groupId, tags, Extras.MIN_EXTRAS, perPage, page);
    }

    /**
     * Remove the specified photo from the group.
     *
     * @param photoId The photo ID
     * @param groupId The group ID
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public void remove(String photoId, String groupId) throws IOException, SAXException,
            FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_REMOVE));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photo_id", photoId));
        parameters.add(new Parameter("group_id", groupId));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transport.post(transport.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
    }

}
