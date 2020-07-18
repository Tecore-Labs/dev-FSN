package com.cpms.constants;

public interface ServiceAPIs {

    interface cims {
        String fileService = "/cims/v1/imageUpload";
    }

    interface Ccns{
        String getFollowDetails = "/follow/details";
        String getFollowerAndConnections = "/follower/connections";
        String getConnectionLevel = "/connection/level";
    }

    interface Companyinfo{
        String getCompanyDetails = "/get/companyids/details";
    }

    interface Accontinfo{
        String getAccountDetails = "/account/user/all";
    }

    interface Cnms {
        String sendNotifications = "/notification/kafka/send/notification";
    }

    interface Cgss{
        String addUpdatePost = "/cgss/post";
    }

    interface Cats {
        String authenticateAccessToken = "/access/validateToken";
    }

}