package hw2;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

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
	
	public final double LAMBDA = 0.6;
	
	private long faults;
	private LinkedList<Page> request = new LinkedList<Page>();
	
	public static void main(String[] args) {
		
		System.out.println(Math.log(0.0000000000000001) / -1);
		
		/*Simulation sim = new Simulation();
		
		long l = System.currentTimeMillis();
		System.out.println(sim.geometric());
		System.out.println(System.currentTimeMillis() - l);*/
		
		//sim.generateProcesses(Simulation.Request.EQUIPROBABLE);
		//sim.FIFO();
	}
	
	
	public void generateProcesses(Request type) {
		request.clear();
		
		if (type == Request.EQUIPROBABLE)
			for (long l = 0; l < NUM_REQUESTS; l++)		//TODO current time can be too similar 
				request.add(new Page(l, (int) (Math.random() * NUM_PAGES)));
		else if (type == Request.EXPONENTIAL) {
			//TODO
		}
		else if (type == Request.BIASED) {}			//TODO these ones		
	}
	
	public void FIFO() {algorithm(false);}
	
	public void secondChance() {algorithm(true);}
	
	public void algorithm(boolean secondChance) {	//base FIFO algorithm, has second chance variant
		faults = 0;
		long max = NUM_REQUESTS;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		for (int l = 0; l < request.size(); l++) {
			if (memory.size() >= NUM_FRAMES) {
				faults++;
				if (secondChance)
					while (!memory.peek().secondChance) {
						memory.peek().secondChance = true;
						memory.peek().timestamp = (max++);
					}
				memory.poll();
			}
			memory.add(request.poll());
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}
		
	public void optimal() {
		//TODO
	}

	/*public long geometric() {
		long k = (long) (Math.log(Math.random()) / -LAMBDA);
		if (k < 36)
	}*/

	

}
