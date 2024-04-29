package comp533.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import stringProcessors.HalloweenCommandProcessor;

public interface InCoupler extends Remote, Serializable {
	// notifyClient 
	// holds reference to client and calls a remote method in the remote client object
	// observes server and calls processCommand(event.getgetNewValue() in client that references this coupler
	void updateState(String command) throws RemoteException;
	void setLocalSim(HalloweenCommandProcessor sim);
	int getId() throws RemoteException;
}
