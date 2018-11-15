package com.expiryqueue.model;

import be.joengenduvel.java.verifiers.ToStringVerifier;
import com.expiryqueue.error.model.Errors;
import com.expiryqueue.error.model.RequestError;
import com.expiryqueue.model.internal.DataInternal;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static com.expiryqueue.error.model.ErrorsBuilder.errorsBuilder;
import static com.expiryqueue.error.model.RequestErrorBuilder.requestErrorBuilder;
import static com.expiryqueue.model.DataJsonBuilder.dataJsonBuilder;
import static com.expiryqueue.model.FullDataJsonBuilder.fullDataJsonBuilder;
import static com.expiryqueue.model.MessageJsonBuilder.messageJsonBuilder;
import static com.expiryqueue.model.internal.DataInternalBuilder.dataInternalBuilder;

public class ModelTest {

    @Test
    public void shouldTestEqualsAndHashCode() {
        EqualsVerifier.forClass(DataInternal.class).verify();
        EqualsVerifier.forClass(DataJson.class).verify();
        EqualsVerifier.forClass(FullDataJson.class).verify();
        EqualsVerifier.forClass(MessageJson.class).verify();
        EqualsVerifier.forClass(Errors.class).verify();
        EqualsVerifier.forClass(RequestError.class).verify();
    }

    @Test
    public void shouldTestToString() {
        ToStringVerifier.forClass(DataInternal.class).containsAllPrivateFields(dataInternalBuilder().build());
        ToStringVerifier.forClass(DataJson.class).containsAllPrivateFields(dataJsonBuilder().build());
        ToStringVerifier.forClass(FullDataJson.class).containsAllPrivateFields(fullDataJsonBuilder().build());
        ToStringVerifier.forClass(MessageJson.class).containsAllPrivateFields(messageJsonBuilder().build());
        ToStringVerifier.forClass(Errors.class).containsAllPrivateFields(errorsBuilder().build());
        ToStringVerifier.forClass(RequestError.class).containsAllPrivateFields(requestErrorBuilder().build());
    }
}