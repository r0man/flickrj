package com.aetrion.flickr.panda;

import java.io.IOException;
import java.util.ArrayList;
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
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotoUtils;
import com.aetrion.flickr.util.StringUtilities;
import com.aetrion.flickr.util.XMLUtilities;

/**
 * Flickr Panda.
 *
 * @author mago
 * @version $Id: PandaInterface.java,v 1.3 2009/07/11 20:30:27 x-mago Exp $
 * @see <a href="http://www.flickr.com/explore/panda">Flickr Panda</a>
 */
public class PandaInterface {
    private static final String METHOD_GET_PHOTOS = "flickr.panda.getPhotos";
    private static final String METHOD_GET_LIST = "flickr.panda.getList";

    private String apiKey;
    private String sharedSecret;
    private Transport transportAPI;

    public PandaInterface(
        String apiKey,
        String sharedSecret,
        Transport transportAPI
    ) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transportAPI = transportAPI;
    }

    /**
     * Return a list of Flickr pandas, from whom you can request photos using
     * the {@link com.aetrion.flickr.panda.PandaInterface#getPhotos(Panda, Set, int, int)}
     * API method.
     *
     * This method does not require authentication.
     *
     * @return A list of pandas
     * @throws FlickrException
     * @throws IOException
     * @throws SAXException
     */
    public ArrayList getList() throws FlickrException, IOException, SAXException {
        ArrayList pandas = new ArrayList();
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST));
        parameters.add(new Parameter("api_key", apiKey));

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element pandaElement = response.getPayload();
        NodeList pandaNodes = pandaElement.getElementsByTagName("panda");
        for (int i = 0; i < pandaNodes.getLength(); i++) {
            pandaElement = (Element) pandaNodes.item(i);
            Panda panda = new Panda();
            panda.setName(XMLUtilities.getValue(pandaElement));
            pandas.add(panda);
        }
        return pandas;
    }

    /**
     * Ask the Flickr Pandas for a list of recent public (and "safe") photos.
     *
     * This method does not require authentication.
     *
     * @param panda The panda to ask for photos from.
     * @param extras A set of Strings controlling the extra information to fetch for each returned record. {@link com.aetrion.flickr.photos.Extras#ALL_EXTRAS}
     * @param perPage The number of photos to show per page
     * @param page The page offset
     * @return A PhotoList
     * @throws FlickrException
     * @throws IOException
     * @throws SAXException
     * @see com.aetrion.flickr.photos.Extras
     */
    public PhotoList getPhotos(Panda panda, Set extras, int perPage, int page) throws FlickrException, IOException, SAXException {
        ArrayList pandas = new ArrayList();
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_PHOTOS));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("panda_name", panda.getName()));

        if (extras != null && !extras.isEmpty()) {
            parameters.add(new Parameter("extras", StringUtilities.join(extras, ",")));
        }
        if (perPage > 0) {
            parameters.add(new Parameter("per_page", perPage));
        }
        if (page > 0) {
            parameters.add(new Parameter("page", page));
        }

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element photosElement = response.getPayload();
        PhotoList photos = PhotoUtils.createPhotoList(photosElement);
        return photos;
    }
}
