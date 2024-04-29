package comp533.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import stringProcessors.HalloweenCommandProcessor;

public class RemoteInCoupler extends AnAbstractSimulationParametersBean implements InCoupler {
	private HalloweenCommandProcessor localSim;
	static int numCouplers;
	private int inCouplerId;
	
	public RemoteInCoupler(HalloweenCommandProcessor localSim) {
		this.inCouplerId = numCouplers;
		this.localSim = localSim;
		numCouplers++;
	}

	@Override
	public void updateState(String command) {
		localSim.processCommand(command);
	}
	
	@Override
	public void setLocalSim(HalloweenCommandProcessor sim) {
		this.localSim = sim;
	}

	@Override
	public int getId() throws RemoteException {
		// TODO Auto-generated method stub
		return this.inCouplerId;
	}
}