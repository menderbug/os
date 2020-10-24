package hw2;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	public final int NUM_PAGES = 4096 * 8;
	public final long NUM_REQUESTS = 1000000;
	
	public final double LAMBDA = 0.6;
	
	private long faults;
	private LinkedList<Page> requestMaster = new LinkedList<Page>();
	
	public static void main(String[] args) {
		
		Simulation sim = new Simulation();
		
		System.out.println("For a system with " + sim.NUM_FRAMES + " frames and " + sim.NUM_PAGES + 
				" pages, simulating " + sim.NUM_REQUESTS + " requests:\n");
		System.out.println("\n\nUSING AN EQUIPROBABLE DISTRIBUTION");
		sim.generateProcesses(Request.EQUIPROBABLE);
		sim.FIFO(new LinkedList<Page>(sim.requestMaster));
		sim.secondChance(new LinkedList<Page>(sim.requestMaster));
		sim.LRU(new LinkedList<Page>(sim.requestMaster));
		System.out.println("\n\nUSING AN EXPONENTIAL DISTRIBUTION");
		sim.generateProcesses(Request.EXPONENTIAL);
		sim.FIFO(new LinkedList<Page>(sim.requestMaster));
		sim.secondChance(new LinkedList<Page>(sim.requestMaster));
		sim.LRU(new LinkedList<Page>(sim.requestMaster));
		System.out.println("\n\nUSING A BIASED DISTRIBUTION");
		sim.generateProcesses(Request.BIASED);
		sim.FIFO(new LinkedList<Page>(sim.requestMaster));
		sim.secondChance(new LinkedList<Page>(sim.requestMaster));
		sim.LRU(new LinkedList<Page>(sim.requestMaster));
		
	}
	
	
	public void generateProcesses(Request type) {
		requestMaster.clear();
		Random r = new Random();
		
		if (type == Request.EQUIPROBABLE)
			for (long l = 0; l < NUM_REQUESTS; l++)		
				requestMaster.add(new Page(l, r.nextInt(NUM_PAGES) + 1));	//maybe loss of precision from Math.random TODO
		else if (type == Request.EXPONENTIAL)
			for (long l = 0; l < NUM_REQUESTS; l++)		
				requestMaster.add(new Page(l, exponential()));
		else if (type == Request.BIASED)
			for (long l = 0; l < NUM_REQUESTS; l++)			//some hard coded numbers here TODO
				requestMaster.add(new Page(l, r.nextFloat() < 0.8 ? r.nextInt(5) + 100 : exponential()));
	}
	
	public void FIFO(LinkedList<Page> request) {
		System.out.println("First-in, First-out Algorithm");
		faults = 0;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();
			if (memory.contains(current)) continue;
			faults++;	//if fault necessary
			if (memory.size() >= NUM_FRAMES)	//as this is a priority queue, earliest automatically removed
				memory.poll();	//removes from memory
			memory.add(current);
		}
		
		metrics(faults);
	}
	
	public void secondChance(LinkedList<Page> request) {	
		System.out.println("Second Chance Algorithm");
		faults = 0;
		long max = NUM_REQUESTS; //stores max timestamp
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();
			if (memory.contains(current)) {	//if used, gains second chance
				memory.stream().filter(p -> p.equals(current)).findAny().get().hasSecondChance = true;
				continue;	
			}
			faults++;	//if fault necessary
			if (memory.size() >= NUM_FRAMES) {
				while (memory.peek().hasSecondChance)
					memory.add(new Page(max++, memory.poll().ID));	//removal and readding simulates circular queue
				memory.poll();	//removes from memory
			}
			memory.add(current);
		}
		
		metrics(faults);
	}
	
	public void LRU(LinkedList<Page> request) {
		System.out.println("Least Recently Used Algorithm");
		faults = 0;
		long max = NUM_REQUESTS; //stores max timestamp
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();	//only LRU difference from FIFO, i think
			if (memory.contains(current)) {
				memory.removeIf(p -> p.equals(current));	//removes and readds to refresh priorityqueue sorting
				memory.add(new Page(max++, current.ID));	//updates timestamp when used, so earliest timestamp reflects LRU 
				continue;
			}
			faults++;
			if (memory.size() >= NUM_FRAMES)
				memory.poll();
			memory.add(current);
		}
		
		metrics(faults);
	}
		
	public void optimal(LinkedList<Page> request) {	//unfortunately, could not be finished in time. Work is left here for grader curiosity.
		System.out.println("Optimal Algorithm");
		
		Map<Integer, LinkedList<Page>> ordering = new HashMap<Integer, LinkedList<Page>>();
		while (!request.isEmpty()) {
			Page current = request.pop();
			ordering.putIfAbsent(current.ID, new LinkedList<Page>());
			ordering.get(current.ID).add(current);					
		}
		
		faults = 0;
		List<Page> memory = new LinkedList<Page>();

		while (ordering.values().stream().flatMap(l -> l.stream()).count() != 0) {
			Page current = ordering.values().stream().filter(l -> !l.isEmpty()).sorted(Comparator.comparing(l -> l.peek())).findFirst().get().poll();
			if (memory.contains(current)) continue;
			faults++;
			if (memory.size() >= NUM_FRAMES) {
				Page victim = null;
				for (Page page : memory)
					if (ordering.containsKey(page.ID)) {
						victim = page;
						break;
					}
				if (victim == null) {/* TODO  */}
				if (victim == null) break;
				for (int i = memory.size() - 1; i >= 0; i--) 
					if (memory.get(i).equals(victim)) memory.remove(i);
			}
			memory.add(current);
		}
		
		metrics(faults);
		
		
	}
	
	private void metrics(long faults) {
		System.out.println("Number of hits:\t\t" + (NUM_REQUESTS - faults));
		System.out.println("Number of misses:\t" + faults);
		System.out.println("Fault percentage:\t" + faults * 100.0 / NUM_REQUESTS + "%\n");
	}

	private int exponential() {
		int k = (int) Math.ceil((Math.log(Math.random()) / -LAMBDA));
		return k > NUM_PAGES ? exponential() : k;	//retries in the event of out of bounds error	
	}

	

}
