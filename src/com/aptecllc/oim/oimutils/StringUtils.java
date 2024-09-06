/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.oimutils;

/**
 *
 * @author foresfr
 */
public class StringUtils {
    
    public static boolean isEmpty(String str)
    {
        if (str == null)
            return true;
        if (str.length() == 0)
            return true;
        return false;
        
    }
    
    public static boolean isBlank(String str)
    {
        if (isEmpty(str))
            return true;
        
        if (str.trim().length() == 0)
            return true;
        return false;
    }
    
    public static boolean isInteger( String input )  
    {  
       try  
       {  
          Integer.parseInt( input );  
          return true;  
       }  
       catch( Exception e)  
       {  
          return false;  
       }  
    }
    
}
