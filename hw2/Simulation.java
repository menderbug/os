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
		Random r = new Random();
		
		if (type == Request.EQUIPROBABLE)
			for (long l = 0; l < NUM_REQUESTS; l++)		
				request.add(new Page(l, r.nextInt(NUM_PAGES) + 1));	//maybe loss of precision from Math.random TODO
		else if (type == Request.EXPONENTIAL)
			for (long l = 0; l < NUM_REQUESTS; l++)		
				request.add(new Page(l, geometric()));
		else if (type == Request.BIASED)
			for (long l = 0; l < NUM_REQUESTS; l++)			//some hard coded numbers here TODO
				request.add(new Page(l, r.nextFloat() < 0.8 ? r.nextInt(5) + 100 : geometric()));
	}
	
	public void FIFO() {
		faults = 0;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();
			if (memory.contains(current)) continue;
			faults++;
			if (memory.size() >= NUM_FRAMES)
				memory.poll();
			memory.add(current);
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}
	
	public void secondChance() {		//repeat code but i think its fine for clarity?
		faults = 0;
		long max = NUM_REQUESTS;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();
			if (memory.contains(current)) {
				memory.stream().filter(p -> p.equals(current)).findAny().get().secondChance = false;
				continue;
			}
			faults++;
			if (memory.size() >= NUM_FRAMES) {
				while (!memory.peek().secondChance) {
					memory.peek().secondChance = true;
					memory.peek().timestamp = max++;
				}
				memory.poll();
			}
			memory.add(current);
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}
	
	public void LRU() {
		faults = 0;
		long max = NUM_REQUESTS;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();	//only LRU difference from FIFO, i think
			current.timestamp = max++;
			if (memory.contains(current)) continue;
			faults++;
			if (memory.size() >= NUM_FRAMES)
				memory.poll();
			memory.add(current);
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}
		
	public void optimal() {
		
		Map<Integer, LinkedList<Page>> ordering = new HashMap<Integer, LinkedList<Page>>();
		while (!request.isEmpty()) {
			Page current = request.pop();
			ordering.putIfAbsent(current.ID, new LinkedList<Page>());
			ordering.get(current.ID).add(current);					
		}
		
		faults = 0;
		List<Page> memory = new LinkedList<Page>();
		
		while (!request.isEmpty()) {
			Page current = ordering.values().stream().sorted(Comparator.comparing(l -> l.peek())).findFirst().get().pop();
			if (memory.contains(current)) continue;
			faults++;
			if (memory.size() >= NUM_FRAMES) {
				Page victim = null;
				for (Page page : memory)
					if (ordering.containsKey(page.ID)) {
						victim = page;
						break;
					}
				if (victim == null) {/* TODO basically now it compares by timestamp ugghhhh */}
			}
			memory.add(current);
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
		
		
	}

	private int geometric() {
		int k = (int) (Math.log(Math.random()) / -LAMBDA);
		return k;	//TODO this needs work
	}

	

}
