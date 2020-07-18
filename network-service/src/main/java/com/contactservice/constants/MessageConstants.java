package com.contactservice.constants;

import java.util.HashMap;
import java.util.Map;

public class MessageConstants {

    // MessageTags - MessageBody Map
    public static Map<String, String> messages = new HashMap<String, String>(14);
    public static Map<String, Short> messageCodes = new HashMap<String, Short>(15);

    static {

        //  Message Tag -  Messages
        messages.put(MessageTags.INVALID_INPUT, "Your input is not valid.");
        messages.put(MessageTags.DATA_NOT_FOUND, "No details are found.");
        messages.put(MessageTags.DATA_FOUND, "Details are found.");
        messages.put(MessageTags.INVALID_REQUESTER_ID, "Requester id is not valid.");
        messages.put(MessageTags.INVALID_COMPANY_ID, "Company id is not valid.");
        messages.put(MessageTags.INVALID_REQUEST_STATUS, "Request status is not valid.");
        messages.put(MessageTags.INVALID_REQUEST_TYPE, "Request type is not valid.");
        messages.put(MessageTags.CONNECTION_REQUEST_ADDED, "Connect request details are added.");
        messages.put(MessageTags.INVALID_ID, "Connection request id is not valid.");
        messages.put(MessageTags.CONNECTION_REQUEST_UPDATED, "Connection request details are updated.");
        messages.put(MessageTags.INVALID_FOLLOWER_ID, "Follower id is not valid.");
        messages.put(MessageTags.INVALID_FOLLOWING_ID, "Following id is not valid.");
        messages.put(MessageTags.INVALID_FOLLOWER_TYPE, "Follower type is not valid.");
        messages.put(MessageTags.INVALID_FOLLOW_STATUS, "Follow status is not valid.");
        messages.put(MessageTags.INVALID_CONNECTED_STATUS, "Connected status is not valid.");
        messages.put(MessageTags.INVALID_FOLLOW_ID, "Follow detail id is not valid.");
        messages.put(MessageTags.INVALID_RELATION_TYPE, "relationship type is not valid.");
        messages.put(MessageTags.CONNECTION_ADDED, "Connection is added between two companies.");
        messages.put(MessageTags.CONNECTION_UPDATED, "Connection is updated between two companies.");
        messages.put(MessageTags.INVALID_USER_ID, "User id is not valid.");
        messages.put(MessageTags.FOLLOW_DETAILS_ADDED, "Follow detail are added.");
        messages.put(MessageTags.FOLLOW_DETAILS_UPDATED, "Follow detail are updated.");
        messages.put(MessageTags.CONNECTION_REQ_EXISTS, "Connection request is already sent.");
        messages.put(MessageTags.ALREADY_FOLLOWING, "You are already following this person.");
        messages.put(MessageTags.INVALID_COMPANY_IDS, "Company id is not valid.");
        messages.put(MessageTags.BLOCK_DETAIL_ADDED, "Block details are added.");
        messages.put(MessageTags.BLOCK_DETAIL_DELETED, "Block details are deleted.");
        messages.put(MessageTags.ALREADY_BLOCKED, "you already blocked this account.");
        messages.put(MessageTags.DETAILS_ADDED, "Details are added.");
        messages.put(MessageTags.ALREADY_POKED, "You are already poking this account.");
        messages.put(MessageTags.BLOCKED_MSG, "You are blocked by this person.");


        //  Message Code -  Messages
        messageCodes.put(MessageTags.INVALID_INPUT, (short) 1);
        messageCodes.put(MessageTags.DATA_NOT_FOUND, (short) 2);
        messageCodes.put(MessageTags.INVALID_REQUESTER_ID, (short) 3);
        messageCodes.put(MessageTags.INVALID_COMPANY_ID, (short) 4);
        messageCodes.put(MessageTags.INVALID_REQUEST_STATUS, (short) 5);
        messageCodes.put(MessageTags.INVALID_REQUEST_TYPE, (short) 6);
        messageCodes.put(MessageTags.CONNECTION_REQUEST_ADDED, (short) 7);
        messageCodes.put(MessageTags.INVALID_ID, (short) 8);
        messageCodes.put(MessageTags.CONNECTION_REQUEST_UPDATED, (short) 9);
        messageCodes.put(MessageTags.DATA_FOUND, (short) 10);
        messageCodes.put(MessageTags.INVALID_FOLLOWER_ID, (short) 11);
        messageCodes.put(MessageTags.INVALID_FOLLOWING_ID, (short) 12);
        messageCodes.put(MessageTags.INVALID_FOLLOWER_TYPE, (short) 13);
        messageCodes.put(MessageTags.INVALID_FOLLOW_STATUS, (short) 14);
        messageCodes.put(MessageTags.INVALID_CONNECTED_STATUS, (short) 15);
        messageCodes.put(MessageTags.INVALID_FOLLOW_ID, (short) 16);
        messageCodes.put(MessageTags.INVALID_RELATION_TYPE, (short) 17);
        messageCodes.put(MessageTags.CONNECTION_ADDED, (short) 18);
        messageCodes.put(MessageTags.CONNECTION_UPDATED, (short) 19);
        messageCodes.put(MessageTags.INVALID_USER_ID, (short) 20);
        messageCodes.put(MessageTags.FOLLOW_DETAILS_ADDED, (short) 21);
        messageCodes.put(MessageTags.FOLLOW_DETAILS_UPDATED, (short) 22);
        messageCodes.put(MessageTags.CONNECTION_REQ_EXISTS, (short) 23);
        messageCodes.put(MessageTags.ALREADY_FOLLOWING, (short) 24);
        messageCodes.put(MessageTags.INVALID_COMPANY_IDS, (short) 25);
        messageCodes.put(MessageTags.BLOCK_DETAIL_ADDED, (short) 26);
        messageCodes.put(MessageTags.BLOCK_DETAIL_DELETED, (short) 27);
        messageCodes.put(MessageTags.ALREADY_BLOCKED, (short) 28);
        messageCodes.put(MessageTags.DETAILS_ADDED, (short) 29);
        messageCodes.put(MessageTags.ALREADY_POKED, (short) 30);
        messageCodes.put(MessageTags.BLOCKED_MSG, (short) 31);
    }

