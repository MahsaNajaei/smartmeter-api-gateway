package com.energymeter.apigateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@ControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleAllExceptions(Exception e) {
        log.error("Unexpected Exception Occurred! ", e);
        e.printStackTrace();
        return generateBaseApiErrorMessage("Sorry! Some Internal Error Occurred! Please Try Again Later!");
    }

    @ExceptionHandler(ApplicationRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleApplicationRuntimeExceptions(ApplicationRuntimeException e) {
        log.warn(e.getMessage());
        return generateBaseApiErrorMessage(e.getMessage());
    }

    private Map<String, String> generateBaseApiErrorMessage(String message) {
        var responseBody = new HashMap<String, String>();
        responseBody.put("message", message);
        return responseBody;
    }

}
