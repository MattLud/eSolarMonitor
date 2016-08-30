package com.solartrackr.egauge.widget.util.extensions;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormatSymbols;


public class DateExtensions {

    public static String AsMonth( Date date) {
        String month = "";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        Calendar cal = Calendar.getInstance();
        cal.setTime( date);
        int monthInt = cal.get( Calendar.MONTH);
        month = months[ monthInt];

        return month;
    }

    public static String AsShortMonth( Date date) {
        String month = "";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getShortMonths();

        Calendar cal = Calendar.getInstance();
        cal.setTime( date);
        int monthInt = cal.get( Calendar.MONTH);
        month = months[ monthInt];

        return month;
    }
}
