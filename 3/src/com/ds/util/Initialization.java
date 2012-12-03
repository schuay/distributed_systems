package com.ds.util;

public class Initialization {

    private Initialization() { }

    public static void setSystemProperties() {
        /* Grant ourselves all permissions (otherwise the RMI code complains about
         * missing rights). */
        System.setProperty("java.security.policy", "security.policy");
    }
}
