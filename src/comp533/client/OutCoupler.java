package comp533.client;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface OutCoupler extends PropertyChangeListener, Remote, Serializable {
	void broadcastToServer(String command) throws RemoteException;
}
