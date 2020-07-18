package com.contactservice.controller;


import com.contactservice.constants.ContactConstants;
import com.contactservice.entity.BlockAccount;
import com.contactservice.entity.ConnectionRequest;
import com.contactservice.entity.FollowDetail;
import com.contactservice.entity.Poke;
import com.contactservice.neo4j.domain.Contact;
import com.contactservice.neo4j.domain.ContactRelationship;
import com.contactservice.neo4j.service.Neo4jContactService;
import com.contactservice.service.BlockService;
import com.contactservice.service.ContactService;
import com.contactservice.service.PokeService;
import com.contactservice.util.ContactUtil;
import com.contactservice.util.HttpResponse;
import com.contactservice.util.NotificationUtil;
import com.contactservice.util.RestTemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.contactservice.constants.ContactConstants.AccountType.COMPANY;
import static com.contactservice.constants.ContactConstants.AccountType.USER;
import static com.contactservice.constants.ContactConstants.ConnectedStatus.CONNECTED;
import static com.contactservice.constants.ContactConstants.ConnectedStatus.NOT_CONNECTED;
import static com.contactservice.constants.ContactConstants.FollowStatus.FOLLOW;
import static com.contactservice.constants.ContactConstants.FollowStatus.UNFOLLOW;
import static com.contactservice.constants.ContactConstants.NotificationTemplateTypes.*;
import static com.contactservice.constants.ContactConstants.RequestStatus.*;
import static com.contactservice.constants.ContactConstants.RequestType.*;
import static com.contactservice.constants.MessageConstants.MessageTags.*;
import static com.contactservice.constants.MessageConstants.statusCode.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ContactController {

    @Autowired
    ContactService contactService;

    @Autowired
    Neo4jContactService neo4jContactService;

    @Autowired
    RestTemplateUtil restTemplateUtil;

    @Autowired
    NotificationUtil notificationUtil;

    @Autowired
    ContactUtil contactUtil;

    @Autowired
    BlockService blockService;

    @Autowired
    PokeService pokeService;

    @PostMapping("/test")
    public BlockAccount test(@RequestBody BlockAccount name) {
        System.out.println("name");
        System.out.println(name);
        return name;
    }

    //This api will be used for adding connection request
    @PostMapping("/connection/request")
    public Map<String, Object> addConnectionRequest(@RequestParam("requester_id") String requesterId,
                                                    @RequestParam("company_id") String companyId,
                                                    @RequestParam("request_status") Short requestStatus,
                                                    @RequestParam("request_type") Short requestType,
                                                    @RequestParam("user_id") String userId,
                                                    @RequestHeader("Authorization") String token,
                                                    @RequestParam("token_type") String tokenType) {

        System.out.println("");
        System.out.println("ADD CONNECTION REQUEST");
        System.out.println("requester_id : " + requesterId);
        System.out.println("company_id : " + companyId);
        System.out.println("request_status : " + requestStatus);
        System.out.println("request_type : " + requestType);
        System.out.println("user_id : " + userId);
        System.out.println("");

        if (requesterId == null || requesterId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUESTER_ID);
        }

        if (companyId == null || companyId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        Set<String> companyIds = new HashSet<>(2);
        companyIds.add(companyId);
        companyIds.add(requesterId);

        Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, userId, tokenType);

        System.out.println("");
        System.out.println("companyDetailResponse");
        System.out.println(companyDetailResponse);
        System.out.println("");

        if ((int) companyDetailResponse.get("status") != SUCCESS_STATUS_CODE) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

        if (data.size() != companyIds.size()) {
            return HttpResponse.getResponse(FORBIDDEN_STATUS_CODE, null, INVALID_COMPANY_IDS);
        }

        if (userId == null || userId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_USER_ID);
        }

        if (requestStatus == null || !requestStatus.equals(PENDING)) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUEST_STATUS);
        }

        if (requestType == null || (!requestType.equals(CUSTOMER) && !requestType.equals(PROFESSIONAL) && !requestType.equals(VENDOR) &&
                !requestType.equals(SUPPLIER) && !requestType.equals(OTHER))) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUEST_TYPE);
        }


        BlockAccount blockAccount = blockService.getBlockDetail(companyId, requesterId);
        if (blockAccount != null) {
            return HttpResponse.getResponse(UNAUTHORIZED_STATUS_CODE, null, BLOCKED_MSG);
        }

        List<ConnectionRequest> connectionRequests = contactService.getConnectionRequestByIds(companyIds);

        System.out.println("");
        System.out.println("CONNECTION REQUEST LIST");
        System.out.println(connectionRequests);
        System.out.println("");

        if (connectionRequests != null && !connectionRequests.isEmpty()) {
            if (contactUtil.isConnectionRequestExists(connectionRequests, requestStatus)) {
                return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, CONNECTION_REQ_EXISTS);
            }
        }

        UUID connectionRequestId = UUID.randomUUID();
//        ConnectionRequest connectionRequest = new ConnectionRequest(connectionRequestId, requesterId, companyId, ContactUtil.epoch(),
//                null, requestStatus, requestType, null);

        ConnectionRequest connectionRequest = new ConnectionRequest();
        connectionRequest.setId(connectionRequestId);
        connectionRequest.setRequester_id(requesterId);
        connectionRequest.setCompany_id(companyId);
        connectionRequest.setCreation_ts(ContactUtil.epoch());
        connectionRequest.setModified_ts(Long.valueOf(0));
        connectionRequest.setRequest_status(requestStatus);
        connectionRequest.setRequest_type(requestType);
        connectionRequest.setRequest_accept_ts(Long.valueOf(0));

        contactService.addConnectionRequest(connectionRequest);

        System.out.println("");
        System.out.println("CONNECTION REQUEST ADDED");
        System.out.println(connectionRequest);
        System.out.println("");

        List<FollowDetail> followDetails = contactService.getFollowDetail(companyId, requesterId);

        System.out.println("");
        System.out.println("GET FOLLOW DETAILS RESPONSE");
        System.out.println(followDetails);
        System.out.println("");

        if (followDetails != null && !followDetails.isEmpty()) {

            if (!contactUtil.isAlreadyFollowing(followDetails, FOLLOW)) {

                Map<String, Object> updateDataMap = new HashMap<>(2);
                updateDataMap.put("status", FOLLOW);

                boolean isUpdated = contactService.updateFollowDetail(followDetails.get(0).getId(), requesterId, updateDataMap);

                System.out.println("");
                System.out.println("RESULT OF UPDATE FOLLOW DETAIL FOR ADD CONNECTION REQUEST");
                System.out.println("isUpdated : " + isUpdated);
                System.out.println("");
            }
        } else {
            UUID followDetailId = UUID.randomUUID();
            FollowDetail followDetail = new FollowDetail(followDetailId, requesterId, companyId, ContactUtil.epoch(),
                    null, COMPANY, FOLLOW, NOT_CONNECTED);

            contactService.addFollowDetail(followDetail);

            System.out.println("");
            System.out.println("FOLLOW DETAILS ADDED FOR ADD CONNECTION REQUEST");
            System.out.println(followDetail);
            System.out.println("");
        }

        System.out.println("-----------");
        System.out.println("SENDING NOTIFICATION");
        System.out.println("-----------");

        String requesterCompanyName = null;
        String requesterCompanyDes = null;
        String companyEmailId = null;
        String companyUserId = null;
        Map<String, Object> companyLogoImage = new HashMap<>(2);

        for (Map<String, Object> companyData : data) {

            if (companyData.get("company_id").equals(companyId)) {
                companyEmailId = (String) companyData.get("email_id");
                companyUserId = companyData.get("account_id").toString();
            }

            if (companyData.get("company_id").equals(requesterId)) {
                requesterCompanyName = (String) companyData.get("company_name");
                requesterCompanyDes = (String) companyData.get("description");
                companyLogoImage = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;
            }
        }

        System.out.println("requesterCompanyName : " + requesterCompanyName);
        System.out.println("requesterCompanyDes : " + requesterCompanyDes);
        System.out.println("companyEmailId : " + companyEmailId);

        // sending notification
        Map<String, Map<String, Object>> templatePayload = new HashMap<>(3);
        Map<String, Object> payloadData = new HashMap<>(4);

        payloadData.put("company_name", requesterCompanyName);
        payloadData.put("company_description", requesterCompanyDes);
        payloadData.put("email_id", companyEmailId);
