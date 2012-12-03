package com.ds.billing;

import java.rmi.RemoteException;

public class LoginException extends RemoteException {

    private static final long serialVersionUID = 1L;

    LoginException(String msg) {
        super(msg);
    }
}
