package com.aptecllc.oim.oimutils;


import java.text.*;

import java.util.*;


public class DateUtils {


    public static boolean isValidDateStr(String date) {
        try {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT); // YYYY-MM-DD
            df.setLenient(false);   // this is important!
            df.parse(date);
        }
        catch (ParseException e) {
            return false;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public static boolean isValidDateStr(String date, String format) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);
            sdf.parse(date);
        }
        catch (ParseException e) {
            return false;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }


    public static String currentDate(String format,String suffix)
    {

        String mysfx="";
        if (suffix != null && suffix.length() > 0)
            mysfx = suffix.trim();
        DateFormat dateFormat = new SimpleDateFormat(format);
        java.util.Date date = new java.util.Date();
        String oidDate = dateFormat.format(date);
        oidDate = oidDate + mysfx;
        return oidDate;
    }

    public static int compareDates(Date d1,Date d2)
    {
        int isequal = 0;
        int isgreater = 1;
        int isless = -1;

        if (d1.equals(d2))
            return isequal;
        else if (d1.before(d2))
            return isless;
        else
            return isgreater;
    }


    public static String convertFormat(String from,String to,String dt)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(from);
            SimpleDateFormat sdt = new SimpleDateFormat(to);
            Date old = sdf.parse(dt);
            String newDt = sdt.format(old);
            return newDt;
        }
        catch(ParseException pe)
        {
            return null;
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String format(Date date,String format)
    {
        DateFormat dateFormat = new SimpleDateFormat(format);
        String oidDate = dateFormat.format(date);
        return oidDate;
    }
    
    public static String getDateTime()
    {
        java.util.Date date = new java.util.Date(System.currentTimeMillis());
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return(df.format(date));
    }
    
    public static java.sql.Timestamp getTimeSQLStamp(String tsStr,String format)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date old = sdf.parse(tsStr);
            java.sql.Timestamp ts = new java.sql.Timestamp(old.getTime());
            return ts;
        }
        catch(ParseException pe)
        {
            return null;
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static java.util.Date getUtilDate(String tsStr,String format)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            java.util.Date dt = sdf.parse(tsStr);
            return dt;
        }
        catch(ParseException pe)
        {
            System.out.println(pe.getMessage());
            return null;
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

} // end class
