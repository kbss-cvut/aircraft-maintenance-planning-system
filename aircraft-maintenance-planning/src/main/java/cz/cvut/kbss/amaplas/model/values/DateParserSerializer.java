package cz.cvut.kbss.amaplas.model.values;

import cz.cvut.kbss.amaplas.io.SparqlDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParserSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(DateParserSerializer.class);


    // TODO - refactoring in progress - move date parsing code elsewhere
    public static final String dateFormatPattern = "yyyy-MM-dd'T'HH:mm:ss";
    public static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(dateFormatPattern);
        }
    };
    public static final String dateFormatPattern1 = "yyyy-MM-dd'T'HH:mm:ssX";
    public static final ThreadLocal<SimpleDateFormat> df = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(dateFormatPattern);
        }
    };
    public static final String dateFormatPattern2 = "dd.MM.yyyy'T'HH:mm";
    // TODO - refactoring in progress - move date parsing code elsewhere
    public static final SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");

    // TODO - refactoring in progress - move date parsing code elsewhere
    public static String formatDate(Date d){
        return formatDate(dateFormat.get(), d);
    }

    // TODO - refactoring in progress - move date parsing code elsewhere
    public static String formatDate(SimpleDateFormat sdf, Date d){
        try {
            return d != null ? sdf.format(d) : "";
        }catch (Exception e){
            LOG.error("Could not format date {}", d, e);// TODO - adhoc DEBUG try-catch
        }
        return "";
    }

    // TODO - refactoring in progress - move date parsing code elsewhere
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
}
