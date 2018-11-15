package com.expiryqueue.util;

import java.net.URI;

public class IntegrationTestHelper {

    public static URI apiUrl(final String path, final int port) {
        return URI.create("http://localhost:" + port + path);
    }
}