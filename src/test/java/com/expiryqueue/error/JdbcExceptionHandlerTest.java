package com.expiryqueue.error;

import com.expiryqueue.config.JacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class JdbcExceptionHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = new JacksonConfig().jacksonBuilder().build();

    private final MockMvc mockMvc = standaloneSetup(new Controller())
                .setControllerAdvice(new JdbcExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(OBJECT_MAPPER))
                .build();

    @Test
    public void shouldReturn409OnDuplicateKeyException() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/test/duplicate_key_exception")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(409);
    }

    @RestController
    private static class Controller {

        @RequestMapping(path = "/test/duplicate_key_exception", method = RequestMethod.GET)
        public void duplicateKeyException() throws Exception {
            throw new DuplicateKeyException("duplicate entity");
        }
    }

}