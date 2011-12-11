/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.auth.AuthUtilities;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotoUtils;
import com.aetrion.flickr.util.XMLUtilities;

/**
 * Interface for working with Flickr tags.
 *
 * @author Anthony Eden
 * @version $Id: TagsInterface.java,v 1.19 2009/07/02 21:52:35 x-mago Exp $
 */
public class TagsInterface {

    public static final String METHOD_GET_CLUSTERS = "flickr.tags.getClusters";
    public static final String METHOD_GET_HOT_LIST = "flickr.tags.getHotList";
    public static final String METHOD_GET_LIST_PHOTO = "flickr.tags.getListPhoto";
    public static final String METHOD_GET_LIST_USER = "flickr.tags.getListUser";
    public static final String METHOD_GET_LIST_USER_POPULAR = "flickr.tags.getListUserPopular";
    public static final String METHOD_GET_LIST_USER_RAW = "flickr.tags.getListUserRaw";
    public static final String METHOD_GET_RELATED = "flickr.tags.getRelated";
    public static final String METHOD_GET_CLUSTER_PHOTOS = "flickr.tags.getClusterPhotos";

    public static final String PERIOD_WEEK = "week";
    public static final String PERIOD_DAY = "day";

    private String apiKey;
    private String sharedSecret;
    private Transport transportAPI;

