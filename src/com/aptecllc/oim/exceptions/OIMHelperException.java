/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.exceptions;

/**
 *
 */
public class OIMHelperException extends Exception {
    
    private String externalMessage = "";

    public OIMHelperException(String message)
    {
        super(message);
    }

    public OIMHelperException(String message, Exception e)
    {
        super(message,e);
        externalMessage = e.getMessage();
    }

    public OIMHelperException(String message, long longVal)
    {
        super(message + " " + new Long(longVal).toString());
    }

    public OIMHelperException(Exception e)
    {
        super(e);
        externalMessage = e.getMessage();
    }

    public String getExternalMessage() {
        if (externalMessage == null)
            externalMessage = "";
        return externalMessage;
    }

    public void setExternalMessage(String externalMessage) {
        this.externalMessage = externalMessage;
    }
    
}