    // MESSAGE CODES
    public interface MessageTags {

        //Message Codes
        String INVALID_INPUT = "INVALID_INPUT";
        String DATA_NOT_FOUND = "DATA_NOT_FOUND";
        String DATA_FOUND = "DATA_FOUND";
        String INVALID_REQUESTER_ID = "INVALID_REQUESTER_ID";
        String INVALID_COMPANY_ID = "INVALID_COMPANY_ID";
        String INVALID_REQUEST_STATUS = "INVALID_REQUEST_STATUS";
        String INVALID_REQUEST_TYPE = "INVALID_REQUEST_TYPE";
        String CONNECTION_REQUEST_ADDED = "CONNECTION_REQUEST_ADDED";
        String INVALID_ID = "INVALID_ID"; // this is used for connection request's id
        String CONNECTION_REQUEST_UPDATED = "CONNECTION_REQUEST_UPDATED";
        String INVALID_FOLLOWER_ID = "INVALID_FOLLOWER_ID";
        String INVALID_FOLLOWING_ID = "INVALID_FOLLOWING_ID";
        String INVALID_FOLLOWER_TYPE = "INVALID_FOLLOWER_TYPE";
        String INVALID_FOLLOW_STATUS = "INVALID_FOLLOW_STATUS";
        String INVALID_CONNECTED_STATUS = "INVALID_CONNECTED_STATUS";
        String INVALID_FOLLOW_ID = "INVALID_FOLLOW_ID";
        String INVALID_RELATION_TYPE = "INVALID_RELATION_TYPE";
        String CONNECTION_ADDED = "CONNECTION_ADDED";
        String CONNECTION_UPDATED = "CONNECTION_UPDATED";
        String INVALID_USER_ID = "INVALID_USER_ID";
        String FOLLOW_DETAILS_ADDED = "FOLLOW_DETAILS_ADDED";
        String FOLLOW_DETAILS_UPDATED = "FOLLOW_DETAILS_UPDATED";
        String CONNECTION_REQ_EXISTS = "CONNECTION_REQ_EXISTS";
        String ALREADY_FOLLOWING = "ALREADY_FOLLOWING";
        String INVALID_COMPANY_IDS = "INVALID_COMPANY_IDS";
        String BLOCK_DETAIL_ADDED = "BLOCK_DETAIL_ADDED";
        String BLOCK_DETAIL_DELETED = "BLOCK_DETAIL_DELETED";
        String ALREADY_BLOCKED = "ALREADY_BLOCKED";
        String DETAILS_ADDED = "DETAILS_ADDED";
        String ALREADY_POKED = "ALREADY_POKED";
        String BLOCKED_MSG = "BLOCKED_MSG";
    }


    public interface statusCode {

        // HTTP RESPONSE STATUS CODE
        int SUCCESS_STATUS_CODE = 200;
        int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
        int CONTENT_NOT_FOUND_STATUS_CODE = 204;
        int FORBIDDEN_STATUS_CODE = 403;
        int UNAUTHORIZED_STATUS_CODE = 401;
        int BAD_REQUEST_STATUS_CODE = 400;
        int SERVICE_UNAVAILABLE_STATUS_CODE = 502;
    }

}

