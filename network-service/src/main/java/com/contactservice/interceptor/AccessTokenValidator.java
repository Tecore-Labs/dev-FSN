package com.contactservice.interceptor;

import com.contactservice.util.HttpResponse;
import com.contactservice.util.RestTemplateUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.contactservice.constants.MessageConstants.MessageTags.INVALID_INPUT;
import static com.contactservice.constants.MessageConstants.statusCode.BAD_REQUEST_STATUS_CODE;
import static com.contactservice.constants.MessageConstants.statusCode.SUCCESS_STATUS_CODE;

@Component
public class AccessTokenValidator implements HandlerInterceptor {

    @Autowired
    RestTemplateUtil restTemplateUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("------------------");
        System.out.println("ACCESS TOKEN INTERCEPTOR CALLED");
        System.out.println("");

        String token_type = request.getParameter("token_type");

        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Headers","*");

        System.out.println("token_type : " + token_type);
        System.out.println("request_url : " + request.getRequestURI());
        System.out.println("");

        if (token_type == null || !token_type.equals("access")) {

            System.out.println("");
            System.out.println("Token type is not valid");
            System.out.println("");

            response.getWriter().write(new JSONObject(HttpResponse.getResponse(BAD_REQUEST_STATUS_CODE, null, INVALID_INPUT)).toString());
            response.setContentType("application/json");

            return false;
        }


        Map<String, Object> accessTokenValidationResponse = restTemplateUtil.authenticateAccessToken(request.getParameter("account_id"), request.getHeader("Authorization"));

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
