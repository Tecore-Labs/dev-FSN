package com.contactservice.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ContactConstants {

    Short CONNECTION_LEVEL_LENGTH = 3;

    interface RequestStatus {
        short PENDING = 0;
        short ACCEPTED = 1;
        short DECLINED = 2;
        short CANCELED = 3;
    }

    interface RequestType {
        Short CUSTOMER = 1;
        Short PROFESSIONAL = 2;
        Short VENDOR = 3;
        Short SUPPLIER = 4;
        Short OTHER = 5;
    }

    interface MaterializedViews {
        String CONNECTION_REQUEST_MV = "connection_by_requester_id";
        String FOLLOW_DETAIL_MV = "follow_detail_by_following_id";
        String POKE_MV = "poke_detail_by_poked_id";
    }

    interface AccountType {
        Short USER = 1;
        Short COMPANY = 2;
    }

    interface FollowStatus {
        Short FOLLOW = 1;
        Short UNFOLLOW = 0;
    }

    interface ConnectedStatus {
        Short CONNECTED = 1;
        Short NOT_CONNECTED = 0;
    }

    interface PokeStatus {
        Short PENDING = 0;
        Short ACCEPTED = 1;
        Short DECLINED = 2;
    }

    interface NotificationTemplateTypes {

        String CONNECTION_REQUEST_SEND = "connection_request_receive";
        String CONNECTION_REQUEST_ACCEPTED = "connection_request_accepted";
        String CONNECTION_REQUEST_DENIED = "connection_request_denied";
        String FOLLOW_REQUEST_SEND = "follow_request_receive";
        String POKE_REQUEST_RECEIVED = "poke_request_received";
        String POKE_REQUEST_ACCEPTED = "poke_request_accepted";
    }

    interface InterceptorApiUrls {
        List<String> API_URLS = new ArrayList<>(Arrays.asList("/connection/*", "/follow/*", "/add/contact", "/contacts", "/connection", "/block/account", "/remove/block", "/block/detail", "/poke", "/poke/detail", "/follower/connections", "/connection/level"));
    }

}
