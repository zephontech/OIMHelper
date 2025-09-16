/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tech.zephon.oim.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tech.zephon.oim.csv.CsvReader;

/**
 *
 */
public class BaseHelper {

    
    public static List<HashMap<String,String>> loadFile(String filename) throws Exception
    {
        List<HashMap<String,String>> recordMapList = new ArrayList();
        if (filename == null && filename.length() <= 0)
        {
            throw new Exception("Invalid File Name");
        }
        
        CsvReader reader = null;
        try
        {
            reader = new CsvReader(filename);
            reader.readHeaders();
            String[] fileHeaders = reader.getHeaders();

            if (fileHeaders == null || fileHeaders.length == 0)
            {
                throw new Exception("No Header Record");
            }

            
            recordMapList = new ArrayList();
            while (reader.readRecord())
            {
                HashMap recordMap = new HashMap();
                for(int i=0;i<fileHeaders.length;i++)
                {
                    String x = reader.get(fileHeaders[i]);
                    recordMap.put(fileHeaders[i],x);
                }
                recordMapList.add(recordMap);

            }
        }
        catch(FileNotFoundException fnfe)
        {
            throw new Exception("File Not Found");
        }
        catch(IOException ioe)
        {
            throw new Exception("File IO Error " + ioe.getMessage());
        }
        finally
        {
            if (reader != null)
                reader.close();
        }
        return recordMapList;
    }
    
}
