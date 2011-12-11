/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.contacts;

import com.aetrion.flickr.util.BuddyIconable;
import com.aetrion.flickr.util.UrlUtilities;

/**
 * Class representing a Flickr contact.
 *
 * @author Anthony Eden
 * @version $Id: Contact.java,v 1.5 2009/07/12 22:43:07 x-mago Exp $
 */
public class Contact implements BuddyIconable {
	private static final long serialVersionUID = 12L;

    private String id;
    private String username;
    private String realName;
    private boolean friend;
    private boolean family;
    private boolean ignored;
    private OnlineStatus online;
    private String awayMessage;
    private int iconFarm;
    private int iconServer;

    public Contact() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public boolean isFamily() {
        return family;
    }

    public void setFamily(boolean family) {
        this.family = family;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public OnlineStatus getOnline() {
        return online;
    }

    public void setOnline(OnlineStatus online) {
        this.online = online;
    }

    /**
     * Get the contact's away message.  This method may return null if the contact online status is not 'away'.
     *
     * @return The away message or null
     */
    public String getAwayMessage() {
        return awayMessage;
    }

    public void setAwayMessage(String awayMessage) {
        this.awayMessage = awayMessage;
    }

    /**
     * Construct the BuddyIconUrl.<p>
     * If none available, return the
     * <a href="http://www.flickr.com/images/buddyicon.jpg">default</a>,
     * or an URL assembled from farm, iconserver and nsid.
     *
     * @see <a href="http://flickr.com/services/api/misc.buddyicons.html">Flickr Documentation</a>
     * @return The BuddyIconUrl
     */
    public String getBuddyIconUrl() {
        return UrlUtilities.createBuddyIconUrl(iconFarm, iconServer, id);
    }

    public int getIconFarm() {
        return iconFarm;
    }

    public void setIconFarm(int iconFarm) {
        this.iconFarm = iconFarm;
    }

    public void setIconFarm(String iconFarm) {
        setIconFarm(Integer.parseInt(iconFarm));
    }

    public int getIconServer() {
        return iconServer;
    }

    public void setIconServer(int iconServer) {
        this.iconServer = iconServer;
    }

    public void setIconServer(String iconServer) {
        setIconServer(Integer.parseInt(iconServer));
    }
}
