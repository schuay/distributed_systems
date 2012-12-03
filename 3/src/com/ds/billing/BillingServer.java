package com.ds.billing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BillingServer extends Remote {

    BillingServerSecure login(String username, String password) throws RemoteException;
}
