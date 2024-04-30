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
import util.trace.bean.BeanTraceUtility;
import util.trace.port.consensus.ProposalLearnedNotificationReceived;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.ProposedStateSet;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.rpc.gipc.GIPCRPCTraceUtility;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

@Tags({DistributedTags.CLIENT_CONFIGURER, DistributedTags.RMI})
public class ClientRMIImpl extends AStandAloneTwoCoupledHalloweenSimulations implements RemoteClient {
	RemoteServer serverProxy;
	InCoupler in;
	OutCoupler out;
	HalloweenCommandProcessor localSim;
	public static int numClients = 0;
	private int clientId;
	private Registry rmiRegistry;
	
	public ClientRMIImpl() {
		this.localSim = createSimulation(numClients + ":");
		this.out = new RemoteOutCoupler(this.localSim, getIPCMechanism());
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
		ProposalMade.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
		setBroadcastMetaState(newValue);
		ProposedStateSet.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
		try {
			this.serverProxy.receiveMetaState(newValue);
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
		setIPCMechanism(newValue);
		if (this.broadcastMetaState) {
			// callback in server
			try {
				this.serverProxy.receiveIPCMechanism(newValue);
				this.serverProxy.broadcastChanges(newValue, this.getId());
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
			ClientRMIImpl client = new ClientRMIImpl();
			client.start(args);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void init(String[] args) {
		try {
			this.setTracing();
			this.locateRegistry(ClientArgsProcessor.getRegistryHost(args), ClientArgsProcessor.getRegistryPort(args));
			this.findServer(args);
			this.exportClient();
			RMIObjectRegistered.newCase(this, CLIENT, this, rmiRegistry);
			this.serverProxy.registerClient(this);
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
	public void broadcastChangeRMI(String cmd) {
		// invoke server callback to update all other proxies
		try {
			ProposalMade.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
			this.localSim.setInputString(cmd);
			RemoteProposeRequestSent.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
//			System.out.println("broadcasting from client: " + this.getId());
			this.serverProxy.broadcastChanges(cmd, this.getId());
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
	     this.broadcastChangeRMI(aCommand);
	 }
	
	
	@Override
	public void start(String args[]) {
		// register client with server
		try {
			this.init(args);
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
//		System.out.println("setting rmi id: " + this.clientId);
	}
	
	

	void processArgs(String args[]) {
		System.out.println("Client name:" + ClientArgsProcessor.getClientName(args));
		System.out.println("Registry host:" + ClientArgsProcessor.getRegistryHost(args));
		System.out.println("Registry port:" + ClientArgsProcessor.getRegistryPort(args));

		System.out.println("Headless:" + ClientArgsProcessor.getHeadless(args));
		System.setProperty("java.awt.headless", ClientArgsProcessor.getHeadless(args));
	}
	
	@Override
	public void receiveIPCChange(IPCMechanism ipc) {
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, ipc);
		ProposalMade.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, ipc);
		ProposedStateSet.newCase(this, CommunicationStateNames.IPC_MECHANISM, -1, ipc);
		setIPCMechanism(ipc);
	}
	
	@Override
	public void receiveMetaStateChange(boolean newValue) {
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
		ProposalMade.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
		ProposedStateSet.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, newValue);
		setBroadcastMetaState(newValue);
	}

	@Override
	public void receiveChangeRMI(String command) throws RemoteException {
		// TODO Auto-generated method stu
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.COMMAND, -1, command);
		ProposedStateSet.newCase(this, CommunicationStateNames.COMMAND, -1, command);
		this.localSim.setInputString(command);
	}

}
