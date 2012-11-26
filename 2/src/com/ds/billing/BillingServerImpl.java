package com.ds.billing;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;

import com.ds.loggers.Log;

public class BillingServerImpl implements BillingServer {
    private Authentication auth;

    BillingServerImpl() throws IOException, NoSuchAlgorithmException {
        auth = new Authentication();
    }

    public BillingServerSecure login(String username, String password)
            throws RemoteException {
        Log.d("Logging in %s", username);
        if (!auth.loginValid(username, password)) {
            Log.d("login failed");
            throw new RemoteException("Login failed");
        }

        Log.d("login successful");
        return (BillingServerSecure) UnicastRemoteObject.exportObject(
                new BillingServerSecureImpl(), 0);
    }

}
