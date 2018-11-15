package com.expiryqueue.util;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;

import static com.expiryqueue.util.CharacterType.LOWER_CASE;
import static com.expiryqueue.util.CharacterType.NUMERIC;
import static com.expiryqueue.util.CharacterType.UPPER_CASE;

public class Random {

    private static final SecureRandom RND = new SecureRandom();
    private static final String ALPHA_NUM = NUMERIC.getCharacters() + UPPER_CASE.getCharacters() + LOWER_CASE.getCharacters();

    public static int intVal() { return intVal(1000); }
    public static int intVal(final int max) { return max == 0 ? 0 : RND.nextInt(max); }

    public static String string() { return string(8); }
    public static String string(final int length) {
        return RND.ints(length, (int) '!', ((int) '~') + 1)
                .mapToObj((i) -> (char) i)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString()
                .trim();
    }

    public static String alphaNumeric() { return alphaNumeric(8); }
    public static String alphaNumeric(final int length) {
        return RND.ints(length, 0, ALPHA_NUM.length())
                .mapToObj(ALPHA_NUM::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public static OffsetDateTime pastOffsetDateTime(int seconds) { return OffsetDateTime.now(zoneId()).minusSeconds((long)intVal(seconds)); }
    public static OffsetDateTime futureOffsetDateTime(int seconds) { return OffsetDateTime.now(zoneId()).plusSeconds((long)intVal(seconds)); }

    public static ZoneId zoneId() { return ZoneId.of(Random.value(ZoneId.getAvailableZoneIds())); }

    @SafeVarargs
    public static <T> T value(final T... values) { return values[intVal(values.length)]; }

    @SuppressWarnings("unchecked")
    public static <T> T value(final Collection<T> values) {
        return (T) values.toArray()[intVal(values.size())];
    }
}