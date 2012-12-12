package com.ds.interfaces;

import java.io.IOException;

public interface StringChannel {

    void printf(String format, Object... args);

    String readLine() throws IOException;
}
