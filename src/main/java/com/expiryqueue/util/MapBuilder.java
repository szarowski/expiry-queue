package com.expiryqueue.util;

import java.util.LinkedHashMap;

/**
 * An utility map class used for holding parameters for NamedParameterJdbcTemplate in repositories.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class MapBuilder<K, V> extends LinkedHashMap<K, V> {

    public static <K, V> MapBuilder<K, V> mapWith(final K key, final V value) {
        return new MapBuilder<K, V>().and(key, value);
    }

    public MapBuilder<K, V> and(final K key, final V value) {
        put(key, value);
        return this;
    }
}
