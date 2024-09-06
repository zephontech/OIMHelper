package com.aptecllc.oim.oimutils;

import java.util.Hashtable;
import java.util.Enumeration;


public class CommandArgs extends Hashtable {
    
    
    private String _Application;

    public CommandArgs(String[] argv) {
        addArgs(argv);
    }

    
    public void addArgs(String[] argv) {
        int argc = argv.length;
        String tempKey, tempValue;
        if (argc > 0) {
            if ((argc % 2) == 0) {
                for (int i = 0; i < argc; i += 2) {
                    tempKey = argv[i];
                    tempValue = argv[i + 1];
                    if (tempKey == null || tempKey.length()==0)
                        continue;
                    if (tempKey.charAt(0) != '-') {
                        System.out.println("Invalid Arg Format for " + tempKey);

                    } else {
                        tempKey = tempKey.substring(1);  // strip off the -
                        put(tempKey, tempValue);
                    }
                }
            } else {
                System.out.println("Must have even number of arguments ");
            }
        }
    }


    public String get(String key) {
        return (String)super.get(key);
    }
    
    public String getApp() {
        return _Application;
    }
    public boolean isTest() {
        return isTrue("test");
    }
    public boolean isTrue(String key) {
        if (containsKey(key)) {
            String str = get(key).toLowerCase();
            if (str.equals("true") || str.equals("t") || str.equals("1") ) {
                return true;
            }
        }
        return false;
    }

    public String [] getArgv() {
        String [] array = new String[size() * 2];
        Enumeration en = keys();
        int i = 0;
        while(en.hasMoreElements()) {
            String key = (String)en.nextElement();
            array[i++] = "-" +key;
            array[i++] = (String)get(key);
        }

        return array;
    }

    public static String[] plusToEquals(String[] arg)
    {
        String[] ret = new String[arg.length];
        for(int i=0;i<arg.length;i++)
        {
            ret[i] = arg[i].replaceAll("\\+","=");
            System.out.println("ARG " + ret[i]);
        }
        return ret;

    }
}