//        payloadData.put("email_id", "t.chetnajoshi889@gmail.com");

        templatePayload.put(companyUserId, payloadData);

        Map<String, Map<String, Object>> backgroundPayload = new HashMap<>(3);
        Map<String, Object> bgPayloadData = new HashMap<>(4);

        bgPayloadData.put("company_id", companyId);
        bgPayloadData.put("requester_id", requesterId);
        bgPayloadData.put("id", connectionRequestId);
        bgPayloadData.put("name", requesterCompanyName);
        bgPayloadData.put("image", companyLogoImage != null ? companyLogoImage.get("image_path") : null);
        bgPayloadData.put("type", "connection"); //this is used by client to show icon for the notification

        backgroundPayload.put(companyUserId, bgPayloadData);

        Integer is_batch = 0;  //This variable is used for email in sending multiple email at one time to users.

        Map<String, Object> sendNotificationResponse = restTemplateUtil.sendNotification(userId,
                "", "", templatePayload, backgroundPayload, is_batch, CONNECTION_REQUEST_SEND);

        System.out.println("");
        System.out.println("NOTIFICATION SENT RESPONSE");
        System.out.println("");
        System.out.println(sendNotificationResponse);
        System.out.println("");

        return HttpResponse.getResponse(SUCCESS_STATUS_CODE, connectionRequest, CONNECTION_REQUEST_ADDED);
    }


    /*
       This api will be used for update status of connection request
        which can be (accepted, declined, cancel)

        * In case of accepted -> relation type is necessary to send otherwise not

        if status is accepted then it first update in connection request table
        then add it into neo4j connection table.

        if status is declined or cancel then it delete the entry from connection request table.

        it is also sending notification only when status is accepted or declined
     */
    @PutMapping("/connection/request")
    public Map<String, Object> updateConnectRequestStatus(@RequestParam("request_status") Short requestStatus,
                                                          @RequestParam("id") UUID id, // or connection request id
                                                          @RequestParam("company_id") String companyId,
                                                          @RequestParam("user_id") String userId,
                                                          @RequestParam("requester_id") String requesterId,
                                                          @RequestParam(value = "relation_type", required = false) Short relationType,
                                                          @RequestHeader("Authorization") String token,
                                                          @RequestParam("token_type") String tokenType) {

        System.out.println("");
        System.out.println("UPDATE CONNECTION REQUEST");
        System.out.println("id : " + id);
        System.out.println("request_status :" + requestStatus);
        System.out.println("company_id : " + companyId);
        System.out.println("user_id : " + userId);
        System.out.println("requester_id : " + requesterId);
        System.out.println("relation_type : " + relationType);
        System.out.println("");

        if (requesterId == null || requesterId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUESTER_ID);
        }

        if (companyId == null || companyId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (id == null || id.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_ID);
        }

        if (requestStatus == null || (!requestStatus.equals(ACCEPTED) && !requestStatus.equals(DECLINED) && !requestStatus.equals(CANCELED))) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUEST_STATUS);
        }

        if (requestStatus.equals(ACCEPTED)) {
            if (relationType == null || (!relationType.equals(CUSTOMER) && !relationType.equals(PROFESSIONAL) && !relationType.equals(VENDOR) &&
                    !relationType.equals(SUPPLIER) && !relationType.equals(OTHER))) {
                return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUEST_TYPE);
            }
        }

        Set<String> companyIds = new HashSet<>(2);
        companyIds.add(companyId);
        companyIds.add(requesterId);

        Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, userId, tokenType);

        System.out.println("");
        System.out.println("companyDetailResponse");
        System.out.println(companyDetailResponse);
        System.out.println("");

        if ((int) companyDetailResponse.get("status") != SUCCESS_STATUS_CODE) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

        if (data.size() != companyIds.size()) {
            return HttpResponse.getResponse(FORBIDDEN_STATUS_CODE, null, INVALID_COMPANY_IDS);
        }

