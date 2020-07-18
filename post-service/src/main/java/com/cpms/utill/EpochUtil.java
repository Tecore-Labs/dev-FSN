package com.cpms.utill;

import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

@Component
public class EpochUtil {

    public static long epoch() {
        return (int) (System.currentTimeMillis() / 1000);
    }
/*
    public static String convertEpochToDateAndTime(long epochTime) throws ParseException {

        System.out.println("");
        System.out.println("----------------");
        System.out.println("");
        System.out.println("Converting Value to Epoch");
        System.out.println("");
        System.out.println("epochTime : " + epochTime);
        System.out.println("");
        System.out.println("----------------");
        System.out.println("");

        epochTime = epochTime * 1000;

        Date date = new Date(epochTime);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        String dateString = dateFormat.format(date);
        long epoch = dateFormat.parse(dateString).getTime();

        System.out.println("");
        System.out.println("dateString");
        System.out.println(dateString);
        System.out.println("");
        System.out.println("===============");
        System.out.println("");
        System.out.println("epoch");
        System.out.println(epoch);
        System.out.println("");

        return dateString;

    }*/

    public static long convertEpoch(String date, String time, String timeZone) throws ParseException {

        System.out.println("");
        System.out.println("Converting Date To Epoch Function");
        System.out.println("");
        System.out.println("FOR TimeZone : " + timeZone);
        System.out.println("");
        System.out.println("==================================");
        System.out.println("");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(timeZone)));
        Date fullDate = simpleDateFormat.parse(date + " " + time);

        System.out.println("");
        System.out.println("Full Date : " + fullDate);
        System.out.println("");

        return fullDate.getTime() / 1000;

    }



    public static long convertEpoch(String date, String time) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date fullDate = simpleDateFormat.parse(date + " " + time);

        System.out.println("");
        System.out.println("Full Date : " + fullDate);
        System.out.println("");

        return fullDate.getTime() / 1000;

    }

    public static String convertEpochToDateAndTime(long epochTime, String timeZone) throws ParseException {
        System.out.println("");
        System.out.println("----------------");
        System.out.println("");
        System.out.println("Converting Value to Epoch");
        System.out.println("");
        System.out.println("epochTime : " + epochTime);
        System.out.println("timeZone : " + timeZone);
        System.out.println("");
        System.out.println("----------------");
        System.out.println("");

        epochTime = epochTime * 1000;

        Date date = new Date(epochTime);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(timeZone)));


        String dateString = dateFormat.format(date);
        long epoch = dateFormat.parse(dateString).getTime();

        System.out.println("");
        System.out.println("dateString");
        System.out.println(dateString);
        System.out.println("");
        System.out.println("===============");
        System.out.println("");
        System.out.println("epoch");
        System.out.println(epoch);
        System.out.println("");

        return dateString;

    }


}

