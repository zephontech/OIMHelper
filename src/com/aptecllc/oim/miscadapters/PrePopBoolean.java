/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.miscadapters;

/**
 *
 * @author fforester
 */
public class PrePopBoolean {

    
    private String booleanValue;
    
    public PrePopBoolean() {
    }

    public PrePopBoolean(String booleanValue) {
        this.booleanValue = booleanValue;
    }
    
    public void oimDummy()
    {
        
    }

    public void setBooleanValue(String booleanValue) {
        this.booleanValue = booleanValue;
    }

    public String getBooleanValue() {
        return booleanValue;
    }
    
    public String getBooleanValue(String value) {
        return value;
    }
    
    public Boolean getAsBooleanValue() {
        if (this.booleanValue == null)
            return Boolean.FALSE;
        
        if (this.booleanValue.equalsIgnoreCase("y"))
            return Boolean.TRUE;
        if (this.booleanValue.equalsIgnoreCase("yes"))
            return Boolean.TRUE;
        if (this.booleanValue.equalsIgnoreCase("true"))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }
    
    public boolean getNativeBooleanValue() {
        Boolean b = getAsBooleanValue();
        return b.booleanValue();
    }
    
     
    
}
