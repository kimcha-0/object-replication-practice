package comp533.server;

import java.rmi.RemoteException;
import port.ATracingConnectionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import assignments.util.mainArgs.ServerArgsProcessor;
import comp533.client.Client;
import comp533.client.GIPCClient;
import comp533.client.RemoteClient;
import inputport.InputPort;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.consensus.ProposalLearnedNotificationSent;
import util.trace.port.consensus.RemoteProposeRequestReceived;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.gipc.GIPCObjectRegistered;
import util.trace.port.rpc.gipc.GIPCRPCTraceUtility;
import util.trace.port.rpc.gipc.GIPCRegistryCreated;
import util.trace.port.rpc.gipc.GIPCRegistryLocated;
import util.trace.port.rpc.rmi.RMITraceUtility;
import util.tags.DistributedTags;

@Tags({DistributedTags.SERVER, DistributedTags.SERVER_REMOTE_OBJECT, DistributedTags.SERVER_CONFIGURER, DistributedTags.RMI, DistributedTags.GIPC})
public class ServerGIPC extends Server implements GIPCServer {
	List<GIPCClient> clientProxies;
	GIPCRegistry gipcRegistry;
	
	public ServerGIPC() {
		this.clientProxies = new ArrayList<>();
	}
	
	@Override
	public void setTracing() {
		FactoryTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		RMITraceUtility.setTracing();
		ConsensusTraceUtility.setTracing();
        ThreadDelayed.enablePrint();
        GIPCRPCTraceUtility.setTracing();
        NIOTraceUtility.setTracing();
        super.trace(true);
	}
	
	
	@Override
	protected void init(String args[]) {
		this.setTracing();
		super.init(args);
		this.createRegistry(args);
		this.registerServer();
		InputPort gipcPort = this.gipcRegistry.getInputPort();
		ATracingConnectionListener listener = new ATracingConnectionListener(gipcPort);
        gipcPort.addConnectionListener(listener);

	}

	@Override
	public void registerClientGIPC(GIPCClient client) {
		// TODO Auto-generated method stub
		this.clientProxies.add(client);
		 try {
			 client.setId(this.clientProxies.size());
			 System.out.println("gipc id init: " + client.getId());
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		
	}

	@Override
	public void broadcastChangesGIPC(String cmd, int id) throws RemoteException {
		RemoteProposeRequestReceived.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
		// out coupler calls this method. If the IPCMechanism is GIPC, coupler will call this method
//		System.out.println("originating client gipc id: " + id);
		for (GIPCClient c : clientProxies) {
//			System.out.println("gipc id from server: " + c.getId());
			if (c.getId() != id) {
				ProposalLearnedNotificationSent.newCase(this, CommunicationStateNames.COMMAND, -1, cmd);
				System.out.println("broadcasting to gipc id from server: " + c.getId());
				((GIPCClient)c).receiveChangeGIPC(cmd);
			}
		}
		
	}
	
	
	
	@Override
	public void start(String[] args) {
		this.init(args);
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
		SimulationParametersControllerFactory.getSingleton().processCommands();
		
	}
	
	public void registerServer() {
		try {
			GIPCObjectRegistered.newCase(this, GIPCServer.SERVER, this.gipcRegistry, this.gipcRegistry);
			this.gipcRegistry.rebind(GIPCServer.SERVER, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createRegistry(String[] args) {
		try {
			GIPCRegistryCreated.newCase(this, ServerArgsProcessor.getGIPCServerPort(args));
			this.gipcRegistry= GIPCLocateRegistry.createRegistry(ServerArgsProcessor.getGIPCServerPort(args));
			
//			GIPCRegistryLocated.newCase(this, ServerArgsProcessor.getRegistryHost(args), ServerArgsProcessor.getRegistryPort(args), SERVER);
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

		GIPCServer gipcServer = new ServerGIPC();
		gipcServer.start(args);
	}

}
