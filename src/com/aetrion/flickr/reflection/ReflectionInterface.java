/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.reflection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.auth.AuthUtilities;
import com.aetrion.flickr.util.XMLUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Interface for testing the complete implementation of all Flickr-methods.<p>
 *
 * @author Anthony Eden
 * @version $Id: ReflectionInterface.java,v 1.10 2008/01/28 23:01:45 x-mago Exp $
 */
public class ReflectionInterface {

    public static final String METHOD_GET_METHOD_INFO = "flickr.reflection.getMethodInfo";
    public static final String METHOD_GET_METHODS     = "flickr.reflection.getMethods";

    private String apiKey;
    private String sharedSecret;
    private Transport transport;

    /**
     * Construct a ReflectionInterface.
     *
     * @param apiKey The API key
     * @param sharedSecret The Shared Secret
     * @param transport The Transport interface
     */
    public ReflectionInterface(
        String apiKey,
        String sharedSecret,
        Transport transport
    ) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transport = transport;
    }

    /**
     * Get the info for the specified method.
     *
     * @param methodName The method name
     * @return The Method object
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Method getMethodInfo(String methodName) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_METHOD_INFO));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("method_name", methodName));
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

        Element methodElement = response.getPayload();
        Method method = new Method();
        method.setName(methodElement.getAttribute("name"));
        method.setNeedsLogin("1".equals(methodElement.getAttribute("needslogin")));
        method.setNeedsSigning("1".equals(methodElement.getAttribute("needssigning")));
        String requiredPermsStr = methodElement.getAttribute("requiredperms");
        if (requiredPermsStr != null && requiredPermsStr.length() > 0) {
            try {
                int perms = Integer.parseInt(requiredPermsStr);
                method.setRequiredPerms(perms);
            } catch (NumberFormatException e) {
                // what shall we do?
                e.printStackTrace();
            }
        }
        method.setDescription(XMLUtilities.getChildValue(methodElement, "description"));
        method.setResponse(XMLUtilities.getChildValue(methodElement, "response"));
        method.setExplanation(XMLUtilities.getChildValue(methodElement, "explanation"));

        List arguments = new ArrayList();
        Element argumentsElement = XMLUtilities.getChild(methodElement, "arguments");
        // tolerant fix for incorrect nesting of the <arguments> element
        // as observed in current flickr responses of this method
        //
        // specified as 
        // <rsp>
        //	<method>
        //   <arguments>
        //   <errors>
        //  <method>
        // </rsp>
        //
        // observed as
        // <rsp>
        //  <method>
        //  <arguments>
        //  <errors>
        // </rsp>
        //
        if (argumentsElement == null) {
        	//System.err.println("getMethodInfo: Using workaround for arguments array");
            Element parent = (Element)methodElement.getParentNode();
            Element child = XMLUtilities.getChild(parent, "arguments");
            if (child != null) {
            	argumentsElement = child;
            }
        }
        NodeList argumentElements = argumentsElement.getElementsByTagName("argument");
        for (int i = 0; i < argumentElements.getLength(); i++) {
            Argument argument = new Argument();
            Element argumentElement = (Element) argumentElements.item(i);
            argument.setName(argumentElement.getAttribute("name"));
            argument.setOptional("1".equals(argumentElement.getAttribute("optional")));
            argument.setDescription(XMLUtilities.getValue(argumentElement));
            arguments.add(argument);
        }
        method.setArguments(arguments);

        Element errorsElement = XMLUtilities.getChild(methodElement, "errors");
        // tolerant fix for incorrect nesting of the <errors> element
        // as observed in current flickr responses of this method
        // as of 2006-09-15
        //
        // specified as 
        // <rsp>
        //	<method>
        //   <arguments>
        //   <errors>
        //  <method>
        // </rsp>
        //
        // observed as
        // <rsp>
        //  <method>
        //  <arguments>
        //  <errors>
        // </rsp>
        //
        if (errorsElement == null) {
           	//System.err.println("getMethodInfo: Using workaround for errors array");
            Element parent = (Element)methodElement.getParentNode();
            Element child = XMLUtilities.getChild(parent, "errors");
            if (child != null) {
            	errorsElement = child;
            }
        }
        List errors = new ArrayList();
        NodeList errorElements = errorsElement.getElementsByTagName("error");
        for (int i = 0; i < errorElements.getLength(); i++) {
            Error error = new Error();
            Element errorElement = (Element) errorElements.item(i);
            error.setCode(errorElement.getAttribute("code"));
            error.setMessage(errorElement.getAttribute("message"));
            error.setExplaination(XMLUtilities.getValue(errorElement));
            errors.add(error);
        }
        method.setErrors(errors);

        return method;
    }

    /**
     * Get a list of all methods.
     *
     * @return The method names
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getMethods() throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_METHODS));
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

        Element methodsElement = response.getPayload();

        List methods = new ArrayList();
        NodeList methodElements = methodsElement.getElementsByTagName("method");
        for (int i = 0; i < methodElements.getLength(); i++) {
            Element methodElement = (Element) methodElements.item(i);
            methods.add(XMLUtilities.getValue(methodElement));
        }
        return methods;
    }

}
