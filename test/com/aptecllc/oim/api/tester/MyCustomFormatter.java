

package com.aptecllc.oim.api.tester;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


public class MyCustomFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        Date now = new Date(record.getMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(now);
        StringBuilder buf = new StringBuilder();
        buf.append(formattedDate);
        buf.append(" - ");
        buf.append(record.getLoggerName());
        buf.append(" - ");
        buf.append(record.getMessage());
        buf.append(System.lineSeparator());
        return buf.toString();
    }

}
