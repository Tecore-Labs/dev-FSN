package com.contactservice.util;

import java.util.HashMap;
import java.util.Map;

import static com.contactservice.constants.MessageConstants.messageCodes;
import static com.contactservice.constants.MessageConstants.messages;
import static com.contactservice.constants.MessageConstants.statusCode.SUCCESS_STATUS_CODE;

public class HttpResponse {

    public static Map<String, Object> getResponse(int statusCode, Object data, String messageTag) {

        if (statusCode != SUCCESS_STATUS_CODE && data != null) {
            Map<String, Object> messageData = (Map<String, Object>) data;

            System.out.println("");
            System.out.println("Message Code Keys");
            System.out.println(messageData.keySet());
            System.out.println("");

            Map<String, Map<String, Object>> responseExceptionData = new HashMap<>(messageData.size());

            for (String key : messageData.keySet()) {

                Map<String, Object> messageDetails = new HashMap<>(2);
                messageDetails.put("message", messages.get(key));
                messageDetails.put("message_code", messageCodes.get(key));

                String fieldName = (String) messageData.get(key);
                responseExceptionData.put(fieldName, messageDetails);
            }

            data = responseExceptionData;
        }

        Map<String, Object> response = new HashMap<>(4);
        response.put("status", statusCode);

        if (data != null) {
            response.put("data", data);
        }
        if (messageTag != null) {
            response.put("message", messages.get(messageTag));
            response.put("message_code", messageCodes.get(messageTag));
        }

        return response;

    }


}
