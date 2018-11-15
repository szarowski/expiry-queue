package com.expiryqueue.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.base.CharMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * Jackson configuration for Spring
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .modules(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES), new StringTrimModule())
                .findModulesViaServiceLoader(true)
                .serializationInclusion(JsonInclude.Include.NON_ABSENT)
                .failOnUnknownProperties(false)
                .featuresToDisable(
                        SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,
                        DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
    }

    private static final class StringTrimModule extends SimpleModule {
        StringTrimModule() {
            addDeserializer(String.class, new StdScalarDeserializer<String>(String.class) {
                @Override
                public String deserialize(final JsonParser jsonParser, final DeserializationContext ctx) throws IOException {
                    return CharMatcher.whitespace().trimFrom(jsonParser.getValueAsString());
                }
            });
        }
    }
}