package com.expiryqueue.util;

import org.junit.Test;

import java.util.Map;

import static com.expiryqueue.util.MapBuilder.mapWith;
import static org.assertj.core.api.Assertions.assertThat;

public class MapBuilderTest {

    @Test
    public void shouldCreateMultiValueMap() {
        String key1 = Random.string();
        String key1Val = Random.string();
        String key2 = Random.string();
        String key2Val = Random.string();

        Map<String, String> map = mapWith(key1, key1Val).and(key2, key2Val);

        assertThat(map)
                .containsEntry(key1, key1Val)
                .containsEntry(key2, key2Val);
    }
}