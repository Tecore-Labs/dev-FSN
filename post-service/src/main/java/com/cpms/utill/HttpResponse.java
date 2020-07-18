package com.cpms.utill;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpResponse {


    public <T> Map<String, Object> getResponse(int statusCode, String message, T data) {


        Map<String, Object> jsonResult = new HashMap<String, Object>(3);
        jsonResult.put("status", statusCode);

        if (message != null) {
            jsonResult.put("message", message);
        }
        if (data != null) {
            jsonResult.put("data", data);
        }
        System.out.println("=========");
        System.out.println("");
        System.out.println("RESPONSE");
        System.out.println("");
        System.out.println(jsonResult);
        System.out.println("");
        System.out.println("=========");

        return jsonResult;

    }


}
