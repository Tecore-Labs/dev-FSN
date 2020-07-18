package com.contactservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceUrlsUtil {

    @Value("${services.ccms.address}")
    private String COMPANY_ADDRESS;

    @Value("${services.ccms.port}")
    private String COMPANY_PORT;

    @Value("${services.cnms.address}")
    private String NOTIFICATION_ADDRESS;

    @Value("${services.cnms.port}")
    private String NOTIFICATION_PORT;

    @Value("${services.cams.address}")
    private String ACCOUNT_ADDRESS;

    @Value("${services.cams.port}")
    private String ACCOUNT_PORT;

    @Value("${services.cats.address}")
    private String AUTH_ADDRESS;

    @Value("${services.cats.port}")
    private String AUTH_PORT;

    public String getCompanyUrl() {

        if (COMPANY_PORT.equals("")) {
            return COMPANY_ADDRESS;
        }

        return COMPANY_ADDRESS + ":" + COMPANY_PORT;
    }

    public String getAccountUrl() {

        if (ACCOUNT_PORT.equals("")) {
            return ACCOUNT_ADDRESS;
        }

        return ACCOUNT_ADDRESS + ":" + ACCOUNT_PORT;
    }

    public String getNotificationUrl() {

        if (NOTIFICATION_PORT.equals("")) {
            return NOTIFICATION_ADDRESS;
        }

        return NOTIFICATION_ADDRESS + ":" + NOTIFICATION_PORT;
    }

    public String getAuthUrl() {

        if (AUTH_PORT.equals("")) {
            return AUTH_ADDRESS;
        }

        return AUTH_ADDRESS + ":" + AUTH_PORT;
    }
}
