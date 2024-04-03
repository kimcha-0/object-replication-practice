package comp533;

import assignments.util.mainArgs.ClientArgsProcessor;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;

public class Client extends AStandAloneTwoCoupledHalloweenSimulations {
	
	@Override
	public void start(String args[]) {
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
