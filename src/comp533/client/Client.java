package comp533.client;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import assignments.util.mainArgs.ClientArgsProcessor;
import comp533.server.RemoteServer;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import coupledsims.Simulation;
import coupledsims.Simulation1;
import main.BeauAndersonFinalProject;
import stringProcessors.HalloweenCommandProcessor;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.misc.ThreadSupport;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalLearnedNotificationReceived;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.ProposedStateSet;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

@Tags({DistributedTags.CLIENT, DistributedTags.CLIENT_CONFIGURER, DistributedTags.CLIENT_REMOTE_OBJECT, DistributedTags.RMI})
public class Client extends AStandAloneTwoCoupledHalloweenSimulations implements RemoteClient {
	RemoteServer serverProxy;
	InCoupler in;
	OutCoupler out;
	HalloweenCommandProcessor localSim;
	public static int numClients = 0;
	private int clientId;
	private Registry rmiRegistry;
	
	public Client() {
		this.localSim = createSimulation(numClients + ":");
		this.out = new RemoteOutCoupler(this.localSim);
		this.localSim.addPropertyChangeListener(out);
	}
	
	private HalloweenCommandProcessor createSimulation(String prefix) {
		return 	BeauAndersonFinalProject.createSimulation(
                prefix,
                0,
                Simulation.SIMULATION_Y_OFFSET,
                Simulation.SIMULATION_WIDTH,
                Simulation.SIMULATION_HEIGHT,
                0,
                Simulation.SIMULATION_Y_OFFSET);
	}
	
	@Override
	public void broadcastMetaState(boolean newValue) {
		this.broadcastIPCMechanism = true;
		try {
			this.serverProxy.broadcastChanges(newValue, this.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void ipcMechanism(IPCMechanism newValue) {
		// called when an IPC changing command is executed by the user
		// change an internal local copy of a replicable object/variable whose logical, traced name, is “ipc_mechanism”. 
		// choose the IPC mechanism used to communicate simulation commands based on this object/variable.
		// use the ProposalMade trace class to trace the execution of the interactive command.
		// use the ProposalSet trace class to trace the setting of the internal object/variable
		ProposalMade.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, newValue);
		ProposedStateSet.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, newValue);
		this.ipcMechanism = newValue;
		if (this.broadcastIPCMechanism) {
			// callback in server
			try {
				ProposalMade.newCase(this, CommunicationStateNames.COMMAND, -1, newValue);
				this.serverProxy.broadcastChanges(newValue, this.getId());
				RemoteProposeRequestSent.newCase(this, CommunicationStateNames.COMMAND, -1, newValue);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void exportClient() throws RemoteException {
		try {
			UnicastRemoteObject.exportObject(this, 0);
			this.rmiRegistry.rebind(CLIENT, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		try {
			Client client = new Client();
			client.setTracing();
			client.processArgs(args);
			client.locateRegistry(ClientArgsProcessor.getRegistryHost(args), ClientArgsProcessor.getRegistryPort(args));
			client.findServer(args);
			client.exportClient();
			client.start(args);

			client.start(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void findServer(String[] args) {
		// TODO Auto-generated method stub
		try {
			this.serverProxy = (RemoteServer)this.rmiRegistry.lookup(RemoteServer.SERVER);
			RMIObjectLookedUp.newCase(this, this.serverProxy, CLIENT, this.rmiRegistry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void locateRegistry(String host, int port) {
		try {
			this.rmiRegistry = LocateRegistry.getRegistry(port);
			RMIRegistryLocated.newCase(
					this, 
					host,
					port,
					rmiRegistry
					);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void broadcastChange(String cmd) {
		// invoke server callback to update all other proxies
		try {
			ProposalMade.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
			this.localSim.setInputString(cmd);
			RemoteProposeRequestSent.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
			this.serverProxy.broadcastChanges(cmd, this.clientId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	 @Override
	 public void simulationCommand(String aCommand) {
	     long aDelay = this.getDelay();
	     if (aDelay > 0) {
	         ThreadSupport.sleep(aDelay);
	     }
	     this.broadcastChange(aCommand);
	 }
	
	
	@Override
	public void start(String args[]) {
		// register client with server
		try {
			RMIObjectRegistered.newCase(this, CLIENT, this, rmiRegistry);
			this.serverProxy.registerClient(this);
			SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
			SimulationParametersControllerFactory.getSingleton().processCommands();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public void receiveIPCChange(IPCMechanism ipc) {
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, ipc);
		ProposedStateSet.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, ipc);
		this.ipcMechanism = ipc;
	}
	
	public void receiveMetaStateChange(boolean newValue) {
		this.broadcastIPCMechanism = true;
	}

	@Override
	public void receiveChange(String command) throws RemoteException {
		// TODO Auto-generated method stu
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, command);
		ProposedStateSet.newCase(this, CommunicationStateNames.COMMAND, -1, command);
		this.localSim.setInputString(command);
	}

}