//        if (requestStatus == null || (!requestStatus.equals(ACCEPTED) && !requestStatus.equals(PENDING) && !requestStatus.equals(DECLINED))) {
//            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUEST_STATUS);
//        }

        boolean isUpdated = false;
        if (requestStatus.equals(ACCEPTED)) {
//            Map<String, Object> updateDataMap = new HashMap<>(10);
//            updateDataMap.put("request_status", requestStatus);
//            updateDataMap.put("request_type", relationType);
//            updateDataMap.put("modified_ts", ContactUtil.epoch());
//            updateDataMap.put("request_accept_ts", ContactUtil.epoch());
//
//            isUpdated = contactService.updateConnectionRequest(id, companyId, updateDataMap);
//
//            System.out.println("");
//            System.out.println("RESPONSE OF CONNECTION REQUEST UPDATE");
//            System.out.println("isUpdated : " + isUpdated);
//            System.out.println("");

            boolean isDeleted = contactService.deleteConnectionRequest(id, companyId);

            System.out.println("");
            System.out.println("RESPONSE OF CONNECTION REQUEST DELETE");
            System.out.println("isDeleted : " + isDeleted);
            System.out.println("");

            if (isDeleted) {
                isUpdated = true; //for sending notification
                addContact(companyId, requesterId, CONNECTED, relationType, 0);
            }

//            Object addContactResult = isUpdated ? addContact(companyId, requesterId, CONNECTED, relationType, 0) : null;

        } else {

            boolean isDeleted = contactService.deleteConnectionRequest(id, companyId);

            System.out.println("");
            System.out.println("RESPONSE OF CONNECTION REQUEST DELETE");
            System.out.println("isDeleted : " + isDeleted);
            System.out.println("");

            if (requestStatus.equals(CANCELED) && isDeleted) {

                List<FollowDetail> followDetails = contactService.getFollowDetail(companyId, requesterId);

                if (followDetails == null || followDetails.isEmpty()) {
                    return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, CONNECTION_REQUEST_UPDATED);
                }

                Map<String, Object> updateDataMap = new HashMap<>(5);
                updateDataMap.put("status", UNFOLLOW);

                boolean isStatusUpdated = contactService.updateFollowDetail(followDetails.get(0).getId(), requesterId, updateDataMap);

                System.out.println("");
                System.out.println("RESULT OF UPDATE FOLLOW DETAIL STATUS TO UNFOLLOW");
                System.out.println("isStatusUpdated : " + isStatusUpdated);
                System.out.println("");

                if (isStatusUpdated) {
                    return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, CONNECTION_REQUEST_UPDATED);
                }

                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);

            } else if (!isDeleted) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            isUpdated = true;
        }

        if (isUpdated) {

            String companyName = null;
            String companyDes = null;
            String requesterEmailId = null;
            Map<String, Object> companyLogoImage = new HashMap<>(2);

            for (Map<String, Object> companyData : data) {
                if (companyData.get("company_id").equals(companyId)) {
                    companyName = (String) companyData.get("company_name");
                    companyDes = (String) companyData.get("description");
                    companyLogoImage = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;
                }

                if (companyData.get("company_id").equals(requesterId)) {
                    requesterEmailId = (String) companyData.get("email_id");
                }
            }

            System.out.println("companyName : " + companyName);
            System.out.println("companyDes : " + companyDes);
            System.out.println("requesterEmailId : " + requesterEmailId);

            // sending notification
            Map<String, Map<String, Object>> templatePayload = new HashMap<>(3);
            Map<String, Object> payloadData = new HashMap<>(4);
            payloadData.put("company_name", companyName);
            payloadData.put("company_description", companyDes);
            payloadData.put("email_id", requesterEmailId);
//            payloadData.put("email_id", "t.chetnajoshi889@gmail.com");
            templatePayload.put(userId, payloadData);

            Map<String, Map<String, Object>> backgroundPayload = new HashMap<>(3);
            Map<String, Object> bgPayloadData = new HashMap<>(4);
            bgPayloadData.put("company_id", companyId);
            bgPayloadData.put("requester_id", requesterId);
            bgPayloadData.put("id", id);
            bgPayloadData.put("name", companyName);
            bgPayloadData.put("image", companyLogoImage != null ? companyLogoImage.get("image_path") : null);
            bgPayloadData.put("type", "connection"); //this is used by client to show icon for the notification
            backgroundPayload.put(userId, bgPayloadData);

            notificationUtil.sendNotification(userId, templatePayload, backgroundPayload, requestStatus);

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, CONNECTION_REQUEST_UPDATED);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }

    /*
     This api will be used for getting connection request by requester_id
     or company_id
     or also with combination of id -> connection request id
     and can also used for getting pending request list of a requester or a company

     and this api will also used for getting status of connection btw two companies by giving is_connected => 1
     */
    @GetMapping("/connection/request")
    public Map<String, Object> getConnectionRequest(@RequestParam(value = "requester_id", required = false) String requesterId,
                                                    @RequestParam(value = "company_id", required = false) String companyId,
                                                    @RequestParam(value = "id", required = false) UUID connectionRequestId,
                                                    @RequestParam(value = "status", required = false) Short status,
                                                    @RequestParam(value = "is_connected", required = false) Short isConnected) {

        System.out.println("");
        System.out.println("GET CONNECTION REQUEST");
        System.out.println("requester_id : " + requesterId);
        System.out.println("company_id : " + companyId);
        System.out.println("id : " + connectionRequestId);
        System.out.println("status : " + status);
        System.out.println("is_connected : " + isConnected);
        System.out.println("");

        if (requesterId != null && requesterId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUESTER_ID);
        }

        if (companyId != null && companyId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (connectionRequestId != null && connectionRequestId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_ID);
        }

        if (status != null && (!status.equals(PENDING) && !status.equals(ACCEPTED) && !status.equals(DECLINED) && !status.equals(CANCELED))) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_REQUEST_STATUS);
        }

        if (isConnected != null && isConnected.equals((short) 1)) {

            if (requesterId != null && companyId != null) {

                Set<String> companyIds = new HashSet<>(2);
                companyIds.add(companyId);
                companyIds.add(requesterId);

                List<ConnectionRequest> connectionRequests = contactService.getConnectionRequestByIds(companyIds);

                System.out.println("");
                System.out.println("CONNECTION REQUEST LIST");
                System.out.println(connectionRequests);
                System.out.println("");

                if (connectionRequests != null && !connectionRequests.isEmpty()) {
                    return HttpResponse.getResponse(SUCCESS_STATUS_CODE, connectionRequests, DATA_FOUND);
                }
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        //This map is used to search in the connection request table using companyId or id
        Map<String, Object> getConnByComIdMap = new HashMap<>(5);

        if (companyId != null) {
            getConnByComIdMap.put("company_id", companyId);
        }

        if (connectionRequestId != null && companyId != null) {
            getConnByComIdMap.put("id", connectionRequestId);
        }

        System.out.println("");
        System.out.println("getConnByComIdMap");
        System.out.println(getConnByComIdMap);
        System.out.println("");

        if (!getConnByComIdMap.isEmpty()) {

            List<ConnectionRequest> connectionRequestList = new ArrayList<>(20);
            connectionRequestList = contactService.getConnectionRequest(getConnByComIdMap);

            System.out.println("");
            System.out.println("RESULT OF GET CONNECTION REQUEST BY COMPANY ID");
            System.out.println("connectionRequestList");
            System.out.println(connectionRequestList);
            System.out.println("");

            if (connectionRequestList == null || connectionRequestList.isEmpty()) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            //check status is null or not if not then filter detail on the basis of the status
            if (status != null) {
                connectionRequestList = contactUtil.getConnectionRequestByStatus(connectionRequestList, status);

                if (connectionRequestList == null || connectionRequestList.isEmpty()) {
                    return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
                }
            }

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, connectionRequestList, DATA_FOUND);
        }

        if (requesterId != null) {

            List<ConnectionRequest> connectionRequestList = contactService.getConnectionRequestByMV(connectionRequestId, requesterId);

            System.out.println("");
            System.out.println("RESULT OF GET CONNECTION REQUEST BY REQUESTER ID");
            System.out.println("connectionRequestList");
            System.out.println(connectionRequestList);
            System.out.println("");

            if (connectionRequestList.isEmpty()) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            //check status is null or not if not then filter detail on the basis of the status
            if (status != null) {

                connectionRequestList = contactUtil.getConnectionRequestByStatus(connectionRequestList, status);

                if (connectionRequestList == null || connectionRequestList.isEmpty()) {
                    return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
                }
            }

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, connectionRequestList, DATA_FOUND);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);

    }


    //This api will be used to add follow detail
    @PostMapping("/follow/detail")
    public Map<String, Object> addFollowDetail(@RequestParam("follower_id") String followerId,
                                               @RequestParam("following_id") String followingId,
                                               @RequestParam("follower_type") Short followerType,
                                               @RequestParam("status") Short status,
                                               @RequestParam("is_connected") Short isConnected,
                                               @RequestParam("user_id") String userId,
                                               @RequestHeader("Authorization") String token,
                                               @RequestParam("token_type") String token_type) {

        System.out.println("");
        System.out.println("ADD FOLLOW DETAIL");
        System.out.println("follower_id : " + followerId);
        System.out.println("following_id : " + followingId);
        System.out.println("followerType : " + followerType);
        System.out.println("status : " + status);
        System.out.println("is_connected : " + isConnected);
        System.out.println("user_id : " + userId);
        System.out.println("");

        if (followerId == null || followerId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOWER_ID);
        }

        if (followingId == null || followingId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOWING_ID);
        }

        if (followerType == null || (!followerType.equals(COMPANY) && !followerType.equals(USER))) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOWER_TYPE);
        }

        String followerName = null;
        String followingEmailId = null;
        Map<String, Object> followerImage = new HashMap<>(2);
        String companyUserId = null;

        if (followerType == USER) {

            Set<String> userIds = new HashSet<>(2);
            Set<String> fields = new HashSet<>(2);

            userIds.add(followerId);

            fields.add("first_name");
            fields.add("last_name");
            fields.add("avatar_image");

            Map<String, Object> userDetailsResponse = restTemplateUtil.getUserDetails(userIds, fields, token, userId, token_type);

            System.out.println("");
            System.out.println("userDetailsResponse");
            System.out.println(userDetailsResponse);
            System.out.println("");

            if ((int) userDetailsResponse.get("status") != SUCCESS_STATUS_CODE) {
                return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOWER_ID);
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) userDetailsResponse.get("data");

            followerName = (String) data.get(0).get("first_name") + data.get(0).get("last_name");
            followerImage = data.get(0).get("avatar_image") != null ? (Map<String, Object>) data.get(0).get("avatar_image") : null;
        }

        Set<String> companyIds = new HashSet<>(2);
        companyIds.add(followingId);

        int isFollowerCompany = 0;
        if (followerType == COMPANY) {

            isFollowerCompany = 1;
            companyIds.add(followerId);
        }

        Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, userId, token_type);

        System.out.println("");
        System.out.println("companyDetailResponse");
        System.out.println(companyDetailResponse);
        System.out.println("");

        if ((int) companyDetailResponse.get("status") != SUCCESS_STATUS_CODE) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

        if (data.size() != companyIds.size()) {
            return HttpResponse.getResponse(FORBIDDEN_STATUS_CODE, null, INVALID_COMPANY_IDS);
        }

        if (status == null || !status.equals(FOLLOW)) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOW_STATUS);
        }

        if (isConnected == null || (!isConnected.equals(CONNECTED) && !isConnected.equals(NOT_CONNECTED))) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_CONNECTED_STATUS);
        }

        BlockAccount blockAccount = blockService.getBlockDetail(followingId, followerId);
        if (blockAccount != null) {
            return HttpResponse.getResponse(UNAUTHORIZED_STATUS_CODE, null, BLOCKED_MSG);
        }

