/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptecllc.oim.miscadapters;

/**
 *
 */
public class PrePopString {

    private String str;

    /*
     * Constructor for prepopping a String
     */
    public PrePopString()
    {

    }

    /*
     * Constructor for prepopping a String
     */
    public PrePopString(String str)
    {
        this.str = str;
    }

    /*
     * Assigned as the method for the constructor in the design console
     * if you would like to use a peristent object and run it bean like
     */
    public void oimDummy()
    {

    }

    /*
     * set the string in beanlike fasion
     */
    public void setString(String str)
    {
        this.str = str;
    }

    /*
     * get the string in beanlike fasion
     */
    public String getString()
    {
        return this.str;
    }

}
