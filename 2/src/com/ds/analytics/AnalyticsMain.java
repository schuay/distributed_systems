package com.ds.analytics;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.ds.loggers.Log;
import com.ds.util.Initialization;

public class AnalyticsMain {

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
            AnalyticsServer server = new AnalyticsServer();
            Analytics stub = (Analytics)UnicastRemoteObject.exportObject(server, 0);

            /* Create the registry; if we only call getRegistry(), registry.rebind()
             * fails because the remote object does not exist. */
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind(bindingName, stub);

            Log.i("%s bound", AnalyticsServer.class.getName());
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }

    private AnalyticsMain() { }
}
