package com.aetrion.flickr.panda;

/**
 * Holds the name of a panda.
 *
 * @author mago
 * @version $Id: Panda.java,v 1.3 2009/07/12 22:43:07 x-mago Exp $
 * @see com.aetrion.flickr.panda.PandaInterface#getPhotos(Panda, java.util.Set, int, int)
 */
public class Panda {
    private static final long serialVersionUID = 12L;

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
