package comp533;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import assignments.util.mainArgs.RegistryArgsProcessor;
import assignments.util.mainArgs.ServerArgsProcessor;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.RemoteProposeRequestReceived;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

@Tags({DistributedTags.SERVER, DistributedTags.RMI})
public class Server extends AStandAloneTwoCoupledHalloweenSimulations implements RemoteServer {
	private static final String SERVER = "SERVER";
	// register server proxy with RMIRegisty
	// provide method to register client in coupler proxies with server
	// provide method to notify all
	// when a client proxy forwards an input to the server, the server sends this message to
	// all other in-couplers

	List<RemoteClient> clientProxies;

	public Server() {

		this.clientProxies = new ArrayList<>();
	}
	
	@Override
	public void registerClient(RemoteClient client) {
		RMIObjectRegistered.newCase(this, client.CLIENT, client, null);
		clientProxies.add(client);
		try {
			client.setId(clientProxies.size());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void processArgs(String[] args) {
		System.out.println("Registry host:" + ServerArgsProcessor.getRegistryHost(args));
		System.out.println("Registry port:" + ServerArgsProcessor.getRegistryPort(args));
		
	}


	public static void main(String args[]) {
		try {
			Server server = new Server();
			server.processArgs(args);
			server.setTracing();
			Registry registry = LocateRegistry.getRegistry(ServerArgsProcessor.getRegistryPort(args));
			RMIRegistryLocated.newCase(Server.class, 
					ServerArgsProcessor.getRegistryHost(args), 
					ServerArgsProcessor.getRegistryPort(args),
					registry);
			RemoteServer serverProxy = (RemoteServer)UnicastRemoteObject.exportObject(server, 0);
			RMIObjectRegistered.newCase(Server.class, SERVER, serverProxy, registry);
			registry.rebind(SERVER, server);
			// blocking server start
			server.start();
			
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public static void start() {
		
	}

	@Override
	public void broadcastChanges(String msg, int id) throws RemoteException {
		RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.COMMAND, -1, msg);
		// broadcasts messages to incouplers of clients except for the original sender
		for (RemoteClient c : clientProxies) {
			if (c.getId() != id) {
				try {
					c.receiveChange(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


}