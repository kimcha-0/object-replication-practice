package comp533.client;
import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import stringProcessors.HalloweenCommandProcessor;
import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.trickOrTreat.LocalCommandObserved;

@Tags({DistributedTags.CLIENT_OUT_COUPLER, DistributedTags.RMI, DistributedTags.GIPC})
public class RemoteOutCoupler extends AnAbstractSimulationParametersBean implements OutCoupler {
	private static final long serialVersionUID = 1L;
	// sends message to Server that change has occurred
	// observes client
	HalloweenCommandProcessor localSim;
	IPCMechanism mode;

	public RemoteOutCoupler(HalloweenCommandProcessor observable, IPCMechanism mode) {
		this.localSim = observable;
		this.mode = mode;
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// new command is received, update local sim and notify server to broadcast changes to in couplers
		if (!evt.getPropertyName().equals("InputString")) return;
		String newCommand = (String)evt.getNewValue();
		LocalCommandObserved.newCase(this, newCommand);
		if (getIPCMechanism().equals(IPCMechanism.RMI))
		localSim.processCommand(newCommand);
	}


	@Override
	public void broadcastToServer(String command) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
