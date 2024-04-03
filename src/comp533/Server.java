package comp533;

import assignments.util.mainArgs.ServerArgsProcessor;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;

public class Server extends AStandAloneTwoCoupledHalloweenSimulations {
	
	@Override
	public void start(String args[]) {
		init(args);
		SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
		SimulationParametersControllerFactory.getSingleton().processCommands();
	}
	
	void processArgs(String args[]) {
		System.out.println("Registry host:" + ServerArgsProcessor.getRegistryHost(args));
		System.out.println("Registry port:" + ServerArgsProcessor.getRegistryPort(args));
		System.out.println("Server host:" + ServerArgsProcessor.getRegistryHost(args));
	}

}
