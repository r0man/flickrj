/*
 * Copyright (c) 2005 Aetrion LLC.
 */

package com.aetrion.flickr;

import java.util.ArrayList;
import java.util.List;

import com.aetrion.flickr.auth.Auth;

/**
 * A thread local variable used to hold contextual information used in requests.  To get an instance of this class use
 * RequestContext.getRequestContext().  The method will return a RequestContext object which is only usable within the
 * current thread.
 *
 * @author Anthony Eden
 */
public class RequestContext {

    private static RequestContextThreadLocal threadLocal =
            new RequestContextThreadLocal();

    private Auth auth;
    private String sharedSecret;
    private List extras;

    /**
     * Get the RequestContext instance for the current Thread.
     *
     * @return The RequestContext
     */
    public static RequestContext getRequestContext() {
        return (RequestContext) threadLocal.get();
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    /**
     * Get a shared secret which is used for any calls which require signing.
     *
     * @deprecated Get the secret from {@link Flickr#getSharedSecret()}.
     * @return The shared secret
     */
    public String getSharedSecret() {
        return sharedSecret;
    }

    /**
     * Set the shared secret which is used for any calls which require signing.
     *
     * @deprecated Set the secret in {@link Flickr#setSharedSecret(String)}.
     * @param sharedSecret The shared secret
     */
    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    /**
     * Get the List of extra return values requested.
     *
     * @return List of extra return values requested
     */
    public List getExtras() {
        if (extras == null) extras = new ArrayList();
        return extras;
    }

    public void setExtras(List extras) {
        this.extras = extras;
    }

    private static class RequestContextThreadLocal extends ThreadLocal {

        protected Object initialValue() {
            return new RequestContext();
        }

    }

}
