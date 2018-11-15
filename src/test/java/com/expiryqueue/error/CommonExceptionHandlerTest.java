package com.expiryqueue.error;

import com.expiryqueue.config.JacksonConfig;
import com.expiryqueue.error.model.Errors;
import com.expiryqueue.error.model.RequestError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class CommonExceptionHandlerTest {

    private static final String EMPTY_JSON_STRING_VALUE = "\"          \"";

    private static final ObjectMapper OBJECT_MAPPER = new JacksonConfig().jacksonBuilder().build();

    private final MockMvc mockMvc = standaloneSetup(new Controller())
                .setControllerAdvice(new CommonExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(OBJECT_MAPPER))
                .build();

    @Test
    public void shouldReturn400ForInvalidJson() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/invalid_json")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldReturn400ForBadRequestException() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/bad_request")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldReturn400ForMissingFormDataParameter() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/missing_form_param")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldReturn400ForWrongPathParameter() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/method_argument_mismatch/123")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(400);
        Errors errors = OBJECT_MAPPER.readValue(response.getContentAsByteArray(), Errors.class);
        assertThat(errors.getErrors())
                .hasSize(1)
                .containsOnly(new RequestError(null, "Invalid UUID string: 123 in URL Path Parameter some_id", null, null));
    }

    @Test
    public void shouldReturn400OnMissingServletRequestParameter() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/missing_servlet_request_parameter")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(400);
        Errors errors = OBJECT_MAPPER.readValue(response.getContentAsByteArray(), Errors.class);
        assertThat(errors.getErrors())
                .hasSize(1)
                .containsOnly(new RequestError("Missing required query parameter some_parameter."));
    }

    @Test
    public void shouldReturn404OnContentNotFound() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/content_not_found")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturn405OnWrongMethod() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/method_not_allowed")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    public void shouldReturn409OnDuplicateContent() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/duplicate_content")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(409);
    }

    @Test
    public void shouldReturn415ForWrongMediaType() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/invalid_media_type")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(415);
    }

    @Test
    public void shouldReturn422OnMethodValidationError() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/method_validation_error")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(422);
        Errors errors = OBJECT_MAPPER.readValue(response.getContentAsByteArray(), Errors.class);
        assertThat(errors.getErrors())
                .hasSize(1)
                .containsOnly(new RequestError(null, format("%s %s", Bean.FIELD_NAME_SNAKE_CASE, Bean.MESSAGE), Bean.FIELD_NAME_SNAKE_CASE, null));
    }

    @Test
    public void shouldReturn422OnJavaxValidationError() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/javax_validation_error")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(422);
        Errors errors = OBJECT_MAPPER.readValue(response.getContentAsByteArray(), Errors.class);
        assertThat(errors.getErrors())
                .hasSize(1)
                .containsOnly(new RequestError("NotNull", "some_field must not be null", "some_field", null));
    }

    @Test
    public void shouldReturn422OnJavaxEmptyStringValidationError() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/javax_empty_string_validation_error")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(422);
        Errors errors = OBJECT_MAPPER.readValue(response.getContentAsByteArray(), Errors.class);
        assertThat(errors.getErrors())
                .hasSize(1)
                .containsOnly(new RequestError("NotEmpty", "some_field must not be empty", "some_field", null));
    }

    @Test
    public void shouldReturn422OnJavaxValidationErrorOnModelAttribute() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/javax_validation_error_on_model_attribute")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(422);
        Errors errors = OBJECT_MAPPER.readValue(response.getContentAsByteArray(), Errors.class);
        assertThat(errors.getErrors())
                .hasSize(1)
                .containsOnly(new RequestError("NotNull", "some_field must not be null", "some_field", null));
    }

    @Test
    public void shouldReturn422OnUnprocessableEntityException() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/unprocessable_entity_exception")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(422);
        Errors errors = OBJECT_MAPPER.readValue(response.getContentAsByteArray(), Errors.class);
        assertThat(errors.getErrors())
                .hasSize(1)
                .containsOnly(new RequestError("validation failed!"));
    }

    @Test
    public void shouldReturn500OnUnhandledException() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/unhandled_exception")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(500);
        Errors errors = OBJECT_MAPPER.readValue(response.getContentAsByteArray(), Errors.class);
        assertThat(errors.getErrors())
                .hasSize(1)
                .containsOnly(new RequestError("Internal Server Error"));
    }

    @RestController
    private static class Controller {

        @RequestMapping(path = "/test/invalid_json", method = RequestMethod.GET)
        public void invalidJson() {
            throw new HttpMessageNotReadableException("test", new HttpInputMessage() {
                @Override
                public InputStream getBody() throws IOException {
                    return null;
                }

                @Override
                public HttpHeaders getHeaders() {
                    return null;
                }
            });
        }

        @RequestMapping(path = "/test/invalid_media_type", method = RequestMethod.GET)
        public void invalidMediaType() throws Exception {
            throw new HttpMediaTypeNotSupportedException("test");
        }

        @RequestMapping(path = "/test/unhandled_exception", method = RequestMethod.GET)
        public void unhandledException() throws Exception {
            throw new Exception("test");
        }

        @RequestMapping(path = "/test/duplicate_content", method = RequestMethod.GET)
        public void duplicateContent() {
            throw new DuplicateContentException("test");
        }

        @RequestMapping(path = "/test/content_not_found", method = RequestMethod.GET)
        public void notFound() {
            throw new NotFoundException("test");
        }

        @RequestMapping(path = "/test/method_not_allowed", method = RequestMethod.GET)
        public void methodNotAllowed() throws Exception {
            throw new HttpRequestMethodNotSupportedException("GET");
        }

        @RequestMapping(path = "/test/method_validation_error", method = RequestMethod.GET)
        public void methodValidationError() throws Exception {
            BindException bindException = new BindException(new Bean(), "bean");
            bindException.addError(new FieldError("bean", Bean.FIELD_NAME, Bean.MESSAGE));
            throw new MethodArgumentNotValidException(
                    new MethodParameter(this.getClass().getMethod("methodValidationError"), -1),
                    bindException);
        }

        @RequestMapping(path = "/test/javax_validation_error", method = RequestMethod.GET)
        public void javaxValidationError() {
            LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
            validatorFactoryBean.afterPropertiesSet();
            throw new ConstraintViolationException(
                    validatorFactoryBean.getValidator().validate(new InvalidBean())
            );
        }

        @RequestMapping(path = "/test/javax_empty_string_validation_error", method = RequestMethod.GET)
        public void javaxEmptyStringValidationError() throws Exception {
            LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
            validatorFactoryBean.afterPropertiesSet();
            throw new ConstraintViolationException(
                    validatorFactoryBean.getValidator().validate(new InvalidEmptyBean())
            );
        }

        @RequestMapping(path = "/test/javax_validation_error_on_model_attribute", method = RequestMethod.GET)
        public void javaxValidationErrorOnModelAttribute() throws Throwable {
            LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
            validatorFactoryBean.afterPropertiesSet();
            BindingResult bindingResult = new BeanPropertyBindingResult(new InvalidBean(), "invalidBean");
            validatorFactoryBean.validate(new InvalidBean(), bindingResult);
            throw new BindException(bindingResult);
        }

        @RequestMapping(path = "/test/bad_request", method = RequestMethod.GET)
        public void badRequest() {
            throw new BadRequestException("test");
        }

        @RequestMapping(path = "/test/missing_form_param", method = RequestMethod.GET)
        public void missingFormParam() throws Exception {
            throw new MissingServletRequestPartException("test");
        }

        @RequestMapping(path = "/test/method_argument_mismatch/{someId}", method = RequestMethod.GET)
        public void methodArgumentMismatch(@PathVariable("someId") UUID someId) {}

        @RequestMapping(path = "/test/unprocessable_entity_exception", method = RequestMethod.GET)
        public void methodArgumentMismatch() {
            throw new UnprocessableEntityException(new RequestError("validation failed!"));
        }

        @RequestMapping(path = "/test/missing_servlet_request_parameter", method = RequestMethod.GET)
        public void missingServletRequestParameter() throws Exception {
            throw new MissingServletRequestParameterException("someParameter", String.class.getName());
        }
    }

    private static final class Bean {
        private static final String FIELD_NAME = "myNumber";
        private static final String FIELD_NAME_SNAKE_CASE = "my_number";
        private static final String MESSAGE = "missing";
    }

    private static final class InvalidBean {
        @NotNull
        private final String someField = null;
    }

    private static final class InvalidEmptyBean {
        @NotEmpty
        private final String someField;

        private InvalidEmptyBean() throws IOException {
            someField = OBJECT_MAPPER.readValue(EMPTY_JSON_STRING_VALUE, String.class);
        }
    }

    private static final class InvalidBeanService {

        @Validated
        public void validate(@Valid InvalidBean bean) {}
    }
}