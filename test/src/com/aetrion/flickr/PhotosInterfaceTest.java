/* Copyright 2004, Aetrion LLC.  All Rights Reserved. */

package com.aetrion.flickr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.people.User;
import com.aetrion.flickr.photos.Note;
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
 * @version $Id: PhotosInterfaceTest.java,v 1.20 2009/07/23 21:49:35 x-mago Exp $
 */
public class PhotosInterfaceTest extends TestCase {

    Flickr flickr = null;
    Properties properties = null;

    public void setUp() throws ParserConfigurationException, IOException, FlickrException, SAXException {

        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            properties = new Properties();
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
            auth.setPermission(Permission.WRITE); 
            requestContext.setAuth(auth);
            Flickr.debugStream = false;
            Flickr.debugRequest = false; 
        } finally {
            IOUtilities.close(in);
        }
    }

    public void testAddAndRemoveTags() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        String[] tagsToAdd = {"test"};
        iface.addTags(photoId, tagsToAdd);
        Photo photo = iface.getInfo(photoId, null);
        Collection tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(4, tags.size());

        String tagId = null;
        Iterator tagsIter = tags.iterator();
        while (tagsIter.hasNext()) {
            Tag tag = (Tag) tagsIter.next();
            if (tag.getValue().equals("test")) {
                tagId = tag.getId();
                break;
            }
        }

        iface.removeTag(tagId);
        photo = iface.getInfo(photoId, null);
        tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(3, tags.size());
    }

    public void testGetInfo() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Photo photo = iface.getInfo(properties.getProperty("photoid"), null);
        assertNotNull(photo);
        assertNotNull(photo.getUrl());
        assertNotNull(photo.getTitle());
        assertEquals("green1", photo.getTitle());
        assertEquals(properties.getProperty("photoid"), photo.getId());
        assertEquals("5edf8d0892", photo.getSecret());
        assertEquals("152", photo.getServer());
        assertEquals("1", photo.getFarm());
        assertEquals("0", photo.getLicense());
        assertEquals("jpg", photo.getOriginalFormat());
        // no pro, no original :-(
        //assertEquals("3317148c87", photo.getOriginalSecret());
        // available from InterestingnessInterface!
        assertEquals("", photo.getIconServer());
        assertEquals("", photo.getIconFarm());
        assertFalse(photo.isFavorite());
        //assertTrue(photo.getViews() > -1);

        User owner = photo.getOwner();
        assertEquals("7317713@N04", owner.getId());
        assertEquals("javatest3", owner.getUsername());
        assertEquals("", owner.getRealName());

        ArrayList tags = (ArrayList) photo.getTags();
        assertEquals("green", ((Tag) tags.get(0)).getValue());
        assertEquals("grn", ((Tag) tags.get(1)).getValue());

		ArrayList notes = (ArrayList) photo.getNotes();
        assertEquals("This region is important", ((Note) notes.get(0)).getText());
        assertEquals(
            "java.awt.Rectangle[x=154,y=41,width=70,height=76]",
            ((Note) notes.get(0)).getBounds().toString()
        );
    }

    public void testGetContactsPhotos() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        PhotoList photos = iface.getContactsPhotos(0, false, false, false);
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
        PhotosInterface iface = flickr.getPhotosInterface();
        PhotoContext photoContext = iface.getContext(properties.getProperty("photoid"));
        assertNotNull(photoContext);
    }

    public void testGetCounts() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Date[] dates = new Date[2];
        dates[0] = new Date(100000);
        dates[1] = new Date(); // now
        Collection counts = iface.getCounts(dates, null);
        assertNotNull(counts);

        Iterator countsIter = counts.iterator();
        while (countsIter.hasNext()) {
            Photocount photocount = (Photocount) countsIter.next();
            //System.out.println("count: " + photocount.getCount());
        }
    }

    public void testGetExif() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection exifs = iface.getExif(properties.getProperty("photoid"), null);
        assertNotNull(exifs);
    }

    public void testGetNotInSet() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getNotInSet(-1, -1);
        assertNotNull(photos);
    }

    public void testGetPerms() throws FlickrException, IOException, SAXException {
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
        assertEquals(5, photos.getTotal());
    }

    public void testBoundingBoxSearch() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setBBox("-122.9", "45.0", "-122.0", "45.9");
        Collection photos = iface.search(searchParameters, -1, -1);
        assertNotNull(photos);
    }

    public void testRadialGeoSearch() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setLatitude("45.521694");
        searchParameters.setLongitude("-122.691806");
        searchParameters.setRadius(2);
        searchParameters.setRadiusUnits("km");
        Collection photos = iface.search(searchParameters, -1, -1);
        assertNotNull(photos);
    }

    public void testTagSearch() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        SearchParameters searchParameters = new SearchParameters();
        String[] tags = {"flowers"};
        searchParameters.setTags(tags);
        Collection photos = iface.search(searchParameters, -1, -1);
        assertNotNull(photos);
    }


    public void testSetContentType() throws FlickrException, IOException, SAXException {
        Auth auth = flickr.getAuthInterface().checkToken(properties.getProperty("token"));
        RequestContext.getRequestContext().setAuth(auth);

        PhotosInterface iface = flickr.getPhotosInterface();
        iface.setContentType(
            properties.getProperty("photoid"),
            Flickr.CONTENTTYPE_PHOTO
        );
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
        iface.setMeta(photo.getId(), oldTitle, "Description");
    }

    public void testSetPerms() {

    }

    public void testSetSafetyLevel() throws FlickrException, IOException, SAXException {
        Auth auth = flickr.getAuthInterface().checkToken(properties.getProperty("token"));
        RequestContext.getRequestContext().setAuth(auth);

        PhotosInterface iface = flickr.getPhotosInterface();
        iface.setSafetyLevel(
            properties.getProperty("photoid"),
            Flickr.SAFETYLEVEL_SAFE,
            new Boolean(false)
        );
    }

    public void testSetTags() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");

        String[] tagsAfterRemove = {};
        iface.setTags(photoId, tagsAfterRemove);

        Photo photo = iface.getInfo(photoId, null);
        Collection tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(0, tags.size());

        String[] tagsToAdd = {"green","grn","grngrn"};
        iface.setTags(photoId, tagsToAdd);

        photo = iface.getInfo(photoId, null);
        tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(3, tags.size());