    /**
     * Construct a TagsInterface.
     *
     * @param apiKey The API key
     * @param transportAPI The Transport interface
     */
    public TagsInterface(
        String apiKey,
        String sharedSecret,
        Transport transportAPI
    ) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transportAPI = transportAPI;
    }

    /**
     * Search for tag-clusters.<p/>
     *
     * <p>This method does not require authentication.</p>
     *
     * @since 1.2
     * @param searchTag
     * @return a list of clusters
     */
    public ClusterList getClusters(String searchTag)
      throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_CLUSTERS));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("tag", searchTag));

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        ClusterList clusters = new ClusterList();
        Element clustersElement = response.getPayload();
        NodeList clusterElements = clustersElement.getElementsByTagName("cluster");
        for (int i = 0; i < clusterElements.getLength(); i++) {
            Cluster cluster = new Cluster();
            NodeList tagElements = ((Element) clusterElements.item(i))
              .getElementsByTagName("tag");
            for (int j = 0; j < tagElements.getLength(); j++) {
                Tag tag = new Tag();
                tag.setValue(
                    ((Text) tagElements.item(j).getFirstChild()).getData()
                );
                cluster.addTag(tag);
            }
            clusters.addCluster(cluster);
        }
        return clusters;
    }

    /**
     * Returns the first 24 photos for a given tag cluster.
     *
     * <p>This method does not require authentication.</p>
     *
     * @param tag
     * @param clusterId
     * @return PhotoList
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public PhotoList getClusterPhotos(String tag, String clusterId)
      throws IOException, SAXException, FlickrException {
        PhotoList photos = new PhotoList();
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_CLUSTER_PHOTOS));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("tag", tag));
        parameters.add(new Parameter("cluster_id", clusterId));

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element photosElement = response.getPayload();
        NodeList photoNodes = photosElement.getElementsByTagName("photo");
        photos.setPage("1");
		photos.setPages("1");
		photos.setPerPage("" + photoNodes.getLength());
		photos.setTotal("" + photoNodes.getLength());
        for (int i = 0; i < photoNodes.getLength(); i++) {
            Element photoElement = (Element) photoNodes.item(i);
            photos.add(PhotoUtils.createPhoto(photoElement));
        }
        return photos;
    }

    /**
     * Returns a list of hot tags for the given period.
     *
     * <p>This method does not require authentication.</p>
     *
     * @param period valid values are 'day' or 'week'
     * @param count maximum is 200
     * @return The collection of HotlistTag objects
     */
    public Collection getHotList(String period, int count) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_HOT_LIST));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("period", period));
        parameters.add(new Parameter("count", "" + count));

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element tagsElement = response.getPayload();

        List tags = new ArrayList();
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            HotlistTag tag = new HotlistTag();
            tag.setScore(tagElement.getAttribute("score"));
            tag.setValue(((Text) tagElement.getFirstChild()).getData());
            tags.add(tag);
        }
        return tags;
    }

    /**
     * Get a list of tags for the specified photo.
     *
     * <p>This method does not require authentication.</p>
     *
     * @param photoId The photo ID
     * @return The collection of Tag objects
     */
    public Photo getListPhoto(String photoId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST_PHOTO));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("photo_id", photoId));

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element photoElement = response.getPayload();
        Photo photo = new Photo();
        photo.setId(photoElement.getAttribute("id"));

        List tags = new ArrayList();
        Element tagsElement = (Element) photoElement.getElementsByTagName("tags").item(0);
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            Tag tag = new Tag();
            tag.setId(tagElement.getAttribute("id"));
            tag.setAuthor(tagElement.getAttribute("author"));
            tag.setAuthorName(tagElement.getAttribute("authorname"));
            tag.setRaw(tagElement.getAttribute("raw"));
            tag.setValue(((Text) tagElement.getFirstChild()).getData());
            tags.add(tag);
        }
        photo.setTags(tags);
        return photo;
    }

    /**
     * Get a collection of tags used by the specified user.
     *
     * <p>This method does not require authentication.</p>
     *
     * @param userId The User ID
     * @return The User object
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getListUser(String userId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST_USER));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("user_id", userId));

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element whoElement = response.getPayload();

        List tags = new ArrayList();
        Element tagsElement = (Element) whoElement.getElementsByTagName("tags").item(0);
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            Tag tag = new Tag();
            tag.setValue(((Text) tagElement.getFirstChild()).getData());
            tags.add(tag);
        }
        return tags;
    }

    /**
     * Get a list of the user's popular tags.
     *
     * <p>This method does not require authentication.</p>
     *
     * @param userId The user ID
     * @return The collection of Tag objects
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getListUserPopular(String userId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST_USER_POPULAR));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("user_id", userId));

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element whoElement = response.getPayload();

        List tags = new ArrayList();
        Element tagsElement = (Element) whoElement.getElementsByTagName("tags").item(0);
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            Tag tag = new Tag();
            tag.setCount(tagElement.getAttribute("count"));
            tag.setValue(((Text) tagElement.getFirstChild()).getData());
            tags.add(tag);
        }
        return tags;
    }

    /**
     * Get a list of the user's (identified by token) popular tags.
     *
     * <p>This method does not require authentication.</p>
     *
     * @param tagVal a tag to search for, or null
     * @return The collection of Tag objects
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getListUserRaw(String tagVal) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST_USER_RAW));
        parameters.add(new Parameter("api_key", apiKey));

        if (tagVal != null) {
            parameters.add(new Parameter("tag", tagVal));
        }

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element whoElement = response.getPayload();

        List tags = new ArrayList();
        Element tagsElement = (Element) whoElement.getElementsByTagName("tags").item(0);
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            TagRaw tag = new TagRaw();
            tag.setClean(tagElement.getAttribute("clean"));
            NodeList rawElements = tagElement.getElementsByTagName("raw");
            for (int j = 0; j < rawElements.getLength(); j++) {
                Element rawElement = (Element) rawElements.item(j);
                tag.addRaw(((Text) rawElement.getFirstChild()).getData());
            }
            tags.add(tag);
        }
        return tags;
    }

    /**
     * Get the related tags.
     *
     * <p>This method does not require authentication.</p>
     *
     * @param tag The source tag
     * @return A RelatedTagsList object
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public RelatedTagsList getRelated(String tag) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_RELATED));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("tag", tag));

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }

        Element tagsElement = response.getPayload();

        RelatedTagsList tags = new RelatedTagsList();
        tags.setSource(tagsElement.getAttribute("source"));
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            Tag t = new Tag();
            t.setValue(XMLUtilities.getValue(tagElement));
            tags.add(t);
        }
        return tags;
    }

}
