package com.example.sachvault.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    
    private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat(Constants.DATE_FORMAT_ISO, Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(Constants.TIME_FORMAT_24, Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_TIME_FORMAT = new SimpleDateFormat(Constants.TIME_FORMAT_DISPLAY, Locale.getDefault());
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    
    /**
     * Get current date in ISO format (yyyy-MM-dd)
     */
    public static String getCurrentDateISO() {
        return ISO_DATE_FORMAT.format(new Date());
    }
    
    /**
     * Get current time in 24-hour format (HH:mm)
     */
    public static String getCurrentTime24() {
        return TIME_FORMAT.format(new Date());
    }
    
    /**
     * Format date for display (MMM dd, yyyy)
     */
    public static String formatDateForDisplay(String isoDate) {
        try {
            Date date = ISO_DATE_FORMAT.parse(isoDate);
            return DISPLAY_DATE_FORMAT.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }
    
    /**
     * Format time for display (h:mm a)
     */
    public static String formatTimeForDisplay(String time24) {
        try {
            Date time = TIME_FORMAT.parse(time24);
            return DISPLAY_TIME_FORMAT.format(time);
        } catch (ParseException e) {
            return time24;
        }
    }
    
    /**
     * Get month pattern for database queries (yyyy-MM%)
     */
    public static String getMonthPattern(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return format.format(cal.getTime()) + "%";
    }
    
    /**
     * Get current month pattern (yyyy-MM%)
     */
    public static String getCurrentMonthPattern() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return format.format(new Date()) + "%";
    }
    
    /**
     * Get month name for display (September 2026)
     */
    public static String getMonthYearDisplay(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        
        return MONTH_FORMAT.format(cal.getTime());
    }
    
    /**
     * Get formatted month-year string (e.g., "September 2026")
     */
    public static String getMonthYearString(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1); // Calendar.MONTH is 0-based, our month is 1-based
        calendar.set(Calendar.YEAR, year);
        
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return formatter.format(calendar.getTime());
    }
    
    /**
     * Get current month and year
     */
    public static int[] getCurrentMonthYear() {
        Calendar cal = Calendar.getInstance();
        return new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)};
    }
    
    /**
     * Add months to current date
     */
    public static int[] addMonths(int year, int month, int monthsToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.add(Calendar.MONTH, monthsToAdd);
        
        return new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)};
    }
    
    /**
     * Check if date is today
     */
    public static boolean isToday(String isoDate) {
        return getCurrentDateISO().equals(isoDate);
    }
    
    /**
     * Check if date is yesterday
     */
    public static boolean isYesterday(String isoDate) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = ISO_DATE_FORMAT.format(cal.getTime());
        return yesterday.equals(isoDate);
    }
    
    /**
     * Get relative date string (Today, Yesterday, or formatted date)
     */
    public static String getRelativeDateString(String isoDate) {
        if (isToday(isoDate)) {
            return "Today";
        } else if (isYesterday(isoDate)) {
            return "Yesterday";
        } else {
            return formatDateForDisplay(isoDate);
        }
    }
    
    /**
     * Convert date picker values to ISO date string
     */
    public static String datePickerToISO(int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        return ISO_DATE_FORMAT.format(cal.getTime());
    }
    
    /**
     * Convert time picker values to 24-hour time string
     */
    public static String timePickerTo24Hour(int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        
        return TIME_FORMAT.format(cal.getTime());
    }
    
    /**
     * Parse ISO date to Calendar
     */
    public static Calendar parseISODate(String isoDate) {
        try {
            Date date = ISO_DATE_FORMAT.parse(isoDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (ParseException e) {
            return Calendar.getInstance(); // Return current date if parsing fails
        }
    }
    
    /**
     * Parse 24-hour time to Calendar
     */
    public static Calendar parse24HourTime(String time24) {
        try {
            Date time = TIME_FORMAT.parse(time24);
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            return cal;
        } catch (ParseException e) {
            return Calendar.getInstance(); // Return current time if parsing fails
        }
    }
}