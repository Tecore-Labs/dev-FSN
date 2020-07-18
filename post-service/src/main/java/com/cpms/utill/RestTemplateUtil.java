package com.cpms.utill;

import com.cpms.constants.ServiceAPIs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Component
public class RestTemplateUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.cims.address}" + ":" + "${services.cims.port}" + ServiceAPIs.cims.fileService)
    private String fileService;

    @Value("${services.ccns.address}" + ":" + "${services.ccns.port}" + ServiceAPIs.Ccns.getFollowDetails)
    private String followDetailUrl;

    @Value("${services.ccns.address}" + ":" + "${services.ccns.port}" + ServiceAPIs.Ccns.getFollowerAndConnections)
    private String getFollowerAndConnections;

    @Value("${services.ccns.address}" + ":" + "${services.ccns.port}" + ServiceAPIs.Ccns.getConnectionLevel)
    private String getConnectionLevel;

    @Value("${services.ccms.address}" + ":" + "${services.ccms.port}" + ServiceAPIs.Companyinfo.getCompanyDetails)
    private String getCompanyDetails;

    @Value("${services.cams.address}" + ":" + "${services.cams.port}" + ServiceAPIs.Accontinfo.getAccountDetails)
    private String getAccountDetails;

    @Value("${services.cnms.address}" + ":" + "${services.cnms.port}" + ServiceAPIs.Cnms.sendNotifications)
    private String sendNotifications;

    @Value("${services.cgss.address}" + ":" + "${services.cgss.port}" + ServiceAPIs.Cgss.addUpdatePost)
    private String addUpdatePostUrl;

    @Value("${services.cats.address}" + ":" + "${services.cats.port}" + ServiceAPIs.Cats.authenticateAccessToken)
    private String authenticateAccessToken;

    public Map<String, Object> accessMediaImageFile(String post_id, MultipartFile[] media_file, String subType, UUID fileId, String token, String token_type, String account_id) throws IOException {

        System.out.println("------------------------");
        System.out.println("");
        System.out.println("MULTIPART FILE");
        System.out.println("post_id:" + post_id);
        System.out.println("media_file" + media_file);
        System.out.println("");
        System.out.println("------------------------");

        MultiValueMap<String, Object> postImageMap = new LinkedMultiValueMap<>();
        postImageMap.add("id", post_id);
        postImageMap.add("type", "post");
        postImageMap.add("sub_type", subType);
        postImageMap.add("account_id", account_id);
        postImageMap.add("token_type", token_type);

        List<String> tempFileNames = new ArrayList<>();
        String tempFileName;
        FileOutputStream fo;
        Map<String, Object> response = null;
        HttpHeaders imageHeaders = new HttpHeaders();
        imageHeaders.add("Authorization", token);

        if (media_file != null) {
            try {
                for (MultipartFile file : media_file) {
                    tempFileName = file.getOriginalFilename();
                    tempFileNames.add(tempFileName);
                    fo = new FileOutputStream(tempFileName);
                    fo.write(file.getBytes());
                    fo.close();
                    postImageMap.add("file", new FileSystemResource(tempFileName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            imageHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        }

        if (fileId != null) {

            postImageMap.add("file_id", fileId.toString());
        }

        HttpEntity<MultiValueMap<String, Object>> imageHttpEntity = new HttpEntity<>(postImageMap, imageHeaders);

        System.out.println("call file service");
        if (fileId == null) {
            // save media file
            System.out.println("Call file service to save file");
            System.out.println("");
            response = (Map<String, Object>) (restTemplate.postForObject(fileService, imageHttpEntity, Map.class));
        } else {

            if (post_id == null && media_file == null && subType == null && fileId != null) {
                // delete media file
                System.out.println("Call file service to delete the file");
                System.out.println("");
                response = (Map<String, Object>) (restTemplate.exchange(fileService, HttpMethod.DELETE, imageHttpEntity, Map.class).getBody());
            } else {

                // update media file
                System.out.println("Call file service to update the file");
                System.out.println("");
                response = (restTemplate.exchange(fileService, HttpMethod.PUT, imageHttpEntity, Map.class).getBody());
            }
            System.out.println("get a response" + response);
        }

        System.out.println("");
        return response;

    }

    public Map<String, Object> getFollowDetails(String followerId, String token, String account_id, String token_type) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(followDetailUrl);
        builder.queryParam("follower_id", followerId);
        builder.queryParam("account_id", account_id);
        builder.queryParam("token_type", token_type);

        HttpHeaders followDetailHeader = new HttpHeaders();
        followDetailHeader.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> followDetailHttpEntity = new HttpEntity(followDetailHeader);

        return (Map<String, Object>) restTemplate.exchange(builder.toUriString(), HttpMethod.GET, followDetailHttpEntity, Map.class).getBody();

    }

    public Map<String, Object> getFollowerAndConnections(String companyId, String token, String token_type, String account_id) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getFollowerAndConnections);
        builder.queryParam("company_id", companyId);
        builder.queryParam("account_id", account_id);
        builder.queryParam("token_type", token_type);

        HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity(httpHeader);

        return (Map<String, Object>) restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, Map.class).getBody();

    }

    /*
     * Calling company service
     * Type commenter is USER
     *
     * */
    public Map<String, Object> accessCompanyDetails(Set<String> companyIds, String token, String account_id, String token_type) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getCompanyDetails);
        builder.queryParam("company_ids", companyIds.toArray());
        builder.queryParam("account_id", account_id);
        builder.queryParam("token_type", token_type);

        HttpHeaders companyDetailHeader = new HttpHeaders();
        companyDetailHeader.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> companyDetailHttpEntity = new HttpEntity(companyDetailHeader);

        return (Map<String, Object>) restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                companyDetailHttpEntity,
                Map.class).getBody();
    }

    /*
     * Calling account service
     * Type commenter is COMPANY
     *
     * */
    public Map<String, Object> accessAccountDetails(Set<String> commenter_id, String token, String token_type, String account_id) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getAccountDetails);
        builder.queryParam("account_ids", commenter_id.toArray());
        builder.queryParam("account_id", account_id);
        builder.queryParam("token_type", token_type);

        HttpHeaders companyDetailHeader = new HttpHeaders();
        companyDetailHeader.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> companyDetailHttpEntity = new HttpEntity(companyDetailHeader);

        return (Map<String, Object>) restTemplate.exchange(builder.toUriString(), HttpMethod.GET, companyDetailHttpEntity, Map.class).getBody();
    }

    public Map<String, Object> getUserDetails(Set<String> userIds, Set<String> fields, String token, String account_id, String token_type) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getAccountDetails);
        builder.queryParam("account_ids", userIds.toArray());
        builder.queryParam("fields", fields.toArray());
        builder.queryParam("account_id", account_id);
        builder.queryParam("token_type", token_type);

        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> accessTokenHttpEntity = new HttpEntity(accessTokenHeader);

        return (Map<String, Object>) restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                accessTokenHttpEntity,
                Map.class).getBody();

    }

    public Map<String, Object> getCompanyDetail(Set<String> companyIds, String token, String token_type, String account_id) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getCompanyDetails);
        builder.queryParam("company_ids", companyIds.toArray());
        builder.queryParam("token_type", token_type);
        builder.queryParam("account_id", account_id);

        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> accessTokenHttpEntity = new HttpEntity(accessTokenHeader);

        return (Map<String, Object>) restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                accessTokenHttpEntity,
                Map.class).getBody();

    }

    public Map<String, Object> sendNotification(String accountId, String tokenType, String accessToken,
                                                Map<String, Map<String, Object>> template_payload,
                                                Map<String, Map<String, Object>> background_payload, Integer is_batch,
                                                String template_id) {


        MultiValueMap<String, Object> notificationParams = new LinkedMultiValueMap<>(7);
        notificationParams.add("template_payload", template_payload);
        notificationParams.add("background_payload", background_payload);
        notificationParams.add("is_batch", is_batch);
        notificationParams.add("template_id", template_id);
        notificationParams.add("account_id", accountId);
        notificationParams.add("token_type", tokenType);
//        notificationParams.add("type", type);

        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Authorization", accessToken);

        HttpEntity<MultiValueMap<String, Object>> sendNotificationHttpEntity = new HttpEntity<>(notificationParams, accessTokenHeader);

        Map<String, Object> sendPushNotificationResponse = restTemplate.postForObject(sendNotifications, sendNotificationHttpEntity, Map.class);

        return sendPushNotificationResponse;
    }

    public Map<String, Object> addUpdatePost(Map<String, Object> postDataMap, String postId, boolean isUpdate, String token, String account_id, String token_type) {

        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>(postDataMap.size());
        multiValueMap.add("post_id", postId);
        multiValueMap.add("account_id", account_id);
        multiValueMap.add("token_type", token_type);
        if (isUpdate) {
            multiValueMap.add("update_data", postDataMap);
        } else {
            for (String key : postDataMap.keySet()) {
                multiValueMap.add(key, postDataMap.get(key));
            }
        }

        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Authorization", token);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(multiValueMap, accessTokenHeader);
        return restTemplate.postForObject(addUpdatePostUrl, httpEntity, Map.class);
    }

    public Map<String,Object> getConnectionLevel(String fromCompanyId, Set<String> toCompanyIds, String token, String account_id, String token_type){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getConnectionLevel);
        builder.queryParam("from_company_id", fromCompanyId);
        builder.queryParam("to_company_ids", toCompanyIds.toArray());
        builder.queryParam("token_type", token_type);
        builder.queryParam("account_id", account_id);

        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> accessTokenHttpEntity = new HttpEntity(accessTokenHeader);

        return (Map<String, Object>) restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                accessTokenHttpEntity,
                Map.class).getBody();
    }

    public Map<String, Object> authenticateAccessToken(String user_id, String access_token){
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>(3);
        requestParams.add("account_id", user_id);

        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.add("Authorization", access_token);

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<MultiValueMap<String, String>>(requestParams, requestHeader);


        Map<String, Object> accessTokenValidationResponse = restTemplate.postForObject(authenticateAccessToken, httpRequest, Map.class);

        return accessTokenValidationResponse;
    }

}
