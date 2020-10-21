package hw2;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

//REQUIRED: FIFO
//REQUIRED: second chance (clock)
//OPTIONAL: optimal
//OPTIONAL: LRU
//OPTIONAL: LFU, MFU
//OPTIONAL: enhanced second chance

public class Simulation {
	
	public enum Request {EQUIPROBABLE, EXPONENTIAL, BIASED}
	
	
	//public final int FAULT_TIME = 10;		//TODO optional parameter
	public final int NUM_FRAMES = 4096;
	public final int NUM_PAGES = 1048576;
	public final long NUM_REQUESTS = 1000000;
	
	public final double RATE_PARAMETER = 1;
	
	private long faults;
	private LinkedList<Page> request = new LinkedList<Page>();
	
	public static void main(String[] args) {
		Simulation sim = new Simulation();
		sim.generateProcesses(Simulation.Request.EQUIPROBABLE);
		sim.FIFO();
	}
	
	
	public void generateProcesses(Request type) {
		request.clear();
		
		if (type == Request.EQUIPROBABLE)
			for (long l = 0; l < NUM_REQUESTS; l++)
				request.add(new Page(System.currentTimeMillis(), (int) (Math.random() * NUM_PAGES)));
		else if (type == Request.EXPONENTIAL) {}
		else if (type == Request.BIASED) {}			//TODO these ones		
	}
	
	public void FIFO() {
		faults = 0;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		for (int l = 0; l < request.size(); l++) {
			if (memory.size() >= NUM_FRAMES) {
				faults++;
				memory.poll();
			}
			memory.add(request.poll());
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}
	
	public void secondChance() {
		faults = 0;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		for (int l = 0; l < request.size(); l++) {
			if (memory.size() >= NUM_FRAMES) {
				while (!memory.peek().secondChance) {
					memory.peek().secondChance = true;
					memory.peek().timestamp = System.currentTimeMillis();
				}
				
			}
			memory.add(request.poll());
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}


	

}
