/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.oimutils;

import Thor.API.tcResultSet;

/**
 *
 * @author fforester
 */
public class PrintResultSet {
    //DEBUG ONLY!!!!!!!
    public static void printResultSet(tcResultSet rs)
    {
        String[] headers = null;
        try
        {
            int recCount = rs.getRowCount();
            if (recCount > 0)
            {
                headers = rs.getColumnNames();
            }
            for(int i=0;i<recCount;i++)
            {
                rs.goToRow(i);
                for(String name : headers)
                {
                    System.out.println(name + ":" + rs.getStringValue(name));
                }
                
            }
        }
        catch(Exception e)
        {
            System.out.println("APIErrir");
            e.printStackTrace();
        }
    }
    
}
