package comp533.server;

import java.io.Serializable;
import util.annotations.Tags;
import java.rmi.Remote;
import java.rmi.RemoteException;

import comp533.client.RemoteClient;
import util.tags.DistributedTags;

@Tags({DistributedTags.SERVER_REMOTE_INTERFACE})
public interface RemoteServer extends Remote, Serializable {
	public static final String SERVER = "SERVER";
	void registerClient(RemoteClient client) throws RemoteException;
	void broadcastChanges(Object cmd, int id) throws RemoteException;
}