//        String tagId = null;
//        Iterator tagsIter = tags.iterator();
//        TAG_LOOP: while (tagsIter.hasNext()) {
//            Tag tag = (Tag) tagsIter.next();
//            if (tag.getValue().equals("test")) {
//                tagId = tag.getId();
//                break TAG_LOOP;
//            }
//        }

    }

    public void testGetSmallImage() throws FlickrException, IOException, SAXException {
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
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.SQUARE);
        assertNotNull(image);
        assertEquals(75, image.getWidth());
        assertEquals(75, image.getHeight());
        ImageIO.write(image, "jpg", new File("out.smallsquare.jpg"));
    }

/*    public void testGetOriginalImage() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoidOriginal");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.ORIGINAL);
        assertNotNull(image);
        assertEquals(600, image.getWidth());
        assertEquals(450, image.getHeight());
        ImageIO.write(image, "jpg", new File("out.original.jpg"));
    } */

    public void testGetMediumImage() throws FlickrException, IOException, SAXException {
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
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getInfo(photoId, null);
        BufferedImage image = iface.getImage(photo, Size.LARGE);
        assertNotNull(image);
        assertEquals(1024, image.getWidth());
        assertEquals(768, image.getHeight());
        ImageIO.write(image, "jpg", new File("out.large.jpg"));
    }

    public void testGetPhoto() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        Photo photo = iface.getPhoto(photoId, null);
        assertNotNull(photo);
    }

    /**
     * Testing the generation of URLs and the
     * overriding by setSizes().
     */
    public void testSetSizes() {
        List sizes = new ArrayList();
        Size size = new Size();
        size.setLabel("Square");
        size.setWidth("75");
        size.setHeight("75");
        size.setSource("urlSquare");
        size.setUrl("urlSquarePage");
        sizes.add(size);
        size = new Size();
        size.setLabel("Thumbnail");
        size.setWidth("100");
        size.setHeight("75");
        size.setSource("urlThumb");
        size.setUrl("urlThumbPage");
        sizes.add(size);
        size = new Size();
        size.setLabel("Small");
        size.setWidth("240");
        size.setHeight("180");
        size.setSource("urlSmall");
        size.setUrl("urlSmallPage");
        sizes.add(size);
        size = new Size();
        size.setLabel("Medium");
        size.setWidth("240");
        size.setHeight("180");
        size.setSource("urlMedium");
        size.setUrl("urlMediumPage");
        sizes.add(size);
        size = new Size();
        size.setLabel("Original");
        size.setWidth("240");
        size.setHeight("180");
        size.setSource("urlOriginal");
        size.setUrl("urlOriginalPage");
        sizes.add(size);
        size = new Size();
        size.setLabel("Large");
        size.setWidth("240");
        size.setHeight("180");
        size.setSource("urlLarge");
        size.setUrl("urlLargePage");
        sizes.add(size);

        Photo p = new Photo();
        p.setId("id");
        p.setServer("server");
        p.setSecret("secret");
        p.setOriginalSecret("osecret");
        p.setFarm("1");

        assertEquals("http://farm1.static.flickr.com/server/id_secret_m.jpg", p.getSmallUrl());
        assertEquals("http://farm1.static.flickr.com/server/id_secret_s.jpg", p.getSmallSquareUrl());
        assertEquals("http://farm1.static.flickr.com/server/id_secret_t.jpg", p.getThumbnailUrl());
        assertEquals("http://farm1.static.flickr.com/server/id_secret.jpg", p.getMediumUrl());
        assertEquals("http://farm1.static.flickr.com/server/id_secret_b.jpg", p.getLargeUrl());
        try {
            assertEquals("http://farm1.static.flickr.com/server/id_osecret_o.jpg", p.getOriginalUrl());
        } catch (FlickrException ex) {}
        // setSizes() to override the generated URLs.
        p.setSizes(sizes);
        assertEquals("urlSmall", p.getSmallUrl());
        assertEquals("urlSquare", p.getSmallSquareUrl());
        assertEquals("urlThumb", p.getThumbnailUrl());
        assertEquals("urlMedium", p.getMediumUrl());
        assertEquals("urlLarge", p.getLargeUrl());
        try {
            assertEquals("urlOriginal", p.getOriginalUrl());
        } catch (FlickrException ex) {}
    }
}
