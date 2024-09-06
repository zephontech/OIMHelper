/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptecllc.oim.miscadapters;

import com.aptecllc.oim.oimutils.DateUtils;

/**
 *
 */
public class PrePopDate {

    private java.util.Date dateUtil;
    private java.sql.Date dateSql;

    /*
     * Constructor for prepopping a Date
     */
    public PrePopDate() {
    }

    /*
     * prepop a SQL Date
     */
    public PrePopDate(java.sql.Date date) {
        this.dateSql = date;
    }

    /*
     * Constructor for prepopping a Date
     */
    public PrePopDate(java.util.Date date) {
        this.dateUtil = date;
    }

    /*
     * Assigned as the method for the constructor in the design console
     * if you would like to use a peristent object and run it bean like
     */
    public void oimDummy() {
    }

    /*
     * allows using this adapter as beanlike
     */
    public void setdateUtil(java.util.Date date) {
        this.dateUtil = date;
    }

    /*
     * allows using this adapter as beanlike
     */
    public java.util.Date getdateUtil() {
        return dateUtil;
    }

    /*
     * Return the String representation of the Date object.
     * you must have called setDateUtil
     * allows using this adapter as beanlike
     */
    public String getdateUtilString() {
        if (dateUtil == null) {
            return "";
        }
        long s = dateUtil.getTime();
        if (s == 0) {
            return "";
        }
        return dateUtil.toString();
    }

    /*
     * Return the Formatted representation of the Date object.
     * you must have called setDateUtil
     * allows using this adapter as beanlike
     */
    public String getdateUtilString(String format) {
        if (dateUtil == null) {
            return "";
        }
        long s = dateUtil.getTime();
        if (s == 0) {
            return "";
        }
        return DateUtils.format(dateUtil, format);
    }

    /*
     * Set a SQL Date in beanlike fashion
     */
    public void setdateSql(java.sql.Date date) {
        this.dateSql = date;
    }

    /*
     * Get a SQL Date in beanlike fashion
     */
    public java.sql.Date getdateSql() {
        return dateSql;
    }

    /*
     * Return the String representation of the Date object.
     * you must have called setDateSql
     * allows using this adapter as beanlike
     */
    public String getdateSqlString() {
        if (dateSql == null) {
            return "";
        }
        long s = dateSql.getTime();
        if (s == 0) {
            return "";
        }
        return dateSql.toString();
    }

    /*
     * Return the Formatted representation of the Date object.
     * you must have called setDateSql
     * allows using this adapter as beanlike
     */
    public String getdateSqlString(String format) {
        if (dateSql == null) {
            return "";
        }
        long s = dateSql.getTime();
        if (s == 0) {
            return "";
        }
        return DateUtils.format(dateSql, format);
    }
}
