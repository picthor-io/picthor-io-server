package io.picthor.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realcnbs.horizon.framework.rest.response.ExceptionResponse;
import com.realcnbs.horizon.framework.rest.response.GenericExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException e
    ) throws IOException {
        log.trace("Sending 401 response");

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ExceptionResponse exceptionResponse = new GenericExceptionResponse();
        exceptionResponse.setErrorType(ExceptionResponse.ErrorType.AUTHENTICATION);
        exceptionResponse.setMessage("Not Authenticated");

        ObjectMapper mapper = new ObjectMapper();
        response.getOutputStream().print(mapper.writeValueAsString(exceptionResponse));
    }
}
