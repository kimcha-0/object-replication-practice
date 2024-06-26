package comp533.client;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.tags.DistributedTags;

@Tags({DistributedTags.CLIENT_REMOTE_INTERFACE, DistributedTags.RMI})
public interface RemoteClient extends Remote, Serializable {
	public static final String CLIENT = "CLIENT";
	int getId() throws RemoteException;
	void setId(int id) throws RemoteException;
	void broadcastChangeRMI(String command) throws RemoteException;
	void receiveChangeRMI(String command) throws RemoteException;
	void receiveIPCChange(IPCMechanism ipc) throws RemoteException;
	void receiveMetaStateChange(boolean newValue) throws RemoteException;
	void ipcMechanism(IPCMechanism newValue) throws RemoteException;
}
