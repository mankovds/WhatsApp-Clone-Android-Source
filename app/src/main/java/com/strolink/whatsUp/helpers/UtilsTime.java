package com.strolink.whatsUp.helpers;

import android.annotation.SuppressLint;
import android.content.Context;

import com.strolink.whatsUp.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 6/20/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class UtilsTime {

    public static long getStringMillis(String str) {
        return new DateTime(str).getMillis();
    }

    /**
     * method to get the correct date
     *
     * @param date this is the parameter for  getCorrectDate  method
     * @return it is return value
     */
    public static DateTime getCorrectDate(String date) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        return DateTimeFormat.forPattern(pattern)
                .parseDateTime(date)
                .withZone(DateTimeZone.getDefault());
    }


    /**
     * method to convert Date to String
     *
     * @param context this is the first parameter for convertDateToString
     * @param date    convertDateToString
     * @return date string
     */
    public static String convertDateToString(Context context, DateTime date) {
        return reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));
    }

    public static String convertMessageDateToStringFormat(Context context, DateTime date) {


        return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                String formatedDate;

                formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));
                subscriber.onNext(formatedDate);
                subscriber.onComplete();
            } catch (Exception throwable) {
                subscriber.onError(throwable);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    public static String convertDateToStringFormatLastSeen(Context context, DateTime date) {


        return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                int time_dd = date.getDayOfMonth();
                int time_MM = date.getMonthOfYear();
                String formatedDate;

                //Current time
                Calendar now = Calendar.getInstance();
                String nowMonth = (String) android.text.format.DateFormat.format("MM", now); //06
                String nowDay = (String) android.text.format.DateFormat.format("dd", now); //29

                int c_dd = Integer.parseInt(nowDay);
                int c_MM = Integer.parseInt(nowMonth);
                if (time_MM == c_MM) {
                    if (time_dd == c_dd)
                        formatedDate = context.getResources().getString(R.string.date_format_today)  +" "+  reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));
                    else if (time_dd == c_dd - 1)
                        formatedDate = context.getResources().getString(R.string.date_format_yesterday)  +" "+  reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));
                    else
                        formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format)) +" "+ reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));
                } else
                    formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format))  +" "+  reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));
                subscriber.onNext(formatedDate);
                subscriber.onComplete();
            } catch (Exception throwable) {
                subscriber.onError(throwable);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    public static String convertDateToStringFormat(Context context, DateTime date) {


        return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                int time_dd = date.getDayOfMonth();
                int time_MM = date.getMonthOfYear();
                String formatedDate;

                //Current time
                Calendar now = Calendar.getInstance();
                String nowMonth = (String) android.text.format.DateFormat.format("MM", now); //06
                String nowDay = (String) android.text.format.DateFormat.format("dd", now); //29

                int c_dd = Integer.parseInt(nowDay);
                int c_MM = Integer.parseInt(nowMonth);
                if (time_MM == c_MM) {
                    if (time_dd == c_dd)
                        formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));
                    else if (time_dd == c_dd - 1)
                        formatedDate = context.getResources().getString(R.string.date_format_yesterday);
                    else
                        formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format));
                } else
                    formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format));
                subscriber.onNext(formatedDate);
                subscriber.onComplete();
            } catch (Exception throwable) {
                subscriber.onError(throwable);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }


    @SuppressLint("CheckResult")
    public static String convertDateToStringHeader(Context context, DateTime date) {


        return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {

                String formatedDate;
                int time_dd = date.getDayOfMonth();
                int time_MM = date.getMonthOfYear();

                //Current time
                Calendar now = Calendar.getInstance();
                String nowMonth = (String) android.text.format.DateFormat.format("MM", now); //06
                String nowDay = (String) android.text.format.DateFormat.format("dd", now); //29

                int c_dd = Integer.parseInt(nowDay);
                int c_MM = Integer.parseInt(nowMonth);
                if (time_MM == c_MM) {
                    if (time_dd == c_dd)
                        formatedDate = context.getResources().getString(R.string.date_format_today);
                    else if (time_dd == c_dd - 1)
                        formatedDate = context.getResources().getString(R.string.date_format_yesterday);
                    else
                        formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format));
                } else
                    formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format));
                subscriber.onNext(formatedDate);
                subscriber.onComplete();
            } catch (Exception throwable) {
                subscriber.onError(throwable);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    private static String reformatCurrentDate(DateTime mDate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(mDate.toDate());
    }


    /**
     * Function to convert milliseconds time to
     * Timer Format
     */
    public static String getFileTime(long milliseconds) {
        String TimerString = "";
        String secondsString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            TimerString = hours + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        TimerString = TimerString + minutes + ":" + secondsString;

        return TimerString;
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration this is the first parameter for  getProgressPercentage
     * @param totalDuration   this is the second parameter for  getProgressPercentage
     */
    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage;
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        percentage = (((double) currentSeconds) / totalSeconds) * 100;
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      this is the first parameter for  progressToTimer
     * @param totalDuration returns current duration in milliseconds
     */
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration;
        totalDuration = (int) totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);
        return currentDuration * 1000;
    }

    public static String formatDate(Date date, Context context) {
        DateFormat finalDataFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        DateFormat finalTimeFormat;

        if (android.text.format.DateFormat.is24HourFormat(context)) {
            finalTimeFormat = new SimpleDateFormat("HH:mm");
        } else {
            finalTimeFormat = new SimpleDateFormat("hh:mm a");
        }

        String finalData = finalDataFormat.format(date);
        String finalTime = finalTimeFormat.format(date);
        return finalData + " " + finalTime;
    }


    public static String convertStoryDateToStringFormat(Context context, DateTime date) {


        return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            try {
                int time_dd = date.getDayOfMonth();
                int time_MM = date.getMonthOfYear();
                String formatedDate = reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));


                //Current time
                Calendar now = Calendar.getInstance();
                String nowMonth = (String) android.text.format.DateFormat.format("MM", now); //06
                String nowDay = (String) android.text.format.DateFormat.format("dd", now); //29

                int c_dd = Integer.parseInt(nowDay);
                int c_MM = Integer.parseInt(nowMonth);
                if (time_MM == c_MM) {
                    if (time_dd == c_dd)
                        formatedDate = context.getResources().getString(R.string.date_format_today) + ", " + reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));
                    else if (time_dd == c_dd - 1)
                        formatedDate = context.getResources().getString(R.string.date_format_yesterday) + ", " + reformatCurrentDate(date, context.getResources().getString(R.string.date_format_today_hour));

                }
                subscriber.onNext(formatedDate);
                subscriber.onComplete();
            } catch (Exception throwable) {
                subscriber.onError(throwable);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }


    public static String getCurrentISOTime() {
        return ISODateTimeFormat.dateTime().print(new DateTime());
    }


}
