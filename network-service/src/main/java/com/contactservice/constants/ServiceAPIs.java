package com.contactservice.constants;

public interface ServiceAPIs {

    interface Cnms {
        String sendNotifications = "/notification/kafka/send/notification";
    }

    interface Cats {
        String authenticateAccessToken = "/access/validateToken";
    }

    interface Cams {
        String getUserDetails = "/account/user/all";
    }

    interface Ccms{
        String getCompanyDetails = "/company/detail";
        String getCompanyDetailByCompanyIds = "/get/companyids/details";
    }
}
