package com.ds.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ds.event.Event;

/** An object which can receive and process events.
 * For example, the billing server, analytics server, and
 * management client should all implement this interface.
 */
public interface EventProcessor extends Remote {

    void processEvent(Event event) throws RemoteException;

}
