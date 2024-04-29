package comp533.registry;

import java.rmi.registry.LocateRegistry;
import util.trace.port.rpc.rmi.RMIRegistryCreated;
import util.trace.port.rpc.rmi.RMITraceUtility;

import java.rmi.registry.Registry;
import java.util.Scanner;

import assignments.util.mainArgs.RegistryArgsProcessor;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import util.annotations.Tags;
import util.tags.DistributedTags;
			
@Tags({DistributedTags.REGISTRY, DistributedTags.RMI})
public class RMIRegistry {
	public static final String REGISTRY = "REGISTRY";
	public RMIRegistry() {}
	

	public static void main(String args[]) {
		RMIRegistry registry = new RMIRegistry();
		registry.start(args);


	}
	
	public void start(String[] args) {
		try {
			RMITraceUtility.setTracing();
			System.out.println("Registry Port:" + RegistryArgsProcessor.getRegistryPort(args));
			Registry registry = LocateRegistry.createRegistry(RegistryArgsProcessor.getRegistryPort(args));
			RMIRegistryCreated.newCase(registry, RegistryArgsProcessor.getRegistryPort(args));
			Scanner scanner = new Scanner(System.in);
			scanner.nextLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
