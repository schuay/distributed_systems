package com.ds.billing;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;

import com.ds.loggers.Log;

class BillingServerImpl implements BillingServer {
    private Authentication auth;
    private BillingServerSecureImpl secureBilling;
    private BillingServerSecure stub;

    BillingServerImpl() throws IOException, NoSuchAlgorithmException, RemoteException {
        auth = new Authentication();
        secureBilling = BillingServerSecureImpl.getInstance();
        stub = (BillingServerSecure) UnicastRemoteObject.exportObject(secureBilling, 0);
    }

    public BillingServerSecure login(String username, String password)
            throws RemoteException {
        Log.d("Logging in %s", username);
        if (!auth.loginValid(username, password)) {
            Log.d("login failed");
            throw new LoginException("Login failed");
        }

        Log.d("login successful");

        return stub;
    }

    void shutdown() {
        try {
            UnicastRemoteObject.unexportObject(secureBilling, true);
        } catch (RemoteException rx) {}
    }
}
