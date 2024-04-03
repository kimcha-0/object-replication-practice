package comp533;
import grader.basics.execution.BasicProjectExecution;
import gradingTools.comp533s24.assignment4.S24Assignment3Suite;
import trace.grader.basics.GraderBasicsTraceUtility;

public class A4Grader {
	// if you set this to false, grader steps will not be traced
	public static void main(String args[]) {
			GraderBasicsTraceUtility.setTracerShowInfo(true);	
			// if you set this to false, all grader steps will be traced,
			// not just the ones that failed		
			GraderBasicsTraceUtility.setBufferTracedMessages(true);
			// Change this number if a test trace gets longer than 600 and is clipped
			int maxPrintedTraces = 600;
			GraderBasicsTraceUtility.setMaxPrintedTraces(maxPrintedTraces);
			// Change this number if all traces together are longer than 2000
			int maxTraces = 2000;
			GraderBasicsTraceUtility.setMaxTraces(maxTraces);
			// Change this number if your process times out prematurely
			int processTimeOut = 5;
			BasicProjectExecution.setProcessTimeOut(processTimeOut);
			// You need to always call such a method
			S24Assignment4Suite.main(args);
		}
}
