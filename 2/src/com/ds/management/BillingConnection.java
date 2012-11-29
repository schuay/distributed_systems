package com.ds.management;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.ds.billing.BillingServer;
import com.ds.util.RegistryProperties;

public class BillingConnection {

    private final BillingServer billing;

    public BillingConnection(String billingBindingName) throws NotBoundException, IOException {
        RegistryProperties props = new RegistryProperties();
        Registry registry = LocateRegistry.getRegistry(props.getHost(), props.getPort());
        billing = (BillingServer)registry.lookup(billingBindingName);
    }

    public BillingServer getBillingServer() {
        return billing;
    }
}
