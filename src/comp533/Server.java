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

@Tags({DistributedTags.SERVER, DistributedTags.RMI})
public class Server extends AStandAloneTwoCoupledHalloweenSimulations implements RemoteServer {
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
		// in coupler takes state from server and sends to clients
		clientProxies.add(client);
	}
	
	void processArgs(String[] args) {
		System.out.println("Registry host:" + ServerArgsProcessor.getRegistryHost(args));
		System.out.println("Registry port:" + ServerArgsProcessor.getRegistryPort(args));
		System.out.println("Server port:" + ServerArgsProcessor.getServerPort(args));
		
	}
	
	@Override
	public void start(String args[]) {
		setTracing();
		processArgs(args);
		init(args);
		// register a callback to process actions by the user commands
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
		// use the calling back library
		SimulationParametersControllerFactory.getSingleton().processCommands();
	}


	public static void main(String args[]) {
		try {
			Registry registry = LocateRegistry.getRegistry(RegistryArgsProcessor.getRegistryPort(args));
			RemoteServer server = new Server();
			UnicastRemoteObject.exportObject(server, 0);
			registry.rebind("SERVER", server);
			
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void broadcastChanges() throws RemoteException {
		// TODO Auto-generated method stub
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

}
