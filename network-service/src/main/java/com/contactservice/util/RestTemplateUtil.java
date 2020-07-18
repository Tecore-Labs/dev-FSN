package com.contactservice.util;

import com.contactservice.constants.ServiceAPIs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

@Component
public class RestTemplateUtil {

    @Autowired
    ServiceUrlsUtil serviceUrlsUtil;

    @PostConstruct
    public void test() {

        System.out.println("");
        System.out.println("---------------------");
        System.out.println("SERVICES URLS");
        System.out.println("");
        System.out.println(serviceUrlsUtil.getAccountUrl());
        System.out.println(serviceUrlsUtil.getAuthUrl());
        System.out.println(serviceUrlsUtil.getCompanyUrl());
        System.out.println(serviceUrlsUtil.getNotificationUrl());
        System.out.println("");
        System.out.println("---------------------");
        System.out.println("");
    }

    @Autowired
    private RestTemplate restTemplate;

    @Value(ServiceAPIs.Cnms.sendNotifications)
    private String sendVerificationEmail;

    @Value(ServiceAPIs.Cams.getUserDetails)
    private String getUserDetails;

    @Value(ServiceAPIs.Cats.authenticateAccessToken)
    private String authenticateAccessToken;

    @Value(ServiceAPIs.Ccms.getCompanyDetails)
    private String getCompanyDetails;

    @Value(ServiceAPIs.Ccms.getCompanyDetailByCompanyIds)
    private String getCompanyDetailByCompanyIds;

    public Map<String, Object> sendNotification(String accountId, String tokenType, String accessToken,
                                                Map<String, Map<String, Object>> template_payload,
                                                Map<String, Map<String, Object>> background_payload, Integer is_batch,
                                                String template_id) {

        String notificationUrl = serviceUrlsUtil.getNotificationUrl() + sendVerificationEmail;

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

        Map<String, Object> sendPushNotificationResponse = restTemplate.postForObject(notificationUrl, sendNotificationHttpEntity, Map.class);

        return sendPushNotificationResponse;
    }

    public Map<String, Object> getUserDetails(Set<String> userIds, Set<String> fields, String access_token, String account_id, String token_type) {

        String accountServiceUrl = serviceUrlsUtil.getAccountUrl() + getUserDetails;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(accountServiceUrl);
        builder.queryParam("account_ids", userIds.toArray());
        builder.queryParam("fields", fields.toArray());
        builder.queryParam("account_id", account_id);
        builder.queryParam("token_type", token_type);

        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Authorization", access_token);

        HttpEntity<MultiValueMap<String, String>> accessTokenHttpEntity = new HttpEntity(accessTokenHeader);

        return (Map<String, Object>) restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                accessTokenHttpEntity,
                Map.class).getBody();

    }

    public Map<String, Object> authenticateAccessToken(String user_id, String access_token) {

        String authServiceUrl = serviceUrlsUtil.getAuthUrl() + authenticateAccessToken;

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>(3);
        requestParams.add("account_id", user_id);

        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.add("Authorization", access_token);

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<MultiValueMap<String, String>>(requestParams, requestHeader);


        Map<String, Object> accessTokenValidationResponse = restTemplate.postForObject(authServiceUrl, httpRequest, Map.class);

        return accessTokenValidationResponse;
    }


    public Map<String, Object> getCompanyDetail(Set<String> companyIds, String accessToken, String accountId, String tokenType) {

        String companyUrl = serviceUrlsUtil.getCompanyUrl() + getCompanyDetails;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(companyUrl);
        builder.queryParam("company_ids", companyIds.toArray());
        builder.queryParam("account_id", accountId);
        builder.queryParam("token_type", tokenType);

        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Authorization", accessToken);

        HttpEntity<MultiValueMap<String, String>> accessTokenHttpEntity = new HttpEntity(accessTokenHeader);

        return (Map<String, Object>) restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                accessTokenHttpEntity,
                Map.class).getBody();

    }

    public Map<String, Object> getCompanyDetailByCompanyIds(Set<String> companyIds, String accessToken, String accountId, String tokenType) {

        String companyUrl = serviceUrlsUtil.getCompanyUrl() + getCompanyDetailByCompanyIds;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(companyUrl);
        builder.queryParam("company_ids", companyIds.toArray());
        builder.queryParam("account_id", accountId);
        builder.queryParam("token_type", tokenType);

        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Authorization", accessToken);

        HttpEntity<MultiValueMap<String, String>> accessTokenHttpEntity = new HttpEntity(accessTokenHeader);

        return (Map<String, Object>) restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                accessTokenHttpEntity,
                Map.class).getBody();

    }
}
