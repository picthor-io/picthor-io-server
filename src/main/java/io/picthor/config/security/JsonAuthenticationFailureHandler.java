package io.picthor.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realcnbs.horizon.framework.rest.response.ExceptionResponse;
import com.realcnbs.horizon.framework.rest.response.GenericExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception
    ) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ExceptionResponse exceptionResponse = new GenericExceptionResponse();
        exceptionResponse.setErrorType(ExceptionResponse.ErrorType.AUTHENTICATION);
        if (exception.getMessage() == null){
            exceptionResponse.setMessage("Invalid credentials");
        }
        else {
            exceptionResponse.setMessage(exception.getMessage());
        }


        ObjectMapper mapper = new ObjectMapper();
        response.getOutputStream().print(mapper.writeValueAsString(exceptionResponse));
    }
}
