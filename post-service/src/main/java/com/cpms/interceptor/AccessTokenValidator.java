package com.cpms.interceptor;

import com.cpms.utill.HttpResponse;
import com.cpms.utill.RestTemplateUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.cpms.utill.HttpConstants.*;

@Component
public class AccessTokenValidator implements HandlerInterceptor {

    @Autowired
    RestTemplateUtil restTemplateUtil;

    @Autowired
    HttpResponse httpResponse;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("------------------");
        System.out.println("ACCESS TOKEN INTERCEPTOR CALLED");
        System.out.println("");

        String token_type = request.getParameter("token_type");

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");

        System.out.println("token_type : " + token_type);
        System.out.println("request_url : " + request.getRequestURI());
        System.out.println("account_id : " + request.getParameter("account_id"));
        System.out.println("token : " + request.getHeader("Authorization"));
        System.out.println("");

        if (token_type == null || !token_type.equals("access")) {

            System.out.println("");
            System.out.println("Token type is not valid");
            System.out.println("");

            response.getWriter().write(new JSONObject(httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, INVALID_INPUT, null)).toString());
            response.setContentType("application/json");

            return false;
        }


        Map<String, Object> accessTokenValidationResponse = restTemplateUtil.authenticateAccessToken(request.getParameter("account_id"), request.getHeader("Authorization"));

        System.out.println("");
        System.out.println("accessTokenValidationResponse");
        System.out.println(accessTokenValidationResponse);
        System.out.println("");

        if ((int) accessTokenValidationResponse.get("status") != SUCCESS_STATUS_CODE) {

            System.out.println("");
            System.out.println("Access token is not valid");
            System.out.println("");

            response.setContentType("application/json");
            response.getWriter().write(new JSONObject(accessTokenValidationResponse).toString());

            return false;
        }


        return true;
    }
}
