package com.ds.billing;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import com.ds.loggers.Log;
import com.ds.util.Initialization;
import com.ds.util.RegistryProperties;
import com.ds.util.Utils;

public class BillingMain {

    private static BillingServerImpl server = null;

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
            server = new BillingServerImpl();
            BillingServer stub = (BillingServer) UnicastRemoteObject.exportObject(server, 0);

            RegistryProperties prop = new RegistryProperties();
            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(prop.getHost(), prop.getPort());
                registry.rebind(bindingName, stub);
            } catch (RemoteException e) {
                registry = LocateRegistry.createRegistry(prop.getPort());
                registry.rebind(bindingName, stub);
            }

            Log.i("%s bound", BillingServerImpl.class.getName());
        } catch (Exception e) {
            shutdown();
            Log.e(e.getMessage());
            return;
        }

        Utils.printRunningMsg("Billing server");
        Utils.waitForExit();
        shutdown();
    }

    private static void shutdown() {
        try {
            if (server != null) {
                UnicastRemoteObject.unexportObject(server, true);
                server.shutdown();
            }
        } catch (RemoteException rx) {}
    }
}
