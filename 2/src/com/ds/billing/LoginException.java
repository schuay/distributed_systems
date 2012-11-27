package com.ds.billing;

import java.rmi.RemoteException;

public class LoginException extends RemoteException {
    LoginException(String msg) {
        super(msg);
    }
}
