package com.contactservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.contactservice.constants.ContactConstants.NotificationTemplateTypes.CONNECTION_REQUEST_ACCEPTED;
import static com.contactservice.constants.ContactConstants.NotificationTemplateTypes.CONNECTION_REQUEST_DENIED;
import static com.contactservice.constants.ContactConstants.RequestStatus.ACCEPTED;
import static com.contactservice.constants.ContactConstants.RequestStatus.DECLINED;

@Component
public class NotificationUtil {

    @Autowired
    RestTemplateUtil restTemplateUtil;

    public void sendNotification(String userId, Map<String, Map<String, Object>> templatePayload,
                                 Map<String, Map<String, Object>> backgroundPayload, Short requestStatus) {

        System.out.println("-----------");
        System.out.println("SENDING NOTIFICATION");
        System.out.println("-----------");

        Integer is_batch = 0;  //This variable is used for email in sending multiple email at one time to users.
        String template_id_alias = null;


        switch (requestStatus) {
            case ACCEPTED:
                System.out.println("request status is accepted");
                template_id_alias = CONNECTION_REQUEST_ACCEPTED;
                break;
            case DECLINED:
                System.out.println("request status is declined");
                template_id_alias = CONNECTION_REQUEST_DENIED;
                break;
        }

        Map<String, Object> sendNotificationResponse = restTemplateUtil.sendNotification(userId,
                "", "", templatePayload, backgroundPayload, is_batch, template_id_alias);

        System.out.println("");
        System.out.println("NOTIFICATION SENT RESPONSE");
        System.out.println("");
        System.out.println(sendNotificationResponse);
        System.out.println("");
    }
}
