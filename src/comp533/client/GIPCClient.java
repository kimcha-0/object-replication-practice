package comp533.client;

import java.rmi.RemoteException;

import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.tags.DistributedTags;

@Tags({DistributedTags.GIPC, DistributedTags.CLIENT_REMOTE_INTERFACE})
public interface GIPCClient {
	void receiveChangeGIPC(String command) throws RemoteException;
	void broadcastChangeGIPC(String command) throws RemoteException;
	void locateRegistry(String[] args);
	void findGIPCServerProxy();
	int getId() throws RemoteException;
	void setId(int val) throws RemoteException;
	void start(String[] args);
}
