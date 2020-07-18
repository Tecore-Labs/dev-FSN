package com.contactservice.util;

import com.contactservice.constants.ContactConstants;
import com.contactservice.entity.BlockAccount;
import com.contactservice.entity.ConnectionRequest;
import com.contactservice.entity.FollowDetail;
import com.contactservice.entity.Poke;
import com.contactservice.neo4j.domain.Contact;
import com.contactservice.neo4j.service.Neo4jContactService;
import com.contactservice.service.BlockService;
import com.contactservice.service.ContactService;
import com.contactservice.service.PokeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.contactservice.constants.ContactConstants.FollowStatus.FOLLOW;
import static com.contactservice.constants.ContactConstants.NotificationTemplateTypes.FOLLOW_REQUEST_SEND;

@Component
public class ContactUtil {

    @Autowired
    ContactService contactService;

    @Autowired
    BlockService blockService;

    @Autowired
    Neo4jContactService neo4jContactService;

    @Autowired
    PokeService pokeService;

    @Autowired
    RestTemplateUtil restTemplateUtil;

    public static Long epoch() {
        return (System.currentTimeMillis() / 1000);
    }

    public boolean isConnectionRequestExists(List<ConnectionRequest> connectionRequests, Short requestStatus) {

        for (ConnectionRequest connectionRequest : connectionRequests) {
            if (connectionRequest.getRequest_status().equals(requestStatus)) {
                return true;
            }
        }

        return false;
    }

    public boolean isAlreadyFollowing(List<FollowDetail> followDetails, Short status) {

        for (FollowDetail followDetail : followDetails) {
            if (followDetail.getStatus().equals(status)) {
                return true;
            }
        }
        return false;
    }

    public List<ConnectionRequest> getConnectionRequestByStatus(List<ConnectionRequest> connectionRequestList, Short status) {
        List<ConnectionRequest> filteredList = new ArrayList<>(connectionRequestList.size());

        for (ConnectionRequest connectionRequest : connectionRequestList) {
            if (connectionRequest.getRequest_status().equals(status)) {
                filteredList.add(connectionRequest);
            }
        }

        return filteredList;
    }

    // This method will be used to get follow, connection and block detail of two companies
    public Map<String, Object> getFollowConnectionBlockPokeDetail(String requesterId, String companyId) {

        Map<String, Object> returnedMap = new HashMap<>(10);

        //getting block detail
        Set<String> userIds = new HashSet<>(2);
        userIds.add(requesterId);
        userIds.add(companyId);
        List<BlockAccount> blockAccount = blockService.getBlockDetail(userIds);

        System.out.println("");
        System.out.println("BLOCK DETAIL");
        System.out.println(blockAccount);
        System.out.println("");

        if (blockAccount != null && !blockAccount.isEmpty()) {

            if (blockAccount.get(0).getBlocker_id().equals(requesterId)){
                returnedMap.put("is_blocker",true);
            }else{
                returnedMap.put("is_blocker",false);
            }
            returnedMap.put("is_blocked", 1);
            return returnedMap; //return if blocked account because if account is blocked then there is no connection and follow detail in database
        }

        //getting follow detail
        List<FollowDetail> followDetails = contactService.getFollowDetail(companyId, requesterId);

        System.out.println("");
        System.out.println("FOLLOW DETAIL");
        System.out.println(followDetails);
        System.out.println("");

        if (followDetails != null && !followDetails.isEmpty()) {
            returnedMap.put("follow_status", followDetails.get(0).getStatus());
            returnedMap.put("follow_id", followDetails.get(0).getId());
        }

        Set<String> companyIds = new HashSet<>(2);
        companyIds.add(companyId);
        companyIds.add(requesterId);

        //getting connection request
        List<ConnectionRequest> connectionRequests = contactService.getConnectionRequestByIds(companyIds);

        System.out.println("");
        System.out.println("CONNECTION REQUEST LIST");
        System.out.println(connectionRequests);
        System.out.println("");

        if (connectionRequests != null && !connectionRequests.isEmpty()) {
            returnedMap.put("is_connected", connectionRequests.get(0).getRequest_status());
            returnedMap.put("connection_id", connectionRequests.get(0).getId());

        }else {
            //getting connection detail
            List<Contact> contactList = neo4jContactService.getRelationOfTwoNodes(requesterId, companyId, 1);

            System.out.println("");
            System.out.println("CONTACT LIST");
            System.out.println(contactList);
            System.out.println("");

            if (contactList != null && !contactList.isEmpty()) {
                returnedMap.put("is_connected", ContactConstants.ConnectedStatus.CONNECTED);
            }
        }

        //getting poke detail
        Poke poke = pokeService.getPokeDetail(requesterId, companyId);

        System.out.println("POKE DETAIL");
        System.out.println(poke);
        System.out.println("");

        if (poke != null){
            returnedMap.put("is_poked", poke.getStatus());
        }

        return returnedMap;
    }

    public List<Poke> getPokeDetailByStatus(List<Poke> pokeList, Short status) {
        List<Poke> filteredList = new ArrayList<>(pokeList.size());

        for (Poke poke : pokeList) {
            if (poke.getStatus().equals(status)) {
                filteredList.add(poke);
            }
        }

        return filteredList;
    }

