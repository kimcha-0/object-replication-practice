package comp533.client;

import java.rmi.RemoteException;

import assignments.util.mainArgs.ClientArgsProcessor;
import comp533.server.GIPCServer;
import comp533.server.ServerGIPC;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.tags.DistributedTags;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.consensus.ProposalLearnedNotificationReceived;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.ProposedStateSet;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.gipc.GIPCObjectLookedUp;
import util.trace.port.rpc.gipc.GIPCRPCTraceUtility;
import util.trace.port.rpc.gipc.GIPCRegistryLocated;
import util.trace.port.rpc.rmi.RMITraceUtility;

@Tags({DistributedTags.CLIENT, DistributedTags.CLIENT_CONFIGURER, DistributedTags.CLIENT_REMOTE_OBJECT, DistributedTags.RMI, DistributedTags.GIPC})
public class GIPCClientImpl extends ClientRMIImpl implements GIPCClient, RemoteClient {
	private GIPCServer serverProxy;
	private int gipcId;
	private GIPCRegistry gipcRegistry;
	private static int id = 0;
	
	public GIPCClientImpl() {
		id++;
	}
	
	public void broadcastChangeGIPC(String command) {
		// notify GIPC server of local change in order to keep replicated objects consistent
		try {
			ProposalMade.newCase(this, CommunicationStateNames.COMMAND, -1, command);
			this.localSim.setInputString(command);
			RemoteProposeRequestSent.newCase(this, CommunicationStateNames.COMMAND, -1, command);
			System.out.println("broadcasting from client: " + this.getId());
			this.serverProxy.broadcastChangesGIPC(command, this.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int getId() {
		return this.gipcId;
	}
	
	@Override
	public void setId(int newVal) {
		this.gipcId = newVal;
		System.out.println("setting gipc id: " + this.getId());
	}
	
	public void locateRegistry(String[] args) {
		System.out.println(id);
		try {
			GIPCRegistryLocated.newCase(
					this, 
					ClientArgsProcessor.getRegistryHost(args), 
					ClientArgsProcessor.getGIPCPort(args), 
					String.valueOf(id));
			this.gipcRegistry = GIPCLocateRegistry.getRegistry(ClientArgsProcessor.getRegistryHost(args), 
					ClientArgsProcessor.getGIPCPort(args), 
					ClientArgsProcessor.DEFAULT_CLIENT_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void init(String[] args) {
		this.setTracing();
		super.init(args);
		this.locateRegistry(args);
		this.findGIPCServerProxy();
		try {
			this.serverProxy.registerClientGIPC(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void simulationCommand(String command) {
		if (getIPCMechanism().equals(IPCMechanism.RMI)) {
			super.simulationCommand(command);
		} else {
			this.broadcastChangeGIPC(command);
		}
	}

	@Override
	public void receiveChangeGIPC(String command) {
		// receiving message from GIPC server
		ProposalLearnedNotificationReceived.newCase(this, CommunicationStateNames.BROADCAST_MODE, -1, command);
		ProposedStateSet.newCase(this, CommunicationStateNames.COMMAND, -1, command);
		this.localSim.setInputString(command);
	}
	
	public void findGIPCServerProxy() {
		try {
			this.serverProxy = (GIPCServer) gipcRegistry.lookup(GIPCServer.class, GIPCServer.SERVER);
			GIPCObjectLookedUp.newCase(this, serverProxy, GIPCServer.class, GIPCServer.SERVER, gipcRegistry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void setTracing() {
		super.setTracing();
		BeanTraceUtility.setTracing();
		GIPCRPCTraceUtility.setTracing();
		NIOTraceUtility.setTracing();
	}
	
	@Override
	public void start(String[] args) {
		try {
			this.init(args);
			SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
			SimulationParametersControllerFactory.getSingleton().processCommands();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FactoryTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		RMITraceUtility.setTracing();
		ConsensusTraceUtility.setTracing();
		ThreadDelayed.enablePrint();
		GIPCRPCTraceUtility.setTracing();
		NIOTraceUtility.setTracing();

		GIPCClient client = new GIPCClientImpl();
		// init client
		client.start(args);
	}
}