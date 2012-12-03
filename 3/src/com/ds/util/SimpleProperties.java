package com.ds.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SimpleProperties {

    private final Properties properties = new Properties();

    protected SimpleProperties(String filename) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(filename);
        if (is == null) {
            throw new FileNotFoundException();
        }

        try {
            properties.load(is);
        } finally {
            is.close();
        }
    }

    /**
     * Returns null if not found.
     */
    protected String getProperty(String property) {
        return properties.getProperty(property);
    }
}
