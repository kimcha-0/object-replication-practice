package comp533;

import java.rmi.registry.LocateRegistry;

import assignments.util.mainArgs.ClientArgsProcessor;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;

import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.CLIENT, DistributedTags.RMI})
public class Client extends AStandAloneTwoCoupledHalloweenSimulations {
	Server serverProxy;
	
	public Client() {
		
	}
	
	public static void main(String args[]) {
		try {
			Registry registry = LocateRegistry.getRegistry(4999);
			Client client = new Client());
		}
	}
	
	@Override
	public void start(String args[]) {
		setTracing();
		processArgs(args);
		init(args);
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
		SimulationParametersControllerFactory.getSingleton().processCommands();
	}
	
	void processArgs(String args[]) {
		ClientArgsProcessor.getClientName(args);
		ClientArgsProcessor.getRegistryHost(args);
		ClientArgsProcessor.getRegistryPort(args);
	}

}
