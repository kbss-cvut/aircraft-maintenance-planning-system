package cz.cvut.kbss.amaplas.model.values;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DateUtils.class);


    public static final String dateTimeFormatPattern = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String dateFormatPattern = "yyyy-MM-dd";
    public static final String timeFormatPattern = "HH:mm:ss";


    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);

    public static final ThreadLocal<SimpleDateFormat> ltDateTimeFormatter = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(dateTimeFormatPattern);
        }
    };

    public static final ThreadLocal<SimpleDateFormat> ltDateFormatter = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(dateFormatPattern);
        }
    };

    public static final ThreadLocal<SimpleDateFormat> ltTimeFormatter = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(timeFormatPattern);
        }
    };

    public static String formatDateTime(Date d){
        return formatDateTime(ltDateTimeFormatter.get(), d);
    }

    public static String formatDate(Date d){
        return formatDateTime(ltDateFormatter.get(), d);
    }

    public static String formatTime(Date d){
        return formatDateTime(ltTimeFormatter.get(), d);
    }

    public static String formatDate(LocalDate d){
        return dateFormatter.format(d);
    }

    public static String formatDateTime(SimpleDateFormat sdf, Date d){
        try {
            return d != null ? sdf.format(d) : "";
        }catch (Exception e){
            LOG.error("Could not format date {}", d, e);// TODO - adhoc DEBUG try-catch
        }
        return "";
    }

    public static Date parseDate(SimpleDateFormat df, String dateTimeString){
        if(dateTimeString.isBlank())
            return null;

        try {
            return df.parse(dateTimeString);
        } catch (ParseException | NumberFormatException e) {
            LOG.warn("Could not parse date time string \"{}\"", dateTimeString, e);
        }
        return null;
    }

    public static Date toDateAtTime(LocalDate lDate, int hours, int minutes){
        Date d = Date.from(lDate.atStartOfDay( ZoneId.systemDefault()).toInstant());
        d.setTime(d.getTime() + hours * 3600*1000 + minutes*60*1000);
        return d;
    }
}
