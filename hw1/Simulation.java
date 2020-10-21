package hw1;
//OS HW1 - Part C
//David Ye

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class Simulation {

	public final double ARRIVAL_RATE = 0.02;
	public final double HIGH_PRIORITY_FRACTION = 0.22;
	public final int AVERAGE_BURST_TIME = 30;
	public final int BURST_TIME_VARIANCE = 29;
	
	public final int TIMESLICE = 1;
	public final int CONTEXT_SWITCH_OVERHEAD = 3;
	public final int NUM_PROCESSES = 10000;
	
	int time = 0;
	int cpuTime = 0;
	int busyUntil = 0;
	Process current = null;
	
	int responseTimeHigh = 0;
	int responseTimeLow = 0;
	int waitTimeHigh = 0;
	int waitTimeLow = 0;
	int overheadTime = 0;
	
	PriorityQueue<Process> readyQueue = new PriorityQueue<Process>(10, new Comparator<Process>() {
		public int compare(Process p1, Process p2) {
			return p1.priority.compareTo(p2.priority) != 0 ? p1.priority.compareTo(p2.priority) : 
				Integer.compare(p1.arrivalTime, p2.arrivalTime);
		}
	});
	
	public static void main(String[] args) {
		Simulation sim = new Simulation();
		PriorityQueue<Process> processes = sim.generateProcesses();
		
		int numHigh = (int) processes.stream().filter(p -> p.priority == Process.Priority.HIGH).count();
		int numLow = sim.NUM_PROCESSES - numHigh;
		
		System.out.println("METRICS (all times given in CPU cycles): ");
		System.out.println("Average burst time: " + ((double) processes.stream().mapToInt(p -> p.burstTime).sum() / sim.NUM_PROCESSES) + "\n");
		System.out.println("Number of processes: " + sim.NUM_PROCESSES);
		System.out.println("Number of high priority processes: " + numHigh);
		System.out.println("Number of low priority processes: " + numLow);
		
		sim.readyQueue(new PriorityQueue<Process>(processes));
		
		System.out.println("For a ready queue system: ");
		System.out.println("----------------------------------------------------------------");
		
		sim.displayMetrics(numHigh, numLow);
		
		System.out.println("\n\nFor a time slice interrupt system: ");
		System.out.println("----------------------------------------------------------------");
		sim.timeSliceInterrupt(new PriorityQueue<Process>(processes));
		
		sim.displayMetrics(numHigh, numLow);
	}
	
	public PriorityQueue<Process> generateProcesses() {
		PriorityQueue<Process> processes = new PriorityQueue<Process>();
		
		Random random = new Random();
		
		for (int time = 0; processes.size() < NUM_PROCESSES; time++)
			if (Math.random() < ARRIVAL_RATE)	//burst time is normal distribution
				processes.add(new Process(time, AVERAGE_BURST_TIME + (int) (BURST_TIME_VARIANCE * random.nextGaussian()), 
						Math.random() < HIGH_PRIORITY_FRACTION ? Process.Priority.HIGH : Process.Priority.LOW));
		return processes;
	}
	
	public void readyQueue(PriorityQueue<Process> processes) {
		
		resetMetrics();				
		
		while (!processes.isEmpty() || !readyQueue.isEmpty() || time < busyUntil) {
			
			while (!processes.isEmpty() && time >= processes.peek().arrivalTime)		//simulating process arrival
				readyQueue.add(processes.poll());
			
			if (time >= busyUntil && current != null) {		//if a task has just completed, record response time
				incrementResponseTime();
				current = null;
			}

			if (!readyQueue.isEmpty() && current == null) {	//start a new process from the ready queue, record wait time
				busyUntil = time + (current = readyQueue.poll()).burstTime;
				incrementWaitTime();
			}	
			
			if (current != null) cpuTime += TIMESLICE;	//increment cpu time whenever there is an active process		
			time += TIMESLICE;
		}
	}
	
	public void timeSliceInterrupt(PriorityQueue<Process> processes) {
		
		resetMetrics();

		while (!processes.isEmpty() || !readyQueue.isEmpty() || time < busyUntil) {
			
			while (!processes.isEmpty() && time >= processes.peek().arrivalTime)		//simulating process arrival
				readyQueue.add(processes.poll());
		
			if (time >= busyUntil && current != null) {		//if a task has just completed, record turnaround time
				incrementResponseTime();
				current = null;
			}
					//start a new process from the ready queue, record wait time
			if (!readyQueue.isEmpty() && (current == null || shouldInterrupt())) {	
				
				if (current != null && shouldInterrupt()) {	//if interrupting
					current.burstTime -= (time - current.arrivalTime);	//reduce remaining time of process
					readyQueue.add(current);		//return process to queue
					time += CONTEXT_SWITCH_OVERHEAD;	
					cpuTime += CONTEXT_SWITCH_OVERHEAD;
					overheadTime += CONTEXT_SWITCH_OVERHEAD;
				}
				
				busyUntil = time + (current = readyQueue.poll()).burstTime;	//assigning new process to CPU
				incrementWaitTime();
			}	

			if (current != null) cpuTime += TIMESLICE;	//increment cpu time whenever there is an active process		
			time += TIMESLICE;
		}
	}
	
	private boolean shouldInterrupt() {return readyQueue.peek().priority.compareTo(current.priority) < 0;}
	
	private void incrementResponseTime() {
		if (current.priority == Process.Priority.HIGH)
			responseTimeHigh += time - current.arrivalTime;
		else
			responseTimeLow += time - current.arrivalTime;
	}
	
	private void incrementWaitTime() {
		if (current.priority == Process.Priority.HIGH)
			waitTimeHigh += time - current.arrivalTime;
		else
			waitTimeLow += time - current.arrivalTime;
	}
	
	private void displayMetrics(int numHigh, int numLow) {
		System.out.println("Total time taken: " + time);
		System.out.println("Average time per process: " + (double) time / NUM_PROCESSES);
		System.out.println("% CPU utilization: " + 100.0 * cpuTime / time);
		System.out.println("% overhead time: " + 100.0 * overheadTime / time);
		
		System.out.println("Average wait time for high priority processes: " + ((double) waitTimeHigh / numHigh));
		System.out.println("Average wait time for low priority processes: " + ((double) waitTimeLow / numLow));
		System.out.println("Average response time for high priority processes: " + ((double) responseTimeHigh / numHigh));
		System.out.println("Average response time for low priority processes: " + ((double) responseTimeLow / numLow));

	}
	
	private void resetMetrics() {
		
		readyQueue.clear();
		
		time = 0;
		cpuTime = 0;
		busyUntil = 0;
		current = null;
		
		responseTimeHigh = 0;
		responseTimeLow = 0;
		waitTimeHigh = 0;
		waitTimeLow = 0;
		overheadTime = 0;
	}
	
	
}
