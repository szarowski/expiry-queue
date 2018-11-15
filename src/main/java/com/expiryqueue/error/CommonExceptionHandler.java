package com.expiryqueue.error;

import com.expiryqueue.error.model.Errors;
import com.expiryqueue.error.model.RequestError;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@RestControllerAdvice
@Order(200)
@ConditionalOnWebApplication
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException exception,
                                                                  final HttpHeaders headers,
                                                                  final HttpStatus status,
                                                                  final WebRequest request) {
        final List<RequestError> errors = exception.getBindingResult().getAllErrors().stream()
                .map(FieldError.class::cast)
                .map(fieldError -> new RequestError(
                        fieldError.getCode(),
                        format("%s %s", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldError.getField()),
                                fieldError.getDefaultMessage()),
                        CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldError.getField()),
                        null))
                .collect(toList());
        return handleExceptionInternal(exception, new Errors(errors), headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException exception,
                                                         final HttpHeaders headers,
                                                         final HttpStatus status,
                                                         final WebRequest request) {
        final List<RequestError> errors = exception.getBindingResult().getAllErrors().stream()
                .map(FieldError.class::cast)
                .map(fieldError -> new RequestError(
                        fieldError.getCode(),
                        format("%s %s", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldError.getField()),
                                fieldError.getDefaultMessage()),
                        CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldError.getField()),
                        null))
                .collect(toList());
        return handleExceptionInternal(exception, new Errors(errors), headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException exception,
                                                                          final HttpHeaders headers,
                                                                          final HttpStatus status,
                                                                          final WebRequest request) {
        final RequestError error = new RequestError("Missing required query parameter " +
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, exception.getParameterName()) + ".");
        return handleExceptionInternal(exception, new Errors(singletonList(error)), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(final MissingServletRequestPartException exception,
                                                                     final HttpHeaders headers,
                                                                     final HttpStatus status,
                                                                     final WebRequest request) {
        return handleExceptionInternal(exception, new Errors(singletonList(new RequestError(exception.getMessage()))),
                headers, status, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Errors handleValidationError(final ConstraintViolationException exception) {
        final List<RequestError> errors = exception.getConstraintViolations().stream()
                .map(violation -> new RequestError(
                        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        format("%s %s", propertyPath(violation), violation.getMessage()),
                        propertyPath(violation),
                        null))
                .collect(toList());
        return new Errors(errors);
    }

    private String propertyPath(final ConstraintViolation<?> violation) {
        final String propertyPath = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, violation.getPropertyPath().toString());
        final int fieldNamesPathStart = propertyPath.indexOf('.');
        final String fieldNamesPath = propertyPath.substring(fieldNamesPathStart == -1 ? 0 : fieldNamesPathStart);
        final Optional<String> parameterName = Streams.stream(violation.getPropertyPath())
                .filter(node -> node.getKind() == ElementKind.PARAMETER)
                .map(node -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getName()))
                .findFirst();
        return parameterName.map(name -> (fieldNamesPath.indexOf(name) + name.length() + 1 < fieldNamesPath.length()
                ? fieldNamesPath.substring(fieldNamesPath.indexOf(name) + name.length() + 1) : name))
                .orElse(fieldNamesPath);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Errors handleValidationError(final UnprocessableEntityException exception) {
        return new Errors(exception.getErrors());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Errors handleOtherError(final Exception exception) {
        LOG.error("Returning HTTP status 500 due to unhandled exception:", exception);
        return new Errors(singletonList(new RequestError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Errors handleContentNotFound(final NotFoundException exception) {
        return new Errors(singletonList(new RequestError(exception.getMessage())));
    }

    @ExceptionHandler(DuplicateContentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Errors handleDuplicateContentFound(final DuplicateContentException exception) {
        return new Errors(singletonList(new RequestError(exception.getMessage())));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Errors handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException exception) {
        return new Errors(singletonList(new RequestError(exception.getCause().getMessage() + " in URL Path Parameter " +
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, exception.getName()))));
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Errors handleMethodArgumentTypeMismatch(final BadRequestException exception) {
        return new Errors(singletonList(new RequestError(exception.getMessage())));
    }
}