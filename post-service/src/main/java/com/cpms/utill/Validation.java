package com.cpms.utill;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Validation {

    @Value("${valid.image.extensions}")
    public String validImageExtensions;

    @Value("${valid.video.extensions}")
    public String validVideoExtensions;

    @Value("${valid.full.extensions}")
    public String validFullExtensions;

    /*
     * Validate any type of file extension
     *
     * */
    public boolean validateCommonFileExtension(String fileName) {

        System.out.println("");
        System.out.println("Valid Image Extensions");
        System.out.println(validImageExtensions);
        System.out.println("");
        final String FILE_PATTERN = "([^*]+(\\.(?i)(" + validFullExtensions + "))$)";

        Pattern pattern = Pattern.compile(FILE_PATTERN);

        System.out.println("File Pattern");
        System.out.println(FILE_PATTERN);

        Matcher matcher = pattern.matcher(fileName.toLowerCase());

        return matcher.matches();
    }

    /*
     * Validate image file extension
     *
     * */
    public boolean validateImageFileExtension(String fileName) {

        System.out.println("");
        System.out.println("Valid Image Extensions");
        System.out.println(validImageExtensions);
        System.out.println("");
        final String FILE_PATTERN = "([^*]+(\\.(?i)(" + validImageExtensions + "))$)";

        Pattern pattern = Pattern.compile(FILE_PATTERN);

        System.out.println("File Pattern");
        System.out.println(FILE_PATTERN);

        Matcher matcher = pattern.matcher(fileName.toLowerCase());
        return matcher.matches();
    }

    /*
     * Validate video file extension
     *
     * */
    public boolean validateVideoFileExtension(String fileName) {

        System.out.println("");
        System.out.println("Valid Image Extensions");
        System.out.println(validImageExtensions);
        System.out.println("");
        final String FILE_PATTERN = "([^*]+(\\.(?i)(" + validVideoExtensions + "))$)";

        Pattern pattern = Pattern.compile(FILE_PATTERN);

        System.out.println("File Pattern");
        System.out.println(FILE_PATTERN);

        Matcher matcher = pattern.matcher(fileName.toLowerCase());
        return matcher.matches();
    }
}
