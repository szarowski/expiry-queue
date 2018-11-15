package com.expiryqueue.error;

import com.expiryqueue.error.model.Errors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static java.util.Collections.emptyList;

@RestControllerAdvice
@Order(110)
@ConditionalOnClass(DuplicateKeyException.class)
public class JdbcExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Errors handleDuplicateKeyException() {
        return new Errors(emptyList());
    }
}