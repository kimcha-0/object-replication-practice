package comp533;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import assignments.util.mainArgs.ClientArgsProcessor;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import coupledsims.Simulation1;
import main.BeauAndersonFinalProject;
import stringProcessors.HalloweenCommandProcessor;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;

import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.CLIENT, DistributedTags.RMI})
public class Client extends AStandAloneTwoCoupledHalloweenSimulations implements RemoteClient {
	RemoteServer serverProxy;
	InCoupler in;
	OutCoupler out;
	HalloweenCommandProcessor localSim;
	
	public Client() {
	}
	
	public static void main(String args[]) {
		try {
			Registry rmiRegistry = LocateRegistry.getRegistry(4999);
			RemoteClient client = new Client();
			RemoteServer server = (RemoteServer) rmiRegistry.lookup("SERVER");
			UnicastRemoteObject.exportObject(client, ClientArgsProcessor.getRegistryPort(args));
			server.registerClient(client);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void start(String args[]) {
		setTracing();
		processArgs(args);
		this.localSim = createSimulation1(Simulation1.SIMULATION1_PREFIX);
		this.in = new RemoteInCoupler(localSim);
		// local sim instance now an observable of the out coupler
		localSim.addPropertyChangeListener(out);
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
		SimulationParametersControllerFactory.getSingleton().processCommands();
	}
	


	void processArgs(String args[]) {
		System.out.println("Client name:" + ClientArgsProcessor.getClientName(args));
		System.out.println("Registry host:" + ClientArgsProcessor.getRegistryHost(args));
		System.out.println("Registry port:" + ClientArgsProcessor.getRegistryPort(args));

		System.out.println("Headless:" + ClientArgsProcessor.getHeadless(args));
		System.setProperty("java.awt.headless", ClientArgsProcessor.getHeadless(args));
	}

}
