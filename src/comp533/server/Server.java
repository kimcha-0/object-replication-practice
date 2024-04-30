package comp533.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import assignments.util.mainArgs.RegistryArgsProcessor;
import assignments.util.mainArgs.ServerArgsProcessor;
import comp533.client.RemoteClient;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalLearnedNotificationSent;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.ProposedStateSet;
import util.trace.port.consensus.RemoteProposeRequestReceived;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

@Tags({DistributedTags.SERVER, DistributedTags.SERVER_REMOTE_OBJECT, DistributedTags.SERVER_CONFIGURER, DistributedTags.RMI})
public class Server extends AStandAloneTwoCoupledHalloweenSimulations implements RemoteServer {
	private static final String SERVER = "SERVER";
	// register server proxy with RMIRegisty
	// provide method to register client in coupler proxies with server
	// provide method to notify all
	// when a client proxy forwards an input to the server, the server sends this message to
	// all other in-couplers

	List<RemoteClient> clientProxies;
	Registry rmiRegistry;

	public Server() {

		this.clientProxies = new ArrayList<>();
	}
	
	@Override
	public void registerClient(RemoteClient client) {
		clientProxies.add(client);
		try {
			client.setId(clientProxies.size());
			 System.out.println("rmi id init: " + client.getId());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void processArgs(String[] args) {
		System.out.println("Registry host:" + ServerArgsProcessor.getRegistryHost(args));
		System.out.println("Registry port:" + ServerArgsProcessor.getRegistryPort(args));
		
	}
	
	
	@Override
	protected void init(String args[]) {
		this.processArgs(args);
		this.setTracing();
		try {
			this.rmiRegistry = LocateRegistry.getRegistry(ServerArgsProcessor.getRegistryPort(args));
			RMIRegistryLocated.newCase(Server.class, 
					ServerArgsProcessor.getRegistryHost(args), 
					ServerArgsProcessor.getRegistryPort(args),
					this.rmiRegistry);
			RemoteServer serverProxy = (RemoteServer)UnicastRemoteObject.exportObject(this, 0);
			RMIObjectRegistered.newCase(Server.class, SERVER, serverProxy, this.rmiRegistry);
			this.rmiRegistry.rebind(SERVER, this);
		} catch (RemoteException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void ipcMechanism(IPCMechanism newValue) {
		ProposalMade.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, newValue);
		ProposedStateSet.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, newValue);
		this.ipcMechanism = newValue;
		if (this.broadcastMetaState) {
			// callback in server
			try {
				this.broadcastChanges(newValue, -1);
				RemoteProposeRequestSent.newCase(this, CommunicationStateNames.COMMAND, -1, newValue);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void broadcastMetaState(boolean newValue) {
		ProposalMade.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
		this.broadcastMetaState = newValue;
		ProposedStateSet.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
		try {
			RemoteProposeRequestSent.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
			this.broadcastChanges(newValue, -1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String args[]) {
		Server server = new Server();
		server.init(args);
	
		// blocking server start
		server.start();
			
	}
	

	@Override
	public void receiveMetaState(boolean newValue) {
		this.broadcastMetaState = newValue;
	}

	
	@Override
	public void receiveIPCMechanism(IPCMechanism newValue) {
		this.ipcMechanism = newValue;
	}

	
	public void start() {
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
        SimulationParametersControllerFactory.getSingleton().processCommands();
	}

	@Override
	public void broadcastChanges(Object msg, int id) throws RemoteException {
		RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.COMMAND, -1, msg);
		// broadcasts messages to incouplers of clients except for the original sender
		System.out.println("originating rmi id: " + id);
		for (RemoteClient c : clientProxies) {
			try {
				System.out.println("rmi id from server: " + c.getId());
				if (c.getId() != id) {
					System.out.println("broadcasting to rmi id from server: " + c.getId());
					if (msg instanceof IPCMechanism)
						c.receiveIPCChange((IPCMechanism)msg);
					else if (msg instanceof String)
						c.receiveChangeRMI((String)msg);
					else
						c.receiveMetaStateChange((boolean)msg);	
					ProposalLearnedNotificationSent.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, msg);
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


}