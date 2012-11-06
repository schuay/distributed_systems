package com.ds.event;

public class UserEvent extends Event {

    private static final long serialVersionUID = 1L;

    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String USER_LOGOUT = "USER_LOGOUT";
    public static final String USER_DISCONNECTED = "USER_DISCONNECTED";

    private final String userName;

    public UserEvent(String type, String userName) {
        super(type);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
