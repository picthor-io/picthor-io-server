package io.picthor.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.realcnbs.horizon.framework.data.filter.PageOutOfBoundsException;
import com.realcnbs.horizon.framework.rest.exception.NotFoundException;
import com.realcnbs.horizon.framework.rest.response.ExceptionResponse;
import com.realcnbs.horizon.framework.rest.response.GenericExceptionResponse;
import com.realcnbs.horizon.framework.rest.response.ValidationError;
import com.realcnbs.horizon.framework.rest.response.ValidationExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.UnexpectedTypeException;
import javax.validation.ValidationException;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public GenericExceptionResponse handleError(Exception e) {
        return handle(e, ExceptionResponse.ErrorType.INTERNAL, true);
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public GenericExceptionResponse handleError(NullPointerException e) {
        return handle(e, ExceptionResponse.ErrorType.INTERNAL, true);
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public GenericExceptionResponse handleError(UnexpectedTypeException e) {
        return handle(e, ExceptionResponse.ErrorType.INTERNAL, true);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public GenericExceptionResponse handleError(NoHandlerFoundException e) {
        return handle(e, "Resource not found", ExceptionResponse.ErrorType.NOT_FOUND, false);
    }

    @ExceptionHandler(RequestRejectedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public GenericExceptionResponse handleError(RequestRejectedException e) {
        return handle(e, "Resource not found", ExceptionResponse.ErrorType.NOT_FOUND, false);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public GenericExceptionResponse handleError(NotFoundException e) {
        return handle(e, e.getMessage(), ExceptionResponse.ErrorType.NOT_FOUND, false);
    }

    @ExceptionHandler(PageOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public GenericExceptionResponse handleError(PageOutOfBoundsException e) {
        return handle(e, e.getMessage(), ExceptionResponse.ErrorType.NOT_FOUND, false);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public GenericExceptionResponse handleError(AccessDeniedException e) {
        return handle(e, "Access denied", ExceptionResponse.ErrorType.AUTHORIZATION, false);
    }

    @ExceptionHandler(JsonParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericExceptionResponse handleError(JsonParseException e) {
        GenericExceptionResponse response = new GenericExceptionResponse();
        JsonMappingException jex = (JsonMappingException) e.getCause();
        response.setErrorType(ExceptionResponse.ErrorType.INVALID_JSON);
        response.setMessage(jex.getOriginalMessage());
        return response;
    }

    @ExceptionHandler(EOFException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericExceptionResponse handleError(EOFException e) {
        GenericExceptionResponse response = new GenericExceptionResponse();
        response.setMessage("Failed to parse request payload");
        return handle(e, "Failed to parse request payload", ExceptionResponse.ErrorType.INVALID_REQUEST, true);
    }


    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericExceptionResponse handleError(MultipartException e) {
        GenericExceptionResponse response = new GenericExceptionResponse();
        response.setMessage(e.getMessage());
        return handle(e, e.getMessage(), ExceptionResponse.ErrorType.INVALID_REQUEST, true);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationExceptionResponse handleError(BindException e) {
        ValidationExceptionResponse response = new ValidationExceptionResponse();
        response.setErrors(buildErrorMap(e.getBindingResult().getFieldErrors()));
        return response;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public GenericExceptionResponse handleError(HttpRequestMethodNotSupportedException e) {
        GenericExceptionResponse response = new GenericExceptionResponse();
        response.setMessage("Method " + e.getMethod() + " not allowed for this endpoint");
        return handle(e, response.getMessage(), ExceptionResponse.ErrorType.INVALID_REQUEST, false);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationExceptionResponse handleError(MethodArgumentNotValidException e) {
        ValidationExceptionResponse response = new ValidationExceptionResponse();
        response.setErrors(buildErrorMap(e.getBindingResult().getFieldErrors()));
        return response;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericExceptionResponse handleValidationException(ValidationException e) {
        log.error("Validation failed: " + e.getMessage());
        GenericExceptionResponse response = new GenericExceptionResponse();
        response.setErrorType(ExceptionResponse.ErrorType.VALIDATION);
        response.setMessage(e.getMessage());
        return response;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericExceptionResponse handleError(MissingServletRequestParameterException e) {
        return handle(
                e, "Missing required url parameter: " + e.getParameterName(),
                ExceptionResponse.ErrorType.INVALID_REQUEST, false
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericExceptionResponse handleError(MethodArgumentTypeMismatchException e) {
        return handle(e, "Failed to parse parameter: " + e.getName(), ExceptionResponse.ErrorType.INVALID_REQUEST, false);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericExceptionResponse handleError(HttpMediaTypeNotSupportedException e) {
        return handleWarning(
                e, "Invalid content type: " + e.getContentType().getType(),
                ExceptionResponse.ErrorType.INVALID_REQUEST, false
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericExceptionResponse handleError(HttpMessageNotReadableException e) {
        GenericExceptionResponse response = new GenericExceptionResponse();

        if (e.getCause() instanceof JsonMappingException) {
            log.warn("Failed to map json", e);
            response.setErrorType(ExceptionResponse.ErrorType.INVALID_JSON);
            response.setMessage("Failed to map JSON");
        }

        if (e.getCause() instanceof JsonParseException) {
            log.warn("Failed to parse json", e);
            response.setErrorType(ExceptionResponse.ErrorType.INVALID_JSON);
            response.setMessage("Failed to parse JSON");
        }

        if (e.getCause() == null && e.getMessage() != null && e.getMessage().startsWith(
                "Required request body is missing")) {
            log.warn("Required request body is missing", e);
            response.setErrorType(ExceptionResponse.ErrorType.INVALID_REQUEST);
            response.setMessage("Required request body is missing");
        }

        return response;
    }

    private Map<String, List<ValidationError>> buildErrorMap(List<FieldError> fieldErrors) {
        Map<String, List<ValidationError>> errorMap = new HashMap<>();
        for (FieldError fieldError : fieldErrors) {
            List<ValidationError> errorList = new ArrayList<>();
            ValidationError error = new ValidationError();
            error.setCode(StringUtils.uncapitalize(fieldError.getCode()));
            error.setField(fieldError.getField());
            error.setMessage(fieldError.getDefaultMessage());
            errorList.add(error);
            errorMap.put(fieldError.getField(), errorList);
        }
        return errorMap;
    }

    private GenericExceptionResponse handle(Throwable e, ExceptionResponse.ErrorType type, boolean logTrace) {
        return handle(e, "Server encountered an error", type, logTrace);
    }

    private GenericExceptionResponse handle(Throwable e, String message, ExceptionResponse.ErrorType type, boolean logTrace) {
        GenericExceptionResponse response = new GenericExceptionResponse();
        response.setErrorType(type);
        response.setMessage(message);
        org.slf4j.MDC.put("error_ref", response.getErrorRef());
        if (logTrace) {
            log.error("#" + response.getErrorRef() + ": " + e.getMessage(), e);
        } else {
            log.debug("#" + response.getErrorRef() + ": " + e.getMessage());
        }
        return response;
    }

    private GenericExceptionResponse handleWarning(Throwable e, String message, ExceptionResponse.ErrorType type, boolean logTrace) {
        GenericExceptionResponse response = new GenericExceptionResponse();
        response.setErrorType(type);
        response.setMessage(message);
        org.slf4j.MDC.put("error_ref", response.getErrorRef());
        if (logTrace) {
            log.warn("#" + response.getErrorRef() + ": " + e.getMessage(), e);
        } else {
            log.debug("#" + response.getErrorRef() + ": " + e.getMessage());
        }
        return response;
    }
}
