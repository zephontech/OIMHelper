

package com.aptecllc.oim.api.tester;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;


public class CustomTestLogger {

    static public class MyHandler extends StreamHandler {

        public MyHandler() {
            super(System.out, new MyCustomFormatter());
        }

        @Override
        public synchronized void publish(LogRecord record) {
            super.publish(record);
            flush();
        }

        @Override
        public synchronized void close() throws SecurityException {
            flush();
        }
    }
    
    private Logger logger;
    
    public CustomTestLogger(Class clazz)
    {
        logger = Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);
        MyHandler myHandler = new MyHandler();
        logger.addHandler(myHandler);
        logger.setLevel(Level.ALL);
    }
    
    public CustomTestLogger(String className)
    {
        logger = Logger.getLogger(className);
        logger.setUseParentHandlers(false);
        MyHandler myHandler = new MyHandler();
        logger.addHandler(myHandler);
        logger.setLevel(Level.ALL);
    }
    
    public static CustomTestLogger getLogger(Class clazz)
    {
        return new CustomTestLogger(clazz);
    }
    
    public static CustomTestLogger getLogger(String className)
    {
        return new CustomTestLogger(className);
    }
    
    public void debug(String msg)
    {
        logger.fine(msg);
    }
    
    public void info(String msg)
    {
        logger.info(msg);
    }

    public void error(String msg,Throwable e)
    {
        logger.log(Level.SEVERE, msg, e);
    }
    
    public void error(String msg)
    {
        logger.log(Level.SEVERE, msg);
    }
}
