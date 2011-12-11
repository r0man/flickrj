/* Copyright 2004, Aetrion LLC.  All Rights Reserved. */

package com.aetrion.flickr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.photos.Permissions;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoContext;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.Photocount;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;
import com.aetrion.flickr.photos.Size;
import com.aetrion.flickr.tags.Tag;
import com.aetrion.flickr.util.IOUtilities;

/**
 * @author Anthony Eden
 */
public class PhotosInterfaceSOAPTest extends TestCase {

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

    public void testGetInfo() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Photo photo = iface.getInfo(properties.getProperty("photoid"), null);
        assertNotNull(photo);
        System.out.println("photo id: " + photo.getId());
    }

    public void testAddAndRemoveTags() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        String[] tagsToAdd = {"test"};
        iface.addTags(photoId, tagsToAdd);
        Photo photo = iface.getInfo(photoId, null);
        Collection tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(1, tags.size());

        String tagId = null;
        Iterator tagsIter = tags.iterator();
        TAG_LOOP: while (tagsIter.hasNext()) {
            Tag tag = (Tag) tagsIter.next();
            if (tag.getValue().equals("test")) {
                tagId = tag.getId();
                break TAG_LOOP;
            }
        }

        iface.removeTag(tagId);
    }

    public void testGetContactsPhotos() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getContactsPhotos(0, false, false, false);
        assertNotNull(photos);
        assertTrue(photos.size() > 0);
    }

    public void testGetContactsPublicPhotos() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getContactsPublicPhotos(properties.getProperty("nsid"), 0, false, false, false);
        assertNotNull(photos);
        assertTrue(photos.size() > 0);
    }

    public void testGetContext() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        PhotoContext photoContext = iface.getContext(properties.getProperty("photoid"));
        assertNotNull(photoContext);
    }

    public void testGetCounts() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        Date[] dates = new Date[2];
        dates[0] = new Date(100000);
        dates[1] = new Date(); // now
        Collection counts = iface.getCounts(dates, null);
        assertNotNull(counts);

        Iterator countsIter = counts.iterator();
        while (countsIter.hasNext()) {
            Photocount photocount = (Photocount) countsIter.next();
            System.out.println("count: " + photocount.getCount());
        }
    }

    public void testGetExif() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection exifs = iface.getExif(properties.getProperty("photoid"), null);
        assertNotNull(exifs);
    }

    public void testGetNotInSet() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getNotInSet(-1, -1);
        assertNotNull(photos);
    }

    public void testGetPerms() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        Permissions perms = iface.getPerms(properties.getProperty("photoid"));
        assertNotNull(perms);
    }

    public void testGetRecent() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getRecent(0, 0);
        assertNotNull(photos);
    }

    public void testGetSizes() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection sizes = iface.getSizes(properties.getProperty("photoid"));
        assertNotNull(sizes);
    }

    public void testGetUntagged() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getUntagged(0, 0);
        assertNotNull(photos);
    }

    public void testSearch() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        SearchParameters searchParams = new SearchParameters();
        searchParams.setUserId(properties.getProperty("nsid"));
        PhotoList photos = iface.search(searchParams, 0, 0);
        assertNotNull(photos);
        assertEquals(1, photos.getPage());
        assertEquals(1, photos.getPages());
        assertEquals(100, photos.getPerPage());
        assertEquals(3, photos.getTotal());
    }

    public void testTagSearch() throws FlickrException, IOException, SAXException {
//        RequestContext requestContext = RequestContext.getRequestContext();
//        requestContext.setAuthentication(authentication);
        PhotosInterface iface = flickr.getPhotosInterface();
        SearchParameters searchParameters = new SearchParameters();
        String[] tags = {"flowers"};
        searchParameters.setTags(tags);
        Collection photos = iface.search(searchParameters, -1, -1);
        assertNotNull(photos);

    }

    public void testSetDates() {

    }

    public void testSetMeta() throws FlickrException, IOException, SAXException {
        Auth auth = flickr.getAuthInterface().checkToken(properties.getProperty("token"));
        RequestContext.getRequestContext().setAuth(auth);

        String newTitle = "New Title";
        PhotosInterface iface = flickr.getPhotosInterface();
        Photo photo = iface.getInfo(properties.getProperty("photoid"), null);
        String oldTitle = photo.getTitle();
        photo.setTitle(newTitle);
        iface.setMeta(photo.getId(), photo.getTitle(), null);
        Photo updatedPhoto = iface.getInfo(properties.getProperty("photoid"), null);
        assertEquals(newTitle, updatedPhoto.getTitle());
        iface.setMeta(photo.getId(), oldTitle, null);
    }

    public void testSetPerms() {

    }

    public void testSetTags() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        String[] tagsToAdd = {"test"};
        iface.setTags(photoId, tagsToAdd);

        Photo photo = iface.getInfo(photoId, null);
        Collection tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(1, tags.size());

//        String tagId = null;
//        Iterator tagsIter = tags.iterator();
//        TAG_LOOP: while (tagsIter.hasNext()) {
//            Tag tag = (Tag) tagsIter.next();
//            if (tag.getValue().equals("test")) {
//                tagId = tag.getId();
//                break TAG_LOOP;
//            }
//        }

        String[] tagsAfterRemove = {};
        iface.setTags(photoId, tagsAfterRemove);

        photo = iface.getInfo(photoId, null);
        tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(0, tags.size());
    }

    public void testGetSmallImage() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.SMALL);
        assertNotNull(image);
        assertEquals(240, image.getWidth());
        assertEquals(180, image.getHeight());
//        System.out.println("Image width: " + image.getWidth());
//        System.out.println("Image height: " + image.getHeight());
        ImageIO.write(image, "jpg", new File("out.small.jpg"));
    }

    public void testGetThumbnailImage() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.THUMB);
        assertNotNull(image);
        assertEquals(100, image.getWidth());
        assertEquals(75, image.getHeight());
        ImageIO.write(image, "jpg", new File("out.thumbnail.jpg"));
    }

    public void testGetSmallSquareImage() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.SQUARE);
        assertNotNull(image);
        assertEquals(75, image.getWidth());
        assertEquals(75, image.getHeight());
        ImageIO.write(image, "jpg", new File("out.smallsquare.jpg"));
    }

    public void testGetOriginalImage() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.ORIGINAL);
        assertNotNull(image);
        assertEquals(800, image.getWidth());
        assertEquals(600, image.getHeight());
        ImageIO.write(image, "jpg", new File("out.original.jpg"));
    }

    public void testGetMediumImage() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.MEDIUM);
        assertNotNull(image);
        assertEquals(500, image.getWidth());
        assertEquals(375, image.getHeight());
        ImageIO.write(image, "jpg", new File("out.medium.jpg"));
    }

    public void testGetLargeImage() throws FlickrException, IOException, SAXException {
        RequestContext requestContext = RequestContext.getRequestContext();
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.LARGE);
        assertNotNull(image);
        assertEquals(500, image.getWidth());
        assertEquals(375, image.getHeight());
        ImageIO.write(image, "jpg", new File("out.large.jpg"));
    }
}
