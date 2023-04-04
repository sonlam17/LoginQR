package com.secsign.java.rest;

/**
 * Wrapping class
 * 
 * @author SecSign Technologies Inc.
 */
public class SecSignIDRESTException extends Exception {

    /**
     * serial version uid
     */
    private static final long serialVersionUID = 2654740961129412362L;
    
    /**
     * localized/i18n message
     */
    private String message;
    

    /**
     * Constructor
     * @param message
     * @param i18nMessage
     * @param errorCode
     */
    public SecSignIDRESTException(String message){
        super(message);
        
        this.message = message;
    }

    /**
     * @return the localizedMessage
     */
    public String getMessage() {
        return message;
    }

}
