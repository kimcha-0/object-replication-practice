package comp533.client;
import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import stringProcessors.HalloweenCommandProcessor;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.consensus.communication.CommunicationStateNames;
import util.trace.trickOrTreat.LocalCommandObserved;

@Tags({DistributedTags.CLIENT_OUT_COUPLER, DistributedTags.RMI})
public class RemoteOutCoupler implements OutCoupler {
	private static final long serialVersionUID = 1L;
	// sends message to Server that change has occurred
	// observes client
	HalloweenCommandProcessor localSim;

	public RemoteOutCoupler(HalloweenCommandProcessor observable) {
		this.localSim = observable;
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// new command is received, update local sim and notify server to broadcast changes to in couplers
		if (!evt.getPropertyName().equals("InputString")) return;
		String newCommand = (String)evt.getNewValue();
		LocalCommandObserved.newCase(this, newCommand);
		localSim.processCommand(newCommand);
	}


	@Override
	public void broadcastToServer(String command) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