//        List<FollowDetail> followDetails = contactService.searchByFollowerIds(companyIds);
        List<FollowDetail> followDetails = contactService.getFollowDetail(followingId, followerId);

        if (followDetails != null && !followDetails.isEmpty()) {
//            if (contactUtil.isAlreadyFollowing(followDetails, status)) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, ALREADY_FOLLOWING);
//            }
        }

        UUID followDetailId = UUID.randomUUID();
        FollowDetail followDetail = new FollowDetail(followDetailId, followerId, followingId, ContactUtil.epoch(),
                null, followerType, status, isConnected);

        contactService.addFollowDetail(followDetail);

        System.out.println("");
        System.out.println("FOLLOW DETAILS ADDED");
        System.out.println(followDetail);
        System.out.println("");

        for (Map<String, Object> companyData : data) {

            if (companyData.get("company_id").equals(followingId)) {
                followingEmailId = (String) companyData.get("email_id");
                companyUserId = companyData.get("account_id").toString();
            }

            if (isFollowerCompany == 1 && companyData.get("company_id").equals(followerId)) {
                followerName = (String) companyData.get("company_name");
                followerImage = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;
            }
        }


        System.out.println("followerName : " + followerName);
        System.out.println("followingEmailId : " + followingEmailId);

        // sending notification
        Map<String,Object> followNotificationData = new HashMap<>(20);
        followNotificationData.put("followerName",followerName);
        followNotificationData.put("followingEmailId", followingEmailId);
        followNotificationData.put("followerImage",followerImage);
        followNotificationData.put("companyUserId", companyUserId);
        followNotificationData.put("id", followDetailId);
        followNotificationData.put("followingId", followingId);
        followNotificationData.put("followerId", followerId);
        followNotificationData.put("followerType", followerType.toString());

        contactUtil.sendFollowNotification(followNotificationData,userId);

        return HttpResponse.getResponse(SUCCESS_STATUS_CODE, followDetail, FOLLOW_DETAILS_ADDED);
    }


    //This api will be used for getting follow detail by using following_id or follower_id
    @GetMapping("/follow/details")
    public Map<String, Object> getFollowDetail(@RequestParam(value = "following_id", required = false) String followingId,
                                               @RequestParam(value = "follower_id", required = false) String followerId,
                                               @RequestParam(value = "fetch_size", required = false, defaultValue = "10") int fetchSize,
                                               @RequestParam(value = "page_state", required = false) String pageState,
                                               @RequestParam(value = "is_by_pagination", required = false, defaultValue = "0") Short isByPagination) {

        System.out.println("");
        System.out.println("GET FOLLOW DETAIL");
        System.out.println("following_id : " + followingId);
        System.out.println("follower_id : " + followerId);
        System.out.println("fetch_size : " + fetchSize);
        System.out.println("page_state : " + pageState);
        System.out.println("is_by_pagination : " + isByPagination);
        System.out.println("");

        if (followerId != null && followerId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOWER_ID);
        }

        if (followingId != null && followingId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOWING_ID);
        }

        if (pageState != null && pageState.equals("")) {
            System.out.println("page state in not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        if (followerId != null) {

            if (isByPagination == 1) {
                Map<String, Object> followDetailMap = contactService.searchFollowDetailByPagination(followerId, fetchSize, pageState, true);

                System.out.println("");
                System.out.println("FOLLOW DETAILS BY FOLLOWER ID");
                System.out.println(followDetailMap);
                System.out.println("");

                if (followDetailMap == null || followDetailMap.isEmpty()) {
                    return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
                }

                return HttpResponse.getResponse(SUCCESS_STATUS_CODE, followDetailMap, DATA_FOUND);
            }

            List<FollowDetail> followDetailList = contactService.searchByFollowerId(followerId);

            if (followDetailList == null || followDetailList.isEmpty()) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, followDetailList, DATA_FOUND);
        }

        if (followingId != null) {

            if (isByPagination == 1) {
                Map<String, Object> followDetailMap = contactService.searchFollowDetailByPagination(followingId, fetchSize, pageState, false);

                System.out.println("");
                System.out.println("FOLLOW DETAILS BY FOLLOWING ID");
                System.out.println(followDetailMap);
                System.out.println("");

                if (followDetailMap == null || followDetailMap.isEmpty()) {
                    return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
                }

                return HttpResponse.getResponse(SUCCESS_STATUS_CODE, followDetailMap, DATA_FOUND);
            }

            List<FollowDetail> followDetailList = contactService.searchByFollowingId(followingId);

            if (followDetailList == null || followDetailList.isEmpty()) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, followDetailList, DATA_FOUND);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }


    //This api will be used to update a particular follow detail
    @PutMapping("/follow/detail")
    public Map<String, Object> updateFollowDetail(@RequestParam("id") UUID id, // follow detail id
                                                  @RequestParam("follower_id") String followerId,
                                                  @RequestParam(value = "status", required = false) Short status,
                                                  @RequestParam(value = "is_connected", required = false) Short isConnected,
                                                  @RequestParam("account_id") String userId,
                                                  @RequestHeader("Authorization") String token,
                                                  @RequestParam("token_type") String token_type) {

        System.out.println("");
        System.out.println("UPDATING FOLLOW DETAIL");
        System.out.println("id : " + id);
        System.out.println("follower_id : " + followerId);
        System.out.println("status : " + status);
        System.out.println("is_connected : " + isConnected);
        System.out.println("token : " + token);
        System.out.println("account_id : " + userId);
        System.out.println("token_type : " + token_type);
        System.out.println("");

        if (id == null || id.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOW_ID);
        }

        if (followerId == null || id.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOWER_ID);
        }

        if (status != null && (!status.equals(FOLLOW) && !status.equals(UNFOLLOW))) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOW_STATUS);
        }

        if (isConnected != null && (!isConnected.equals(CONNECTED) && !isConnected.equals(NOT_CONNECTED))) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_CONNECTED_STATUS);
        }

        FollowDetail followDetail = contactService.getFollowDetail(followerId, id);

        if (followDetail == null) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        String followingId = followDetail.getFollowing_id();
        Short followerType = followDetail.getFollower_type();
        if (status != null && status.equals(FOLLOW)) {
            BlockAccount blockAccount = blockService.getBlockDetail(followingId, followerId);
            if (blockAccount != null) {
                return HttpResponse.getResponse(UNAUTHORIZED_STATUS_CODE, null, BLOCKED_MSG);
            }
        }

        if (status != null || isConnected != null) {

            Map<String, Object> updateDataMap = new HashMap<>(5);
            updateDataMap.put("status", status);
            updateDataMap.put("is_connected", isConnected);

            boolean isUpdated = contactService.updateFollowDetail(id, followerId, updateDataMap);

            System.out.println("");
            System.out.println("RESULT OF UPDATE FOLLOW DETAIL");
            System.out.println("isUpdated : " + isUpdated);
            System.out.println("");

            if (isUpdated) {

                if (status.equals(FOLLOW)) {
                    String followerName = null;
                    String followingEmailId = null;
                    Map<String, Object> followerImage = new HashMap<>(2);
                    String companyUserId = null;

                    if (followerType == USER) {

                        Set<String> userIds = new HashSet<>(2);
                        Set<String> fields = new HashSet<>(2);

                        userIds.add(followerId);

                        fields.add("first_name");
                        fields.add("last_name");
                        fields.add("avatar_image");

                        Map<String, Object> userDetailsResponse = restTemplateUtil.getUserDetails(userIds, fields, token, userId, token_type);

                        System.out.println("");
                        System.out.println("userDetailsResponse");
                        System.out.println(userDetailsResponse);
                        System.out.println("");

                        if ((int) userDetailsResponse.get("status") != SUCCESS_STATUS_CODE) {
                            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_FOLLOWER_ID);
                        }

                        List<Map<String, Object>> data = (List<Map<String, Object>>) userDetailsResponse.get("data");

                        followerName = (String) data.get(0).get("first_name") + data.get(0).get("last_name");
                        followerImage = data.get(0).get("avatar_image") != null ? (Map<String, Object>) data.get(0).get("avatar_image") : null;
                    }

                    Set<String> companyIds = new HashSet<>(2);
                    companyIds.add(followingId);

                    int isFollowerCompany = 0;
                    if (followerType == COMPANY) {

                        isFollowerCompany = 1;
                        companyIds.add(followerId);
                    }

                    Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, userId, token_type);

                    System.out.println("");
                    System.out.println("companyDetailResponse");
                    System.out.println(companyDetailResponse);
                    System.out.println("");

                    if ((int) companyDetailResponse.get("status") != SUCCESS_STATUS_CODE) {
                        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
                    }

                    List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

                    if (data.size() != companyIds.size()) {
                        return HttpResponse.getResponse(FORBIDDEN_STATUS_CODE, null, INVALID_COMPANY_IDS);
                    }

                    for (Map<String, Object> companyData : data) {

                        if (companyData.get("company_id").equals(followingId)) {
                            followingEmailId = (String) companyData.get("email_id");
                            companyUserId = companyData.get("account_id").toString();
                        }

                        if (isFollowerCompany == 1 && companyData.get("company_id").equals(followerId)) {
                            followerName = (String) companyData.get("company_name");
                            followerImage = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;
                        }
                    }


                    System.out.println("followerName : " + followerName);
                    System.out.println("followingEmailId : " + followingEmailId);

                    // sending notification
                    Map<String,Object> followNotificationData = new HashMap<>(20);
                    followNotificationData.put("followerName",followerName);
                    followNotificationData.put("followingEmailId", followingEmailId);
                    followNotificationData.put("followerImage",followerImage);
                    followNotificationData.put("companyUserId", companyUserId);
                    followNotificationData.put("id", id);
                    followNotificationData.put("followingId", followingId);
                    followNotificationData.put("followerId", followerId);
                    followNotificationData.put("followerType", followerType.toString());

                    contactUtil.sendFollowNotification(followNotificationData,userId);
                }
                return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, FOLLOW_DETAILS_UPDATED);
            }
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }


    //This service will be used for add or update two companies connection into neo4j database
    @PostMapping("/add/contact")
    public Map<String, Object> addContact(@RequestParam("to_company_id") String toCompanyId,
                                          @RequestParam("from_company_id") String fromCompanyId,
                                          @RequestParam(value = "status", required = false) Short status,
                                          @RequestParam(value = "relation_type", required = false) Short relationType,
                                          @RequestParam(value = "is_update", required = false, defaultValue = "0") Integer isUpdate) {

        System.out.println("");
        System.out.println("ADD CONTACT INTO NEO4J");
        System.out.println("to_company_id : " + toCompanyId);
        System.out.println("from_company_id : " + fromCompanyId);
        System.out.println("status : " + status);
        System.out.println("relation_type : " + relationType);
        System.out.println("is_update : " + isUpdate);
        System.out.println("");

        // validating the params
        if (toCompanyId == null || toCompanyId.equals("")) {
            System.out.println("toCompanyId is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (fromCompanyId == null || fromCompanyId.equals("")) {
            System.out.println("fromCompanyId is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (status != null && (!status.equals(CONNECTED) && !status.equals(NOT_CONNECTED))) {
            System.out.println("status is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_CONNECTED_STATUS);
        }

        if (relationType != null && (!relationType.equals(CUSTOMER) && !relationType.equals(PROFESSIONAL) && !relationType.equals(VENDOR) &&
                !relationType.equals(SUPPLIER) && !relationType.equals(OTHER))) {
            System.out.println("relationType is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_RELATION_TYPE);
        }

        if (isUpdate != 0 && isUpdate != 1) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        Contact toCompanyDetail = null;
        Contact fromCompanyDetail = null;

        // getting (from) and (to) company detail if exists already into the DB
        toCompanyDetail = neo4jContactService.getNodeByCompanyId(toCompanyId);
        fromCompanyDetail = neo4jContactService.getNodeByCompanyId(fromCompanyId);

        boolean isToCompanyFound = true;
        boolean isFromCompanyFound = true;

        // checking for (to) company detail found or not
        if (toCompanyDetail == null) {

            //checking if it is an update request then (to) company detail must not be null
            if (isUpdate == 1) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            // these are set when new node is going to add in the DB
            isToCompanyFound = false;
            toCompanyDetail = new Contact(toCompanyId, ContactUtil.epoch(), (long) 0);
        }

        // checking for (from) company detail found or not
        if (fromCompanyDetail == null) {

            //checking if it is an update request then (from) company detail must not be null
            if (isUpdate == 1) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            // these are set when new node is going to add in the DB
            isFromCompanyFound = false;
            fromCompanyDetail = new Contact(fromCompanyId, ContactUtil.epoch(), (long) 0);
        }


        // checking that is it is an update request call(if isUpdate value is 0 means is an add request else update)
        if (isUpdate == 0) {

            // if it is an add request then status and relationType must not be null
            if (status == null) {
                return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_CONNECTED_STATUS);
            }

            if (relationType == null) {
                return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_RELATION_TYPE);
            }

            //here, checking that is both node are connected already if it is then perform an update otherwise add these
            boolean isRelationUpdate = false;
            if (isFromCompanyFound && isToCompanyFound) {

                //getting relationship details of two nodes
                List<Contact> contactList = neo4jContactService.getRelationOfTwoNodes(toCompanyId, fromCompanyId, 1);

                //checking is detail found
                if (contactList != null && !contactList.isEmpty()) {

                    System.out.println("");
                    System.out.println("RELATION FOUND BETWEEN NODES AND GOING TO UPDATE");
                    System.out.println("");

                    //found an relationship between nodes so going to update the information
                    isRelationUpdate = true;
                    neo4jContactService.updateRelationOfTwoNodes(toCompanyId, fromCompanyId, status, relationType);
                }
            }

            //checking if nodes are not connected already
            if (!isRelationUpdate) {

                System.out.println("");
                System.out.println("ADDING CONTACT CONNECTION BETWEEN NODES");
                System.out.println("");

                //adding new nodes with contact relationship
                ContactRelationship contactRelationship = new ContactRelationship(toCompanyDetail, fromCompanyDetail);
                contactRelationship.setRelation_type(relationType);
                contactRelationship.setStatus(status);

                Set<ContactRelationship> contactRelationshipSet = new HashSet<>(2);
                contactRelationshipSet.add(contactRelationship);
                fromCompanyDetail.setConnections(contactRelationshipSet);


                String contactId = neo4jContactService.addContact(fromCompanyDetail);

                System.out.println("");
                System.out.println("CONTACT RELATION ADDED IN NEO4J");
                System.out.println(contactId);
                System.out.println("");

                Set<String> companyIds = new HashSet<>(2);
                companyIds.add(toCompanyId);
                companyIds.add(fromCompanyId);

                List<FollowDetail> followDetails = contactService.searchByFollowerIds(companyIds);

                if (followDetails != null && !followDetails.isEmpty()) {

                    System.out.println("FOLLOW DETAIL FOUND");
                    System.out.println("");

                    Map<String, Object> updateDataMap = new HashMap<>(2);
                    updateDataMap.put("is_connected", CONNECTED);

                    contactService.updateFollowDetail(followDetails.get(0).getId(),
                            followDetails.get(0).getFollower_id(), updateDataMap);
                }


                Map<String, Object> responseMap = new HashMap<String, Object>(2);
                responseMap.put("contact_id", contactId);

                return HttpResponse.getResponse(SUCCESS_STATUS_CODE, responseMap, CONNECTION_ADDED);
            }

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, CONNECTION_UPDATED);
        }

        //if it is an update request then both status and relationType must not be null (one of them should contain value going for update operation)
        if (status != null && status.equals(NOT_CONNECTED)) {

            neo4jContactService.deleteRelationOfTwoNode(fromCompanyId, toCompanyId);

            Set<String> companyIds = new HashSet<>(2);
            List<FollowDetail> followDetailList = contactService.searchByFollowerIds(companyIds);

            if (followDetailList != null && !followDetailList.isEmpty()) {

                Map<String, Object> updateDataMap = new HashMap<>(2);
                updateDataMap.put("is_connected", NOT_CONNECTED);

                boolean isFollowUpdated = contactService.updateFollowDetail(followDetailList.get(0).getId(),
                        followDetailList.get(0).getFollower_id(), updateDataMap);

                System.out.println("UPDATE RESULT OF FOLLOW DETAIL");
                System.out.println("isFollowUpdated : " + isFollowUpdated);
                System.out.println("");
            }

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, CONNECTION_UPDATED);
        }

        if (relationType != null) {

            //updating relationship properties of two nodes
            neo4jContactService.updateRelationOfTwoNodes(toCompanyId, fromCompanyId, status, relationType);
            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, CONNECTION_UPDATED);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }


    // This api is used for getting suggested contacts or can also for get contact detail by level
    @GetMapping("/contacts")
    public Map<String, Object> getSuggestion(@RequestParam("company_id") String companyId,
                                             @RequestParam(value = "level", required = false, defaultValue = "1") Integer level,
                                             @RequestParam(value = "is_by_level", defaultValue = "0", required = false) Integer isByLevel /* this param will 1 when need contact by level not suggested contact*/) {

        System.out.println("");
        System.out.println("GET CONTACT SUGGESTION");
        System.out.println("company_id : " + companyId);
        System.out.println("level : " + level);
        System.out.println("isByLevel : " + isByLevel);
        System.out.println("");

        if (companyId == null || companyId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (level != null && level.equals("")) {
            System.out.println("depth level is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        if (isByLevel != null && !isByLevel.equals(0) && !isByLevel.equals(1)) {
            System.out.println("isByLevel is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }


        if (isByLevel == 0) {
            List<Contact> contactList = neo4jContactService.getSuggestionContact(companyId, level);

            System.out.println("");
            System.out.println("SUGGESTION CONTACT LIST");
            System.out.println(contactList);
            System.out.println("");

            if (contactList == null || contactList.isEmpty()) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, contactList, DATA_FOUND);
        }

        List<Contact> contactList = neo4jContactService.getContactByDepth(companyId, level);

        System.out.println("");
        System.out.println("DEPTH VISE CONTACT LIST");
        System.out.println(contactList);
        System.out.println("");

        if (contactList == null || contactList.isEmpty()) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        contactList.removeIf(contact -> contact.getCompanyId().equals(companyId));

        return HttpResponse.getResponse(SUCCESS_STATUS_CODE, contactList, DATA_FOUND);
    }

    //This api will be used for get total follower and following of a company or a user
    @GetMapping("/follow/detail/count")
    public Map<String, Object> getFollowerAndFollowing(@RequestParam("account_id") String account_id) {

        System.out.println("");
        System.out.println("GET USER'S FOLLOWER AND FOLLOWING COUNT");
        System.out.println("account_id : " + account_id);
        System.out.println("");

        if (account_id == null || account_id.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_USER_ID);
        }

        List<FollowDetail> followerDetails = contactService.searchByFollowingId(account_id);
        List<FollowDetail> followingDetails = contactService.searchByFollowerId(account_id);

        int totalFollowers = 0;
        int totalFollowing = 0;
        if (followerDetails != null && !followerDetails.isEmpty()) {
            totalFollowers = contactUtil.getFollowCountByStatus(followerDetails);
        }

        if (followingDetails != null && !followingDetails.isEmpty()) {
            totalFollowing = contactUtil.getFollowCountByStatus(followingDetails);
        }

        Map<String, Object> responseMap = new HashMap<>(5);
//        responseMap.put("id", account_id);
        responseMap.put("totalFollowers", totalFollowers);
        responseMap.put("totalFollowing", totalFollowing);

        return HttpResponse.getResponse(SUCCESS_STATUS_CODE, responseMap, DATA_FOUND);
    }

    //This api will be used for getting connection status between two companies
    @GetMapping("/connection")
    public Map<String, Object> getConnection(@RequestParam("to_company_id") String toCompanyId,
                                             @RequestParam("from_company_id") String fromCompanyId) {

        System.out.println("");
        System.out.println("GET CONNECTION BETWEEN TWO COMPANIES");
        System.out.println("to_company_id : " + toCompanyId);
        System.out.println("from_company_id : " + fromCompanyId);
        System.out.println("");

        // validating the params
        if (toCompanyId == null || toCompanyId.equals("")) {
            System.out.println("toCompanyId is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (fromCompanyId == null || fromCompanyId.equals("")) {
            System.out.println("fromCompanyId is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        List<Contact> contactList = neo4jContactService.getRelationOfTwoNodes(toCompanyId, fromCompanyId, 1);

        System.out.println("");
        System.out.println("CONTACT LIST");
        System.out.println(contactList);
        System.out.println("");

        if (contactList != null && !contactList.isEmpty()) {
            Map<String, Object> responseMap = new HashMap<>(2);
            responseMap.put("is_connected", true);
            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, responseMap, DATA_FOUND);
        }
        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }

    @PostMapping("/block/account")
    public Map<String, Object> addBlockDetail(@RequestParam("blocker_id") String blocker_id,
                                              @RequestParam("blocked_id") String blocked_id) {

        System.out.println("");
        System.out.println("ADD BLOCK DETAIL");
        System.out.println("blocker_id : " + blocker_id);
        System.out.println("blocked_id : " + blocked_id);
        System.out.println("");

        if (blocked_id == null || blocked_id.equals("")) {
            System.out.println("blocked_id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        if (blocker_id == null || blocker_id.equals("")) {
            System.out.println("blocker_id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        BlockAccount blockAccount = blockService.getBlockDetail(blocker_id, blocked_id);

        if (blockAccount != null) {

            System.out.println("account is already blocked");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, ALREADY_BLOCKED);
        }

        //---start---removing follow detail------

        System.out.println("GOING TO DELETE FOLLOW DETAIL");
        System.out.println("");

        Set<String> companyIds = new HashSet<>(2);
        companyIds.add(blocked_id);
        companyIds.add(blocker_id);

        List<FollowDetail> followDetails = contactService.searchByFollowerIds(companyIds);

        if (followDetails != null && !followDetails.isEmpty()) {

            System.out.println("FOLLOW DETAIL FOUND");
            System.out.println("");

            boolean isDeleted = contactService.deleteFollowDetail(followDetails.get(0).getFollower_id(),
                    followDetails.get(0).getId());

            System.out.println("DELETE RESPONSE OF FOLLOW DETAIL");
            System.out.println("isDeleted : " + isDeleted);
            System.out.println("");
        }

        //-----start----removing---connection---request-----

        System.out.println("GOING TO DELETE CONNECTION REQUEST");
        System.out.println("");

        companyIds.clear();
        companyIds.add(blocked_id);
        companyIds.add(blocker_id);

        List<ConnectionRequest> connectionRequests = contactService.getConnectionRequestByIds(companyIds);

        if (connectionRequests != null && !connectionRequests.isEmpty()) {

            System.out.println("CONNECTION REQUEST FOUND");
            boolean isDeleted = contactService.deleteConnectionRequest(connectionRequests.get(0).getId(),
                    connectionRequests.get(0).getCompany_id());

            System.out.println("DELETE RESPONSE OF CONNECTION REQUEST");
            System.out.println("isDeleted : " + isDeleted);
            System.out.println("");
        }

        //-------start------removing-----connection----
        System.out.println("GOING TO DELETE CONNECTION");
        System.out.println("");

        neo4jContactService.deleteRelationOfTwoNode(blocker_id, blocked_id);

        //-------start------removing-----poke---request---
        System.out.println("GOING TO DELETE POKE REQUEST");
        System.out.println("");

        boolean isDeleted = pokeService.deletePokeDetail(blocker_id, blocked_id);

        System.out.println("DELETE RESPONSE OF POKE DETAIL");
        System.out.println("isDeleted : " + isDeleted);
        System.out.println("");

        //---adding-----block----detail----

        UUID id = UUID.randomUUID();
        BlockAccount blockAccounts = new BlockAccount(id, blocker_id, blocked_id, ContactUtil.epoch());
        blockService.addBlockDetail(blockAccounts);

        return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, BLOCK_DETAIL_ADDED);
    }

    @DeleteMapping("/remove/block")
    public Map<String, Object> removeBlockDetail(@RequestParam("blocker_id") String blocker_id,
                                                 @RequestParam("blocked_id") String blocked_id) {

        System.out.println("");
        System.out.println("REMOVE BLOCK DETAIL");
        System.out.println("blocker_id : " + blocker_id);
        System.out.println("blocked_id : " + blocked_id);
        System.out.println("");

        if (blocked_id == null || blocked_id.equals("")) {
            System.out.println("blocked_id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        if (blocker_id == null || blocker_id.equals("")) {
            System.out.println("blocker_id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        boolean isDeleted = blockService.removeBlockDetail(blocker_id, blocked_id);

        System.out.println("DELETE RESULT OF BLOCK DETAIL");
        System.out.println("isDeleted : " + isDeleted);
        System.out.println("");

        if (isDeleted) {
            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, BLOCK_DETAIL_DELETED);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }

    @GetMapping("/block/detail")
    public Map<String, Object> getBlockDetail(@RequestParam("blocker_id") String blocker_id,
                                              @RequestHeader("Authorization") String token,
                                              @RequestParam("token_type") String token_type,
                                              @RequestParam("account_id") String account_id) {

        System.out.println("");
        System.out.println("REMOVE BLOCK DETAIL");
        System.out.println("blocker_id : " + blocker_id);
        System.out.println("");

        if (blocker_id == null || blocker_id.equals("")) {
            System.out.println("blocked_id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        List<BlockAccount> blockAccountList = blockService.getBlockDetails(blocker_id);

        if (blockAccountList == null || blockAccountList.isEmpty()) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        Set<String> companyIds = new HashSet<>(blockAccountList.size());
        for (BlockAccount blockAccount : blockAccountList) {
            companyIds.add(blockAccount.getBlocked_id());
        }

        Map<String, Object> companyResponse = restTemplateUtil.getCompanyDetailByCompanyIds(companyIds, token, account_id, token_type);

        System.out.println("");
        System.out.println("RESPONSE OF COMPANY SERVICE");
        System.out.println(companyResponse);
        System.out.println("");

        if ((int) companyResponse.get("status") != SUCCESS_STATUS_CODE) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        List<Map<String, Object>> companiesDetail = (List<Map<String, Object>>) companyResponse.get("data");

        System.out.println("companiesDetail");
        System.out.println(companiesDetail);
        System.out.println("");

        if (companiesDetail != null && !companiesDetail.isEmpty()) {
            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, companiesDetail, DATA_FOUND);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }

    //This api will be used for getting follow, connection, block and poke status between two compnaies
    @GetMapping("/follow/connection/block")
    public Map<String, Object> getFollowConnectionBlockDetail(@RequestParam("requester_id") String requesterId,
                                                              @RequestParam("company_id") String companyId) {

        System.out.println("");
        System.out.println("GET FOLLOW, CONNECTION AND BLOCK DETAIL");
        System.out.println("requester_id : " + requesterId);
        System.out.println("company_id : " + companyId);
        System.out.println("");

        // validating the params
        if (requesterId == null || requesterId.equals("")) {
            System.out.println("requester_id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (companyId == null || companyId.equals("")) {
            System.out.println("company_id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        Map<String, Object> responseMap = contactUtil.getFollowConnectionBlockPokeDetail(requesterId, companyId);

        if (!responseMap.isEmpty()) {
            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, responseMap, DATA_FOUND);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }


    //This api will be used to add poke detail
    @PostMapping("/poke")
    public Map<String, Object> addPokeDetail(@RequestParam("poker_id") String pokerId,
                                             @RequestParam("poked_id") String pokedId,
                                             @RequestParam("user_id") String userId,
                                             @RequestHeader("Authorization") String token,
                                             @RequestParam("token_type") String tokenType) {

        System.out.println("");
        System.out.println("ADD POKE DETAIL");
        System.out.println("poker_id : " + pokerId);
        System.out.println("poked_id : " + pokedId);
        System.out.println("user_id : " + userId);
        System.out.println("");

        if (pokedId == null || pokedId.equals("")) {
            System.out.println("poked id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (pokerId == null || pokerId.equals("")) {
            System.out.println("poker id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        BlockAccount blockAccount = blockService.getBlockDetail(pokedId, pokerId);
        if (blockAccount != null) {
            return HttpResponse.getResponse(UNAUTHORIZED_STATUS_CODE, null, BLOCKED_MSG);
        }

        Poke pokeDetail = pokeService.getPokeDetail(pokerId, pokedId);

        if (pokeDetail != null) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, ALREADY_POKED);
        }

        Set<String> companyIds = new HashSet<>(2);
        companyIds.add(pokedId);
        companyIds.add(pokerId);

        Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, userId, tokenType);

        System.out.println("");
        System.out.println("companyDetailResponse");
        System.out.println(companyDetailResponse);
        System.out.println("");

        if ((int) companyDetailResponse.get("status") != SUCCESS_STATUS_CODE) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

        if (data.size() != companyIds.size()) {
            return HttpResponse.getResponse(FORBIDDEN_STATUS_CODE, null, INVALID_COMPANY_IDS);
        }

        if (userId == null || userId.equals("")) {
            System.out.println("user id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        UUID pokeId = UUID.randomUUID();
        Poke poke = new Poke(pokeId, pokerId, pokedId, ContactUtil.epoch(), ContactConstants.PokeStatus.PENDING);

        pokeService.addPokeDetail(poke);

        System.out.println("-----------");
        System.out.println("SENDING NOTIFICATION");
        System.out.println("-----------");

        String pokerCompanyName = null;
        String pokerCompanyDes = null;
        String companyEmailId = null;
        Map<String, Object> companyLogoImage = new HashMap<>(2);
        String companyUserId = null;

        for (Map<String, Object> companyData : data) {

            if (companyData.get("company_id").equals(pokedId)) {
                companyEmailId = (String) companyData.get("email_id");
                companyUserId = companyData.get("account_id").toString();
            }

            if (companyData.get("company_id").equals(pokerId)) {
                pokerCompanyName = (String) companyData.get("company_name");
                pokerCompanyDes = (String) companyData.get("description");
                companyLogoImage = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;
            }
        }

        System.out.println("requesterCompanyName : " + pokerCompanyName);
        System.out.println("requesterCompanyDes : " + pokerCompanyDes);
        System.out.println("companyEmailId : " + companyEmailId);

        // sending notification
        Map<String, Map<String, Object>> templatePayload = new HashMap<>(3);
        Map<String, Object> payloadData = new HashMap<>(4);

        payloadData.put("company_name", pokerCompanyName);
        payloadData.put("company_description", pokerCompanyDes);
        payloadData.put("email_id", companyEmailId);
//        payloadData.put("email_id", "t.chetnajoshi889@gmail.com");

        templatePayload.put(companyUserId, payloadData);

        Map<String, Map<String, Object>> backgroundPayload = new HashMap<>(3);
        Map<String, Object> bgPayloadData = new HashMap<>(4);

        bgPayloadData.put("poked_id", pokedId);
        bgPayloadData.put("poker_id", pokerId);
        bgPayloadData.put("id", pokeId);
        bgPayloadData.put("name", pokerCompanyName);
        bgPayloadData.put("image", companyLogoImage != null ? companyLogoImage.get("image_path") : null);
        bgPayloadData.put("type", "poke"); //this is used by client to show icon for the notification

        backgroundPayload.put(companyUserId, bgPayloadData);

        Integer is_batch = 0;  //This variable is used for email in sending multiple email at one time to users.

        Map<String, Object> sendNotificationResponse = restTemplateUtil.sendNotification(userId,
                "", "", templatePayload, backgroundPayload, is_batch, POKE_REQUEST_RECEIVED);

        System.out.println("");
        System.out.println("NOTIFICATION SENT RESPONSE");
        System.out.println(sendNotificationResponse);
        System.out.println("");

        return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, DETAILS_ADDED);

    }

    //This api will be used for updating poke status
    @PutMapping("/poke")
    public Map<String, Object> updatePokeDetail(@RequestParam("poker_id") String pokerId,
                                                @RequestParam("poked_id") String pokedId,
                                                @RequestParam("user_id") String userId,
                                                @RequestParam("status") Short status,
                                                @RequestHeader("Authorization") String token,
                                                @RequestParam("token_type") String tokenType) {

        System.out.println("");
        System.out.println("UPDATE POKE DETAIL");
        System.out.println("poker_id : " + pokerId);
        System.out.println("poked_id : " + pokedId);
        System.out.println("user_id : " + userId);
        System.out.println("status : " + status);
        System.out.println("");

        if (pokedId == null || pokedId.equals("")) {
            System.out.println("poked id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (pokerId == null || pokerId.equals("")) {
            System.out.println("poker id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        Set<String> companyIds = new HashSet<>(2);
        companyIds.add(pokedId);
        companyIds.add(pokerId);

        Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, userId, tokenType);

        System.out.println("");
        System.out.println("companyDetailResponse");
        System.out.println(companyDetailResponse);
        System.out.println("");

        if ((int) companyDetailResponse.get("status") != SUCCESS_STATUS_CODE) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

        if (data.size() != companyIds.size()) {
            return HttpResponse.getResponse(FORBIDDEN_STATUS_CODE, null, INVALID_COMPANY_IDS);
        }

        if (userId == null || userId.equals("")) {
            System.out.println("user id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (status == null || !status.equals(ContactConstants.PokeStatus.ACCEPTED) && !status.equals(ContactConstants.PokeStatus.DECLINED)) {
            System.out.println("status is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (status.equals(ContactConstants.PokeStatus.DECLINED)) {
            boolean isDeleted = pokeService.deletePokeDetail(pokerId, pokedId);

            if (isDeleted) {
                return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, DATA_FOUND);
            }
        } else {

            boolean isUpdated = pokeService.updatePokeStatus(pokerId, pokedId, status);

            if (isUpdated) {

                System.out.println("-----------");
                System.out.println("SENDING NOTIFICATION");
                System.out.println("-----------");

                String pokedCompanyName = null;
                String pokedCompanyDes = null;
                String companyEmailId = null;
                Map<String, Object> companyLogoImage = new HashMap<>(2);
                String companyUserId = null;

                for (Map<String, Object> companyData : data) {

                    if (companyData.get("company_id").equals(pokerId)) {
                        companyEmailId = (String) companyData.get("email_id");
                        companyUserId = companyData.get("account_id").toString();
                    }

                    if (companyData.get("company_id").equals(pokedId)) {
                        pokedCompanyName = (String) companyData.get("company_name");
                        pokedCompanyDes = (String) companyData.get("description");
                        companyLogoImage = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;
                    }
                }

                System.out.println("requesterCompanyName : " + pokedCompanyName);
                System.out.println("requesterCompanyDes : " + pokedCompanyDes);
                System.out.println("companyEmailId : " + companyEmailId);

                // sending notification
                Map<String, Map<String, Object>> templatePayload = new HashMap<>(3);
                Map<String, Object> payloadData = new HashMap<>(4);

                payloadData.put("company_name", pokedCompanyName);
                payloadData.put("company_description", pokedCompanyDes);
                payloadData.put("email_id", companyEmailId);
//                payloadData.put("email_id", "t.chetnajoshi889@gmail.com");

                templatePayload.put(companyUserId, payloadData);

                Map<String, Map<String, Object>> backgroundPayload = new HashMap<>(3);
                Map<String, Object> bgPayloadData = new HashMap<>(4);

                bgPayloadData.put("poked_id", pokedId);
                bgPayloadData.put("poker_id", pokerId);
                bgPayloadData.put("name", pokedCompanyName);
                bgPayloadData.put("image", companyLogoImage != null ? companyLogoImage.get("image_path") : null);
                bgPayloadData.put("type", "poke"); //this is used by client to show icon for the notification

                backgroundPayload.put(companyUserId, bgPayloadData);

                Integer is_batch = 0;  //This variable is used for email in sending multiple email at one time to users.

                Map<String, Object> sendNotificationResponse = restTemplateUtil.sendNotification(userId,
                        "", "", templatePayload, backgroundPayload, is_batch, POKE_REQUEST_ACCEPTED);

                System.out.println("");
                System.out.println("NOTIFICATION SENT RESPONSE");
                System.out.println(sendNotificationResponse);
                System.out.println("");

                return HttpResponse.getResponse(SUCCESS_STATUS_CODE, null, DATA_FOUND);
            }
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }

    //This api will be used to get poke detail by poker and poked id
    @GetMapping("/poke")
    public Map<String, Object> getPokeDetail(@RequestParam("poker_id") String pokerId,
                                             @RequestParam("poked_id") String pokedId,
                                             @RequestParam("user_id") String userId) {

        System.out.println("");
        System.out.println("GET POKE DETAIL");
        System.out.println("poker_id : " + pokerId);
        System.out.println("poked_id : " + pokedId);
        System.out.println("user_id : " + userId);
        System.out.println("");

        if (pokerId == null || pokerId.equals("")) {
            System.out.println("poker id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (pokedId == null || pokedId.equals("")) {
            System.out.println("poked id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (userId == null || userId.equals("")) {
            System.out.println("user id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        Poke poke = pokeService.getPokeDetail(pokerId, pokedId);

        if (poke != null) {
            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, poke, DATA_FOUND);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }

    //This api will be used to get poke detail on the basis of poke status
    @GetMapping("/poke/detail")
    public Map<String, Object> getPokeDetailByStatus(@RequestParam(value = "poker_id", required = false) String pokerId,
                                                     @RequestParam(value = "poked_id", required = false) String pokedId,
                                                     @RequestParam("user_id") String userId,
                                                     @RequestParam(value = "status", required = false) Short status,
                                                     @RequestHeader("Authorization") String token,
                                                     @RequestParam("token_type") String tokenType) {

        System.out.println("");
        System.out.println("GET POKE DETAIL BY STATUS");
        System.out.println("poker_id : " + pokerId);
        System.out.println("poked_id : " + pokedId);
        System.out.println("user_id : " + userId);
        System.out.println("status : " + status);
        System.out.println("");

        if (pokerId != null && pokerId.equals("")) {
            System.out.println("poker id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (pokedId != null && pokedId.equals("")) {
            System.out.println("poked id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (pokerId == null && pokedId == null) {
            System.out.println("poked id and poker id are not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        if (userId != null && userId.equals("")) {
            System.out.println("user id is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_ID);
        }

        if (status != null && !status.equals(ContactConstants.PokeStatus.PENDING) &&
                !status.equals(ContactConstants.PokeStatus.ACCEPTED) &&
                !status.equals(ContactConstants.PokeStatus.DECLINED)) {

            System.out.println("status is not valid");
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT);
        }

        if (pokedId != null) {

            System.out.println("");
            System.out.println("GETTING DETAIL BY POKED ID");
            System.out.println("");

            List<Poke> pokeListByPoked = pokeService.getPokeDetailsByPokedId(pokedId);

            System.out.println("");
            System.out.println("POKE DETAIL LIST BY POKED ID");
            System.out.println(pokeListByPoked);
            System.out.println("");

            if (pokeListByPoked.isEmpty()) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            if (status != null) {
                List<Poke> filteredPokeList = contactUtil.getPokeDetailByStatus(pokeListByPoked, status);
                if (filteredPokeList.isEmpty()) {
                    return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
                }
                return HttpResponse.getResponse(SUCCESS_STATUS_CODE, filteredPokeList, DATA_FOUND);
            }

            Set<String> pokerIds = new HashSet<>(pokeListByPoked.size());
            for (Poke poke : pokeListByPoked) {
                pokerIds.add(poke.getPoker_id());
            }

            Map<String, Object> companyResponse = restTemplateUtil.getCompanyDetailByCompanyIds(pokerIds, token, userId, tokenType);

            System.out.println("");
            System.out.println("RESPONSE OF COMPANY SERVICE");
            System.out.println(companyResponse);
            System.out.println("");

            if ((int) companyResponse.get("status") != SUCCESS_STATUS_CODE) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }

            List<Map<String, Object>> companiesDetail = (List<Map<String, Object>>) companyResponse.get("data");

            System.out.println("companiesDetail");
            System.out.println(companiesDetail);
            System.out.println("");

            if (companiesDetail != null && !companiesDetail.isEmpty()) {
//                List<Map<String, Object>> mergedPokeDetail = contactUtil.mergePokeDetailByPokerId(companiesDetail, pokeListByPoked);
//
//                System.out.println("mergedPokeDetail");
//                System.out.println(mergedPokeDetail);
//                System.out.println("");

                Map<String, Map<String, Object>> companiesDataMap = new HashMap<>(companiesDetail.size());
                for (Map<String, Object> companyMap : companiesDetail) {
                    companiesDataMap.put(companyMap.get("company_id").toString(), companyMap);
                }

                Map<String, Object> responseMap = new HashMap<>(3);
                responseMap.put("poke_details", pokeListByPoked);
                responseMap.put("company_details", companiesDataMap);

                return HttpResponse.getResponse(SUCCESS_STATUS_CODE, responseMap, DATA_FOUND);
            }
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }


        System.out.println("");
        System.out.println("GETTING DETAIL BY POKER ID");
        System.out.println("");

        List<Poke> pokeListByPoker = pokeService.getPokeDetailsByPokerId(pokerId);

        System.out.println("");
        System.out.println("POKE DETAIL LIST BY POKER ID");
        System.out.println(pokeListByPoker);
        System.out.println("");

        if (pokeListByPoker.isEmpty()) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        if (status != null) {
            List<Poke> filteredPokeList = contactUtil.getPokeDetailByStatus(pokeListByPoker, status);

            if (filteredPokeList.isEmpty()) {
                return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
            }
            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, filteredPokeList, DATA_FOUND);
        }

        Set<String> pokedIds = new HashSet<>(pokeListByPoker.size());
        for (Poke poke : pokeListByPoker) {
            pokedIds.add(poke.getPoked_id());
        }

        Map<String, Object> companyResponse = restTemplateUtil.getCompanyDetailByCompanyIds(pokedIds, token, userId, tokenType);

        System.out.println("");
        System.out.println("RESPONSE OF COMPANY SERVICE");
        System.out.println(companyResponse);
        System.out.println("");

        if ((int) companyResponse.get("status") != SUCCESS_STATUS_CODE) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        List<Map<String, Object>> companiesDetail = (List<Map<String, Object>>) companyResponse.get("data");

        System.out.println("companiesDetail");
        System.out.println(companiesDetail);
        System.out.println("");

        if (companiesDetail != null && !companiesDetail.isEmpty()) {
//            List<Map<String, Object>> mergedPokeDetail = contactUtil.mergePokeDetailByPokedId(companiesDetail, pokeListByPoker);
//
//            System.out.println("mergedPokeDetail");
//            System.out.println(mergedPokeDetail);
//            System.out.println("");

            Map<String, Map<String, Object>> companiesDataMap = new HashMap<>(companiesDetail.size());
            for (Map<String, Object> companyMap : companiesDetail) {
                companiesDataMap.put(companyMap.get("company_id").toString(), companyMap);
            }

            Map<String, Object> responseMap = new HashMap<>(3);
            responseMap.put("poke_details", pokeListByPoker);
            responseMap.put("company_details", companiesDataMap);

            return HttpResponse.getResponse(SUCCESS_STATUS_CODE, responseMap, DATA_FOUND);
        }

        return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
    }

    /*
        This api will be used for getting follower and connections of a particular company,
        in current scenario it will be called by post service in "create post api"
     */
    @GetMapping("/follower/connections")
    public Map<String, Object> getFollowerAndConnections(@RequestParam("company_id") String companyId) {

        System.out.println("");
        System.out.println("GET FOLLOWER AND CONNECTIONS");
        System.out.println("company_id : " + companyId);
        System.out.println("");

        if (companyId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        List<FollowDetail> followDetailList = contactService.searchByFollowingId(companyId);
        List<Contact> contactList = neo4jContactService.getContactByDepth(companyId, 1);

        System.out.println("");
        System.out.println("FOLLOW DETAIL LIST");
        System.out.println(followDetailList);
        System.out.println("CONTACT DETAIL LIST");
        System.out.println(contactList);
        System.out.println("");

        if (followDetailList.isEmpty() && contactList.isEmpty()) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        List<Object> responseList = new ArrayList<>(25);
        if (!followDetailList.isEmpty()) {
            responseList.addAll(followDetailList);
        }

        if (!contactList.isEmpty()) {
            contactList.removeIf(contact -> contact.getCompanyId().equals(companyId));
            responseList.addAll(contactList);
        }

        return HttpResponse.getResponse(SUCCESS_STATUS_CODE, responseList, DATA_FOUND);
    }

    //This api will be used for getting connection level of companies with respect to a single company
    @GetMapping("/connection/level")
    public Map<String, Object> getConnectionLevel(@RequestParam("from_company_id") String fromCompanyId,
                                                  @RequestParam("to_company_ids") Set<String> toCompanyIds) {

        System.out.println("GET CONNECTION LEVEL");
        System.out.println("fromCompanyId : " + fromCompanyId);
        System.out.println("toCompanyIds : " + toCompanyIds);
        System.out.println("");

        if (fromCompanyId.equals("")) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        if (toCompanyIds.isEmpty()) {
            return HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_COMPANY_ID);
        }

        Map<String, Object> connectionLevelDetails = new HashMap<>(toCompanyIds.size());
        for (String toCompanyId : toCompanyIds) {

            //getting connection detail first, because if both are connected then we go to get connection level
            List<Contact> contactList = neo4jContactService.getRelationOfTwoNodes(toCompanyId, fromCompanyId, 3);

            System.out.println("");
            System.out.println("CONTACT LIST");
            System.out.println(contactList);
            System.out.println("");

            if (contactList != null && !contactList.isEmpty()) {
                Integer connectionLevel = neo4jContactService.getConnectionLevel(fromCompanyId, toCompanyId);
                connectionLevelDetails.put(toCompanyId, connectionLevel);
            }
        }

        if (connectionLevelDetails.isEmpty()) {
            return HttpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, null, DATA_NOT_FOUND);
        }

        return HttpResponse.getResponse(SUCCESS_STATUS_CODE, connectionLevelDetails, DATA_FOUND);
    }
}

