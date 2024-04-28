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
import util.trace.port.consensus.ProposalLearnedNotificationReceived;
import util.trace.port.consensus.ProposedStateSet;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

@Tags({DistributedTags.CLIENT, DistributedTags.RMI})
public class Client extends AStandAloneTwoCoupledHalloweenSimulations implements RemoteClient {
	RemoteServer serverProxy;
	InCoupler in;
	OutCoupler out;
	HalloweenCommandProcessor localSim;
	public static int numClients = 0;
	private int clientId;
	
	public Client() {
		this.localSim = createSimulation1(numClients + ":");
		this.out = new RemoteOutCoupler(this.localSim, this);
		this.localSim.addPropertyChangeListener(out);
	}
	
	public static void main(String args[]) {
		try {
			Client client = new Client();
			client.setTracing();
			client.processArgs(args);
			Registry rmiRegistry = LocateRegistry.getRegistry(ClientArgsProcessor.getRegistryPort(args));
			RMIRegistryLocated.newCase(
					Client.class, 
					ClientArgsProcessor.getRegistryHost(args), 
					ClientArgsProcessor.getRegistryPort(args), 
					rmiRegistry
					);
			client.serverProxy = (RemoteServer) rmiRegistry.lookup(RemoteServer.SERVER);
			RMIObjectLookedUp.newCase(Client.class, client.serverProxy, CLIENT, rmiRegistry);
			RemoteClient clientStub = (RemoteClient) UnicastRemoteObject.exportObject(client, 0);
			client.serverProxy.registerClient(client);
			System.out.println(client.getId());
			client.start(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void broadcastChange(String cmd) {
		// invoke server callback to update all other proxies
		try {
			RemoteProposeRequestSent.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
			this.serverProxy.broadcastChanges(cmd, this.clientId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public void start(String args[]) {
		// register client with server
		
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
		SimulationParametersControllerFactory.getSingleton().processCommands();
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
		SimulationParametersControllerFactory.getSingleton().processCommands();
	}
	
	public int getId() {
		return this.clientId;
	}
	
	public void setId(int id) {
		this.clientId = id;
	}
	


	void processArgs(String args[]) {
		System.out.println("Client name:" + ClientArgsProcessor.getClientName(args));
		System.out.println("Registry host:" + ClientArgsProcessor.getRegistryHost(args));
		System.out.println("Registry port:" + ClientArgsProcessor.getRegistryPort(args));

		System.out.println("Headless:" + ClientArgsProcessor.getHeadless(args));
		System.setProperty("java.awt.headless", ClientArgsProcessor.getHeadless(args));
	}

	@Override
	public void receiveChange(String command) throws RemoteException {
		// TODO Auto-generated method stub
		 ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, command);
		 ProposedStateSet.newCase(this, CommunicationStateNames.COMMAND, -1, command);
		this.localSim.processCommand(command);
		
	}

}
