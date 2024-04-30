package comp533.server;

import java.rmi.RemoteException;

import comp533.client.GIPCClient;
import comp533.client.RemoteClient;
import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.SERVER_REMOTE_INTERFACE, DistributedTags.GIPC})
public interface GIPCServer {
	public static final String SERVER = "GIPCSERVER";
	void registerClientGIPC(GIPCClient client) throws RemoteException;
	void broadcastChangesGIPC(String cmd, int id) throws RemoteException;
	void createRegistry(String[] args);
	void start(String[] args);
}
