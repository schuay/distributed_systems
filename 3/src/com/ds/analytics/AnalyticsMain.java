package com.ds.analytics;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import com.ds.loggers.Log;
import com.ds.util.Initialization;
import com.ds.util.RegistryProperties;
import com.ds.util.Utils;

public class AnalyticsMain {

    private static AnalyticsServer server = null;

    public static void main(String[] args) {
        /* Argument handling. */

        if (args.length != 1) {
            System.err.printf("Usage: java %s <Binding Name>%n", AnalyticsMain.class.getName());
            return;
        }

        String bindingName = args[0];

        /* Start the RMI server. */

        Initialization.setSystemProperties();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            server = new AnalyticsServer();
            Analytics stub = (Analytics)UnicastRemoteObject.exportObject(server, 0);

            /* Create the registry; if we only call getRegistry(), registry.rebind()
             * fails because the remote object does not exist. */
            RegistryProperties prop = new RegistryProperties();
            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(prop.getHost(), prop.getPort());
                registry.rebind(bindingName, stub);
            } catch (RemoteException e) {
                registry = LocateRegistry.createRegistry(prop.getPort());
                registry.rebind(bindingName, stub);
            }

            Log.i("%s bound", AnalyticsServer.class.getName());
        } catch (Exception e) {
            shutdown();
            Log.e(e.getMessage());
            return;
        }

        Utils.printRunningMsg("Analytics server");
        Utils.waitForExit();
        shutdown();
    }

    private static void shutdown() {
        try {
            if (server != null) {
                UnicastRemoteObject.unexportObject(server, true);
            }
        } catch (RemoteException rx) {}
    }

    private AnalyticsMain() { }
}