    //not in use
    public ArrayList<Map<String, Object>> mergePokeDetailByPokerId(List<Map<String, Object>> companyDetailList, List<Poke> pokeList) {
        Map<String, Map<String, Object>> pokeDetailMap = new LinkedHashMap<>(pokeList.size());
        ObjectMapper objectMapper = new ObjectMapper();

        for (Map<String, Object> companyMap : companyDetailList) {
            Map<String, Object> companyData = pokeDetailMap.get(companyMap.get("company_id"));

            if (companyData == null) {
                pokeDetailMap.put(companyMap.get("company_id").toString(), companyMap);
            } else {
                companyMap.putAll(companyData);
                pokeDetailMap.put(companyMap.get("company_id").toString(), companyMap);
            }
        }

        for (Poke poke : pokeList) {

            Map<String, Object> companyData = pokeDetailMap.get(poke.getPoker_id());

            if (companyData == null) {
                pokeDetailMap.put(poke.getPoker_id(), objectMapper.convertValue(poke, Map.class));
            } else {
                companyData.putAll(objectMapper.convertValue(poke, Map.class));
                pokeDetailMap.put(poke.getPoker_id(), companyData);
            }

        }
        return new ArrayList<>(pokeDetailMap.values());
    }

    //not in use
    public ArrayList<Map<String, Object>> mergePokeDetailByPokedId(List<Map<String, Object>> companyDetailList, List<Poke> pokeList) {
        Map<String, Map<String, Object>> pokeDetailMap = new LinkedHashMap<>(pokeList.size());
        ObjectMapper objectMapper = new ObjectMapper();

        for (Map<String, Object> companyMap : companyDetailList) {
            Map<String, Object> companyData = pokeDetailMap.get(companyMap.get("company_id"));

            if (companyData == null) {
                pokeDetailMap.put(companyMap.get("company_id").toString(), companyMap);
            } else {
                companyMap.putAll(companyData);
                pokeDetailMap.put(companyMap.get("company_id").toString(), companyMap);
            }
        }

        for (Poke poke : pokeList) {

            Map<String, Object> companyData = pokeDetailMap.get(poke.getPoked_id());

            if (companyData == null) {
                pokeDetailMap.put(poke.getPoked_id(), objectMapper.convertValue(poke, Map.class));
            } else {
                companyData.putAll(objectMapper.convertValue(poke, Map.class));
                pokeDetailMap.put(poke.getPoked_id(), companyData);
            }

        }

        return new ArrayList<>(pokeDetailMap.values());
    }

    public int getFollowCountByStatus(List<FollowDetail> followDetails){

        int totalFollowDetail = 0;
        for (FollowDetail followDetail : followDetails){
            if (followDetail.getStatus().equals(FOLLOW)){
                totalFollowDetail++;
            }
        }

        return totalFollowDetail;
    }

    public void sendFollowNotification(Map<String,Object> followNotificationData, String userId){

        Map<String,Object> followerImage = (Map<String, Object>) followNotificationData.get("followerImage");
        String followerName = followNotificationData.get("followerName").toString();
        String companyUserId = followNotificationData.get("companyUserId").toString();

        Map<String, Map<String, Object>> templatePayload = new HashMap<>(3);
        Map<String, Object> payloadData = new HashMap<>(4);

        payloadData.put("follower_name", followerName);
        payloadData.put("email_id", followNotificationData.get("followingEmailId"));
//        payloadData.put("email_id", "t.chetnajoshi889@gmail.com");

        templatePayload.put(companyUserId, payloadData);

        Map<String, Map<String, Object>> backgroundPayload = new HashMap<>(3);
        Map<String, Object> bgPayloadData = new HashMap<>(4);

        bgPayloadData.put("follower_id", followNotificationData.get("followerId"));
        bgPayloadData.put("following_id", followNotificationData.get("followingId"));
        bgPayloadData.put("follower_type", followNotificationData.get("followerType"));
        bgPayloadData.put("id", followNotificationData.get("id"));
        bgPayloadData.put("name", followNotificationData.get("followerName"));
        bgPayloadData.put("image", followerImage != null ? followerImage.get("image_path") : null);
        bgPayloadData.put("type", "follow"); //this is used by client to show icon for the notification
        backgroundPayload.put(companyUserId, bgPayloadData);

        System.out.println("templatePayload");
        System.out.println(templatePayload);
        System.out.println("");
        System.out.println("backgroundPayload");
        System.out.println(backgroundPayload);

        Integer is_batch = 0;  //This variable is used for email in sending multiple email at one time to users.

        Map<String, Object> sendNotificationResponse = restTemplateUtil.sendNotification(userId,
                "", "", templatePayload, backgroundPayload, is_batch, FOLLOW_REQUEST_SEND);

        System.out.println("");
        System.out.println("NOTIFICATION SENT RESPONSE");
        System.out.println("");
        System.out.println(sendNotificationResponse);
        System.out.println("");
    }
}
