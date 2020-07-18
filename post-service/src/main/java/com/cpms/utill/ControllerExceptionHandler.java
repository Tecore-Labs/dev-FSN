package com.cpms.utill;

import com.cpms.controller.PostController;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.cpms.utill.HttpConstants.BAD_REQUEST_STATUS_CODE;
import static com.cpms.utill.HttpConstants.INVALID_INPUT;

@EnableWebMvc
//@ControllerAdvice(basePackages = "com.cpms.*")
@ControllerAdvice
public class ControllerExceptionHandler {

    @Autowired
    HttpResponse httpResponse;

    @Autowired
    PostController postController;

    //RESPONSE MESSAGES
    private static final String TECHNICAL_ERROR_MESSAGE = "There is some Technical Error";
    private static final String INVALID_INPUT_MESSAGE = "Invalid Input Format";

    //RESPONSE STATUS CODES
    private static final int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;

    @ExceptionHandler(NumberFormatException.class)
    @ResponseBody
    public Map<String, Object> exceptionHandling(NumberFormatException exception) {

        System.out.println("");
        System.out.println("--------------");
        System.out.println("");
        System.out.println("EXCEPTION OCCURRED");
        System.out.println("");
        System.out.println("NumberFormatException");
        System.out.println("");
        System.out.println(exception.getMessage());
        System.out.println("");
        System.out.println(exception.getClass());
        System.out.println("");
        System.out.println("--------------");
        System.out.println("");

        return httpResponse.getResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, INVALID_INPUT_MESSAGE, null);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public Map<String, Object> exceptionHandling(MissingServletRequestParameterException exception) {

        System.out.println("");
        System.out.println("--------------");
        System.out.println("");
        System.out.println("EXCEPTION OCCURRED");
        System.out.println("");
        System.out.println("MissingServletRequestParameterException");
        System.out.println("");
        System.out.println("");
        System.out.println(exception.getMessage());
        System.out.println("");
        System.out.println(exception.getClass());
        System.out.println("");
        System.out.println("--------------");
        System.out.println("");

        return httpResponse.getResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, exception.getMessage(), null);

    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public Map<String, Object> exceptionHandling(MethodArgumentTypeMismatchException exception) {

        System.out.println("");
        System.out.println("--------------");
        System.out.println("");
        System.out.println("EXCEPTION OCCURRED");
        System.out.println("");
        System.out.println("MethodArgumentTypeMismatchException");
        System.out.println("");
        System.out.println(exception.getMessage());
        System.out.println("");
        System.out.println(exception.getClass());
        System.out.println("");
        System.out.println("--------------");
        System.out.println("");

        return httpResponse.getResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, INVALID_INPUT_MESSAGE, null);

    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public Map<String, Object> exceptionHandling(IllegalArgumentException exception) {

        System.out.println("");
        System.out.println("--------------");
        System.out.println("");
        System.out.println("EXCEPTION OCCURRED");
        System.out.println("");
        System.out.println("IllegalArgumentException");
        System.out.println("");
        System.out.println(exception.getMessage());
        System.out.println("");
        System.out.println(exception.getClass());
        System.out.println("");
        System.out.println("--------------");
        System.out.println("");

        return httpResponse.getResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, INVALID_INPUT_MESSAGE, null);

    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Map<String, Object> exceptionHandling(MaxUploadSizeExceededException exception) throws IOException {

        System.out.println("");
        System.out.println("--------------");
        System.out.println("");
        System.out.println("EXCEPTION OCCURRED");
        System.out.println("");
        System.out.println("SizeLimitExceededException");
        System.out.println("");
        System.out.println(exception.getMessage());
        System.out.println("");
        System.out.println(exception.getClass());
        System.out.println("");
        System.out.println("--------------");
        System.out.println("");

        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.LARGE_FILE_SIZE, null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, Object> exceptionHandling(Exception exception) {

        System.out.println("");
        System.out.println("--------------");
        System.out.println("");
        System.out.println("EXCEPTION OCCURRED");
        System.out.println("");
        System.out.println(exception.getMessage());
        System.out.println("");
        System.out.println("EXCEPTION OCCURRED");
        System.out.println(exception.getClass());
        System.out.println("");
        System.out.println("--------------");
        System.out.println("");

        return httpResponse.getResponse(INTERNAL_SERVER_ERROR_STATUS_CODE, TECHNICAL_ERROR_MESSAGE, null);


    }
}

