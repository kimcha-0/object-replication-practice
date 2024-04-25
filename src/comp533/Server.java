package comp533;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import assignments.util.mainArgs.ServerArgsProcessor;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.SERVER, DistributedTags.RMI})
public class Server extends AStandAloneTwoCoupledHalloweenSimulations {
	// register server proxy with RMIRegisty
	// provide method to register client in coupler proxies with server
	// provide method to notify all
	// when a client proxy forwards an input to the server, the server sends this message to
	// all other in-couplers
	List<Client> clientProxies;
	public Server() {
		this.clientProxies = new ArrayList<>();
	}
	
	public void registerClient(Client client) {
		this.clientProxies.add(client);
	}
	
	@Override
	public void start(String args[]) {
		setTracing();
		processArgs(args);
		// register a callback to process actions by the user commands
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
		// use the calling back library
		SimulationParametersControllerFactory.getSingleton().processCommands();
	}

	public static void main(String args[]) {
		try {
			Registry regsitry = LocateRegistry.getRegistry(4999);
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}

}
