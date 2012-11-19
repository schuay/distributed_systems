package com.ds.billing;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.ds.loggers.Log;
import com.ds.util.Initialization;
import com.ds.util.RegistryProperties;

public class BillingMain {

    /**
     * @param args
     */
    public static void main(String[] args) {
        /* Argument handling. */

        if (args.length != 1) {
            System.err.printf("Usage: java %s <Binding Name>%n", BillingMain.class.getName());
            return;
        }

        String bindingName = args[0];

        /* Start the RMI server. */

        Initialization.setSystemProperties();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            BillingServerImpl server = new BillingServerImpl();
            BillingServer stub = (BillingServer) UnicastRemoteObject.exportObject(server, 0);

            RegistryProperties prop = new RegistryProperties();
            Registry registry = LocateRegistry.getRegistry(prop.getHost(), prop.getPort());
            registry.rebind(bindingName, stub);

            Log.i("%s bound", BillingServerImpl.class.getName());
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }

}
