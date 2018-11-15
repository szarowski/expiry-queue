package com.expiryqueue.util;

public enum CharacterType {
    LOWER_CASE("abcdefghijklmnopqrstuvwxyz"),
    UPPER_CASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    NUMERIC("0123456789");

    private final String characters;

    CharacterType(final String characters) {
        this.characters = characters;
    }

    public String getCharacters() {
        return characters;
    }
}