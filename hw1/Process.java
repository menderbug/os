package hw1;
//OS HW1 - Part C
//David Ye

public class Process implements Comparable<Process>{
	
	public enum Priority{HIGH, LOW}
	
	public int arrivalTime;
	public int burstTime;
	public Priority priority;
	
	public Process(int arrivalTime, int burstTime, Priority priority) {
		this.arrivalTime = arrivalTime;
		this.burstTime = burstTime;
		this.priority = priority;
	}
	
	@Override
	public int compareTo(Process other) {	//this comparator is for the list of processes
		return Integer.compare(arrivalTime, other.arrivalTime);	
	}	//custom comparator will be implemented for ready queue
	
	
			
}
